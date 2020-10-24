# Summer
这是一个支持分布式和集群的java游戏服务器框架，可用于开发棋牌、回合制等游戏。基于netty实现高性能通讯，支持tcp、http、websocket等协议。支持消息加解密、攻击拦截、黑白名单机制。封装了redis缓存、mysql数据库的连接与使用。轻量级，便于上手。

[summer2 coding ...](https://github.com/SwingFrog/Summer2)

## 目录
- [更新说明](#更新说明)
- [环境介绍与安装说明](#环境介绍与安装说明)
- [快捷上手](#快捷上手)
  - [创建项目](#创建项目)
  - [项目结构](#项目结构)
  - [运行项目](#运行项目)
- [框架介绍](#框架介绍)
  - [组件介绍](#组件介绍)
  - [注解介绍](#注解介绍)
  - [核心方法介绍](#核心方法介绍)
  - [异常介绍](#异常介绍)
  - [协议介绍](#协议介绍)
  - [Web介绍](#Web介绍)
  - [运行机制](#运行机制)
  - [其他介绍](#其他介绍)


## 更新说明

### 2020.x
1. 异步请求远程接口增加RemoteCallbackQuick接口，可用于接收响应的数据。
2. 新增meter包，可用于编写压测程序。
3. 新增promise包，可用于异步有序执行。
4. 优化SessionContextGroup，让SessionContext与Channel直接绑定。
5. 优化SessionQueueMgr

### 2020.09.19
1. 新增协议，支持protobuf。
2. 新增标准的WebSocket协议，与原来的区别是去掉了包头的四个字节。
3. remote中的方法限定符如果不是public将不对远程开放。

### 2020.09.13
1. 心跳机制优化。
2. 用户请求默认在用户队列中进行处理，@SessionQueue废弃。

### 2020.09.08
1. SessionContext调整，新增属性token，token可以在用户登录以后手动设置为用户ID，以此作为用户的唯一标识。
2. SessionQueueMgr、SingleQueueMgr优化。SessionQueueMgr，不再使用直接使用SessionContext分配队列，改为使用SessionContext中的token分配队列，当token未设置时使用sessionId。
3. http协议下，sessionId不再作为用户的唯一标识，sessionId仅作为链路标识。当用户请求接口携带的cookie数据中不存在token时，响应时会下发通过UUID生成的32位字符串作为token，用户下一次请求时就会携带有token。通过sessionContext.getToken()获取token，通过sessionContext.clearToken()清空token。
4. 修复部分因为hashcode引发的问题。修复方式，将set改为list，重写hashcode，map中的key如果无法重写hashcode则对结构进行调整。
5. 修正SessionHandler中的accept拼写错误
6. test例子取消lombok依赖。

### 2020.05.18
1. 缓存仓库调整，当PrimaryKey为非自增模式时，使用CacheKey查询经历过remove，add的相同PrimaryKey的实体时，尽管实体中的CacheKey与查询的值不同也依旧能命中缓存，现将其进行修复。当主动remove时，会将存有对应PrimaryKey的CacheKey缓存清除。

### 2020.05.09
1. ServerBootstrap启动参数中的ChannelOption.SO_BACKLOG改为读取配置表，并将ChannelOption.ALLOCATOR设为PooledByteBufAllocator.DEFAULT
2. 仓库调整，增加getOrCreate方法。
3. 当项目未能在预期情况下运行时（例如端口被占用），及时终止进程。

### 2020.04.28
1. 当使用仓库时，实体的某个字段的类型如果为Array、Collection(List Set Queue)、Map、以及自定义的类时，需设定length。当length<=255，使用CHAR类型；当length<=16383，使用VARCHAR类型；当length>16383，使用TEXT类型。
2. SessionContext中的address分为directAddress和realAddress，其中directAddress为链路IP，realAddress通过Head中的"X-Forwarded-For"获取客户端真实IP，仅在HTTP、WEBSOCKET协议且在使用反向代理的情况下生效。
3. AsyncResponseMgr新增process方法可用于捕获异常以及自动发送响应。

### 2020.01.17
1. 日志调整，由原来的log4j改为logback，并且移除直接调用log4j的所有代码，完成了日志解耦，可自由替换成slf4j的其他实现。
2. 由于不再强制加载日志配置文件，Summer.hot移除了日志配置文件路径的参数。
3. Remote新增异步响应请求，可参考test中的例子。

### 2019.12.27
使用mvn重构项目

### 2019.12.26
1. 异步缓存仓库调整，修复了异步add、异步remove在一起使用时，可能导致remove失败的问题，并进行了一定的优化，对同一对象先后进行add、remove，当发生在定时器同一执行期内，其操作相互抵消，不再进行数据库操作
2. Http协议下，抛出CodeException异常时，日志输出级别由error改为warn

### 2019.12.05
1. 缓存仓库调整，移除查询列表时按主键的hash code排序，以此提高性能。如果有业务需求，可以考虑在前端进行排序，分散后端压力。

### 2019.11.07
1. 异步缓存仓库调整，当有一个持久化对象，对它进行add、remove、add、remove操作时不能达到预期效果，因为id在第二次add时就发生变动，由于是同一个对象，所以当定时器触发写库时，第一个add的id已经变成了第二个add时的id，第一个remove的id也发生了变化。正确的用法是clone对象后再进行第二次add、remove，但考虑到有些场景是可以复用对象的，于是对异步缓存仓库层进行了调整，add或remove时会记录当时的id，再进行操作。

### 2019.10.25
1. 修复某些情况下请求接口时会出现参数错误的问题

### 2019.10.17
1. 修复部分线程池不能优雅关闭
2. EventMgr改名为EventBusMgr
3. 修改了业务线程池和推送线程池

### 2019.10.16
1. 修复持久化层仓库模式，list无法正确返回数据，并进行优化，当多次list之间的间隔时间小于缓存过期时间时，可直接命中缓存。
2. 从端口minor新增配置useMainServerThreadPool，默认为false，当置为true时，从端口将使用主端口的线程池 (监听线程池, 读写线程池, 业务线程池)。

### 2019.10.14
1. http协议下，实现了cookie机制，sessionId是识别唯一用户的标志，而sessionContext只是链路。当用户请求接口携带的cookie数据中不存在sessionId时，响应时会下发通过UUID生成的32位字符串作为sessionId，用户下一次请求时就会携带有sessionId。通过sessionContext.getSessionId()获取sessionId，通过sessionContext.clearSessionId()清空sessionId。
2. 新增一个用于统计接口调用情况的静态类RemoteStatistics。
3. 修复持久化层仓库模式，无法解析Date的问题。

### 2019.08.30
1. 移除之前实现的简单持久化层(2019.06.22的第三点)
2. 新增SessionContent.getWaitWriteQueueSize，可获得待写队列的长度
3. 接口SessionHandler新增抽象方法sending(SessionContext ctx)，当尝试数据写入时会回调此方法，可配合第二点当待写队列长度过大时关闭会话
4. 新增持久化层，采用仓库模式(com.swingfrog.summer.db.repository)，实体上使用仓库相关的注解，以及dao继承RepositoryDao，即可实现仓库模式，自动建表，无需手动写SQL语句。除了基础的仓库功能还有缓存仓库CacheRepositoryDao、异步缓存仓库AsyncCacheRepositoryDao，推荐使用异步缓存仓库，支持异步/同步插入、更新、删除，以及同步查询。

### 2019.06.22
1. 新增接口Lifecycle，当组件实现此接口后，即可在服务器启动时触发start方法，在服务器关闭时触发stop方法。
2. 新增方法<T> Set<T> Summer.listDeclaredComponent(Class<T> clazz)，可用于采集容器内的组件，例如第一点正是使用此方法采集所有实现Lifecycle的组件。
3. 实现了一个简单的持久化层，支持缓存和异步写入，但对于结构有特定要求。使用时将实体继承AbstractDelayCacheEntity，dao继承DelayCacheDao，即可直接使用并自动建表。

### 2019.06.12
1. 支持一个进程启动多个服务端口，仅需对配置进行修改，详细请见下方服务器配置文件。在使用@ServerHandler时，默认监听主端口，若需要监听其他端口，可使用@ServerHandler(serverName)指定服务器名。


### 2019.05.22
1. 修复了http协议下请求资源css、js等，content-type错误的问题。
2. 注解@SingleQueue(key)，新增通配符${arg}，arg为Remote上的参数。
3. 修复注解@Synchronized不生效的问题。

### 2019.02.20
1. 新增Summer.getServerEventLoopGroup，用于获取服务器业务线程池
2. 新增Summer.getSessionQueueSize，用于获取会话队列长度
3. 优化SessionQueue、SingleQueue队列，不再分配新线程，将以队列的形式逐个提交到服务器业务线程中（在此特别感谢一位大哥的支持与协助）

### 2019.02.17
1. ClientRemote类，新增rsyncRemote方法，调用接口超时将自动重试直到成功为止。
2. ClientRemote类，新增getServerName方法，用于获取连接其他服务器的节点名称。
3. 新增Summer.getRemoteInvokeObjectWithRetry、Summer.getRandomRemoteInvokeObjectWithRetry，用于获取连接其他服务器的远程调用接口代理对象，超时将自动重试直到成功为止。

### 2019.01.28
1. 修复了，服务器之间远程调用可能出现丢包的问题，原因是消息id不能正确的递增，解决办法是修改了消息id的判断。
2. 当作为web服务器时，若接口返回的类型不是WebView，则将返数据序列化成Json，并返回TextView。

## 环境介绍与安装说明
JDK 1.8 以上<br/>
MySql 5.7 (仅供参考)<br/>
Redis 5.0 (仅供参考)<br/>

## 快捷上手
### 创建项目
1. 创建mvn项目
2. 添加summer到pom.xml
```
    <dependency>
        <groupId>com.swingfrog.summer</groupId>
        <artifactId>summer</artifactId>
        <version>1.1.0</version>
    </dependency>
```

### 项目结构
- src
- lib
- config
- Template #Web项目
- WebContent #Web项目

#### src
##### 包结构
- com.test.summerDemo
  - bean #实体
  - constant #常量
  - dao #数据库操作
  - service #业务逻辑
  - event #事件触发器
  - handler #服务器会话回调
  - push #推送接口
  - remote #远程接口
  - task #定时任务
  - exception #异常信息
  - manager #对象管理
  - util #工具
  - SummerDemoApp.java #启动类

##### 启动类
```java
package com.test.summerDemo;

import com.swingfrog.summer.app.Summer;
import com.swingfrog.summer.app.SummerApp;

public class SummerDemoApp implements SummerApp {

	@Override
	public void init() {

	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}
	
	public static void main(String[] args) throws Exception {
		Summer.hot(new SummerDemoApp());
	}

}
```
##### 其余的组件将在下文逐一介绍

#### lib
引用外部jar包请放在此目录下，并添加引用。<br/>
引用Summer.jar，此jar包依赖SummerServer库。

#### config
- db.properties #数据库配置文件
- redis.properties #缓存配置文件
- task.properties #任务配置文件
- server.properties #服务器配置文件

##### db.properties (druid的配置文件)
```properties
driverClassName=com.mysql.jdbc.Driver
url=jdbc:mysql://127.0.0.1:3306/db_test?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
username=root
password=123456
filters=stat
initialSize=2
maxActive=300
maxWait=60000
timeBetweenEvictionRunsMillis=60000
minEvictableIdleTimeMillis=300000
validationQuery=SELECT 1
testWhileIdle=true
testOnBorrow=false
testOnReturn=false
poolPreparedStatements=false
maxPoolPreparedStatementPerConnectionSize=200

asyncCache.coreThread=0
```

##### redis.properties (jedis配置文件)
```properties
url=127.0.0.1
port=6379
timeout=3000
password=123456
blockWhenExhausted=true
evictionPolicyClassName=org.apache.commons.pool2.impl.DefaultEvictionPolicy
jmxEnabled=true
maxIdle=8
maxTotal=200
maxWaitMillis=100000
testOnBorrow=true
```

##### task.properties (quartz配置文件)
```properties
org.quartz.scheduler.instanceName=Task
org.quartz.scheduler.rmi.export=false
org.quartz.scheduler.rmi.proxy=false
org.quartz.scheduler.wrapJobExecutionInUserTransaction=false
org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool
org.quartz.threadPool.threadCount=10
org.quartz.threadPool.threadPriority=5
org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread=true
org.quartz.jobStore.misfireThreshold=60000
org.quartz.jobStore.class=org.quartz.simpl.RAMJobStore
```

##### server.properties (服务器配置文件) [2020.05.09更新]
```properties
#服务器集群名称
server.cluster=Gate
#服务器节点名称
server.serverName=gate_s1
#绑定地址
server.address=127.0.0.1
#绑定端口
server.port=8828
#通讯协议
server.protocol=Http
#消息编码
server.charset=UTF-8
#消息密码
server.password=123456
#侦听线程数
server.bossThread=0
#读写线程数
server.workerThread=0
#业务线程数
server.eventThread=0
#消息最大长度 单位字节
server.msgLength=1024000
#心跳时间 单位秒
server.heartSec=40
#请求的间隔时间
server.coldDownMs=10
#是否开启连接白名单
server.allowAddressEnable=true
#白名单允许连接的地址
server.allowAddressList=127.0.0.1,127.0.0.2
#SOCKET: SO_BACKLOG
server.optionSoBacklog=1024

#服务器的其他端口列表
server.minorList=gate_s2

minor.gate_s2.cluster=Gate
minor.gate_s2.serverName=gate_s2
minor.gate_s2.address=127.0.0.1
minor.gate_s2.port=8080
minor.gate_s2.protocol=Http
minor.gate_s2.charset=UTF-8
minor.gate_s2.password=
minor.gate_s2.bossThread=0
minor.gate_s2.workerThread=0
minor.gate_s2.eventThread=0
minor.gate_s2.msgLength=1024000
minor.gate_s2.heartSec=40
minor.gate_s2.coldDownMs=10
minor.gate_s2.allowAddressEnable=false
minor.gate_s2.allowAddressList=
minor.gate_s2.optionSoBacklog=1024
#使用主端口的线程池 (监听线程池, 读写线程池, 业务线程池) 默认为false
minor.gate_s2.useMainServerThreadPool=true


#连接其他服务器的列表
server.clientList=account_s1,account_s2

#其他服务器的集群名称
client.account_s1.cluster=Account
#其他服务器的节点名称
client.account_s1.serverName=account_s1
#连接地址
client.account_s1.address=127.0.0.1
#连接端口
client.account_s1.port=8828
#通讯协议
client.account_s1.protocol=StringLine
#消息编码
client.account_s1.charset=UTF-8
#消息密码
client.account_s1.password=123456
#读写线程数
client.account_s1.workerThread=0
#业务线程数
client.account_s1.eventThread=0
#消息最大长度 单位字节
client.account_s1.msgLength=1024
#心跳时间 单位秒
client.account_s1.heartSec=20
#断线重连间隔时间 单位毫秒
client.account_s1.reconnectMs=100
#远程调用超时时间 单位毫秒
client.account_s1.syncRemoteTimeOutMs=5000
#连接数
client.account_s1.connectNum=1

client.account_s2.cluster=Account
client.account_s2.serverName=account_s2
client.account_s2.address=127.0.0.1
client.account_s2.port=8828
client.account_s2.protocol=StringLine
client.account_s2.charset=UTF-8
client.account_s2.password=123456
client.account_s2.workerThread=0
client.account_s2.eventThread=0
client.account_s2.msgLength=1024
client.account_s2.heartSec=20
client.account_s2.reconnectMs=100
client.account_s2.syncRemoteTimeOutMs=5000
client.account_s2.connectNum=1
```

### 运行项目

#### 开发环境
在eclipse中可直接运行或调试，启动类为SummerDemoApp.class

#### 生产环境
##### 打包
在Options中勾选Add directory entries。<br/>
注意，不要导出可运行的jar文件，因为会把lib中引用的jar和引用的库打包进jar中，造成jar体积巨大。
##### 项目结构
- SummerDemo
  - SummerDemo.jar
  - lib
  - config
  - Template
  - WebContent
- SummerRuntime.jar
##### 使用SummerRuntime.jar运行
java -jar SummerRuntime.jar SummerDemo/SummerDemo.jar com.test.summerDemo.SummerDemoApp

## 框架介绍
### 组件介绍
SummerApp由辅助组件和主要组件组成，其中bean、constant、manager、util、exception为辅助组件，dao、service、event、handler、push、remote、task、app为主要组件。

#### bean
javabean、数据表的实体映射

#### constant
常量声明

#### manager
对象管理，使用时在类上方使用注解@Bean
```java
@Bean
public class LoginManager {

    private ConcurrentHashMap<Integer, SessionContext> accountIdMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<SessionContext, Integer> sessionContextMap = new ConcurrentHashMap<>();
    //省略...
	
}
```
#### util
工具类

#### exception
异常信息声明

#### dao
数据库操作，类需继承BaseDao并使用注解@Dao
```java
@Dao
public class AccountDao extends BaseDao<Account> {

    public Account getById(int id) {
        return getBean("select * from t_account where id = ?", id);
    }
	
}
```

```java
public abstract class BaseDao<T> {

    protected int update(String sql, Object... args){}
    protected Long insertAndGetGeneratedKeys(String sql, Object... args){}
    protected T getBean(String sql, Object... args) {}
    protected List<T> listBean(String sql, Object... args) {}
    protected <E> E getValue(String sql, Object... args) {}
    protected <E> List<E> listValue(String sql, Object... args) {}
    protected Map<String, Object> getMap(String sql, Object... args) {}
    protected List<Map<String, Object>> listMap(String sql, Object... args) {}
    protected <E> E getBeanByClass(String sql, Class<E> clazz, Object... args) {}
    protected <E> List<E> listBeanByClass(String sql, Class<E> clazz, Object... args) {}
    
}
```

#### service
业务处理，使用时在类上方使用注解@Service<br/>
```java
@Service
public class AccountService {

    @Autowired
    private AccountDao accountDao;
	
    public Account getAccountById(int accountId) {
        return accountDao.getById(accountId);
    }
	
}
```

#### event
事件处理器，使用时在类上方使用注解@EventHandler<br/>
在对应方法上方使用注解@BindEvent，参数为监听的事件的名称。也可使用@BindEvent(value = "事件名称", index = 1)，index表示同名事件处理器的先后顺序，index小到大，顺序先到后。<br/>
如果该方法的返回值不为viod、null，则表示对该事件进行拦截，因此后面的事件处理器便不会收到通知。
```java
@EventHandler
public class FriendEvent {

    @BindEvent("登录事件")
    public void noticeFriend(int accountId) {}
	
}
```

#### handler
服务器会话回调，类需实现SessionHandler并使用注解@ServerHandler<br/>
此组件主要用于网关服务器，可对用户的连接和请求进行拦截或其他处理
```java
@ServerHandler
public class LoginHandler implements SessionHandler {
	
    //是否允许该会话连接服务器 此处可进行黑名单拦截或白名单放行
    @Override
    public boolean accept(SessionContext sctx) {
        return true;
    }

    //会话连接成功
    @Override
    public void added(SessionContext sctx) {

    }

    //会话心跳超时
    @Override
    public void heartTimeOut(SessionContext sctx) {

    }

    //会话发来的消息长度大于配置
    @Override
	public void lengthTooLongMsg(SessionContext sctx) {

	}

    //是否接收会话发来的消息
    @Override
    public boolean receive(SessionContext sctx, SessionRequest request) {
        return true;
    }

    //是否接收会话发来的消息 用于protobuf
    @Override
    public boolean receive(SessionContext ctx, ProtobufRequest request) {
        return true;
    }

    //会话断开连接
    @Override
    public void removed(SessionContext sctx) {

    }

    //会话发送重复消息
    @Override
    public void repetitionMsg(SessionContext sctx) {
	
    }

    //会话发送消息次数间隔小于配置
    @Override
    public void sendTooFastMsg(SessionContext sctx) {

    }

    //会话发来的消息无法解析
    @Override
    public void unableParseMsg(SessionContext sctx) {

    }

}
```

#### push
推送接口，使用时在类上方使用注解@Push<br/>
用于将消息推送给连接本服务器的其他服务器或客户端
```java
@Push
public class DataPush {
	
    public void pushDataToAll(DataPushMsg msg) {
        Summer.getServerPush().asyncPushToAll(msg.getRemote(), msg.getMethod(), msg.getData());
    }
}
```

#### remote
远程调用接口，使用时在类上方使用注解@Remote<br/>
SessionContext为调用此接口的会话，此参数可省略。<br/>
除了标记@Optional的参数外，皆为必填参数，如有遗漏会抛出异常。
```java
@Remote
public class AccountRemote {

    @Autowired
    private AccountService accountService;
	
    public Account getAccount(SessionContext sctx, int accountId, @Optional remark) {
        return accountService.getAccountById(accountId);
    }
	
}
```

#### task
定时任务，使用时在类上方使用注解@Task<br/>
在对应的方法上方添加注解@CronTask、@IntervalTask，即表示该方法为一个任务。<br/>
@CronTask("cron 表达式")当时间满足cron表达式时执行该方法<br/>
@IntervalTask(1000)每隔1000毫秒执行该方法，或使用@IntervalTask(value = 1000, delay = 2000)2000毫秒后执行该方法，然后每隔1000毫秒执行该方法。
```java
@Task
public class StatTask {

    @CronTask("0 0/5 * * * ? ")
    public void onlineStatTask() {

    }
	
    @IntervalTask(1000) 
    public void updateXX() {
	    
    }
	
    @IntervalTask(value = 1000, delay = 2000)
    public void waitAndUpdate() {
	    
    }
	
}
```

#### app
app启动类，此类需实现SummerApp且添加程序入口main方法，并在main方法中调用启动框架。<br/>
Summer.hot会在后面提到。
```java
public class SupmersGateApp implements SummerApp {

    private static final Logger log = LoggerFactory.getLogger(SupmersGateApp.class);
	
	//框架初始化后回调
    @Override
    public void init() {
        log.info("gate init");
    }

    //框架启动后回调
    @Override
    public void start() {
        log.info("gate start");
    }

    //框架停止后回调
    @Override
    public void stop() {
        log.info("gate stop");
    }
	
    public static void main(String[] args) throws Exception {
        Summer.hot(new SupmersGateApp());
    }
	
}
```

#### 组件之间的调用关系
remote 可调用service、util、manager、constant、bean、exception<br/>
push 可调用service、util、manager、constant、bean<br/>
handler 可调用service、util、manager、constant、bean<br/>
event 可调用service、util、manager、constant、bean<br/>
task 可调用service、util、manager、constant、bean、exception<br/>
service 可调用dao、service、util、manager、constant、bean、exception<br/>
util 可调用util、manager、constant、bean<br/>
manager 可调用util、manager、constant、bean<br/>
constant 无<br/>
bean 无<br/>
<br/>
remote 由远程服务器或客户端调用<br/>
push 由远程服务器推送调用<br/>
handler 由框架根据会话信息调用<br/>
event 由事件驱动器调用<br/>
task 由任务处理器调用


### 注解介绍
注解主要分为两大类，组件类与辅助类。<br/>
#### 组件类注解
@Bean、@Dao、@Service、@Remote、@Push、@Task、@ServerHandler、@EventHandler<br/>
此类注解只用于类<br/>
使用此注解的类，在框架启动时，会自动扫描进容器并实例化常驻于内存中。

#### 辅助类注解
@Autowired、@Synchronized、@SingleQueue、@SessionQueue、@Optional、@Transaction、@CronTask、@IntervalTask、@BindEvent<br/>
此类注解只用于字段、方法、参数

#### @Bean
声明此类为容器中普通组件(manager、other)。
#### @Dao
声明此类为数据库操作(dao)。
#### @Service
声明此类为业务处理(service)。
#### @Remote
声明此类为远程接口(remote)。
#### @Push
声明此类为推送接口(push)。
#### @Task
声明此类为定时任务(task)。
#### @ServerHandler
声明此类为服务器会话回调(handler)。
#### @EventHandler
声明此类为事件处理器(event)。

#### @Autowired
在使用@Service、@Remote、@Push、@Task、@ServerHandler、@EventHandler这些注解的类中，其字段如果使用此注解，即可实现自动注入，注入的对象由容器提供。<br/>
组件中只有@Bean、@Dao、@Service支持被注入。
```java
@Remote
public class AccountRemote {

    @Autowired
    private AccountService accountService;
	
    @Autowired
    private StatService statService;
	
    @Autowired
    private ItemService itemService;
	
    @Autowired
    private DanService danService;
	
    @Autowired
    private PushManager pushManager;
	
}
```

#### @Synchronized
在使用@Service、@Remote、@Task、@EventHandler这些注解的类中，其方法如果使用此注解，即可为该方法上分布式锁。当该方法被调用时，会尝试获取锁，一直等到获取成功，执行完方法或抛异常会自动释放锁。<br/>
此锁适用于多服务器同步。
```java
@Remote
public class ShopRemote {
    
    @Synchronized("购物锁")
    public void buyGoods(int accountId, int goods) {}
    
}
```

#### @SingleQueue
在使用@Remote注解的类中，其方法如果使用此注解，在多个线程调用此方法是，会排进指定的队列中，依次完成调用。
```java
@Remote
public class StatRemote {
    
    @SingleQueue("队列名称")
    public void peopleOnline(int accountId) {}

    // 使用 ${arg} arg为方法内参数名称
    @SingleQueue("队列名称-${accountId}-${a}")
    public void peopleOffline(int accountId, int a) {}

}
```

#### @Optional
在使用@Remote注解的类中，其方法参数如果使用此注解，即视为选填参数。
```java
@Remote
public class AccountRemote {

    public Account getAccount(SessionContext sctx, int accountId, @Optional remark) {}
	
}
```
#### @Transaction
在使用@Remote、@Task、@EventHandler这些注解的类中，其方法如果使用此注解，即可开启mysql事务管理，方法执行完则提交事务，如抛出异常则回滚事务。
```java
@Remote
public class FriendRemote {

    @Transaction
    public void addFriend(int accountId, String name) {}
	
}
```

#### @CronTask
在使用@Task注解的类中，其方法参数如果使用此注解，即视为定时任务。<br/>
@CronTask("cron 表达式")
```java
@Task
public class StatTask {

    @CronTask("0 0/5 * * * ? ")
    public void onlineStatTask() {}
	
}
```
#### @IntervalTask
在使用@Task注解的类中，其方法参数如果使用此注解，即视为间隔任务。<br/>
@IntervalTask(1000) 立即执行并每1000毫秒再执行。<br/>
@IntervalTask(value = 1000, delay = 2000) 等待2000毫秒执行并每1000毫秒再执行。<br/>
```java
@Task
public class StatTask {
	
    @IntervalTask(1000) 
    public void updateXX() {}
	
    @IntervalTask(value = 1000, delay = 2000)
    public void waitAndUpdate() {}
	
}
```
#### @BindEvent
在使用@EventHandler注解的类中，其方法参数如果使用此注解，即为该方法绑定了相应的事件，当有指定的事件发出时，事件驱动就会调用该方法。<br/>
@BindEvent("事件名称")<br/>
@BindEvent(value = "事件名称", index = 1) index表示同名事件处理器的先后顺序，
index小到大，顺序先到后。
```java
@EventHandler
public class FriendEvent {

    @BindEvent("登录事件")
    public void noticeFriendOnline(int accountId) {}
	
    @BindEvent(value = "登出事件", index = 1)
    public void noticeFriendoffline(int accountId) {}
	
}
```

### 核心方法介绍
核心方法可在任意地方使用。

#### Summer.hot
Summer框架启动方法
```java
public static void hot(SummerApp app) throws Exception {}
public static void hot(SummerApp app, String projectPackage) throws Exception {}
public static void hot(SummerConfig config) {}
```

#### Summer.sync
分布式锁
```java
public static void sync(String key, Runnable runnable) {}
```

#### Summer.execute
队列处理
```java
public static void execute(Object key, Runnable runnable) {}
```

#### Summer.addComponent
添加组件到容器
```java
public static void addComponent(Object obj) {}
```

#### Summer.removeComponent
从容器中移除组件
```java
public static void removeComponent(Object obj) {}
```

#### Summer.getComponent
从容器中获取组件
```java
public static <T> T getComponent(Class<?> clazz) {}
```

#### Summer.listDeclaredComponent
从容器中获取组件
```java
public static <T> List<T> listDeclaredComponent(Class<T> clazz) {}
```

#### Summer.getProxyInstance
创建代理对象
```java
public static <T> T getProxyInstance(Object target, ProxyMethodInterceptor interceptor) {}
```

```java
public interface ProxyMethodInterceptor {

    Object intercept(Object obj, Method method, Object[] args) throws Throwable;
	
}
```

#### Summer.autowired
组件注入，为目标对象中使用@Autowired注解的字段，进行对象注入。
```java
public static void autowired(Object obj) {}
```

#### Summer.getRedisSource
获取Redis源，可用于操作Redis。
```java
public static RedisSource getRedisSource() {}
```

#### Summer.getIntervalTask
创建间隔任务
```java
public static TaskTrigger getIntervalTask(long interval, long delay, String taskName, TaskJob taskJob) {}
```

#### Summer.getCronTask
创建定时任务
```java
public static TaskTrigger getCronTask(String cron, String taskName, TaskJob taskJob) {}
```

#### Summer.startTask
开始任务
```java
public static void startTask(TaskTrigger taskTrigger) {}
```

#### Summer.stopTask
停止任务
```java
public static void stopTask(TaskTrigger taskTrigger) {}
```

#### Summer.getClientRemote
通过集群名称和服务器节点名称获取连接其他服务器的远程调用接口对象
```java
public static ClientRemote getClientRemote(String cluster, String name) {}
```

```java
public class ClientRemote {

    //异步调用远程接口
    public void asyncRemote(String remote, String method, Object data, RemoteCallback remoteCallback) {}
    
    //同步调用远程接口 (如果等待时间超出配置，则抛出异常)
    public <T> T syncRemote(String remote, String method, Object data, Type type) {}
    
}
```

#### Summer.getRandomClientRemote
通过集群名称，随机获取连接其他服务器的远程调用接口对象
```java
public static ClientRemote getRandomClientRemote(String cluster) {}
```

#### Summer.getRemoteInvokeObject
通过集群名称和服务器节点名称获取连接其他服务器的远程调用接口代理对象
```java
public static <T> T getRemoteInvokeObject(String cluster, String name, Class<?> clazz) {}
```

#### Summer.getRemoteInvokeObjectWithRetry
通过集群名称和服务器节点名称获取连接其他服务器的远程调用接口代理对象，超时将自动重试直到成功为止
```java
public static <T> T getRemoteInvokeObjectWithRetry(String cluster, String name, Class<?> clazz) {}
```

#### Summer.getRandomRemoteInvokeObject
通过集群名称，随机获取连接其他服务器的远程调用接口代理对象
```java
public static <T> T getRandomRemoteInvokeObject(String cluster, Class<?> clazz) {}
```

#### Summer.getRandomRemoteInvokeObjectWithRetry
通过集群名称，随机获取连接其他服务器的远程调用接口代理对象，超时将自动重试直到成功为止
```java
public static <T> T getRandomRemoteInvokeObjectWithRetry(String cluster, Class<?> clazz) {}
```

##### 账号服务器
```java
@Remote
public class FriendRemote {
    @Autowired
    private FriendService friendService;

    @Transaction
    public void addFriend(int accountId, String name) {
        this.friendService.addFriend(accountId, name);
    }
}
```

```java
public class AccountServerRemote {
    public static FriendRemote getFriendRemote() {
        return Summer.getRandomRemoteInvokeObject(ClusterConst.ACCOUNT, FriendRemote.class);
    }
}
```

##### 网关服务器
将账号服务器的jar包引入网关服务器中，即可像调用本地方法一样调用远程接口。
```java
@Remote
public class FriendRemote {

    @Autowired
    private LoginManager loginManager;
	
    public void addFriend(SessionContext sctx, String name) {
        int accountId = loginManager.getAccountId(sctx);
        AccountServerRemote.getFriendRemote().addFriend(accountId, name);
    }
	
}
```

#### Summer.getServerPush
获取服务器推送接口对象
```java
public static ServerPush getServerPush() {}
```

```java
public class ServerPush {
	
    //异步推送至该集群内所有服务器
    public void asyncPushToClusterAllServer(String cluster, String remote, String method, Object data) {}
	
    //同步推送至该集群内所有服务器
    public void syncPushToClusterAllServer(String cluster, String remote, String method, Object data) {}

    //异步推送至该集群内随机一台服务器
    public void asyncPushToClusterRandomServer(String cluster, String remote, String method, Object data) {}
	
    //同步推送至该集群内随机一台服务器
    public void syncPushToClusterRandomServer(String cluster, String remote, String method, Object data) {}
	
    //异步推送至该集群中指定的服务器
    public void asyncPushToClusterThisServer(String cluster, String serverName, String remote, String method, Object data) {}
	
    //同步推送至该集群中指定的服务器
    public void syncPushToClusterThisServer(String cluster, String serverName, String remote, String method, Object data) {}
	
    //异步推送至该会话
    public void asyncPushToSessionContext(SessionContext sessionContext, String remote, String method, Object data) {}

    //同步推送至该会话
    public void syncPushToSessionContext(SessionContext sessionContext, String remote, String method, Object data) {}
	
    //异步推送至部分会话
    public void asyncPushToSessionContexts(List<SessionContext> sessionContexts, String remote, String method, Object data) {}

    //同步推送至部分会话
    public void syncPushToSessionContexts(List<SessionContext> sessionContexts, String remote, String method, Object data) {}

    //异步推送至所有会话
    public void asyncPushToAll(String remote, String method, Object data) {}

    //同步推送至所有会话
    public void syncPushToAll(String remote, String method, Object data) {}

}
```

#### Summer.closeSession
关闭会话
```java
public static void closeSession(SessionContext sctx) {}
```

#### Summer.getServerEventLoopGroup
获取服务器业务线程池
```java
public static EventLoopGroup getServerEventLoopGroup() {}
```

#### Summer.getSessionQueueSize
获取会话队列大小
```java
public static int getSessionQueueSize(SessionContext sctx){}
```

#### Summer.createCodeException
创建错误码异常对象
```java
public static CodeException createCodeException(long code, String msg) {}
public static CodeException createCodeException(CodeMsg msg, Object ...args) {}
```
#### Summer.createCodeMsg
创建错误码消息
```java
public static CodeMsg createCodeMsg(long code, String msg) {
```

#### Summer.getCluster
获取集群名称
```java
public static String getCluster() {}
```

#### Summer.getServerName
获取服务器节点名称
```java
public static String getServerName() {}
```

#### Summer.syncDispatch
同步发送消息事件
```java
public static void syncDispatch(String eventName, Object ...args) {}
```

#### Summer.asyncDispatch
异步发送消息事件
```java
public static void asyncDispatch(String eventName, Object ...args) {}
```

#### Summer.getWeb
获取Web相关接口
```java
public static WebMgr getWeb() {}
```

### 异常介绍
#### Error Code 100
调用异常 invoke error<br/>
当调用远程接口出现异常并且非自定义ErrorCode时，就会抛出此异常。

#### Error Code 101
远程接口不存在 remote not exist

#### Error Code 102
远程方法不存在 method not exist

#### Error Code 103
参数错误 parameter error

#### Error Code 104 
远程接口受保护 remote was protected

#### Error Code 105
Protobuf不存在 protobuf not exist
	
#### 自定义 Error Code
##### 异常声明
```java
public class AccountException {

    /**金币不足*/
    public static final CodeMsg GOLD_NOT_ENOUGH = Summer.createCodeMsg(101005, "gold not enough, accountId[%s] own[%s] need[%s]");
	
}
```
##### 异常使用
建议只在remote和service中使用
```java
@Service
public class AccountService {

    @Autowired
    private AccountDao accountDao;
	
    public int gainGold(int accountId, int gainGold) {
        int ownGold = accountDao.getGoldByIdForUpdate(accountId);
        int gold = ownGold + gainGold;
        if (gold < 0) {
            throw Summer.createCodeException(AccountException.GOLD_NOT_ENOUGH, accountId, ownGold, gainGold);
        } else if (gold > AccountConst.GOLD_MAX) {
            gold = AccountConst.GOLD_MAX;
        }
        accountDao.updateGold(accountId, gold);
        return gold;
    }
	
}
```

### 协议介绍
#### 消息定义
##### 请求消息
```json
{"id": 0, "remote": null, "method": null, "data": {}}
```
id 由客户端不断递增，由1开始<br/>
remote 远程接口 -> 类名<br/>
method 远程方法 -> 方法名<br/>
data 数据 -> 方法参数名与值<br/>

##### 响应消息
```json
{"code": 0, "id": 0, "remote": null, "method": null, "data": null, "time": 0}
```
code 错误码，为0标识无异常<br/>
id 与客户端请求消息的id一致<br/>
remote 请求的远程接口<br/>
method 请求的远程方法<br/>
data 返回的数据<br/>
time 时间戳<br/>

##### 推送消息
```json
{"code": 0, "id": 0, "remote": null, "method": null, "data": null, "time": 0}
```
code 为0<br/>
id 为0，可根据id是否为0来判断是否是推送消息<br/>
remote 推送接口<br/>
method 推送方法<br/>
data 推送的数据<br/>
time 时间戳<br/>

#### Protobuf消息定义
##### 请求消息
proto名称格式: 类名_Req_协议消息ID<br/>
例如: HeartBeat_Req_0<br/>

##### 响应消息
proto名称格式: 类名_Resp_协议消息ID<br/>
例如: HearBeat_Resp_0<br/>

##### 推送消息
proto名称格式: 类名_Push_协议消息ID<br/>
例如: Test_Push_100<br/>

#### StringLine协议
本协议支持加解密，支持服务器之间使用。消息格式为字符串，在字符串末尾加入\r\n，因此通过判断分隔符\r\n来区分消息。
```properties
#通讯协议
server.protocol=StringLine
```

#### WebSocket协议
本协议支持加解密，基于WebSocket协议。消息格式为二进制，数据包分为包头和包体，包头占四个字节，用来表示包体的长度。
```properties
#通讯协议
server.protocol=WebSocket
```

#### LengthField协议
本协议支持加解密，支持服务器之间使用。消息格式为二进制，数据包分为包头和包体，包头占四个字节，用来表示包体的长度。
```properties
#通讯协议
server.protocol=LengthField
```

#### WebSocket-Protobuf协议
本协议不支持加解密，基于WebSocket协议。消息格式为Protobuf，数据包分为包头和包体，包头占四个字节，用来表示包体的长度。包体前四个字节为协议消息ID，后面的字节为协议内容。
```properties
#通讯协议
server.protocol=WebSocket-Protobuf
```

#### LengthField-Protobuf协议
本协议不支持加解密，不支持服务器之间使用。消息格式为Protobuf，数据包分为包头和包体，包头占四个字节，用来表示包体的长度。
```properties
#通讯协议
server.protocol=LengthField-Protobuf
```

#### WebSocket-Standard协议
本协议支持加解密，基于WebSocket协议。消息格式为二进制，数据包只包含包体。
```properties
#通讯协议
server.protocol=WebSocket-Standard
```

#### WebSocket-Protobuf-Standard协议
本协议不支持加解密，基于WebSocket协议。消息格式为Protobuf，数据包只包含包体，包体前四个字节为协议消息ID，后面的字节为协议内容。
```properties
#通讯协议
server.protocol=WebSocket-Protobuf-Standard
```

#### Http协议
本协议不支持加解密，基于Http协议。
```java
@Remote
public class TestRemote {
    
    public void test(String msg) {
        
    }
    
}
```

```
//地址:端口/远程接口_远程方法?请求数据
http://127.0.0.1:8080/TestRemote_test?msg=hello
```

```properties
#通讯协议
server.protocol=Http
```

#### 消息加解密算法
##### WebSoccket与LengthField
```java
byte[] bytes = new byte[0];
String pass = "123456"; //密码由配置文件配置
int index = bytes.length % 10;
for (int i = 0; i < bytes.length; i++) {
    if (index >= pass.length)
        index = 0;
    int res = bytes[i] ^ pass[index];
    bytes[i] = (byte)res;
    index++;
}
```
##### StringLine
```java
byte[] bytes = new byte[0];
String pass = "123456"; //密码由配置文件配置
int index = bytes.length % 10;
for (int i = 0; i < bytes.length; i++) {
    if (index >= pass.length)
        index = 0;
    int res = bytes[i] ^ pass[index];
    if (res != 10 && res != 13)
        bytes[i] = (byte)res;
    index++;
}
```

### Web介绍

#### Web配置管理
```java
public class WebMgr {
	
    //重新加载模板
    public void reloadTemplate() {}
	
    //获取模板
    public Template getTemplate(String templateName) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {}

    //获取Web内容文件路径
    public String getWebContentPath() {}

    //设置Web内容文件路径
    public void setWebContentPath(String webContentPath) {}

    //获取模板文件路径
    public String getTemplatePath() {}

    //设置模板文件路径
    public void setTemplatePath(String templatePath) {}

    //获取内部视图渲染工厂
    public InteriorViewFactory getInteriorViewFactory() {}

    //设置内部视图渲染工厂
    public void setInteriorViewFactory(InteriorViewFactory interiorViewFactory) {}

    //获取主页路径
    public String getIndex() {}

    //设置主页路径
    public void setIndex(String index) {}

    //获取图标路径
    public String getFavicon() {}

    //设置图标路径
    public void setFavicon(String favicon) {}
	
}
```

##### 内部视图渲染工厂
如需自定义空白视图或错误视图，只需继承此类覆盖相应的方法。
```java
public class InteriorViewFactory {

    //空白视图
    public BlankView createBlankView() {}
	
    //错误视图
    public ErrorView createErrorView(int status, long code, String msg) {}
	
    //错误视图
    public ErrorView createErrorView(int status, String msg) {}
	
}
```
#### WebView视图渲染
##### TextView
文字视图
```java
new TextView(String text);
```

##### JSONView
JSON视图
```java
new JSONView(JSON json);
```

##### FileView
文件视图
```java
new FileView(String fileName);
```

##### ModelView
模型视图
```java
ModelView model = new ModelView(String view);
model.put(String key, Object value);
```
view 模板地址<br/>
key 键<br/>
value 值

##### BlankView
空白视图
```java
new BlankView();
```

##### ErrorView
错误视图
```java
new ErrorView(int status, long code, String msg);
new ErrorView(int status, String msg);
```
status Http状态<br/>
code 错误码<br/>
msg 错误消息

#### Web异常介绍
##### Error Code 201
没有Web视图 not web view

#### 数据提交
```java
@Remote
public class UserRemote {
    
    public JSONView add(String name, Integer age, @Optional remark) {
        JSONObject json = new JSONObject();
        json.put("flag", true);
        return new JSONView(json);
    }
    
}
```

##### get
```
http://127.0.0.1:8080/UserRemote_add?name=toke&age=22 //remark为选填
```
##### post
```html
<form action="http://127.0.0.1:8080/UserRemote_add" method="post">
    <input type="text" name="name"/>;
    <input type="text" name="age"/>;
    <input type="text" name="remark"/>;
    <input class="button" type="submit"/>
</form>
```

#### 上传文件
```java
@Remote
public class FileRemote {
    
    public void upload(WebFileUpload photo) {
        photo.saveToFile("photos/");
    }
    
}
```

```html
<form action="http://127.0.0.1:8080/FileRemote_upload" method="post" enctype="multipart/form-data">
	<input type="file" name="photo"/>;
	<input class="button" type="submit"/>
</form>
```

```java
public class WebFileUpload {

    //获取文件名
    public String getFileName() {}
	
    //获取数据
    public ByteBuf getByteBuf() {}
	
    //保存到指定路径
    public void saveToFile(String path) throws IOException {}
	
}

```

### 运行机制
通过Summer.hot启动框架。<br/>
初始化 -> 启动 -> 运行时 -> 停止 <br/>
停止需要外部进行操作 kill -2 pid、kill -15 pid

#### 初始化
1. 加载jar包
2. 加载server配置
3. 加载redis配置
4. 加载数据库配置
5. 加载任务配置
6. 扫描组件类注解，实例化并添加进容器
7. 服务器管理初始化
8. 客户端管理初始化 (连接其他服务器)
9. 事件驱动初始化
10. 组件注入对象
11. service、remote、task生成代理对象

#### 启动
1. 服务器启动
2. 客户端连接 (连接其他服务器)
3. 任务启动

#### 运行时
##### 服务器管理
与会话保存心跳联系，心跳超时会通过handler通知。

##### 客户端管理 (连接其他服务器)
与其他服务器保存心跳联系，心跳超时后断线会自动重连。

##### 业务触发
两种方式触发业务处理，主动与被动。<br/>
主动，通过外部调用远程接口remote或会话的行为触发。<br/>
被动，通过内部任务处理器执行task触发。

#### 停止
停止一切

### 其他介绍
#### 负载均衡
随机远程调用和随机推送都是通过轮询实现
```java
    public Client getClientWithNext() {
        int size = clientGroupList.size();
        if (size > 0) {
            if (size == 1) {
                return clientGroupList.get(0).getClientWithNext();
            }
            next ++;
            next = next % size;
            return clientGroupList.get(next % size).getClientWithNext();
        }
        return null;
	}
```

#### Redis操作
##### RedisSource
```java
public class RedisSource {
	
    //通过key获取value
    public String get(String key) {}
	
    //设置key、value
    public String put(String key, String value) {}
	
    //设置key、value，返回是否成功
    public boolean putAndSuccess(String key, String value) {}
	
    //设置key、value，key过期时间
    public String putWithTime(String key, int seconds, String value) {}
	
    //设置key过期时间
    public boolean setExpireTime(String key, int seconds) {}
	
    //取消key过期时间
    public boolean delExpireTime(String key) {}
	
    //获取key剩余时间
    public long getRemainTime(String key) {}
	
    //判断key是否存在
    public boolean exists(String key) {}
	
    //移除key
    public boolean remove(String key) {}
	
    //获取key的类型
    public String getType(String key) {}
	
    //获取map
    public RedisMap getMap(String key) {}
	
    //获取list
    public RedisList getList(String key) {}
	
    //获取set
    public RedisSet getSet(String key) {}
	
    //获取deque
    public RedisDeque getDeque(String key) {}
	
    //清除redis
    public void clear() {}
}
```

##### RedisMap
封装了Redis的Hash
```java
public class RedisMap implements Map<String, String> {}
```

##### RedisList
封装了Redis的List
```java
public class RedisList extends RedisCollection implements List<String> {}
```

##### RedisSet
封装了Redis的Set
```java
public class RedisSet implements Set<String> {}
```

##### RedisDeque
封装了Redis的List
```java
public class RedisDeque extends RedisCollection implements Deque<String> {}
```

##### RedisCollection
```java
public abstract class RedisCollection implements Collection<String> {}
```