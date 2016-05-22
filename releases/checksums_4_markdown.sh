#!/bin/bash
HASH_LIST="crc32 md5sum sha1sum sha256sum sha512sum"
OUT_FILE="tmp/TMP_MD_CHECKSUM_LIST.TXT"
echo "Hash | Value" > $OUT_FILE
echo "-------- | --------" >> $OUT_FILE
echo "Hash list:${HASH_LIST}"
for HASH in $HASH_LIST ; do
	FILE_NAME=$(ls *.${HASH})
	echo $FILE_NAME
	HASH_SUM=$(cat ${FILE_NAME} | cut -d ' ' -f 1)
	echo $HASH_SUM
	echo "[${HASH}](https://sites.google.com/site/phoenixefsport/checksums/${FILE_NAME}) | ${HASH_SUM}" >> $OUT_FILE
done

