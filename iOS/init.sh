#!/usr/bin/env bash

cd $(dirname $0)
cd SDK
[ -e TXLiteAVSDK_UGC.framework ] && exit 0
mkdir tmp
cd tmp
curl "http://liteavsdk-1252463788.cosgz.myqcloud.com/TXLiteAVSDK_UGC_iOS_latest.zip" -o TXLiteAVSDK_UGC_iOS_latest.zip
unzip -q TXLiteAVSDK_UGC_iOS_latest.zip
path=$(find . -type d -name "TXLiteAVSDK_UGC.framework" -print -quit)
mv $path ..
cd ..
rm -rf tmp
