# data-camera-web

详细代码说明：

- 前端代码
- 后端代码
- 配置项

## 前端模块

技术选型：

bootstrap（前端样式框架）+jquery+echarts（图表可视化）+thymeleaf（模板）

其他第三方js插件包括：

bootstrap-table、form-validation、jquery-ui等

|            | HTML                                                         | CSS                             | JS                                                           |
| ---------- | ------------------------------------------------------------ | ------------------------------- | ------------------------------------------------------------ |
| 首页       | index.html                                                   | index.css                       |                                                              |
| 场景页     | app.html<br/>app_tab.html（场景内容）                        | app.css<br/>app-content.css     | app/*.js<br/>app-init.js（入口）                             |
| 设备页     | device.html                                                  | device.css                      | device/device-controller.js                                  |
| 内容页     | content.html<br/>hot-content.html                            | content.css<br/>app-content.css | content/content-detail.js<br/>content/hot-content.js<br/>app/app-*.js |
| 内容分享页 | share.html                                                   | share.css                       | content/share.js                                             |
| 管理页     | admin.html                                                   |                                 |                                                              |
| 其他       | login.html<br/>exception.html<br/>denied.html                | login.css<br/>custom.css        |                                                              |
| 通用模块   | common.html（包括导航和脚注）<br/>modal.html（包括各页面弹出的modal） | main.css（通用）                | common.js（通用）                                            |

## 后端模块

@Controller

- ViewController：页面视图路由
- CrudController：场景、设备、数据的增删改查操作
- ActionController：实验的监控和录制操作
- DataController：数据分析、图表数据读取等数据相关操作
- AdminController：后端数据管理使用

@Service

- BaseInfoService：对场景、轨迹、设备、内容的基本操作
- OssService：与阿里云进行上传交互
- ScheduleService：定期的数据清洗操作
- 其他：与@Controller对应

@Config

- 包括：deniedPageConfig、RedisConfig、SecurityConfig、WebSocketConfig、ExceptionConfig、WebMvcConfig

domain和dao：参考数据schema的设计

druid：使用阿里开源数据连接池druid链接mysql

oss：与阿里云文件系统的交互操作

util：一些通用化的工具

## 配置项

- server：web服务的context-path以及端口号
- thymeleaf：模板相关配置
- druid：mysql链接配置
- db-pool：连接池相关配置
- redis：redis链接配置
- log：日志配置
- oss：oss配置