## 学习

学习使用Spring Data Redis发布和订阅通过Redis发送的消息的过程。

## 构建

构建一个StringRedisTemplate用于发布字符串消息的应用程序，并使用它进行POJO订阅MessageListenerAdapter。

> 使用Spring Data Redis作为发布消息的方法可能听起来很奇怪，但正如您将发现的那样，Redis不仅提供NoSQL数据存储，还提供消息传递系统。

## 创建Redis消息接收器

在任何基于消息传递的应用程序中，都有消息发布者和消息接收者。要创建消息接收器，请实现具有响应消息的方法和接收器：

```java
@Slf4j
public class Receiver {

    private CountDownLatch latch;

    @Autowired
    public Receiver(CountDownLatch latch){
        this.latch = latch;
    }

    public void receiveMessage(String message){
        log.info("Received < " + message + " >");
        latch.countDown();
    }

}
```

Receiver是一个简单的POJO，它定义了一种接受消息的方法。正如您在注册Receiver消息监听器时所看到的那样，你可以根据需要为消息处理方法命名。

> 出于演示目的，它由构造函数自动装配，具有倒计时锁存器。这样，它可以在收到消息时发出信号。

## 注册监听器并发送消息

Spring Data Redis提供了使用Redis发送和接收消息所需的所有组件。具体来说，你需要配置：

* 连接工厂
* 消息侦听器容器
* Redis模板

你将使用Redis模板发送消息，你将使用消息监听器容器注册Receiver以便它将接收消息。连接工厂驱动模板和消息侦听器容器，使他们能够连接到Redis服务器。

此示例使用Spring Boot的默认值RedisConnectionFactory，其实例JedisConnectionFactory基于Jedis Redis库。连接工厂将注入消息侦听器容器和Redis模板。

```java
@Slf4j
@SpringBootApplication
public class RedisApplication {

	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
											MessageListenerAdapter listenerAdapter){
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new PatternTopic("chat"));
		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver){
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	@Bean
	Receiver receiver(CountDownLatch latch){
		return new Receiver(latch);
	}

	@Bean
	CountDownLatch latch(){
		return new CountDownLatch(1);
	}

	@Bean
	StringRedisTemplate template(RedisConnectionFactory connectionFactory){
		return new StringRedisTemplate(connectionFactory);
	}

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(RedisApplication.class, args);

		StringRedisTemplate template = ctx.getBean(StringRedisTemplate.class);
		CountDownLatch latch = ctx.getBean(CountDownLatch.class);

		log.info("Sending message...");
		template.convertAndSend("chat", "Hello from Redis!");

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}
}
```

listenerAdapter方法中定义的bean在定义的消息侦听器容器中注册为消息侦听器container，并将侦听“chat”主题上的消息。因为Receiver该类是POJO，所以它需要包装在实现所需MessageListener接口的消息侦听器适配器中addMessageListener()。消息侦听器适配器还配置为在消息到达时调用receiveMessage()方法Receiver。

连接工厂和消息监听器容器bean是监听消息所需的全部内容。要发送消息，您还需要Redis模板。这里，它是一个配置为a的bean StringRedisTemplate，其实现RedisTemplate主要集中在Redis的常用用法，其中键和值都是`String`s。

该main()方法通过创建Spring应用程序上下文来解决所有问题。然后，应用程序上下文启动消息侦听器容器，并且消息侦听器容器bean开始侦听消息。main()然后，该方法StringRedisTemplate从应用程序上下文中检索bean，并使用它发送“Hello from Redis！” 关于“聊天”主题的消息。最后，它关闭Spring应用程序上下文，应用程序结束。

运行程序，你应该看到以下输出：

```
2018-12-03 09:39:11.709  INFO 9240 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2018-12-03 09:39:11.709  INFO 9240 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 2066 ms
2018-12-03 09:39:12.431  INFO 9240 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
2018-12-03 09:39:12.809  INFO 9240 --- [    container-1] io.lettuce.core.EpollProvider            : Starting without optional epoll library
2018-12-03 09:39:12.810  INFO 9240 --- [    container-1] io.lettuce.core.KqueueProvider           : Starting without optional kqueue library
2018-12-03 09:39:13.646  INFO 9240 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2018-12-03 09:39:13.649  INFO 9240 --- [           main] c.g.unclecatmyself.RedisApplication      : Started RedisApplication in 4.452 seconds (JVM running for 5.018)
2018-12-03 09:39:13.650  INFO 9240 --- [           main] c.g.unclecatmyself.RedisApplication      : Sending message...
2018-12-03 09:39:13.663  INFO 9240 --- [    container-2] com.github.unclecatmyself.Receiver       : Received < Hello from Redis! >
2018-12-03 09:39:13.671  INFO 9240 --- [       Thread-3] o.s.s.concurrent.ThreadPoolTaskExecutor  : Shutting down ExecutorService 'applicationTaskExecutor'
```

## 结尾

恭喜！您刚刚使用Spring和Redis开发了一个简单的发布 - 订阅应用程序。
