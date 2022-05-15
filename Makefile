SOURCE_DIR = src/main/java
DEPENDENCIES = dependencies

package: clean
	mvn package

images: package
	docker build --tag datagenerator ./ -f dockerfiles/DataGenerator.docker

clean: 
	mvn clean
	docker image rm datagenerator -f
