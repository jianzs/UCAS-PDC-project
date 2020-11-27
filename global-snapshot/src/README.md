## 项目结构
- Main.java：主程序，构建进程，并构建发送和接收线程，持续发送和接收消息，0号进程当状态（counter）大于等于101时，发起构建快照。当各进程本地快照构建结束时，结束发送和接收线程。
- Process.java：进程类，即该项的主要实体，能够发送和接收消息，状态为接收消息的次数（counter）。
- Status.java：本地状态类，用于构建快照期间，保存各进程在此次构建过程中的本地状态，和input通道的状态。各进程一份。
- Utils.java：工具类，定义了几个常量和Socket发送函数。

## 使用
MacOS或者Linux在配置好JDK的情况下，直接在项目根目录执行start.sh应该就OK。

首先对项目进行编译，输出值out目录下
```shell script
javac -d out/ src/*.java
```

然后进入out目录下，执行Main程序。每个进程由命令行参数初始化一个id号，并告知总进程数processNum，id必须在0-processNum之间。
```shell script
cd out 
java Main <id> <processNum>
```

执行start.sh输出时，换行会比较乱，要想输出换行更清晰，需要在两个终端中，尽可能同时开始多个程序。题目要求两个进程，所以只需要开两个即可。
```shell script
# 一个终端
java Main 0 2

# 另一个终端
java Main 1 2
```


## 不足
- 监听进程写死为7000+id，如果id随意指定，则造成消息队列等数组越界访问。
- 消息格式写死了，发送的消息格式为`<id>!@#<msg>`，目前测试使用的msg有两个，一个为普通消息message+id，一个为标记消息marker+id。