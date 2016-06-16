#!/bin/sh
CWD=$(pwd)
input=`cat $1`
cd ../Java/CarAI/
echo "$input" | java -Djava.library.path=/home/amee/lib/ -jar target/CarAI-0.0.1-SNAPSHOT-jar-with-dependencies.jar "9"
cd $CWD
