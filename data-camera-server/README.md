# data-camera-server

- socket模块，依赖于netty
- web-socket模块

## 报文样式：

| 字段名       | 字段类型  | 字节 |   说明    |
| ----------- | -------- | ---- | -------- |
| msg_type    | byte     | 1    | 0x01 REG_REQ 设备注册请求<br/>0x02 REG_RES 设备注册响应<br/>0x03 NORMAL_REQ 普通请求<br/>0x04 NORMAL_RES 普通响应<br/>0x05 ONE_WAY 单向广播，无需响应<br/>0x06 PING 心跳请求PING<br/>0x07 PONG 心跳响应PONG |
| session_id  | int      | 4    | 用于描述请求——响应的对应关系，任何一个请求和其对应响应session_id是相同的，用于异步情景 |
| flag        | byte     | 1    | 最后一位表示数据类型：0=JSON文本，1=视频流<br/>倒数第二位表示数据是否压缩：0=未压缩，1=压缩（zip or gzip） |
| body_length | int      | 4    | 数据部分的长度                                               |
| body        | string   | x    | json格式的数据                                               |

```javascript
*                  01234567
*    +--------+--------+--------+--------+
*    |              msgType              | -> 8bit
*    +--------+--------+--------+--------+
*    |              SessionId            | -> 32bit
*    +--------+--------+--------+--------+
*    |              Flag                 | -> 8bit
*    +--------+--------+--------+--------+
*    |           Body Length             | -> 32bit
*    +--------+--------+--------+--------+
*    |               Body                |
*    |               ...                 | -> BodyLength bit
*    |               ...                 |
```

## 消息类型

```
 *  1.	0x01 REG_REQ 设备注册请求
 *  2.	0x02 REG_RES 设备注册响应
 *  3.	0x03 NORMAL_REQ 普通请求
 *  4.	0x04 NORMAL_RES 普通响应
 *  5.	0x05 ONE_WAY 单向广播，无需响应
 *  6.	0x06 PING 心跳请求PING
 *  7.	0x07 PONG 心跳响应PONG
 *
 *  20. 0x20 TEST 用于测试
```

## Ack返回包

```java
public enum AckResult {

    UNKNOWN(-1),
    OK(0),
    FAILED(1);

    public final int value;
    AckResult(int value){
        this.value = value;
    }
}
```

## web-socket消息类型

```java
public enum MessageType {
    START_M("START_M"), END_M("END_M"), START_R("START_R"), END_R("END_R"),
    DATA("DATA"), REGISTER("REGISTER");

    private String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
```

## socket包说明

* codec: packet包的编码解码逻辑
* connection: channel connection的管理
* handler: channel handler
* service: packet数据逻辑，包括数据处理和消息发送

## socket流程

接收消息 -> 建立链接(PacketHandler) -> 解码(PacketCodec) -> 处理逻辑(PacketService)
发送消息(xxx.writeAndFlush) -> 编码(PacketCodec)