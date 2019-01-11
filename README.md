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
    新连接接入通过chooser绑定一个NioEventLoop
- NioEventLoop执行逻辑