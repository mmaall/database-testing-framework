#!/bin/bash

# This is going to bundle all the code that we have made into a zip

# Run it from this directory, it's using paths relative to this one so yeah


codeDir="loadTestCode"

rm -rf $codeDir

mkdir $codeDir 

cd $codeDir 

mkdir data_generation
mkdir load_test

cd ../

cp ../json-simple-1.1.1.jar $codeDir 
cp ../mysql-connector-java-5.1.48-bin.jar $codeDir 

cp ../data_generation/UniqueIDGenerator.class ${codeDir}/data_generation/
cp ../load_test/LoadDriver.class ${codeDir}/load_test/
cp ../load_test/DatabaseThread.class ${codeDir}/load_test/
cp ../load_test/RecordInfo.class ${codeDir}/load_test/
cp ../load_test/TransactionInfo.class ${codeDir}/load_test/ 

# Zip it all together

zip -r ${codeDir}.zip ${codeDir}/ 

# Clean up after yourself you filthy animal
rm -rf $codeDir/

