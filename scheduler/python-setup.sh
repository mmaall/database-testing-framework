#!/bin/bash

# Commands that are require for the setup to run
REQUIRED_COMMANDS=("python3")
ENVIRONMENT_NAME=scheduler-env

# Switches to 1 if we identify an error 
COMMAND_MISSING=0

echo "Checking necessary tools are installed"

# Check whether we can execute the commands 
for COMMAND in "${REQUIRED_COMMANDS[@]}"; do
    which $COMMAND > /dev/null

    if [ $? -eq 1 ]; then
        echo "Unable to find ${COMMAND} in your path. Are you sure it is installed?"
        COMMAND_MISSING=1
    fi  
done

# Exit if we identify any commands that are not in the path 
if [ $COMMAND_MISSING -eq 1 ]; then
    echo "Errors identified, aborting setup."
    exit
fi

# Setup python environment 

echo "Setting up Python Environment"

# Remove environment if it already exists 
rm -rf $ENVIRONMENT_NAME

python3 -m venv $ENVIRONMENT_NAME

source ${ENVIRONMENT_NAME}/bin/activate

echo "Installing dependencies"

pip install -r requirements.txt

echo "Setup completed!"

echo "Activate the virtual env to run the python program"
echo "  source ${ENVIRONMENT_NAME}/bin/activate"
