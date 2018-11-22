## 使用JDBC和Spring访问关系数据

将学习完成使用Spring访问关系数据的过程

## 构造Demo

您将使用Spring构建一个应用程序，JdbcTemplate来访问存储在关系数据库中的数据。

## 创建一个Customer对象

您将在下面使用的简单数据访问逻辑管理客户的名字和姓氏。要在应用程序级别表示此数据，请创建一个Customer类。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class pojo {

    private long id;
    private String firstName,lastName;

    @Override
    public String toString() {
        return String.format(
                "Customer[id=%d, firstName='%s', lastName='%s']",
                id, firstName, lastName);
    }

}
```

## 存储和检索数据

当然你需要准备一些Maven资源与配置

```
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

配置文件

```
spring:
  h2:
    console:
      enabled: true
```

Spring提供了一个名为JdbcTemplate的模板类，可以轻松使用SQL关系数据库和JDBC。大多数JDBC代码都陷入资源获取，连接管理，异常处理和一般错误检查之中，这与代码要实现的内容完全无关。该JdbcTemplate你负责这一切的。您所要做的就是专注于手头的任务。

```java
@Slf4j
@SpringBootApplication
public class JdbcSpringApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(JdbcSpringApplication.class, args);
	}

	@Autowired
	JdbcTemplate jdbcTemplate;


	@Override
	public void run(String... strings) throws Exception {
		log.info("Creating tables");

		jdbcTemplate.execute("DROP TABLE customers IF EXISTS");
		jdbcTemplate.execute("CREATE TABLE customers(" +
				"id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");

		// Split up the array of whole names into an array of first/last names
		List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long").stream()
				.map(name -> name.split(" "))
				.collect(Collectors.toList());

		// Use a Java 8 stream to print out each tuple of the list
		splitUpNames.forEach(name -> log.info(String.format("Inserting customer record for %s %s", name[0], name[1])));

		// Uses JdbcTemplate's batchUpdate operation to bulk load data
		jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?,?)", splitUpNames);

		log.info("Querying for customer records where first_name = 'Josh':");
		jdbcTemplate.query(
				"SELECT id, first_name, last_name FROM customers WHERE first_name = ?", new Object[] { "Josh" },
				(rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
		).forEach(customer -> log.info(customer.toString()));

	}
}
```

@SpringBootApplication 是一个便利注释，添加了以下所有内容：

* @Configuration 标记该类作为应用程序上下文的bean定义的源。

* @EnableAutoConfiguration 告诉Spring Boot开始根据类路径设置，其他bean和各种属性设置添加bean。

* @ComponentScan告诉Spring在包中寻找其他组件，配置和服务hello。在这种情况下，没有任何。

该main()方法使用Spring Boot的SpringApplication.run()方法来启动应用程序。您是否注意到没有一行XML？也没有web.xml文件。此Web应用程序是100％纯Java，您无需处理配置任何管道或基础结构。

Spring Boot支持H2，一种内存中的关系数据库引擎，并自动创建连接。因为我们使用的是spring-jdbc，Spring Boot会自动创建一个JdbcTemplate。该@Autowired JdbcTemplate字段自动加载它并使其可用。

这个Application类实现了Spring Boot CommandLineRunner，这意味着它将run()在加载应用程序上下文后执行该方法。

首先，使用JdbcTemplate’s `execute方法安装一些DDL 。

其次，您获取字符串列表并使用Java 8流，将它们拆分为Java数组中的firstname / lastname对。

然后使用JdbcTemplate’s `batchUpdate方法在新创建的表中安装一些记录。方法调用的第一个参数是查询字符串，最后一个参数（Objects 的数组）包含要替换为“？”字符的查询的变量。

> 对于单个插入语句，JdbcTemplate’s `insert方法很好。但对于多个插件，最好使用batchUpdate。

> 使用?的参数，以避免SQL注入攻击通过指示JDBC来绑定变量。

最后，使用该query方法在表中搜索与条件匹配的记录。您再次使用“？”参数为查询创建参数，在进行调用时传入实际值。最后一个参数是用于将每个结果行转换为新Customer对象的Java 8 lambda 。

Java 8 lambdas很好地映射到单个方法接口，如Spring的RowMapper。如果您使用的是Java 7或更早版本，则可以轻松插入匿名接口实现，并具有与lambda expresion正文所包含的相同的方法体，并且它可以毫不费力地使用Spring。

您应该看到以下输出：

```
2018-11-22 11:04:18.669  INFO 2528 --- [           main] c.g.u.JdbcSpringApplication              : Creating tables
2018-11-22 11:04:18.672  INFO 2528 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2018-11-22 11:04:18.759  INFO 2528 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2018-11-22 11:04:18.787  INFO 2528 --- [           main] c.g.u.JdbcSpringApplication              : Inserting customer record for John Woo
2018-11-22 11:04:18.787  INFO 2528 --- [           main] c.g.u.JdbcSpringApplication              : Inserting customer record for Jeff Dean
2018-11-22 11:04:18.787  INFO 2528 --- [           main] c.g.u.JdbcSpringApplication              : Inserting customer record for Josh Bloch
2018-11-22 11:04:18.787  INFO 2528 --- [           main] c.g.u.JdbcSpringApplication              : Inserting customer record for Josh Long
2018-11-22 11:04:18.802  INFO 2528 --- [           main] c.g.u.JdbcSpringApplication              : Querying for customer records where first_name = 'Josh':
2018-11-22 11:04:18.816  INFO 2528 --- [           main] c.g.u.JdbcSpringApplication              : Customer[id=3, firstName='Josh', lastName='Bloch']
2018-11-22 11:04:18.816  INFO 2528 --- [           main] c.g.u.JdbcSpringApplication              : Customer[id=4, firstName='Josh', lastName='Long']
```

## 结尾

恭喜！您刚刚使用Spring开发了一个简单的JDBC客户端。

## About the author

QQ Group：628793702

![Image text](https://raw.githubusercontent.com/UncleCatMySelf/img-myself/master/img/%E5%85%AC%E4%BC%97%E5%8F%B7.png)
