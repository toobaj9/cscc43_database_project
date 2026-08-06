#!/bin/bash

javac -cp mysql-connector-java-8.0.29.jar src/*.java
if [ $? -ne 0 ]; then
    echo "Error Occurred in compilation."
    exit 1
fi
java  -cp src:mysql-connector-java-8.0.29.jar Main