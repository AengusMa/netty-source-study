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
- 内存的回收过程