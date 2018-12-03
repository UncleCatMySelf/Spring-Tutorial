## 学习

完成设置发布和订阅消息的RabbitMQ AMQP服务器的过程。

## 构建

构建一个使用Spring AMQP发布消息的应用程序，RabbitTemplate并使用POJO订阅消息MessageListenerAdapter。

## 创建Rabbit MQ消息接收器

使用任何基于消息传递的应用程序，您需要创建一个响应已发布消息的接收器。

```java
@Slf4j
@Component
public class Receiver {

    private CountDownLatch latch = new CountDownLatch(1);

    public void receiveMessage(String message){
        log.info("Received < " + message + " >");
        latch.countDown();
    }

    public CountDownLatch getLatch(){
        return latch;
    }

}
```

Receiver是一个简单的POJO，它定义了一种接收消息的方法。当您注册它以接收消息时，您可以将其命名为任何您想要的名称。

> 为方便起见，这个POJO也有一个CountDownLatch。这允许它发信号通知接收到消息。这是您不太可能在生产应用程序中实现的。

## 注册监听器并发送消息

Spring AMQP RabbitTemplate 提供了使用RabbitMQ发送和接收消息所需的一切。具体来说，你需要配置：

* 消息侦听器容器
* 声明队列，交换以及它们之间的绑定
* 用于发送一些消息以测试侦听器的组件

> Spring Boot会自动创建连接工厂和RabbitTemplate，从而减少您必须编写的代码量。

您将使用RabbitTemplate发送消息，并将Receiver使用消息侦听器容器注册，以接收消息。连接工厂驱动两者，允许它们连接到RabbitMQ服务器。

```java
@SpringBootApplication
public class RabbitmqApplication {

	static final String topicExchangeName = "spring-boot-exchange";

	static final String queueName = "spring-boot";

	@Bean
	Queue queue(){
		return new Queue(queueName, false);
	}

	@Bean
	TopicExchange exchange(){
		return new TopicExchange(topicExchangeName);
	}

	@Bean
	Binding binding(Queue queue,TopicExchange exchange){
		return BindingBuilder.bind(queue).to(exchange).with("foo.bar.#");
	}

	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
											 MessageListenerAdapter listenerAdapter){
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		container.setMessageListener(listenerAdapter);
		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver){
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqApplication.class, args).close();
	}
}
```

@SpringBootApplication 是一个便利注释，添加了以下所有内容：

* @Configuration 标记该类作为应用程序上下文的bean定义的源。
  
* @EnableAutoConfiguration 告诉Spring Boot开始根据类路径设置，其他bean和各种属性设置添加bean。
  
* 通常你会添加@EnableWebMvc一个Spring MVC应用程序，但Spring Boot会在类路径上看到spring-webmvc时自动添加它。这会将应用程序标记为Web应用程序并激活关键行为，例如设置a DispatcherServlet。
  
* @ComponentScan告诉Spring在包中寻找其他组件，配置和服务hello，允许它找到控制器。
  
该main()方法使用Spring Boot的SpringApplication.run()方法来启动应用程序。您是否注意到没有一行XML？也没有web.xml文件。此Web应用程序是100％纯Java，您无需处理配置任何管道或基础结构。
  
listenerAdapter()方法中定义的bean在定义的容器中注册为消息侦听器container()。它将侦听“spring-boot”队列中的消息。因为Receiver该类是POJO，所以需要将其包装在MessageListenerAdapter指定要调用的位置receiveMessage。

> JMS队列和AMQP队列具有不同的语义。例如，JMS仅向一个使用者发送排队的消息。虽然AMQP队列执行相同的操作，但AMQP生成器不会直接向队列发送消息。相反，消息被发送到交换机，交换机可以转到单个队列，或扇出到多个队列，模仿JMS主题的概念。

消息监听器容器和接收器bean是您监听消息所需的全部内容。要发送消息，您还需要一个Rabbit模板。

该queue()方法创建AMQP队列。该exchange()方法创建主题交换。该binding()方法将这两者绑定在一起，定义RabbitTemplate发布到交换时发生的行为。

> Spring AMQP要求将the Queue，the TopicExchange，和Binding声明为顶级Spring bean才能正确设置。

在这种情况下，我们使用主题交换，并且队列与路由密钥绑定，foo.bar.#这意味着使用以路由键开头的任何消息foo.bar.将被路由到队列。

## 发送测试消息

测试消息由CommandLineRunner,他还等待接收器中的锁存器并关闭应用程序上下文：

```java
@Slf4j
@Component
public class Runner implements CommandLineRunner {

    private final RabbitTemplate rabbitTemplate;
    private final Receiver receiver;

    public Runner(Receiver receiver, RabbitTemplate rabbitTemplate){
        this.receiver = receiver;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(String... strings) throws Exception {
        log.info("Sending message....");
        rabbitTemplate.convertAndSend(RabbitmqApplication.topicExchangeName,"foo.bar.baz","Hello from RabbitMQ!");
        receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
    }
}
```

请注意，模板将消息路由到交换机，其路由密钥foo.bar.baz与绑定匹配。

可以在测试中模拟出运行器，以便可以单独测试接收器。

运行程序，你应该看到如下输出：

```
2018-12-03 10:23:46.779  INFO 10828 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2018-12-03 10:23:46.782  INFO 10828 --- [           main] c.g.unclecatmyself.RabbitmqApplication   : Started RabbitmqApplication in 3.61 seconds (JVM running for 4.288)
2018-12-03 10:23:46.784  INFO 10828 --- [           main] com.github.unclecatmyself.Runner         : Sending message....
2018-12-03 10:23:46.793  INFO 10828 --- [    container-1] com.github.unclecatmyself.Receiver       : Received < Hello from RabbitMQ! >
2018-12-03 10:23:46.799  INFO 10828 --- [           main] o.s.a.r.l.SimpleMessageListenerContainer : Waiting for workers to finish.
2018-12-03 10:23:47.813  INFO 10828 --- [           main] o.s.a.r.l.SimpleMessageListenerContainer : Successfully waited for workers to finish.
2018-12-03 10:23:47.815  INFO 10828 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Shutting down ExecutorService 'applicationTaskExecutor'
2018-12-03 10:23:47.816  INFO 10828 --- [           main] o.s.a.r.l.SimpleMessageListenerContainer : Shutdown ignored - container is not active already
```

## 结尾

恭喜！您刚刚使用Spring和RabbitMQ开发了一个简单的发布 - 订阅应用程序。