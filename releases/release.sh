#!/bin/bash
#
# release.sh
# script to build releases of Phoenix
#
OUTPUT_DIR="target"
if [ "x$1" = "x" ]; then
    echo "Error: need version number as first argument."
    exit
fi
REL_DIR=releases/$1
if [ -a $REL_DIR ]; then
    echo "Error: given version number already exists."
    exit
fi
SRC_LIST_NAME="include/src/srclist.txt"
JAR_FILE_LIST_NAME="include/src/classlist.txt"
#make new release directory
mkdir $REL_DIR
#package name
PKG_NAME=Phoenix_${1}.zip
#tmp to store package files
TMP_DIR=$(mktemp --tmpdir -d phoenix_build.XXXXXXXXXX)
# clean & build
CUR_DIR=$(pwd)
cd ..
mvn clean package
cd $CUR_DIR
#copy "executable"
cp ../${OUTPUT_DIR}/Phoenix.jar $TMP_DIR
# sources list for M$Win batch compile
find ../src/ -name "*.java" | cut -d '/' -f 3- | tr '/' '\' > $SRC_LIST_NAME
# class list for M$Win batch compile
find ../${OUTPUT_DIR}/classes/ -name "*.class" | cut -d '/' -f 4- | tr '/' '\' > $JAR_FILE_LIST_NAME
printf "static.ini\n" >> $JAR_FILE_LIST_NAME
#copy includes
cp -r include/* $TMP_DIR
#copy etc
cp -r ../etc/PHOENIX $TMP_DIR
#copy README
cp ../README.md $TMP_DIR
#copy as README.txt for clarity's sake
cp ../README.md ${TMP_DIR}/README.txt
#copy sources
cp -r ../src $TMP_DIR
#copy resources
cp -r ../target/classes/static.ini ${TMP_DIR}/src
#zip files and move zip to release directory
CUR_DIR=$(pwd)
cd $TMP_DIR
zip -r $PKG_NAME *
cd $CUR_DIR
mv ${TMP_DIR}/${PKG_NAME} $REL_DIR
