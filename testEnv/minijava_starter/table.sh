#!/bin/bash
#This script just builds, runs the code, and checks against the provided binary search file PRETTY PRINT

ant clean
ant

#Uncomment below to compile the cup file and have it display errors/warnings. TURNS OUT THIS WAS AN ERROR IN MY SCRIPT
#java -jar lib/java-cup-11b.jar -expect 1 src/Parser/minijava.cup 

#Runs code
java -cp build/classes:lib/java-cup-11b.jar MiniJava -T "SamplePrograms/SampleMiniJavaPrograms/BinarySearch.java" > result.txt

echo 'Running diff binary result'

#diff BinarySearch-prettyprint.txt result.txt
cat result.txt
echo 'Done!'
