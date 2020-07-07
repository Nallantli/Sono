#!/bin/bash
for f in src/ext/LIB_*.java
do
	filename=`basename $f .java`
	echo "Compiling $filename.java..."
	javac -cp src src/ext/$filename.java -encoding utf8 -d bin/lib
	cd bin/lib
	echo "Creating $filename.jar..."
	jar cvfe $filename.jar ext.$filename ./ext
	cd ../..
done
rm -rf bin/lib/ext
rm -rf bin/lib/main
echo "Done."