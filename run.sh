#!/bin/bash

# Compile Command for Twitter Crawler:
echo "Compiling." 
javac -cp "./JarFiles/jsoup-1.11.3.jar:./JarFiles/gson-2.6.2.jar:./JarFiles/twitter4j-async-4.0.4.jar:./JarFiles/twitter4j-core-4.0.4.jar:./JarFiles/twitter4j-examples-4.0.4.jar:./JarFiles/twitter4j-media-support-4.0.4.jar:./JarFiles/twitter4j-stream-4.0.4.jar" crawler/streamTest.java

# Run Command for Twitter Crawler:
echo "Running Twitter Crawler" 
java -cp "./JarFiles/jsoup-1.11.3.jar:./JarFiles/gson-2.6.2.jar:./JarFiles/twitter4j-async-4.0.4.jar:./JarFiles/twitter4j-core-4.0.4.jar:./JarFiles/twitter4j-examples-4.0.4.jar:./JarFiles/twitter4j-media-support-4.0.4.jar:./JarFiles/twitter4j-stream-4.0.4.jar:." crawler.streamTest
