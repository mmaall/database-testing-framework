# Use an official Python runtime as a parent image
FROM amazoncorretto:17

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY *.class /app

# Install any needed packages specified in requirements.txt
RUN echo "Run commands here"

# Make port 80 available to the world outside this container
EXPOSE 13800

# Define environment variable
ENV SECRET_KEY 24943

# Run app.py when the container launches
CMD ["java", "DataGeneratorDriver"]
