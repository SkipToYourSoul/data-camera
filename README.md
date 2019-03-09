# data-camera
**Let your data [DANCE](http://47.100.187.24:8080/camera/index)**

data-camera是一个传感器数据可视化的数据平台，你可以在系统中绑定你的传感器设备，以应用场景的方式记录你的数据。

data-camera提供了数据分析的功能，你可以任意截取和标注你的记录数据，并做成有意义的内容分享给其他用户。

## data-camera-web
data-camera的web应用模块，提供用户对设备和数据的交互界面，系统中主要包括以下页面：

首页：提供功能列表与场景说明

场景页：对场景进行试验和分析操作

设备页：对传感器设备进行管理

内容页：浏览和管理发布的内容

管理页：管理员专用，提供对设备、用户、权限、数据库后台的管理功能

[说明链接](https://github.com/SkipToYourSoul/data-camera/blob/master/data-camera-web/README.md)

## data-camera-server
**分为三个子模块：**

* listener：数据监听模块，实时监听系统全局变量数据，包括设备信息、实验状态等
* socket：数据接收模块，使用java netty搭建的socket服务，负责与设备进行通信
* filter：数据存储模块，根据当前变量信息，对接收到的数据进行存储
