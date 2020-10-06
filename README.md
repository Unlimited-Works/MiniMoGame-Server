# MiniMOGame-Server
Server side of the mini multi-player online game powered by frame sync(turn-sync).

## client-side project
- https://gitee.com/unlimited-code/minimogame-u3d

## features current support
- Turn-Sync PingPang Game. support:
    - login
    - lobby & room
    - ping-pang game scene

## how to run
- init `game.sql` file for you PG database instance.
- override config properties in `src/main/resources/reference.conf` by create `local.conf` and `online.conf` at `src/main/resources/` path.
- `sbt run` in project root directory.(might cost 5~10min at first running)

## screen shoot
- examples in [screen_shoot folder](https://github.com/Unlimited-Works/MiniMoGame-Server/tree/master/screen_shoot)  

![pingpang_login](https://github.com/Unlimited-Works/MiniMoGame-Server/blob/master/screen_shoot/pingpang_login.png?raw=true)
![pingpang_lobby](https://github.com/Unlimited-Works/MiniMoGame-Server/blob/master/screen_shoot/pingpang_lobby.png?raw=true)
![pingpang_game_begin](https://github.com/Unlimited-Works/MiniMoGame-Server/blob/master/screen_shoot/pingpang_game_begin.png?raw=true)
![pingpang_game_begin](https://github.com/Unlimited-Works/MiniMoGame-Server/blob/master/screen_shoot/pingpang_game_a.png?raw=true)
![pingpang_game_b](https://github.com/Unlimited-Works/MiniMoGame-Server/blob/master/screen_shoot/pingpang_game_a.png?raw=true)
![pingpang_game_end](https://github.com/Unlimited-Works/MiniMoGame-Server/blob/master/screen_shoot/pingpang_game_end.png?raw=true)

## MiniMO Server Code Specification
- 异常处理
    - 异常日志在try-catch中打印，其他地方不要打异常日志
    - 业务异常使用BizException和BizCode定义
        - todo：BizCode用trait + case class代替enum实现。
- MiniMO遵循DDD模型进行开发。
    - entity和service包中的代码代表Domain模型逻辑，方法签名需要能够反应MiniMO Game模块设计时的功能点。
    - 实体对象（拥有Id属性的对象，比如RoomEntity）的类命名的使用Entity后缀
    - 实体对象的创建必须使用apply方法，不允许通过new产生
    - Domain层提供基础的参数和返回值，Router层负责拼接以符合场景/网络格式需要
    - Domain层处理需要保证**并发安全**和核心功能点的**执行效率**。
        - 和Router层不同，这里校验的是相关数据结构整体的合理性。Domain对于游戏逻辑的要求并不保证完全符合上下文环境校验规则。
- 函数返回的结果是异常类型还是Either/Option由业务场景决定，如果出现多个业务场景，
  则默认的函数实现使用Either/Option，函数名使用Ex/Ex2等后缀表示throw exception的场景。
    - 如果业务场景不明确，为了通用性，优先定义Option/Either的返回类型
- JProtocol并发模型
    - 变量需要加锁处理并发。每个用户的JProto消息都是顺序性的，只有前一个消息处理完成，才会处理后续的消息，异步的请求也是顺序处理。
      多个用户之间的操作是并发性的。
- Router层的逻辑代码，维护session状态，需要保证在业务异常时的正确性，保证请求的合理性。
- 【todo】当前的Router模型仅支持面向socket链接的一对一推送，并没有支持广播模式
    - 广播模式支持一对多的消息推送，比如房间中有用户进入时，会广播进入消息到当前房间的用户。
- 校验类别的函数名字以check开头
- 使用伴生对象apply/create/fromXXX等方法作为类的构造工厂方法，优先使用apply方法
- git
    - setting `git config core.autocrlf true` for idea
    - save password: `git config credential.helper store`