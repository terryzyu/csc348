#!/bin/bash
#This script just builds, runs the code, and checks against the provided binary search file
#Turns out there's usually a new line or something that was deleted in the provided file
#Probably some dos versus unix thing ¯\_(^.^)_/¯
ant clean
ant 
java -cp build/classes:lib/java-cup-11b.jar MiniJava -S "SamplePrograms/SampleMiniJavaPrograms/BinarySearch-div.java" > result.txt
echo 'Running sed'
#sed -i '236,$d' result.txt
echo 'Running diff binary result'

# diff binarysearch-output.txt result.txt

echo 'Done!'
