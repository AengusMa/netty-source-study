## netty源码学习


### 1.服务端启动过程
1. 创建channel
bind()[用户代码入口]
    initAndRegister()[初始化注册]
        newChannel()[创建服务器端channel]
            通过用户在ServerBootstrap.channel中传入的NioServerSocketChannel创建
ReflectiveChannelFactory反射创建服务器端Channel()
newSocket()[通过jdk来创建底层jdk channel]
创建NioServerSocketChannelConfig()[tcp参数配置类]
调用父类AbstractNioChannel()构造函数
    configureBlocking(false)[设置非阻塞模式]
    调用父类AbstractChannel()构造函数[创建id(channel标识),unsafe(channel底层的操作),pipeline]
2. 初始化channel
int()[初始化入口]
    set ChannelOptions,ChannelAttrs
    set ChildOptions,ChildAttrs
    config handler[配置服务端pipleline]
    add ServerBootstrapAcceptor[添加连接器]
3. 注册selector
AbstractChannel AbstractUnsafe register(channel)入口
    this.eventloop = eventloop[绑定线程]
    register0()[实际注册]
        doRegister()[调用jdk底层注册]
        invokeHandlerAddedIfNeeded()[传播事件]
        fireChannelRegisted()[传播事件]
4. 端口绑定
AbstractUnsafe.bind()[入口]
    doBind()
        javaChannel().bind()[jdk底层绑定]
    pipeline.fireChannelActive()[传播事件]
        HeadContext.readIfIsAutoRead()
### 2.NioEventLoop
- NioEventLoop创建
new NioEventLoopGroup()[线程组，默认2*CPU]
    new ThreadPerTaskExecutor()[线程创建器]
        ThreadPerTaskExecutor：
            每执行任务都会创建一个线程实体
            NioEventLoop线程命名规则nioEventLoop-1-xx              
    for() {new Child()}[构造NioEventLoop]
        保存线程执行器ThreadPerTaskExecutor
        创建一个MpscQueue
        创建selector
    choosertFactory.newChooser()[线程选择器]
        isPowerOfTwo(){(val & -val) == val;}[判断是否是2的幂。2,4,8,16] 
            PowerOfTowEventExecutorChooser[优化]
                index++&(length-1)
            GenericEventExecutorChooser[普通]
                abs(index++%length)
- NioEventLoop启动
    服务端启动绑定端口
        bind()->execute(task)[入口]
            startThread()->doStartThread()[创建线程]
                ThreadPerTaskExecutor.execute()
                    thread=Thread.currentThread()[保存线程]
                    SingleThreadEventExecutor.this.run(实际NioEventLoop.run()[启动])
    新连接接入通过chooser绑定一个NioEventLoop    
- NioEventLoop执行逻辑
 实际NioEventLoop.run()->for{;;}
    select()[检查是否有io事件]
        deadline以及任务穿插逻辑处理
            1.定时任务截止事时间快到了，中断本次轮询
            2.轮询过程中发现有任务加入，中断本次轮询
        阻塞式select
        避免jdk空轮询的bug
    processSelectedKeys()[处理io事件]
        selected keySet优化
        processSelectedKeysOptimized()
            processSelectedKey(key,channel)[处理新连接]
    runAllTasks()[处理异步任务队列]
        task的分类和添加(普通task(mpscq)和定时task)
        定时任务task(根据截至时间)聚合到普通taskQueue
        任务执行
### 3.新连接接入
- 检测新连接
    processSelectedKey(key,channel)[入口]
        NioMessageUnsafe.read()
            doReadMessages()[while循环] 创建新连接对象
                javaChannel().accept()
                NioSocketChannel(this, ch)
- 创建NioSocketChannel
    new NioSocketChannel(parent, ch)[入口]
        AbstractNioByteChannel(p,ch,op_read)[逐层调用父类构造函数]
            configureBlocking(false) & save op[设置非阻塞，保存op成员变量]
            create id,unsafe,pipline[创建相关组件]
        new NioSocketChannelConfig()
            setTcpNoDelay(true)[禁止Nagle算法(小数据包集成大数据包发送)]
- Netty中的Channel的分类
    - channel层级
    - NioServerScoketChannel
    - NioSocketChannel
    - Unsafe
- 分配线程及注册selector
    - ServerBootstrapAcceptor
        - 添加childHandler
        - 设置options和attrs
        - 选择NioEventLoop并注册了selector
- 向selector注册读事件
### 4.pipeline
- pipeline的初始化
    - pipeline在创建Channel的时候创建
    - pipeline节点数据结构：ChannelHandlerContext
    - pipeline的两大哨兵：head和tail(进行收尾以及资源释放)
- 添加删除ChannelHandler
    - 添加
        - 判读是否重复添加
        - 创建节点并添加至链表
        - 回调添加完成事件
    - 删除
        - 找到节点
        - 链表删除
        - 回调删除handler事件
- 事件和异常的传播
    - inBound事件的传播(与添加channelHandler正相关)
        - 何为inBound事件以及ChannelInboundHandler
        - ChannelRead事件的传播
        - SimpleInBoundHandler处理器
    - outBound事件的传播(与添加channelHandler逆相关)
        - 何为outBound事件以及ChannelOutBoundHandler
        - write()事件的传播
    - 异常的传播
        - 异常的触发链
        - 异常处理的最佳实践
### 5.ByteBuf
- 内存与内存管理器的抽象
    - ByteBuf结构以及重要API
        - ByteBuf结构
        - read,write,set方法
        - mark和reset方法
    - ByteBuf分类
        - Pooled(分配好的内存取内存)和Unpooled
        - Unsafe(使用UnSafe对象)和非Unsafe(不依赖jdk底层的unsafe对象)
        - Heap(堆上内存分配)和Direct(内存不受jvm控制)
    - ByteBufAllocator分析(内存分配器)
        - ByteBufAllocator功能
        - AbstractByteBufAllocator
        - ByteBufAllocator两大子类
            - UnPooledByteBufAllocator分析
                - heap内存的分配
                - direct内存分配
            - PooledByteBufAllocator分析 
                - 拿到线程局部缓存PoolThreadCache
                - 在线程局部缓存的Area上进行内存分配
            - directArena分配direct内存的流程
                - 从对象池里面拿到PooledByteBuf进行复用
                - 缓存上进行内存分配
                - 从内存堆里面进行内存分配
- 不同规格大小和不同类别的内存的分配策略
    - 内存规格(0 (tiny) 512B  (small) 8K  (normal) 16M  (huge))
    - 命中缓存的分配逻辑(MemoryRegionCache)
    - arena,chrunk,page,subpage概念
    - page级别的内存分配：allocateNormal()
        - 尝试在现有的chunk上分配
        - 创建一个chunk进行分配
        - 初始化PooledByteBuf
    - subpage级别的内存分配：allocateTiny()
        - 定位到subpage对象
        - 初始化subpage
        - 初始化PooledByteBuf
- 内存的回收过程
    - 连续的内存区段加到缓存
    - 标记连续的内存区段为未使用
    - ByteBuf加到对象池
### 6.netty解码
- 解码器抽象的解码过程
- netty里面有哪些拆箱即用的解码器

- 解码器的基类(ByteToMessageDecoder)
    - 累加字节流
    - 调用子类的decode方法进行解析
    - 将解析到的ByteBuf向下传播
- Netty中常见的解码器分析
    - 基于固定长度的解码器
    - 基于行解码器分析
        - 丢弃模式
        - 非丢弃模式
    - 基于分隔符解码器分析
        - 行处理器
        - 找到最小分隔符
        - 解码
    - 基于长度域的解码器
        - 基于长度域解码器参数分析
        - 基于长度域解码器分析
            - 计算需要抽取的数据包的长度
            - 跳过字节逻辑处理
            - 丢弃模式下的处理
### 7.netty编码
- writeAndFlush()
    - 从tail节点开始往前传播
    - 逐个调用channelHandle的write方法
    - 逐个调用channelHandle的flush方法
- 编码器处理逻辑：MessageToByteEncode的write方法
    - 匹配对象
    - 分配内存
    - 编码实现
    - 释放对象
    - 传播数据
    - 释放内存
- write-写buffer队列
    - direct化ByteBuf
    - 插入到写队列
    - 设置写状态
- write-刷新buffer队列
    - 添加刷新标志并设置写状态
    - 遍历buffer队列，过滤ByteBuf
    - 调用jdk底层api进行自旋写
### 8.netty性能优化工具类解析
- FastThreadLocal的实现机制
    - FastThreadLocal的创建
    - FastThreadLocal的get()方法实现
        - 获取ThreadLocalMap
        - 直接通过索引取出对象
        - 初始化
    - FastThreadLocal的set()方法实现
        - 获取ThreadLocalMap
        - 直接通过索引set对象
        - remove对象
- Recycler(轻量级对象池)
    - Recycler的创建
    - 从Recycler获取对象
        - 获取当前线程的Stack
        - 从Stack里面弹出对象
        - 创建对象并绑定到Stack
    - 同线程回收对象
    - 异线程回收对象
        - 获取WeakOrderQueue
        - 创建WeakOrderQueue
        - 将对象追加到WeakOrderQueue
    - 异线程收割对象
### 9.netty设计模式应用
- 单例模式
    - 一个类全局只有一个对象
    - 延迟创建
    - 避免线程安全问题
- 策略模式
    - 封装一系列可相互替换的算法家族
    - 动态选择某一策略
- 装饰者模式
    - 装饰者和被装饰者继承同一个接口
    - 装饰者给被装饰者动态修改行为
- 观察者模式
    - 观察者与被观察者
    - 观察者订阅消息，被观察者发布消息
    - 订阅则能收到，取消订阅则收不到消息
- 迭代器模式(内存零拷贝)
    - 迭代器接口
    - 对容器里面各个对象进行访问
- 责任链模式
    - 责任处理器接口(ChannelHandler:ChannelInboundHandler,ChannelOutboundHandler)
    - 创建链，添加删除责任处理器接口(ChannelPipeline)
    - 上下文(ChannelHandlerContext)
    - 责任终止机制(fireChannelRead向下传播)
### 10.netty高并发性能调优
- 单机百万连接调优(vagrant虚拟机)
    - 如何模拟百万模拟
    - 突破局部文件句柄限制
        - ulimit -n (一个线程能够打开的文件数)
        - /etc/security/limits.conf文件末尾添加 重启生效
            * root soft nofile 100001
            * 65 root hard nofile 100002
    - 突破全局文件句柄限制
        - cat /proc/sys/fs/file-max (echo 1000000 >/proc/sys/fs/file-max )
        - /etc/sysctl.conf文件末尾添加 sudo sysctl -p 生效
            fs.file-max=1000000
- netty应用级别性能调优
