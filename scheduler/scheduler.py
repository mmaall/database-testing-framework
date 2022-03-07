import json
import os
import sys
import logging
import argparse
import uuid
import boto3
from datetime import datetime
import time

# Global configs that we may want to change
stack_info_file_name = "stack-info.json"


# Argument documentation
command_helper_string = """
    
        create-stack: This will create necessary infrastructure to start submitting load testing jobs
        update-stack: This will sync code changes and updates to AWS 
"""


def main():

    # Take in arguments
    parser = argparse.ArgumentParser(
        "Program to schedule database load testing jobs.")

    parser.add_argument("command", type=str,  help=command_helper_string)
    parser.add_argument("-v", "--verbose",
                        action="store_true", help="Verbose output")

    args = parser.parse_args()

    command = args.command
    verbose = args.verbose

    # Set logging level
    if verbose:
        logging.basicConfig(level=logging.INFO)
    else:
        logging.basicConfig(level=logging.WARNING)

    # Initialize the stack we are taking a look at
    testing_stack = InfrastructureStack()

    # Read through commands and do necessary operations

    if command == "create-stack":
        testing_stack.create_stack()

    elif command == "delete-stack":
        testing_stack.delete_stack()

    elif command == "update-stack":
        testing_stack.update_stack()

    else:
        print("Hmm, it does not seem this command is recognized ({})".format(command))

    # Cleanup
    testing_stack.cleanup()


class InfrastructureStack():

    # Constants
    repository_root = "../"
    cloudformation_dependency_dir = repository_root + "infra/substacks/"
    base_stack_name = "base-stack.yaml"
    cloudformation_main_stack = repository_root + "infra/" + base_stack_name

    def __init__(self):
        # Get cached stack information
        self.stack_info = self.get_stack_info()
        logging.info("Stack Info: {}".format(self.stack_info))

        # initialize map containing clients
        self._client_map = {}

    @property
    def cloudformation_bucket(self):
        return self.stack_info.get("cloudformation_template_bucket")

    @property
    def stack_name(self):
        return self.stack_info.get("stack_name")

    @stack_name.setter
    def stack_name(self, name):
        self.stack_info["stack_name"] = name

    def cleanup(self):
        self.stack_info["last_access"] = datetime.now().isoformat()
        self.flush_stack_info()

    # Sets up the cloudformation stack that we will be using

    def create_stack(self):

        # Check if we have already created an s3 bucket here
        current_stack_name = self.stack_info.get("stack_name")
        if current_stack_name is not None:
            print("A stack {} has already been created.".format(current_stack_name))
            print("Are you sure you want to proceed to create a new stack? The scheduler will lose all knowledge of the prior stack.")
            print("Type: y or n")

            # Clobber stack_info
            if self.can_continue():
                self.stack_info = {}

            else:
                print("Exiting")
                exit(1)

        # Get a new stack name
        print("Input a stack name")
        stack_name = input("--> ")

        # Do some basic input validation
        if len(stack_name) > 128:
            logging.error("Stack name exceeds 128 characters, exiting.")
            exit(1)

        self.stack_name = stack_name

        # Create an S3 bucket that will hold our necessary cloudformation stacks

        s3_client = self._get_client("s3")
        bucket_name = stack_name + "-" + str(uuid.uuid4())

        try:
            logging.debug("Creating S3 bucket {}".format(bucket_name))
            response = s3_client.create_bucket(Bucket=bucket_name)
            self.stack_info["cloudformation_template_bucket"] = bucket_name

        except S3.Client.exceptions as e:
            logging.error("Error creating s3 bucket: {}".format(str(e)))
            exit(1)

        # Record bucket name
        self.stack_info["cloudformation_template_bucket"] = bucket_name

        print("Cloudformation assets uploaded to S3 ({})".format(bucket_name))

        # Copy in necessary cloudformation dependencies
        try:
            self.upload_cloudformation()
        except Exception as e:

            # Error has occured, lets delete
            logging.error("Unable to upload cloudformation, deleting stack")
            logging.error(str(e))
            self.delete_stack()
            exit(1)

        # Launch the cloudformation stack
        print("Starting stack creation")
        try:
            cfn_client = self._get_client("cloudformation")
            cfn_client.create_stack(StackName=stack_name,
                                    TemplateURL="https://s3.amazonaws.com/" +
                                    bucket_name + "/" + self.base_stack_name,
                                    Capabilities=["CAPABILITY_NAMED_IAM"],
                                    Parameters=[
                                        {
                                            "ParameterKey": "BucketName",
                                            "ParameterValue": bucket_name
                                        }
                                    ]

                                    )
        # TODO: Move from generic exception
        except Exception as e:
            logging.error("Unable to create cloudformation")
            logging.error(str(e))
            self.delete_stack()
            exit(1)

        # Verify success of launch

        created_stack = self._get_client("cfn-stack")

        print("Creation process triggered, waiting for stack to be created.")
        print("Be patient, this may take a minute")
        while True:

            created_stack.reload()

            if created_stack.stack_status == "CREATE_COMPLETE":
                print("Stack created succesfully!")
                break

            elif (created_stack.stack_status == "CREATE_FAILED" or
                    created_stack.stack_status == "ROLLBACK_COMPLETE"):
                logging.error("Stack creation failed.")
                logging.error(created_stack.stack_status_reason)
                logging.error("Use the delete-stack command to delete stack")
                break

            time.sleep(10)

    # update a given stack
    def update_stack(self):

        print("Starting stack update")

        # Re-upload cloudformation
        try:
            self.upload_cloudformation()

        except Exception as e:
            logging.error("Unable to upload cloudformation for update")
            logging.error(str(e))
            exit(1)

        print("Cloudformation uploaded to S3")

        # update the stack

        cfn_stack = self._get_client("cfn-stack")
        try:
            cfn_stack.update(StackName=self.stack_name,
                             TemplateURL="https://s3.amazonaws.com/" +
                             self.cloudformation_bucket + "/" + self.base_stack_name,
                             Capabilities=["CAPABILITY_NAMED_IAM"],
                             Parameters=[
                                 {
                                     "ParameterKey": "BucketName",
                                     "ParameterValue": self.cloudformation_bucket
                                 }
                             ]
                             )

        except Exception as e:
            logging.error("Unable to call update stack")
            logging.error(str(e))
        print("Stack update triggered, this might take a minute")
        while True:

            cfn_stack.reload()

            if cfn_stack.stack_status == "UPDATE_COMPLETE":
                print("Stack updated succesfully!")
                break

            elif (cfn_stack.stack_status == "UPDATE_FAILED" or
                    cfn_stack.stack_status == "UPDATE_ROLLBACK_COMPLETE"):
                logging.error("Stack update failed.")
                logging.error(cfn_stack.stack_status_reason)
                break

            time.sleep(10)

    # Completely delete the stack
    def delete_stack(self):

        logging.info("Stack deletion triggered")

        # Do some basic validation
        if self.stack_name is None:
            logging.error(
                "Unable to get the stack_name. Verify stack-info.json is formatted properly.")
            exit(1)

        logging.info("Deleting {}".format(self.stack_name))

        try:
            # delete cloudformation s3 bucket
            cfn_bucket_client = self._get_client("s3-cfn-bucket")

            cfn_bucket_client.objects.all().delete()

            cfn_bucket_client.delete()

        # TODO: Move away from generic exception
        except Exception as e:

            logging.warning("Unable to delete S3 bucket. Continuing")

        # Remove the client from use and remove
        self._remove_client("s3-cfn-bucket")

        # Delete the base stack itself

        self._get_client("cfn-stack").delete()

        # reset stack_info
        self.stack_info = {}

        print("Stack has been fully deleted")

    def upload_cloudformation(self):

        logging.debug("Uploading cloudformation to S3 ({})".format(
            self.cloudformation_bucket))
        # go to necessary directory
        files = self._list_files(self.cloudformation_dependency_dir)

        # Parse through the dependencies and upload them to s3
        s3_client = self._get_client("s3")
        for cloudformation_file in files:

            file_parser = open(cloudformation_file, mode="rb")

            s3_client.put_object(Bucket=self.cloudformation_bucket, Key=os.path.basename(
                cloudformation_file), Body=file_parser)

            file_parser.close()

        # Upload the main cloudformation file
        file_parser = open(self.cloudformation_main_stack, mode="rb")

        s3_client.put_object(Bucket=self.cloudformation_bucket, Key=os.path.basename(
            self.cloudformation_main_stack), Body=file_parser)

        file_parser.close()

    def can_continue(self) -> bool:

        while (True):
            answer = input("--> ")

            if answer == "y":
                return True

            elif answer == "n":
                return False

    # Read in and return stack info
    def get_stack_info(self) -> list:

        # Check to see if a stack has already been created and verify creation
        try:
            stack_info_file = open(stack_info_file_name)

            stack_info = json.load(stack_info_file)

            stack_info_file.close()

        except FileNotFoundError as e:
            logging.warning(
                "Unable to find cached stack details file {}.".format(stack_info_file_name))
            return {}
        except json.JSONDecodeError as e:
            logging.error("Unable to decode JSON found in stack details file {}.".format(
                stack_info_file_name))
            logging.error(str(e))
            logging.error("Inspect {} to verify it is correct json.".format(
                stack_info_file_name))
            exit(1)

        return stack_info

    # Flush stack_info to disk
    # Will write to the default stack_info file. If it cannot write there it will try three different files
    # If it is unable to write to other files it will print to stderr.

    def flush_stack_info(self, file_name=stack_info_file_name, depth=0, max_write_retries=3):

        stack_info_file = None
        try:

            # write to file
            stack_info_file = open(file_name, "w")

            # Write to file
            json.dump(self.stack_info, stack_info_file)

        # unable to durably write the standard stack info file
        except Exception as e:

            logging.error("Unable to write to {}".format(file_name))
            logging.error(e)

            # Recursively and write to a random file in hopes we can store this durrably
            # Won't try more than 3 times
            if depth < max_write_retries:
                uid = uuid.uuid4()
                error_file_name = "error-stack-data-{}.json".format(uid)
                logging.error("Retry {}: Attempting to write to another file {}".format(
                    depth, error_file_name))

                self.flush_stack_info(file_name=error_file_name, depth=depth+1)
            else:
                logging.error(
                    "Retry maximum reached ({}), logging to stderr".format(max_write_retries))
                print(json.dumps(self.stack_info), file=sys.stderr)

    # Helper functions

    # manage all of our AWS clients.
    # Effectively creating singleton objects and re-using clients
    def _get_client(self, client_name):

        client = self._client_map.get(client_name)

        # return a client if it is found
        if client is not None:

            return client

        # Otherwise lets create a valid client
        if client_name == "s3":

            self._client_map[client_name] = boto3.client("s3")

        elif client_name == "s3-cfn-bucket":
            self._client_map[client_name] = boto3.resource(
                "s3").Bucket(self.cloudformation_bucket)

        elif client_name == "cloudformation":
            self._client_map[client_name] = boto3.client("cloudformation")
        elif client_name == "cfn-stack":
            self._client_map[client_name] = boto3.resource(
                "cloudformation").Stack(self.stack_name)

        # return the new client which was added to the map
        return self._client_map[client_name]

    # Remove a client that we are not using anymore 
    def _remove_client(self, client_name):
        try:
            self._client_map.pop(client_name)
        # Don't do anything if the key doesn't exist 
        except KeyError as err:
            pass 


    # List the files in a given directory
    def _list_files(self, directory: str) -> list:

        output = []

        directory_scanner = os.scandir(directory)

        for item in directory_scanner:

            if item.is_dir():
                continue

            output.append(item.path)

        return output


if __name__ == "__main__":
    main()
