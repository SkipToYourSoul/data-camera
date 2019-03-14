#!/bin/bash

CUR_DIR=$(dirname $0)
cd $CUR_DIR
CUR_DIR=$(pwd)

nohup java -cp ./data-camera-server.jar com.stemcloud.liye.dc.Application > ./logs/nohup.log 2>&1 &
echo "server start!"
