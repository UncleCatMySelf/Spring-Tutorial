## 学习

学习完成配置Web应用程序表单以支持验证的过程。

## 构造

构建一个简单的Spring MVC应用程序，它接受用户输入并使用标准验证注释检查输入。您还将看到如何在屏幕上显示错误消息，以便用户可以重新输入有效输入。

## 创建一个PersonForm对象

该应用程序涉及验证用户的姓名和年龄，因此首先您需要创建一个类来支持表单以创建一个人。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonForm {

    @NotNull
    @Size(min = 2,max = 30)
    private String name;

    @NotNull
    @Min(18)
    private Integer age;

}
```

该PersonForm有两个属性：name和age。它标有几个标准验证注释：

* @Size(min=2, max=30) 只允许长度在2到30个字符之间的名称

* @NotNull 将不允许null值，如果条目为空，则为Spring MVC生成的值

* @Min(18) 如果年龄小于18岁，则不允许

除此之外，你还可以看到getter / setter方法name和age以及一个方便的toString()方法。

## 创建Web控制器

现在您已经定义了表单支持对象，现在是时候创建一个简单的Web控制器了。

```java
@Controller
public class WebController implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/results").setViewName("results");
    }

    @GetMapping("/")
    public String showForm(PersonForm personForm){
        return "form";
    }

    @PostMapping("/")
    public String checkPersonInfo(@Valid PersonForm personForm, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "form";
        }
        return "redirect:/results";
    }

}
```

该控制器有一个GET和一个POST方法，两者都映射到/。

该showForm方法返回form模板。PersonForm在方法签名中包含一个，因此模板可以将表单属性与PersonForm关联。

该checkPersonFormInfo方法接受两个参数：

* personForm标记为的对象，@Valid用于收集您即将构建的表单中填充的属性。

* 一个bindingResult对象，以便您可以测试和检索验证错误。

您可以从绑定到PersonForm对象的表单中检索所有属性。在代码中，您测试错误，如果是，请将用户发送回原始form模板。在这种情况下，将显示所有错误属性。

如果所有人的属性都有效，则会将浏览器重定向到最终results模板。

## 构建HTML前端

构建主页

```html
<html>
<body>
<form action="#" th:action="@{/}" th:object="${personForm}" method="post">
    <table>
        <tr>
            <td>Name:</td>
            <td><input type="text" th:field="*{name}" /></td>
            <td th:if="${#fields.hasErrors('name')}" th:errors="*{name}">Name Error</td>
        </tr>
        <tr>
            <td>Age:</td>
            <td><input type="text" th:field="*{age}" /></td>
            <td th:if="${#fields.hasErrors('age')}" th:errors="*{age}">Age Error</td>
        </tr>
        <tr>
            <td><button type="submit">Submit</button></td>
        </tr>
    </table>
</form>
</body>
</html>
```

该页面包含一个简单的表单，每个字段位于表的单独插槽中。表格适合张贴/。它被标记为由personForm您在Web控制器中的GET方法中看到的对象进行备份。这被称为bean支持的形式。有两个领域的PersonForm豆，你可以看到他们的标签和。每个字段旁边都有一个辅助元素，用于显示任何验证错误。th:field="{name}"th:field="{age}"

最后，您有一个提交按钮。通常，如果用户输入违反@Valid约束的名称或年龄，它将退回到此页面并显示错误消息。如果输入了有效的名称和年龄，则用户将路由到下一个网页。

```html
<html>
	<body>
		Congratulations! You are old enough to sign up for this site.
	</body>
</html>
```

> 在这个简单的例子中，这些网页没有任何复杂的CSS或JavaScript。但对于任何生产网站，了解如何设置网页样式是很有价值的。

## 创建一个Application类

对于此应用程序，您使用的是Thymeleaf的模板语言。此应用程序需要的不仅仅是原始HTML。

```java
@SpringBootApplication
public class FormApplication {

	public static void main(String[] args) {
		SpringApplication.run(FormApplication.class, args);
	}
}
```

要激活Spring MVC，通常会添加@EnableWebMvc到Application类中。但是当Spring Boot 在类路径中@SpringBootApplication检测到spring-webmvc时，它已经添加了这个注释。这个相同的注释允许它找到带注释的@Controller类及其方法。

Thymeleaf配置也由以下方式处理@SpringBootApplication：默认情况下，模板位于类路径下，templates/并通过从文件名中删除“.html”后缀来解析为视图。Thymeleaf设置可以根据您需要的方式以多种方式进行更改和覆盖，但详细信息与本指南无关。

## Maven

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## 结尾

恭喜！您已经编写了一个简单的Web应用程序，并在域对象中内置了验证。这样，您可以确保数据符合特定条件，并且用户可以正确输入数据。