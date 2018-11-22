## 使用 RESTful Web 服务

将学习完成创建使用RESTful Web服务的应用程序的过程

## 构造Demo

将构建一个使用Spring的应用程序，以便RestTemplate在http://gturnquist-quoters.cfapps.io/api/random中检索随机的SpringBoot引用。

## 获取REST资源

完成项目设置后，您可以创建一个使用RESTful服务的简单应用程序。

RESTful服务已经在http://gturnquist-quoters.cfapps.io/api/random上站了起来。它随机获取有关Spring Boot的引用并将它们作为JSON文档返回。

如果您通过Web浏览器或curl请求该URL，您将收到如下所示的JSON文档：

```
{
  "type": "success",
  "value": {
    "id": 4,
    "quote": "Previous to Spring Boot, I remember XML hell, confusing set up, and many hours of frustration."
  }
}
```

很容易，但通过浏览器或通过卷曲获取时不是非常有用。

以编程方式使用REST Web服务的更有用的方法。为了帮助您完成该任务，Spring提供了一个方便的模板类RestTemplate。RestTemplate使与大多数RESTful服务的交互成为一行咒语。它甚至可以将数据绑定到自定义域类型。

首先，创建一个包含所需数据的域类。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {

    private String type;
    private Value value;

    @Override
    public String toString() {
        return "Quote{" +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
    }

}
```

如您所见，这是一个简单的Java类，具有一些属性和匹配的getter方法。它使用@JsonIgnorePropertiesJackson JSON处理库进行注释，以指示应忽略此类型中未绑定的任何属性。

为了直接将数据绑定到自定义类型，您需要指定与从API返回的JSON文档中的键完全相同的变量名称。如果您的JSON doc中的变量名称和键不匹配，则需要使用@JsonProperty批注指定JSON文档的确切键。

嵌入内部引用本身需要一个额外的类。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Value {

    private Long id;
    private String quote;

    @Override
    public String toString() {
        return "Value{" +
                "id=" + id +
                ", quote='" + quote + '\'' +
                '}';
    }

}
```

它使用相同的注释，但只是映射到其他数据字段。

## 使应用程序可执行

虽然可以将此服务打包为传统的WAR文件以部署到外部应用程序服务器，但下面演示的更简单的方法创建了一个独立的应用程序。您将所有内容打包在一个可执行的JAR文件中，由一个好的旧Java main()方法驱动。在此过程中，您使用Spring的支持将Tomcat servlet容器嵌入为HTTP运行时，而不是部署到外部实例。

现在，您可以编写Application用于RestTemplate从Spring Boot报价服务中获取数据的类。

```java
@Slf4j
public class Application {

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        Quote quote = restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
        log.info(quote.toString());
    }

}
```

由于Jackson JSON处理库位于类路径中，因此RestTemplate将使用它（通过消息转换器）将传入的JSON数据转换为Quote对象。从那里，Quote对象的内容将被记录到控制台。

在这里，您只用于RestTemplate发出HTTP GET请求。但RestTemplate也支持其他HTTP动词，如POST，PUT和DELETE。

## 使用Spring Boot 管理应用程序生命周期

到目前为止，我们还没有在我们的应用程序中使用Spring Boot，但这样做有一些优点，并且不难做到。其中一个优点是我们可能希望让Spring Boot管理消息转换器RestTemplate，以便易于以声明方式添加自定义。为此，我们@SpringBootApplication在主类上使用并转换main方法以启动它，就像在任何Spring Boot应用程序中一样。最后，我们将其RestTemplate移至CommandLineRunner回调，以便在启动时由Spring Boot执行：

```java
@Slf4j
@SpringBootApplication
public class RestTemplateApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestTemplateApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder){
		return builder.build();
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception{
		return args -> {
			Quote quote = restTemplate.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
			log.info(quote.toString());
		};
	}
}
```

它RestTemplateBuilder是由Spring注入的，如果你使用它来创建一个，RestTemplate那么你将受益于Spring Boot中带有消息转换器和请求工厂的所有自动配置。我们还将其提取RestTemplate为a @Bean以使其更容易测试（可以通过这种方式更容易地进行模拟）。

## 结果

你应该看到如下的输出，随机引用：

```
2018-11-22 10:32:13.284  INFO 19104 --- [           main] c.g.u.RestTemplateApplication            : Quote{type='success', value=Value{id=11, quote='I have two hours today to build an app from scratch. @springboot to the rescue!'}}
```

> 如果您看到错误，Could not extract response: no suitable HttpMessageConverter found for response type [class hello.Quote]那么您可能处于无法连接到后端服务的环境中（如果您可以访问它，则会发送JSON）。也许你是公司代理人的背后？尝试设置标准系统属性http.proxyHost和http.proxyPort适合您环境的值。

## 结尾

恭喜！您刚刚使用Spring开发了一个简单的REST客户端。

## About the author

QQ Group：628793702

![Image text](https://raw.githubusercontent.com/UncleCatMySelf/img-myself/master/img/%E5%85%AC%E4%BC%97%E5%8F%B7.png)
