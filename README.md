## 创建资源类

假设你已经设置了项目和构建系统，你可以创建Web服务。

通过考虑服务交互来开始这个过程。

该服务将处理GET请求/greeting，可选地使用name查询字符串中的参数。该GET请求应该返回200 OK在表示问候的身体与JSON响应。它应该看起来像这样：

```
{
    "id": 1,
    "content": "Hello, World!"
}
```

该id字段是问候语的唯一标识符，是问候语content的文本表示。

要为问候语表示建模，请创建资源表示形式类。提供一个普通的旧java对象，其中包含id和content数据的字段，构造函数和访问器：

```java
@Data
@AllArgsConstructor
public class Greeting {

    private final long id;
    private final String content;

}
```

> 正如你在下面的步骤中看到的，Spring使用Jackson JSON库自动将类型实例Greeting封送到JSON中。

## 创建资源控制器

在Spring构建RESTful Web服务的方法中，HTTP请求由控制器处理。这些组件很容易通过@RestController注释来识别，GreetingController下面通过返回类的新实例来处理GET请求：

```java
@RestController
public class GreetingController {

    private static final String template = "Hello,%s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name){
        return new Greeting(counter.incrementAndGet(),String.format(template,name));
    }

}
```

这个控制器简洁而简单，但引擎下有很多东西，让我们一步一步地分解它。

所述@RequestMapping注释可以确保HTTP请求/greeting被映射到greeting()方法。

> 上面的示例未指定GETvs. PUT，POST等等，因为@RequestMapping默认情况下映射所有HTTP操作。使用@RequestMapping(method=GET)缩小这种映射。

@RequestParam将查询字符串参数的值绑定name到方法的name参数中greeting()。如果name请求中不存在该参数，defaultValue则使用“World”。

方法体的实现基于来自的下一个值创建并返回Greeting具有id和content属性的新对象，并使用问候counter格式化给定name的格式template。

传统MVC控制器和上面的RESTful Web服务控制器之间的关键区别在于创建HTTP响应主体的方式。这个RESTful Web服务控制器只是填充并返回一个对象，而不是依靠视图技术来执行将问候数据的服务器端呈现为HTML Greeting。对象数据将作为JSON直接写入HTTP响应。

此代码使用Spring 4的新@RestController注释，它将类标记为控制器，其中每个方法都返回一个域对象而不是视图。它是速记@Controller和@ResponseBody拼凑在一起的。

该Greeting对象必须转换为JSON。由于Spring的HTTP消息转换器支持，您无需手动执行此转换。因为Jackson 2在类路径上，所以MappingJackson2HttpMessageConverter会自动选择Spring 来将Greeting实例转换为JSON。

## 使应用程序可执行

虽然可以将此服务打包为传统的WAR文件以部署到外部应用程序服务器，但下面演示的更简单的方法创建了一个独立的应用程序。您将所有内容打包在一个可执行的JAR文件中，由一个好的旧Java main()方法驱动。在此过程中，您使用Spring的支持将Tomcat servlet容器嵌入为HTTP运行时，而不是部署到外部实例。

```java
@SpringBootApplication
public class RestfulWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestfulWebApplication.class, args);
	}
}

```

@SpringBootApplication 是一个便利注释，添加了以下所有内容：

* @Configuration 标记该类作为应用程序上下文的bean定义的源。
* @EnableAutoConfiguration 告诉Spring Boot开始根据类路径设置，其他bean和各种属性设置添加bean。
* 通常你会添加@EnableWebMvc一个Spring MVC应用程序，但Spring Boot会在类路径上看到spring-webmvc时自动添加它。这会将应用程序标记为Web应用程序并激活关键行为，例如设置a DispatcherServlet.
* @ComponentScan告诉Spring在包中寻找其他组件，配置和服务hello，允许它找到控制器。

该main()方法使用Spring Boot的SpringApplication.run()方法来启动应用程序。您是否注意到没有一行XML？也没有web.xml文件。此Web应用程序是100％纯Java，您无需处理配置任何管道或基础结构。

## 构建可执行的JAR

您可以使用Gradle或Maven从命令行运行该应用程序。或者，您可以构建一个包含所有必需依赖项，类和资源的可执行JAR文件，并运行该文件。这使得在整个开发生命周期中，跨不同环境等将服务作为应用程序发布，版本和部署变得容易。

如果您使用的是Gradle，则可以使用运行该应用程序./gradlew bootRun。或者您可以使用构建JAR文件./gradlew build。然后你可以运行JAR文件：

> java -jar build / libs / gs-rest-service-0.1.0.jar

如果您使用的是Maven，则可以使用该应用程序运行该应用程序./mvnw spring-boot:run。或者您可以使用构建JAR文件./mvnw clean package。然后你可以运行JAR文件：

> java -jar target / gs-rest-service-0.1.0.jar

## 测试服务

现在该服务已启动，请访问http：// localhost：8080 / greeting，其中显示：

> {"id":1,"content":"Hello, World!"}

name使用http：// localhost：8080 / greeting？name = User提供查询字符串参数。注意content属性的值如何从“Hello，World！”改变。“你好，用户！”：

> {"id":2,"content":"Hello, User!"}

此更改表明该@RequestParam安排GreetingController正在按预期工作。该name参数已被赋予默认值“World”，但始终可以通过查询字符串显式覆盖。

另请注意id属性如何从更改1为2。这证明您正在GreetingController跨多个请求针对同一实例工作，并且其counter字段在每次调用时按预期递增。

## 结尾

恭喜！您刚刚使用Spring开发了RESTful Web服务。

## About the author

QQ Group：628793702

![Image text](https://raw.githubusercontent.com/UncleCatMySelf/img-myself/master/img/%E5%85%AC%E4%BC%97%E5%8F%B7.png)
