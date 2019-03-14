#!/bin/bash

CUR_DIR=$(dirname $0)
cd $CUR_DIR
CUR_DIR=$(pwd)

nohup java -jar ./data-camera-web.jar > ./logs/nohup.log 2>&1 &
echo "server start!"
