## 学习

Spring Boot Actuator是Spring Boot的子项目，它为您的应用程序添加了几个生产服务，而您只需要付出很少的努力，本文作为入门简单了解。

## 构造

将构造使用Spring Boot Actuator创建“hello world”RESTful Web服务，你将构建一个接收HTTP GET请求的服务。

> curl http://localhost:9000/hello-world

它使用以下JSON响应

> {"id":1,"content":"Hello, World!"}

## 运行空服务

你暂时仅需要一个空的Spring MVC应用程序

```java
@SpringBootApplication
public class ActuatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ActuatorApplication.class, args);
	}
}
```

该@SpringBootApplication注解取决于类路径的内容，和其他东西提供缺省值的负载（如嵌入的servlet容器）。它还打开了Spring MVC的@EnableWebMvc注释，用于激活Web端点。

此应用程序中没有定义任何端点，但足以启动并查看Actuator的一些功能。该SpringApplication.run()命令知道如何启动Web应用程序。您需要做的就是运行此命令。

## 创建一个表示类

您希望处理GET请求/hello-world，可选择使用名称查询参数。为了响应这样的请求，您将发送回JSON，代表问候语，看起来像这样：

```
{
    "id": 1,
    "content": "Hello, World!"
}
```

该id字段是问候语的唯一标识符，是问候语content的文本表示。

要为问候语表示建模，请创建一个表示类：

```java
@Data
@Builder
@AllArgsConstructor
public class Greeting {

    private final long id;
    private final String content;

}
```

现在您将创建将为表示类提供服务的端点控制器。

## 创建资源控制器

在Spring中，REST端点只是Spring MVC控制器。以下Spring MVC控制器处理/ hello-world的GET请求并返回Greeting资源：

```java
@Controller
public class HelloController {

    private static final String template = "Hello,%s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/hello-world")
    @ResponseBody
    public Greeting sayHello(@RequestParam(name = "name",required = false,defaultValue = "Stranger") String name){
        return new Greeting(counter.incrementAndGet(),String.format(template,name));
    }

}
```

面向人的控制器和REST端点控制器之间的关键区别在于如何创建响应。端点控制器不是依赖于视图（例如JSP）来呈现HTML中的模型数据，而是简单地将要写入的数据直接返回到响应的主体。

该@ResponseBody注解告诉Spring MVC不是渲染模型到视图，而是写在返回的对象在响应主体。它通过使用Spring的消息转换器之一来实现。因为Jackson 2在类路径中，这意味着MappingJackson2HttpMessageConverter如果请求的Accept标头指定应该返回JSON ，它将处理Greeting转换为JSON 。

## 配置使用Actuator

Maven引入

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

配置application文件

```
server:
  port: 9000
management:
  endpoints:
    web:
      base-path: /
```

> 访问 http://localhost:9000/health

## 结尾

恭喜！您刚刚使用Spring开发了一个简单的RESTful服务。由于Spring Boot Actuator，您添加了一些有用的内置服务。