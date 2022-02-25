#!/bin/bash


COMMAND=$1

STACK_NAME=db-test-stack


# Create a new stack
if [[ $COMMAND == "create" ]]; then

    # Deploy some cloudformation 
    aws cloudformation create-stack \
        --stack-name ${STACK_NAME}\
        --template-body file://infrastructure.yaml \
        --parameters ParameterKey=EnvironmentName,ParameterValue=DBTestingStack \
        --capabilities CAPABILITY_IAM 

# Update the stack 
elif [[ $COMMAND == "update" ]]; then

    aws cloudformation update-stack \
        --stack-name ${STACK_NAME} \
        --template-body file://infrastructure.yaml \
        --parameters ParameterKey=EnvironmentName,ParameterValue=DBTestingStack \
        --capabilities CAPABILITY_IAM  

# Print default error message
else
    echo "Command ${COMMAND} not recognized. Are you sure it is a valid command?" 

fi







# Get output

