#!/bin/bash
#
# release.sh
# script to build releases of Phoenix
#
if [ "x$1" = "x" ]; then
    echo "Error: need version number as first argument."
    exit
fi
REL_DIR=releases/$1
if [ -a $REL_DIR ]; then
    echo "Error: given version number already exists."
    exit
fi
#make new release directory
mkdir $REL_DIR
#package name
PKG_NAME=Phoenix_${1}.zip
#tmp to store package files
TMP_DIR=$(mktemp --tmpdir -d phoenix_build.XXXXXXXXXX)
# clean & build
CUR_DIR=$(pwd)
cd ..
ant clean jar
cd $CUR_DIR
#copy "executable"
cp ../dist/Phoenix.jar $TMP_DIR
# sources list for M$Win batch compile
find ../src/ -name "*.java" | cut -d '/' -f 3- | tr '/' '\' > include/src/srclist.txt
# class list for M$Win batch compile
find ../build/classes/ -name "*.class" | cut -d '/' -f 4- | tr '/' '\' > include/src/classlist.txt
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
#zip files and move zip to release directory
CUR_DIR=$(pwd)
cd $TMP_DIR
zip -r $PKG_NAME *
cd $CUR_DIR
mv ${TMP_DIR}/${PKG_NAME} $REL_DIR
