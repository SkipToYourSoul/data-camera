# data-camera
**Let your data [DANCE](http://47.100.187.24:8080/camera/index)**

data-camera是一个传感器数据可视化的数据平台，你可以在系统中绑定你的传感器设备，以应用场景的方式记录你的数据。

data-camera提供了数据分析的功能，你可以任意截取和标注你的记录数据，并做成有意义的内容分享给其他用户。

## data-camera-web
data-camera的web应用模块，提供用户对设备和数据的交互界面，系统中主要包括以下页面：

1. 首页：提供功能列表与场景说明

2. 场景页：对场景进行试验和分析操作

3. 设备页：对传感器设备进行管理

4. 内容页：浏览和管理发布的内容

5. 管理页：管理员专用，提供对设备、用户、权限、数据库后台的管理功能

[说明链接](https://github.com/SkipToYourSoul/data-camera/blob/master/data-camera-web/README.md)

## data-camera-server
data-camera的后端设备交互模块，提供与传感器设备进行交互的接口以及与web应用进行通信的接口。

server分为三个子模块：

* socket模块：与传感器设备进行交互，实现接口传感器设备的数据，保持心跳链接，监听系统的设备信息、实验状态等功能
* web-socket模块：与web应用进行通信，通过web-socket接口实时推送数据到前端
* simulator模块：模拟传感器数据的生成和推送，用作测试

[说明链接](https://github.com/SkipToYourSoul/data-camera/blob/master/data-camera-server/README.md)

## data-camera-video

视频流处理模块，暂未使用