#!/bin/bash
#
# release.sh
# script to build releases of Phoenix
#
SRC="src"
INCLUDE="include"
REPOS="Phoenix math"
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
#tmp dir for repository files
TMP_DIR_GIT=$(mktemp --tmpdir -d phoenix_build_rep.XXXXXXXXXX)
# clean & build
CUR_DIR=$(pwd)
cd ..
mvn clean package
cd $CUR_DIR
#copy "executable"
cp ../${OUTPUT_DIR}/Phoenix.jar $TMP_DIR

#copy sources
cd $TMP_DIR
mkdir $SRC
for REPO in ${REPOS} ; do
    cd $CUR_DIR
    cp -r ../../${REPO}/.git* ${TMP_DIR_GIT}
    cd ${TMP_DIR_GIT}
    git checkout master
    git reset --hard
    trash .git*
    cd ${TMP_DIR}/${SRC}
    mkdir ${REPO}
    cp -r ${TMP_DIR_GIT}/* ${REPO}
    trash ${TMP_DIR_GIT}/*
done
#copy README
cd $CUR_DIR
cp ../README.md $TMP_DIR
#copy as README.txt for clarity's sake
cp ../README.md ${TMP_DIR}/README.txt

#copy includes
cp -r ${INCLUDE}/* ${TMP_DIR}
#copy resources
cp -r ${TMP_DIR}/${SRC}/Phoenix/etc/PHOENIX ${TMP_DIR}
#zip files and move zip to release directory
cd $TMP_DIR
zip -r $PKG_NAME *
cd $CUR_DIR
mv ${TMP_DIR}/${PKG_NAME} $REL_DIR
cd $REL_DIR
~/projects/sh/hash_sums/hash_sums.sh
