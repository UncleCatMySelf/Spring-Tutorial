## 学习目标

将学习创建应用程序并使用Spring Security LDAP模块保护应用程序的过程。

## 构建

将构建一个由Spring Security嵌入式基于Java的LDAP服务器保护的简单Web应用程序，你将使用包含一组用户的数据文件加载LDAP服务器。

## 创建一个简单的Web控制器

在Spring中，REST端点只是Spring MVC控制器。以下Spring MVC控制器GET /通过返回一条简单消息来处理请求：

```java
@RestController
public class HomeController {

    @GetMapping("/")
    public String index(){
        return "Welcome to the home page!";
    }

}
```

整个类都被标记为@RestControllerSpring MVC可以使用它的内置扫描功能自动检测控制器并自动配置Web路由。

标记该方法@RequestMapping以标记路径和REST操作。在这种情况下，GET是默认行为; 它会返回一条消息，指示您在主页上。

@RestController还告诉Spring MVC将文本直接写入HTTP响应体，因为没有任何视图。相反，当您访问该页面时，您将在浏览器中收到一条简单的消息，因为本指南的重点是使用LDAP保护页面。

## 构建不安全的Web应用程序

在保护Web应用程序之前，请验证它是否有效。为此，您需要定义一些关键bean。为此，请创建一个Application类。

```java
@SpringBootApplication
@EnableWebSecurity
public class LdapApplication {

	public static void main(String[] args) {
		SpringApplication.run(LdapApplication.class, args);
	}
}
```

@SpringBootApplication 是一个便利注释，添加了以下所有内容：

* @Configuration 标记该类作为应用程序上下文的bean定义的源。
* @EnableAutoConfiguration 告诉Spring Boot开始根据类路径设置，其他bean和各种属性设置添加bean。
* 通常你会添加@EnableWebMvc一个Spring MVC应用程序，但Spring Boot会在类路径上看到spring-webmvc时自动添加它。这会将应用程序标记为Web应用程序并激活关键行为，例如设置一个 DispatcherServlet。
* @ComponentScan告诉Spring在包中寻找其他组件，配置和服务hello，允许它找到控制器。
该main()方法使用Spring Boot的SpringApplication.run()方法来启动应用程序。您是否注意到没有一行XML？也没有web.xml文件。此Web应用程序是100％纯Java，您无需处理配置任何管道或基础结构。

## 设置Spring Security

要配置Spring Security，首先要为构建添加一些额外的依赖项，对于基于Maven的构建

```
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ldap</groupId>
        <artifactId>spring-ldap-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-ldap</artifactId>
    </dependency>
    <dependency>
        <groupId>com.unboundid</groupId>
        <artifactId>unboundid-ldapsdk</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

这些依赖项添加了Spring Security和UnboundId，一个开源LDAP服务器。有了这些，您就可以使用纯Java来配置安全策略。

```java
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest().fullyAuthenticated()
                .and()
                .formLogin();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.ldapAuthentication()
                .userDnPatterns("uid={0},ou=people")
                .groupSearchBase("ou=groups")
                .contextSource()
                .url("ldap://localhost:8389/dc=springframework,dc=org")
                .and()
                .passwordCompare()
                .passwordEncoder(new LdapShaPasswordEncoder())
                .passwordAttribute("userPassword");
    }
}
```

@EnableWebSecurity将对各种Spring安全提供启动条件。

您还需要一个LDAP服务器。Spring Boot为使用纯Java编写的嵌入式服务器提供自动配置，该服务器将用于本指南。该ldapAuthentication()方法配置登录表单中的用户名插入的内容{0}，以便uid={0},ou=people,dc=springframework,dc=org在LDAP服务器中进行搜索。此外，该passwordCompare()方法配置编码器和密码属性的名称。

## 设置用户数据

application配置文件
```
spring:
  ldap:
    embedded:
      ldif: classpath:test-server.ldif
      base-dn: dc=springframework,dc=org
      port: 8389
```

LDAP服务器可以使用LDIF（LDAP数据交换格式）文件来交换用户数据。允许Spring Boot中的spring.ldap.embedded.ldif属性application.properties拉入LDIF数据文件。这样可以轻松预加载演示数据。
```
dn: dc=springframework,dc=org
objectclass: top
objectclass: domain
objectclass: extensibleObject
dc: springframework

dn: ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: groups

dn: ou=subgroups,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: subgroups

dn: ou=people,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: people

dn: ou=space cadets,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: space cadets

dn: ou=\"quoted people\",dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: "quoted people"

dn: ou=otherpeople,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: otherpeople

dn: uid=ben,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Ben Alex
sn: Alex
uid: ben
userPassword: {SHA}nFCebWjxfaLbHHG1Qk5UU4trbvQ=

dn: uid=bob,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Bob Hamilton
sn: Hamilton
uid: bob
userPassword: bobspassword

dn: uid=joe,ou=otherpeople,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Joe Smeth
sn: Smeth
uid: joe
userPassword: joespassword

dn: cn=mouse\, jerry,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Mouse, Jerry
sn: Mouse
uid: jerry
userPassword: jerryspassword

dn: cn=slash/guy,ou=people,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: slash/guy
sn: Slash
uid: slashguy
userPassword: slashguyspassword

dn: cn=quote\"guy,ou=\"quoted people\",dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: quote\"guy
sn: Quote
uid: quoteguy
userPassword: quoteguyspassword

dn: uid=space cadet,ou=space cadets,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: Space Cadet
sn: Cadet
uid: space cadet
userPassword: spacecadetspassword



dn: cn=developers,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfUniqueNames
cn: developers
ou: developer
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
uniqueMember: uid=bob,ou=people,dc=springframework,dc=org

dn: cn=managers,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfUniqueNames
cn: managers
ou: manager
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
uniqueMember: cn=mouse\, jerry,ou=people,dc=springframework,dc=org

dn: cn=submanagers,ou=subgroups,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: groupOfUniqueNames
cn: submanagers
ou: submanager
uniqueMember: uid=ben,ou=people,dc=springframework,dc=org
```

> 使用LDIF文件不是生产系统的标准配置。但是，它对于测试目的或指南非常有用。

如果您访问位于http：// localhost：8080的站点，则应将您重定向到Spring Security提供的登录页面。

输入用户名ben和密码benspassword。您应该在浏览器中看到此消息：

> 欢迎来到主页！

## 结尾

恭喜！您刚刚编写了一个Web应用程序并使用Spring Security进行了保护。在这种情况下，您使用了基于LDAP的用户存储。