# data-camera
**Let your data dance!**

## data-camera-web
web应用模块，提供用户对数据设备的操作界面

## data-camera-server
分为三个子模块：
listener：数据监听模块，实时监听系统全局变量数据，包括设备信息、实验状态等
socket：数据接收模块，使用java netty搭建的socket服务，负责与设备进行通信
filter：数据存储模块，根据当前变量信息，对接收到的数据进行存储

