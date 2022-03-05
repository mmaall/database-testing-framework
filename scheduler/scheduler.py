import json
import os 
import sys 
import logging
import argparse
import uuid
import boto3 
from datetime import datetime 

# Global configs that we may want to change
stack_info_file_name = "stack-info.json"


# Argument documentation
command_helper_string = """
    
        create-stack: This will create necessary infrastructure to start submitting load testing jobs
        update-stack: This will sync code changes and updates to AWS 
"""


def main():


    # Take in arguments 
    parser = argparse.ArgumentParser("Program to schedule database load testing jobs.")


    parser.add_argument("command", type=str,  help=command_helper_string)
    parser.add_argument("-v", "--verbose",action="store_true", help="Verbose output")

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

    # Cleanup 
    testing_stack.cleanup()

class InfrastructureStack():


    def __init__(self):
    # Get cached stack information
        self.stack_info = self.get_stack_info()
        logging.info("Stack Info: {}".format(self.stack_info))


    @property
    def cloudformation_bucket(self):
        return self.stack_info.get("cloudformation_template_bucket")



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
        print ("Input a stack name")
        stack_name = input("--> ")

        # Do some basic input validation
        if len(stack_name) > 128: 
            logging.error("Stack name exceeds 128 characters, exiting.")
            exit(1)

        # Create an S3 bucket that will hold our necessary cloudformation stacks 

        s3_client = boto3.client("s3")
        bucket_name = stack_name + "-" + str(uuid.uuid4())

        try:
            response = s3_client.create_bucket(Bucket=bucket_name)

        except S3.Client.exceptions as e:
            logging.error("Error creating s3 bucket: {}".format(str(e)))
            exit(1)


        # Copy in necessary cloudformation dependencies


        # Launch the cloudformation stack

        # Verify success of launch 

            # Delete S3 bucket if failed

        # Write necessary details to stack_info
        self.stack_info["cloudformation_template_bucket"] = bucket_name


    def upload_cloudformation(self, directory, s3_url):

        pass 



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
            logging.warning("Unable to find cached stack details file {}.".format(stack_info_file_name))
            return {} 
        except json.JSONDecodeError as e:
            logging.error("Unable to decode JSON found in stack details file {}.".format(stack_info_file_name))
            logging.error(str(e))
            logging.error("Inspect {} to verify it is correct json.".format(stack_info_file_name))
            exit(1)

        return stack_info


    # Flush stack_info to disk
    # Will write to the default stack_info file. If it cannot write there it will try three different files
    # If it is unable to write to other files it will print to stderr.  
    def flush_stack_info(self, file_name = stack_info_file_name, depth = 0, max_write_retries = 3):


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
                logging.error("Retry {}: Attempting to write to another file {}".format(depth,error_file_name))

                self.flush_stack_info(file_name = error_file_name, depth = depth+1)
            else: 
                logging.error("Retry maximum reached ({}), logging to stderr".format(max_write_retries))
                print(json.dumps(self.stack_info), file=sys.stderr)











if __name__ == "__main__":
    main()
