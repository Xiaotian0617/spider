# 交易所数据 spider

启动时需要制定配置文件路径 目前项目中已默认配置spring.progiles.active = test  

    - dev 开发环境
    - test 测试环境
    - prod 正式环境
   
三个不同环境系统会自动去检测相应的配置文件中的设置即application-XXX.yml


### 系统版本

    - java版本 1.8
    - spring版本 1.5.9
    - okhttp版本 3.10
    - retrofit2 2.3.0 (建议升级2.5以上，2.5以前版本有高危漏洞)
    
### 交易所前言

交易所接口分两种，Rest和Websocket。我们去调用时也需通过这两种方式，而不同方式都分别有不同的共性信息，所以将两种方式抽象出不同的基类用于方便开发。
    
    1.BaseRest.java
    2.BaseWebsocket.java 
    
这里面已基本定义数据调用时的建立连接，发送订阅，接收消息，异常处理，失败重连等方法，也设计出接收数据时创建线程用于处理并发消息，所以新增交易所时，首先需注意这两个类。

### 交易所数据分类

    - 实时行情数据
    - 实时交易数据
    - 实时K线数据
        - 1分钟K线
        - 3分钟K线
        ……
        - 一周K线
    - 实时深度数据
        - 增量式深度数据
        - 全量式深度数据（目前项目中处理的是全量式深度，即整体传输数据）
        
### OnlyKey的含义

    如何快速定位到某个交易所下的某个市场的某个币种现在值多少钱，比如火币交易所的BTC它在USDT市场里目前值多少钱，我们定义了一个格式，即  交易所_币种_市场  用来快速定位和查询，这个概念一定要熟记，因为它贯穿整个项目。
    Huobi_BTC_USDT
    交易所 首字母大写
    币种和市场全大写
    
### Model定义
    
    - OnlyKey
    - Kline （K线）
    - Market （行情）
    - MarketCap （市值）
    - Depth （深度）
    - BuyAndSellFirst （买一卖一）
    - Trade （交易数据）
    - RealTrade （真实交易数据）

    1. OnlyKey 是个接口，里面就定义了一个方法即onlyKey方法。实现这个接口的实体类都可以快速实现获取OnlyKey方法。
    2. RealTrade 真实交易 是在BuyAndSellFirst（买一卖一）的基础上，根据规则计算出的交易所真实交易数据，其本质和Trade类似，只是数据是过滤后的。

### 项目配置

    application.yml 用以整体配置项目包括Kafka Topic信息，不同交易所域名前缀
    application-XXX.yml文件中来设置交易所是否开启
        例：
            websocket:
               huobi:
                   depth: false
                   kline: true
                   trade: true
                   disable: true
           1. 第一行为请求类型，例子中为websocket
           2. 第二行为交易所名称
           3. 第三行开始为配置项，需注意一点 depth,kline,trade均是true为开启，false为关闭
           4. market 也就是行情信息是默认开启的，无法关闭
           5. disable 即交易所总开关，true为关闭，false为开启，这个开关如果关闭的话，depth,kline,trade无效。

### 交易所的实现

    不同交易所的接口信息，获取方式，数据格式等等都不尽相同，所以建议每个交易所单独来写（BinanceWebsocket更为特殊，不同数据类型都区分了不同类）。
    
    Rest方式：
        1. package com.al.dbspider.base.api 中创建交易所接口信息，继承BaseRest.java。
        2. AAPI.java中加入Spring注入，将域名前缀读取进入系统。
        3. 按需实现BaseRest中定义的抽象方法即可。
        
    Websocet方式：
        1. package com.al.dbspider.websocket 中创建交易所Websocket类，并继承BaseWebsocket.java。
        2. 实现其订阅，onMessageXXX等方法。
  