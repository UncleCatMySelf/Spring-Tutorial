## 学习任务

学习完成使用Spring定时任务的步骤

## 构建Demo

将构建一个应用程序，使用Spring的@Scheduled注释每五秒打印一次当前时间

## 创建计划任务

```java
@Slf4j
@Component
public class ScheduledTasks {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime(){
        log.info("The time is now {}",dateFormat.format(new Date()));
    }

}
```

在Scheduled当特定的方法运行注解定义。注意：此示例使用fixedRate，指定从每次调用的开始时间开始测量的方法调用之间的间隔。还有其他选项，例如fixedDelay，它指定从完成任务开始测量的调用之间的间隔。您还可以使用@Scheduled(cron=". . .")表达式进行更复杂的任务调度。

## 启动计划

虽然计划任务可以嵌入到Web应用程序和WAR文件中，但下面演示的更简单的方法创建了一个独立的应用程序。您将所有内容打包在一个可执行的JAR文件中，由一个好的旧Java main()方法驱动。

```java
@SpringBootApplication
@EnableScheduling
public class SchedTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchedTaskApplication.class, args);
	}
}
```

@SpringBootApplication 是一个便利注释，添加了以下所有内容：

* @Configuration 标记该类作为应用程序上下文的bean定义的源。
* @EnableAutoConfiguration 告诉Spring Boot开始根据类路径设置，其他bean和各种属性设置添加bean。
* 通常你会添加@EnableWebMvc一个Spring MVC应用程序，但Spring Boot会在类路径上看到spring-webmvc时自动添加它。这会将应用程序标记为Web应用程序并激活关键行为，例如设置a DispatcherServlet。
* @ComponentScan告诉Spring在包中寻找其他组件，配置和服务hello，允许它找到控制器。

该main()方法使用Spring Boot的SpringApplication.run()方法来启动应用程序。您是否注意到没有一行XML？也没有web.xml文件。此Web应用程序是100％纯Java，您无需处理配置任何管道或基础结构。

@EnableScheduling确保创建后台任务执行程序。没有它，没有任何安排。

显示日志输出，您可以从日志中看到它在后台线程上。您应该每隔5秒钟看到计划任务：

```
[...]
2018-11-22 10:02:12.660  INFO 11204 --- [   scheduling-1] c.g.unclecatmyself.task.ScheduledTasks   : The time is now 10:02:12
2018-11-22 10:02:17.660  INFO 11204 --- [   scheduling-1] c.g.unclecatmyself.task.ScheduledTasks   : The time is now 10:02:17
```

## 结尾

恭喜！您使用计划任务创建了一个应用程序。哎，实际代码比构建文件短！此技术适用于任何类型的应用程序。

## About the author

QQ Group：628793702

![Image text](https://raw.githubusercontent.com/UncleCatMySelf/img-myself/master/img/%E5%85%AC%E4%BC%97%E5%8F%B7.png)


