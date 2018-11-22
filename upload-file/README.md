## 上传文件

将学习完成创建可以接收HTTP多部分文件上传的服务器应用程序的过程

## 构建Demo

创建一个接收文件上传的Spring Boot Web 应用程序，还将构建一个简单的HTML界面来上传测试文件。

## 创建一个Application类

要启动Spring Boot MVC应用程序，我们首先需要一个启动器; 在这里，spring-boot-starter-thymeleaf和spring-boot-starter-web已经添加为依赖关系。要使用Servlet容器上载文件，您需要注册一个MultipartConfigElement类（将<multipart-config>在web.xml中）。感谢Spring Boot，一切都是自动配置的！

您开始使用此应用程序所需的只是以下Application课程。

```java
@SpringBootApplication
public class UploadFileApplication {

	public static void main(String[] args) {
		SpringApplication.run(UploadFileApplication.class, args);
	}
}
```

作为自动配置Spring MVC的一部分，Spring Boot将创建一个MultipartConfigElementbean并为文件上传做好准备。

## 创建文件上传控制器

初始应用程序已经包含一些类来处理在磁盘上存储和加载上传的文件; 我们将在新的FileUploadController中使用它们。

```java
@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService){
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException{
        model.addAttribute("files",storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile",path.getFileName().toString()).build().toString())
        .collect(Collectors.toList()));
        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename){
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=\""+file.getFilename()+"\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes){
        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded "+file.getOriginalFilename()+"!");
        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc){
        return ResponseEntity.notFound().build();
    }

}
```

这个类带有注释，@Controller因此Spring MVC可以选择并查找路由。每个方法都标记有@GetMapping或@PostMapping将路径和HTTP操作绑定到特定的Controller操作。

在这种情况下：

* GET /从中查找当前上传文件的列表StorageService并将其加载到Thymeleaf模板中。它使用计算实际资源的链接MvcUriComponentsBuilder

* GET /files/{filename}加载资源（如果存在），并将其发送到浏览器以使用"Content-Disposition"响应头进行下载

POST /适用于处理多部分消息file并将其提供给StorageService保存

> 在生产场景中，您更有可能将文件存储在临时位置，数据库或Mongo的GridFS之类的NoSQL存储中。最好不要使用内容加载应用程序的文件系统。
您需要StorageService为控制器提供与存储层（例如文件系统）交互的控件。代码是这样的：

```java
public interface StorageService {

    void init();

    void store(MultipartFile file);

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

}
```

示例应用程序中有一个接口的示例实现。如果您想节省时间，可以复制并粘贴它。(StorageServiceImpl)

当然我们还需定义文件储存路径：

```java
@Component
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String location = "G:\\uploaddir";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
```

## 创建一个简单的HTML模板

为了构建一些有趣的东西，下面的Thymeleaf模板是上传文件以及显示已上传内容的一个很好的例子。

```html
<html xmlns:th="http://www.thymeleaf.org">
<body>

	<div th:if="${message}">
		<h2 th:text="${message}"/>
	</div>

	<div>
		<form method="POST" enctype="multipart/form-data" action="/">
			<table>
				<tr><td>File to upload:</td><td><input type="file" name="file" /></td></tr>
				<tr><td></td><td><input type="submit" value="Upload" /></td></tr>
			</table>
		</form>
	</div>

	<div>
		<ul>
			<li th:each="file : ${files}">
				<a th:href="${file}" th:text="${file}" />
			</li>
		</ul>
	</div>

</body>
</html>
```

该模板有三个部分：

* 顶部的可选消息，Spring MVC写入闪存范围的消息。

* 允许用户上传文件的表单

* 从后端提供的文件列表

## 调整文件上传限制

配置文件上载时，设置文件大小限制通常很有用。想象一下尝试处理5GB文件上传！使用Spring Boot，我们可以MultipartConfigElement使用一些属性设置调整其自动配置。

将以下属性添加到现有属性设置：

```
spring:
  servlet:
    multipart:
      max-file-size: 128KB
      max-request-size: 128KB
```

多部分设置受限制如下：

* spring.http.multipart.max-file-size 设置为128KB，意味着总文件大小不能超过128KB。

* spring.http.multipart.max-request-size设置为128KB，表示a的总请求大小multipart/form-data不能超过128KB。

## 执行应用程序

虽然可以将此服务打包为传统的WAR文件以部署到外部应用程序服务器，但下面演示的更简单的方法创建了一个独立的应用程序。您将所有内容打包在一个可执行的JAR文件中，由一个好的旧Java main()方法驱动。在此过程中，您使用Spring的支持将Tomcat servlet容器嵌入为HTTP运行时，而不是部署到外部实例。

您还需要一个目标文件夹来上传文件，所以让我们增强基本Application类并添加一个Boot CommandLineRunner，它在启动时删除并重新创建该文件夹：

```java
@SpringBootApplication
public class UploadFileApplication {

	public static void main(String[] args) {
		SpringApplication.run(UploadFileApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService){
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}

}
```

@SpringBootApplication 是一个便利注释，添加了以下所有内容：

* @Configuration 标记该类作为应用程序上下文的bean定义的源。

* @EnableAutoConfiguration 告诉Spring Boot开始根据类路径设置，其他bean和各种属性设置添加bean。

* 通常你会添加@EnableWebMvc一个Spring MVC应用程序，但Spring Boot会在类路径上看到spring-webmvc时自动添加它。这会将应用程序标记为Web应用程序并激活关键行为，例如设置a DispatcherServlet。

* @ComponentScan告诉Spring在包中寻找其他组件，配置和服务hello，允许它找到控制器。

该main()方法使用Spring Boot的SpringApplication.run()方法来启动应用程序。您是否注意到没有一行XML？也没有web.xml文件。此Web应用程序是100％纯Java，您无需处理配置任何管道或基础结构。

在服务器运行时，您需要打开浏览器并访问http：// localhost：8080 /以查看上载表单。选择一个（小）文件并按“上传”，您应该从控制器中看到成功页面。选择一个太大的文件，你会得到一个丑陋的错误页面。

## 结尾

恭喜！您刚刚编写了一个使用Spring处理文件上传的Web应用程序。

## About the author

QQ Group：628793702

![Image text](https://raw.githubusercontent.com/UncleCatMySelf/img-myself/master/img/%E5%85%AC%E4%BC%97%E5%8F%B7.png)

