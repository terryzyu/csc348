#!/bin/bash
#This script iterates through all sample programs and runs the Parser on them
#It looks for unexpect or unknown which is some errors I put into the code for me to search for
#If one of those keywords is found it'll print it out 
ant clean
ant
FILES=SamplePrograms/SampleMiniJavaPrograms/*.java
for f in $FILES
do

	java -cp build/classes:lib/java-cup-11b.jar MiniJava -P $f > result.txt
	grep -i "unexpect" result.txt
	grep -i "unknown" result.txt
	echo ${f##*/}
done

echo "Done!"
