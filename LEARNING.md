# 苍穹外卖学习记录

这份文档记录每一步实际写下的代码、它在系统里的位置，以及背后的框架原理。目标不是背 API，而是能够回答“为什么需要它”。

## 2026-09-19：Step 1 - 搭建第一个可运行的 Spring Boot Web 项目

### 1. 本步目标

建立一个最小可运行的 Spring Boot 项目，暂时不连接 MySQL，也不实现员工、菜品等业务。

完成后的第一版调用链：

```text
浏览器
  |
  | HTTP GET /api/hello
  v
Spring Boot 内置 Tomcat
  |
  v
DispatcherServlet
  |
  v
SystemController
  |
  v
返回纯文本
```

当前只解决一个问题：证明 Java 程序能够启动 Web 服务器并响应 HTTP 请求。

### 2. Maven、POM 和依赖

`pom.xml` 是 Maven 项目的说明书。Maven 根据它完成依赖下载、编译、测试和打包。

当前 POM 中有三项关键内容：

1. `spring-boot-starter-parent`
   Spring Boot 的父 POM，负责统一依赖版本和 Maven 插件配置。子项目通常不需要逐个写版本号，可以减少版本冲突。
2. `spring-boot-starter-web`
   Web 场景的依赖集合，主要包含 Spring MVC、JSON 处理库和内置 Tomcat。
3. `spring-boot-starter-test`
   测试依赖集合，包含 JUnit 5、Spring Test 等。

这里的 `starter` 可以理解成“按场景打包好的一组依赖”。它并没有代替 Spring 或 Tomcat，只是替开发者选择了能协同工作的版本组合。

项目配置 Java 17 作为源码兼容版本，系统中的 JDK 21 可以编译和运行 Java 17 项目。

### 3. 启动类做了什么

```java
@SpringBootApplication
public class SkyTakeoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyTakeoutApplication.class, args);
    }
}
```

`@SpringBootApplication` 是组合注解，核心包含：

- `@SpringBootConfiguration`：声明这是 Spring Boot 配置类。
- `@EnableAutoConfiguration`：根据 classpath 中的依赖自动配置组件。例如发现 Web starter 后，自动配置 Spring MVC 和 Tomcat。
- `@ComponentScan`：从当前包 `com.sky.takeout` 开始扫描 `@Component`、`@Controller`、`@Service` 等 Bean。

因此，项目的根包应该放在 `com.sky.takeout`，业务类放到它的子包中，才能被默认扫描到。

### 4. Bean、IOC 和 DI 的初步认识

- Bean：由 Spring 创建和管理的 Java 对象。
- IOC，控制反转：创建和管理对象的控制权从业务代码交给 Spring 容器。
- DI，依赖注入：一个对象需要另一个对象时，由 Spring 把依赖传进来，而不是对象自己 `new`。

当前 Controller 由 Spring 创建，但还没有依赖其他 Bean。学习登录和员工管理时，会看到 Controller 通过构造器注入 Service。

### 5. Controller 和 Spring MVC

```java
@RestController
@RequestMapping("/api")
public class SystemController {

    @GetMapping("/hello")
    public String hello() {
        return "Sky Takeout is running";
    }
}
```

请求过程：

```text
GET /api/hello
  -> Tomcat 接收 TCP/HTTP 请求
  -> DispatcherServlet 查找匹配的 Controller 方法
  -> @RequestMapping("/api") 提供类级路径
  -> @GetMapping("/hello") 匹配 GET 方法和 /hello
  -> hello() 返回内容
  -> Spring MVC 转换成 HTTP 响应
```

`@RestController` 等于 `@Controller + @ResponseBody`，表示方法返回值直接作为响应体，而不是跳转到 HTML 页面。

Controller 的职责是处理 HTTP 协议边界：接收请求、校验基本输入、调用 Service、返回响应。业务规则和数据库访问不应该写进 Controller。

### 6. 运行与验证

项目已经生成 Maven Wrapper：

```text
mvnw                          macOS / Linux 启动脚本
mvnw.cmd                      Windows 启动脚本
.mvn/wrapper/                 固定 Maven 版本的配置
```

Maven Wrapper 的作用是把“项目需要的 Maven 版本”也纳入项目，而不是依赖电脑的全局 `mvn`。第一次执行时，如果本机没有对应版本，Wrapper 会下载并缓存 Maven 3.9.11。

启动：

```bash
./mvnw spring-boot:run
```

也可以直接在 IDEA 中运行：

```text
SkyTakeoutApplication
```

验证：

```bash
curl http://localhost:8080/api/hello
```

预期结果：

```text
Sky Takeout is running
```

本次已经完成以下验证：

```text
./mvnw 等价的 Maven test    通过，1 个测试成功
Spring Boot 3.5.0           启动成功
Tomcat                      监听 8080 端口
GET /api/hello              返回 Sky Takeout is running
```

### 7. 本步检查题

1. `@SpringBootApplication` 为什么能自动发现 `com.sky.takeout.controller` 中的 Controller？
2. `@RestController` 和普通 `@Controller` 的关键区别是什么？
3. 去掉 `spring-boot-starter-web` 后，Tomcat 为什么不能启动？
4. 为什么当前阶段不让 Controller 直接连接数据库？

### 8. 下一小步

下一小步进入员工管理的“查询员工分页”功能，依次学习：

```text
HTTP 参数
-> Controller
-> Service
-> Mapper
-> MySQL
-> 分页结果
```

### 9. Step 1 检查题复盘

#### 第 1 题：为什么能扫描到 Controller？

回答基本正确。补充完整过程：

```text
@SpringBootApplication
-> 包含 @ComponentScan
-> 默认以启动类所在包 com.sky.takeout 为起点
-> 扫描 com.sky.takeout 及其所有子包
-> 发现 @RestController
-> @RestController 间接包含 @Component
-> 创建 SystemController Bean
```

如果启动类放在 `com.sky`，而 Controller 放在 `com.example`，默认扫描不到。可以调整包结构，或者显式配置 `@ComponentScan`，但当前项目保持统一根包最清晰。

#### 第 2 题：@RestController 和 @Controller 的区别

`@Controller` 首先表示“这是一个 Spring MVC Controller”。

普通 `@Controller` 的方法通常返回视图名称：

```text
用户请求
-> Controller 返回 "index"
-> ViewResolver 找到 index.html 或 JSP
-> 返回 HTML
```

`@ResponseBody` 表示方法返回值直接写入 HTTP 响应体，不再查找页面。

`@RestController` 等价于：

```java
@Controller
@ResponseBody
```

因此当前 `hello()` 返回的字符串直接成为响应内容。若返回 Java 对象，Spring 会使用 Jackson 将其转换为 JSON。

#### 第 3 题：去掉 spring-boot-starter-web 后为什么无法启动 Tomcat？

Spring Boot 的自动配置不是无条件生效，它根据 classpath 中是否存在相关类来决定是否创建 Bean。

`spring-boot-starter-web` 间接提供了：

```text
Spring MVC
Servlet API
Jackson
内置 Tomcat
```

去掉它以后会产生两个结果：

1. `@RestController`、`@GetMapping` 等类无法导入，代码可能先编译失败。
2. 即使删除这些 Web 代码，类路径中也没有内置 Tomcat，Spring Boot 会按非 Web 应用启动或直接退出，不会监听 8080 端口。

核心原则：

```text
依赖改变 classpath
-> 自动配置条件改变
-> Spring 创建的 Bean 改变
-> 应用行为改变
```

#### 第 4 题：为什么 Controller 不直接连接数据库？

回答正确。再补充每层各自负责的事情：

```text
Controller：HTTP 请求和响应
Service：业务规则、流程编排、事务边界
Mapper：SQL 和数据库访问
```

如果 Controller 直接操作数据库，会产生这些问题：

- HTTP 协议代码和业务代码混在一起，职责不单一。
- 管理端和用户端需要相同业务时，逻辑无法复用。
- 事务边界难以表达，一个业务流程出错时不容易整体回滚。
- 更换数据访问方式时，需要修改所有 Controller。

下一轮先用一句话重新区分：

```text
@Controller 的作用是什么？
@ResponseBody 的作用是什么？
@RestController 为什么等价于前两者？
```

---

## 2026-09-20：Step 2 - 员工分页 API 工程化

### 1. 本步目标

Step 1 只证明了 Spring Boot 能接收 HTTP 请求。本步在已有员工分页查询基础上补齐 API 工程化能力：

```text
统一响应格式
请求参数校验
错误响应转换
Controller 切片测试
MyBatis 数据库集成测试
独立测试数据库
```

完成后，员工分页接口的调用链是：

```text
GET /admin/employee/page
  |
  v
DispatcherServlet
  |
  v
Spring Validation
  |
  v
EmployeeController
  |
  v
EmployeeService
  |
  +---- countByQuery() ----> MyBatis ----> MySQL / H2
  |
  +---- selectPage() ------> MyBatis ----> MySQL / H2
  |
  v
EmployeeVO + PageResult
  |
  v
Result<PageResult<EmployeeVO>>
  |
  v
JSON Response
```

### 2. 统一响应对象 Result

新增：

```text
src/main/java/com/sky/takeout/common/Result.java
```

核心结构：

```java
public class Result<T> {

    private final int code;
    private final String message;
    private final T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
```

泛型 `T` 表示业务数据的类型。分页接口的数据类型是：

```text
PageResult<EmployeeVO>
```

所以 Controller 返回类型是：

```java
Result<PageResult<EmployeeVO>>
```

成功响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 1,
    "records": [
      {
        "id": 1,
        "name": "Admin",
        "username": "admin",
        "phone": "13800000001",
        "sex": 1,
        "status": 1,
        "updateTime": "2026-09-19T10:00:00"
      }
    ]
  }
}
```

统一响应的作用不是“让 JSON 看起来整齐”，而是让前端、网关和测试可以使用同一套规则判断请求结果：

```text
code：业务状态码
message：可展示或可记录的结果说明
data：成功时返回的数据
```

当前约定 `code` 与 HTTP 状态码保持一致：

```text
200：成功
400：请求参数错误
404：资源不存在
409：数据冲突，例如用户名重复
500：服务端未预期异常
```

### 3. Controller 返回统一结构

修改后的员工分页方法：

```java
@GetMapping("/page")
public Result<PageResult<EmployeeVO>> page(@Valid EmployeePageQueryDTO query) {
    return Result.success(employeeService.pageQuery(query));
}
```

`@Valid` 会触发 `EmployeePageQueryDTO` 上的约束校验。

如果校验成功，调用继续进入 Service。

如果校验失败，方法不会执行，Spring MVC 会抛出参数绑定类异常，再由全局异常处理器转换成统一 JSON。

### 4. 请求参数校验

新增依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

这个 starter 主要提供：

```text
Jakarta Bean Validation API
Hibernate Validator 实现
Spring MVC 参数校验集成
```

`EmployeePageQueryDTO` 上的约束：

```java
public class EmployeePageQueryDTO {

    @Min(value = 1, message = "page must be greater than or equal to 1")
    private int page = 1;

    @Min(value = 1, message = "pageSize must be greater than or equal to 1")
    @Max(value = 100, message = "pageSize must be less than or equal to 100")
    private int pageSize = 10;

    @Size(max = 32, message = "name must not exceed 32 characters")
    private String name;

    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
```

各字段的约束原因：

```text
page：页码从 1 开始
pageSize：限制查询规模，防止一次取出过多数据
name：不能超过数据库 VARCHAR(32)
status：当前只允许 0 和 1
```

### 5. 为什么 Service 仍然保留 normalizeQuery

Controller 校验是“请求边界校验”，它会拒绝非法 HTTP 参数。

Service 中的 `normalizeQuery` 是“内部防御”，即使未来有定时任务、消息消费者或其他 Service 直接调用分页方法，也能避免出现非法分页参数。

```java
private void normalizeQuery(EmployeePageQueryDTO query) {
    query.setPage(Math.max(query.getPage(), 1));
    query.setPageSize(Math.min(Math.max(query.getPageSize(), 1), MAX_PAGE_SIZE));

    if (query.getName() != null) {
        String name = query.getName().trim();
        query.setName(name.isEmpty() ? null : name);
    }
}
```

边界校验和内部防御解决的是不同问题，因此两者可以同时存在。

### 6. 全局异常处理

新增：

```text
src/main/java/com/sky/takeout/exception/BusinessException.java
src/main/java/com/sky/takeout/exception/GlobalExceptionHandler.java
```

`@RestControllerAdvice` 会拦截 Controller 抛出的异常，并把异常转换成统一响应。

当前异常映射：

```text
BindException
  -> HTTP 400
  -> 参数格式、类型转换或字段约束错误

MethodArgumentTypeMismatchException
  -> HTTP 400
  -> URL 或查询参数类型错误

BusinessException
  -> 使用异常中携带的业务状态码
  -> 例如 404 或 409

Exception
  -> HTTP 500
  -> 记录完整服务端日志
  -> 客户端只看到通用错误信息
```

`BusinessException`：

```java
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

它用于表达“请求格式正确，但按业务规则不能继续”的情况，例如：

```text
员工不存在
用户名已经存在
不允许禁用当前管理员
```

当前还没有业务代码主动抛出它，它先作为后续员工 CRUD 的异常扩展点。

### 7. 参数转换失败为什么不能直接返回 500

请求：

```text
GET /admin/employee/page?page=abc&pageSize=10
```

`page` 的目标类型是 `int`，但输入是 `abc`。这不是服务端程序崩溃，而是客户端参数错误，应该返回 HTTP 400。

实现中有一个容易忽略的细节：

```text
MethodArgumentNotValidException 是 BindException 的子类
```

因此类型转换错误会先进入 `handleBindException`。处理器通过：

```java
fieldError.isBindingFailure()
```

区分“类型绑定失败”和“字段规则校验失败”。

绑定失败时返回简洁信息：

```json
{
  "code": 400,
  "message": "Invalid value for parameter: page",
  "data": null
}
```

而不是把 Java 类型转换的完整底层信息暴露给客户端。

### 8. MyBatis 分页 SQL

分页 SQL：

```xml
<select id="selectPage" resultType="Employee">
    SELECT id,
           name,
           username,
           phone,
           sex,
           status,
           update_time
    FROM employee
    <include refid="queryWhere"/>
    ORDER BY update_time DESC, id DESC
    LIMIT #{pageSize} OFFSET #{offset}
</select>
```

`LIMIT ... OFFSET ...` 比 MySQL 特有的 `LIMIT offset, size` 更容易被其他数据库理解，也能被测试环境中的 H2 执行。

分页查询需要两条 SQL：

```text
countByQuery：
  查询满足条件的总记录数

selectPage：
  查询当前页的数据
```

先查总数再查列表，是因为响应需要同时返回：

```text
total：总记录数
records：当前页记录
```

当 `total == 0` 时，Service 直接返回空列表，不再执行 `selectPage`：

```java
long total = employeeMapper.countByQuery(query);
if (total == 0) {
    return new PageResult<>(0, List.of());
}
```

### 9. Controller 切片测试

新增：

```text
src/test/java/com/sky/takeout/controller/admin/EmployeeControllerTest.java
```

`@WebMvcTest(EmployeeController.class)` 只加载 Web MVC 相关组件：

```text
EmployeeController
Spring MVC
Jackson
Validation
GlobalExceptionHandler
```

它不会启动完整 Spring Boot 应用，也不会连接数据库。

Service 使用 Mock：

```java
@MockitoBean
private EmployeeService employeeService;
```

成功场景验证：

```text
HTTP 200
code = 200
message = success
data.total
data.records[0].username
```

参数错误场景验证：

```text
HTTP 400
code = 400
message
data = null
```

Controller 测试关注协议边界：

```text
URL 是否匹配
查询参数是否能绑定
JSON 字段是否符合约定
校验失败是否返回 400
```

它不应该关注真实 SQL 是否正确。

### 10. MyBatis 集成测试

新增：

```text
src/test/java/com/sky/takeout/mapper/EmployeeMapperIntegrationTest.java
src/test/resources/application-test.yml
src/test/resources/schema.sql
```

测试使用 H2 内存数据库：

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:sky_takeout;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
    username: sa
    password:
  sql:
    init:
      mode: always
```

H2 的作用：

```text
每次测试自动创建数据库
自动执行测试 schema.sql
测试之间不污染本机 MySQL
不需要开发人员先手工准备数据库
可以验证 SQL、MyBatis 映射和 Service 调用链
```

H2 使用 `MODE=MySQL`，但测试数据库不能完全代替真实 MySQL：

```text
SQL 方言、索引行为、锁、事务隔离仍可能存在差异
```

因此 H2 测试用于快速回归，发布前仍需要连接真实 MySQL 做验收。

集成测试注入真实的 Controller 之外的完整业务链路：

```java
@SpringBootTest
@ActiveProfiles("test")
class EmployeeMapperIntegrationTest {

    @Autowired
    private EmployeeService employeeService;
}
```

当前覆盖：

```text
状态过滤和分页
姓名模糊查询
数据库行到 VO 的字段映射
无匹配数据时返回空页
```

### 11. 三类测试的职责

本项目当前有三类测试：

```text
EmployeeServiceImplTest
  -> 单元测试
  -> Mock Mapper
  -> 验证业务分支和参数归一化

EmployeeControllerTest
  -> MVC 切片测试
  -> Mock Service
  -> 验证 HTTP、校验和 JSON

EmployeeMapperIntegrationTest
  -> 集成测试
  -> 连接 H2
  -> 验证 Service、MyBatis、SQL 和数据库映射
```

三层测试不是重复劳动，而是分别隔离不同失败原因：

```text
Controller 测试失败：协议或校验问题
Service 单元测试失败：业务分支问题
Mapper 集成测试失败：SQL 或映射问题
```

### 12. 运行与验证

运行全部测试：

```bash
./mvnw test
```

本轮实际结果：

```text
Tests run: 9
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

测试组成：

```text
EmployeeMapperIntegrationTest：3
EmployeeControllerTest：3
SkyTakeoutApplicationTests：1
EmployeeServiceImplTest：2
```

手工启动应用并访问真实 MySQL：

```bash
DB_URL='jdbc:mysql://localhost:3306/sky_takeout?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true' \
DB_USERNAME=root \
DB_PASSWORD='你的密码' \
./mvnw spring-boot:run
```

成功查询：

```bash
curl 'http://localhost:8080/admin/employee/page?page=1&pageSize=10'
```

非法页码：

```bash
curl -i 'http://localhost:8080/admin/employee/page?page=0&pageSize=10'
```

### 13. 本步形成的设计原则

```text
Controller 负责 HTTP 边界和参数校验
Service 负责业务规则和内部防御
Mapper 负责 SQL
统一响应负责稳定客户端契约
全局异常处理负责把异常转换成协议响应
H2 负责快速自动化集成测试
MySQL 负责最终真实数据库验收
```

### 14. 自学检查点

1. `Result<T>` 为什么使用泛型，而不是让 `data` 使用 `Object`？
2. `@Valid` 为什么放在 Controller 参数上，而不是写在 Service 方法内部？
3. 为什么参数格式错误应该返回 400，而数据库连接失败应该返回 500？
4. `MethodArgumentNotValidException` 为什么会进入 `BindException` 的异常处理方法？
5. 为什么 H2 集成测试通过后，仍然需要在 MySQL 上做一次验证？
6. Controller 测试中 Mock Service 后，能够证明哪些事情，不能证明哪些事情？

---

## 2026-09-20：Step 3 - 员工管理完整 CRUD

### 1. 本步目标

在分页查询基础上，完成管理端员工维护闭环：

```text
新增员工
根据 ID 查询详情
修改员工
启用或禁用员工
删除员工
用户名和身份证号唯一性检查
BCrypt 密码加密
Service 事务边界
```

本步最终形成以下接口：

```text
GET    /admin/employee/page
POST   /admin/employee
GET    /admin/employee/{id}
PUT    /admin/employee
POST   /admin/employee/status/{status}?id={id}
DELETE /admin/employee/{id}
```

### 2. 新增员工调用链

```text
POST /admin/employee
  |
  v
EmployeeController
  |
  v
EmployeeCreateDTO
  |
  v
Spring Validation
  |
  v
EmployeeService.create()
  |
  +---- 查询用户名是否重复
  |
  +---- 查询身份证号是否重复
  |
  +---- BCrypt 加密密码
  |
  +---- EmployeeMapper.insert()
  |
  v
MySQL / H2
  |
  v
生成的员工 ID
  |
  v
Result<Long>
```

### 3. 查询员工详情调用链

```text
GET /admin/employee/{id}
  |
  v
EmployeeController
  |
  v
EmployeeService.getById()
  |
  v
EmployeeMapper.selectById()
  |
  v
MySQL / H2
  |
  v
Employee
  |
  v
EmployeeDetailVO
  |
  v
Result<EmployeeDetailVO>
```

`Employee` 是数据库实体，包含密码字段。

接口不能直接返回 `Employee`，否则密码可能被序列化到 JSON。详情接口使用 `EmployeeDetailVO`，只包含前端需要的字段：

```text
id
name
username
phone
sex
idNumber
status
createTime
updateTime
```

分页接口继续使用字段更少的 `EmployeeVO`。

### 4. 新增与修改 DTO

新增：

```text
src/main/java/com/sky/takeout/dto/EmployeeCreateDTO.java
src/main/java/com/sky/takeout/dto/EmployeeUpdateDTO.java
```

新增 DTO 字段：

```text
name
username
password
phone
sex
idNumber
```

修改 DTO 字段：

```text
id
name
username
phone
sex
idNumber
```

修改和新增使用的字段不完全相同，因此没有强行让两个 DTO 继承同一个父类。

新增接口可以设置密码，修改接口不修改密码。密码修改应该有单独的“重置密码”流程，避免普通资料修改意外覆盖密码。

### 5. 参数约束

关键约束：

```java
@NotBlank(message = "username must not be blank")
@Size(max = 32, message = "username must not exceed 32 characters")
private String username;

@Size(min = 6, max = 32, message = "password length must be between 6 and 32")
private String password;

@Pattern(regexp = "^1\\d{10}$", message = "phone must be a valid 11-digit number")
private String phone;

@Min(value = 1, message = "sex must be 1 or 2")
@Max(value = 2, message = "sex must be 1 or 2")
private Integer sex;

@Pattern(
        regexp = "^\\d{17}[0-9Xx]$",
        message = "idNumber must be a valid 18-character ID number"
)
private String idNumber;
```

约束的职责是阻止明显非法的数据进入 Service：

```text
@NotBlank：不能为空或只有空白字符
@Size：限制字符串长度
@Pattern：限制格式
@Min / @Max：限制数值范围
```

业务唯一性不能只用 Bean Validation 完成，因为它需要查询数据库。只有 Service 和数据库层能够判断用户名、身份证号是否已经存在。

### 6. 密码为什么不能明文保存

数据库中保存明文密码的风险：

```text
数据库备份泄露后，攻击者直接获得用户密码
管理员可以看到员工真实密码
员工复用密码时，会扩大其他系统被攻击的范围
```

本步引入：

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

这里没有引入完整 Spring Security，只引入密码加密工具，避免在当前阶段意外启用整套安全过滤链。

配置类：

```java
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

Service 只依赖接口：

```java
private final PasswordEncoder passwordEncoder;
```

而不是直接依赖 `BCryptPasswordEncoder`。

这样带来两个好处：

```text
Service 不需要知道具体加密算法
测试中可以替换 PasswordEncoder
以后升级算法时，改动范围更小
```

保存前加密：

```java
employee.setPassword(passwordEncoder.encode(employeeCreateDTO.getPassword()));
```

BCrypt 每次加密同一个密码都会生成不同的哈希值，因为其中包含随机盐。密码验证不通过解密完成，而是：

```java
passwordEncoder.matches(rawPassword, encodedPassword);
```

### 7. 唯一性校验

Mapper 提供两个统计方法：

```java
long countByUsername(
        @Param("username") String username,
        @Param("excludeId") Long excludeId
);

long countByIdNumber(
        @Param("idNumber") String idNumber,
        @Param("excludeId") Long excludeId
);
```

新增员工时 `excludeId` 为 `null`，表示查询全表。

修改员工时传入当前员工 ID：

```sql
SELECT COUNT(*)
FROM employee
WHERE username = #{username}
  AND id != #{excludeId}
```

这样当前员工可以继续使用自己的用户名，但不能再被其他员工使用。

Service 的统一检查：

```java
private void validateUnique(String username, String idNumber, Long excludeId) {
    if (employeeMapper.countByUsername(username, excludeId) > 0) {
        throw new BusinessException(409, "Username already exists");
    }

    if (employeeMapper.countByIdNumber(idNumber, excludeId) > 0) {
        throw new BusinessException(409, "ID number already exists");
    }
}
```

### 8. 为什么还要依赖数据库唯一索引

先查询再插入存在并发窗口：

```text
请求 A：查询 username = admin，不存在
请求 B：查询 username = admin，不存在
请求 A：插入 admin
请求 B：插入 admin
```

如果数据库没有唯一索引，最后一次写入可能产生重复数据。

当前数据库已经建立：

```sql
UNIQUE KEY uk_employee_username (username)
UNIQUE KEY uk_employee_id_number (id_number)
```

数据库唯一索引是并发情况下最终的数据正确性保障。

Service 对数据库冲突异常进行转换：

```java
try {
    employeeMapper.insert(employee);
} catch (DuplicateKeyException exception) {
    throw new BusinessException(409, "Username or ID number already exists");
}
```

由此形成三层保护：

```text
DTO：格式正确
Service：提前返回可理解的业务错误
MySQL 唯一索引：并发时保证最终不重复
```

### 9. 写操作的事务边界

新增、修改、状态修改和删除方法增加：

```java
@Transactional
```

事务属于 Service，而不是 Controller 或 Mapper。

原因是 Service 表达“一个完整业务操作”：

```text
新增员工：
  检查唯一性
  加密密码
  插入数据

修改员工：
  检查员工是否存在
  检查字段是否与其他员工冲突
  更新数据
```

如果这些步骤中的数据库操作属于同一个事务，任意一步抛错时，前面的写入可以整体回滚。

Controller 只知道 HTTP，不适合定义事务。

Mapper 只知道单条 SQL，也不适合定义跨 SQL 的业务事务。

### 10. 新增员工的 Service 实现

核心代码：

```java
@Override
@Transactional
public Long create(EmployeeCreateDTO employeeCreateDTO) {
    String username = employeeCreateDTO.getUsername().trim();
    String idNumber = employeeCreateDTO.getIdNumber().trim();

    validateUnique(username, idNumber, null);

    Employee employee = new Employee();
    employee.setName(employeeCreateDTO.getName().trim());
    employee.setUsername(username);
    employee.setPassword(passwordEncoder.encode(employeeCreateDTO.getPassword()));
    employee.setPhone(employeeCreateDTO.getPhone());
    employee.setSex(employeeCreateDTO.getSex());
    employee.setIdNumber(idNumber);
    employee.setStatus(ENABLED_STATUS);
    employee.setCreateUser(SYSTEM_USER_ID);
    employee.setUpdateUser(SYSTEM_USER_ID);

    employeeMapper.insert(employee);
    return employee.getId();
}
```

`SYSTEM_USER_ID = 1L` 是登录功能完成前的临时审计用户。

Phase 2 完成登录后，创建人和修改人应该从当前登录用户上下文获取，而不能继续写死。

### 11. 查询详情和不存在处理

Service 统一通过 `requireEmployee` 获取员工：

```java
private Employee requireEmployee(Long id) {
    Employee employee = employeeMapper.selectById(id);
    if (employee == null) {
        throw new BusinessException(404, "Employee not found");
    }
    return employee;
}
```

修改、启停和删除都先调用它。

这样做避免每个方法重复写相同的空值判断：

```text
查询详情：不存在 -> 404
修改：不存在 -> 404
启停：不存在 -> 404
删除：不存在 -> 404
```

### 12. 修改员工

修改流程：

```text
根据 ID 检查员工是否存在
清理用户名和身份证号两端空白
排除当前 ID 后检查唯一性
创建只包含可修改字段的 Employee
执行 UPDATE
```

`EmployeeMapper.update()` 对应 SQL：

```sql
UPDATE employee
SET name = #{name},
    username = #{username},
    phone = #{phone},
    sex = #{sex},
    id_number = #{idNumber},
    update_time = CURRENT_TIMESTAMP,
    update_user = #{updateUser}
WHERE id = #{id}
```

密码、状态和创建信息不在这个 SQL 中。不同写操作修改不同字段，可以避免意外覆盖无关数据。

### 13. 启用与禁用

Controller：

```java
@PostMapping("/status/{status}")
public Result<Void> updateStatus(
        @PathVariable @Min(0) @Max(1) Integer status,
        @RequestParam @Positive Long id
) {
    employeeService.updateStatus(id, status);
    return Result.success(null);
}
```

接口示例：

```text
POST /admin/employee/status/0?id=2
```

含义：

```text
id = 2：目标员工
status = 0：禁用
```

启用：

```text
POST /admin/employee/status/1?id=2
```

Service 仍然对 `status` 做内部检查，防止其他调用方式绕过 Controller 校验。

“不能禁用当前登录管理员”的规则暂时未实现，因为当前系统还没有登录用户上下文。该规则应在 Phase 2 中实现。

### 14. 删除员工

删除接口：

```text
DELETE /admin/employee/{id}
```

当前采用物理删除：

```sql
DELETE FROM employee
WHERE id = #{id}
```

物理删除适合当前学习阶段，但真实系统中通常还要考虑：

```text
员工是否关联了订单、操作日志等数据
是否需要保留历史审计记录
是否应该使用逻辑删除
是否需要权限审批
```

因此当前删除功能不能直接视为最终业务方案。

### 15. MyBatis 获取自增主键

插入配置：

```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO employee (...)
    VALUES (...)
</insert>
```

数据库生成自增 ID 后，MyBatis 会把该值写回传入的 `Employee.id`：

```java
employeeMapper.insert(employee);
return employee.getId();
```

如果去掉 `useGeneratedKeys` 和 `keyProperty`，插入可能成功，但 Java 对象中的 `id` 仍然为 `null`。

### 16. Mapper 字段映射

项目已经配置：

```yaml
mybatis:
  configuration:
    map-underscore-to-camel-case: true
```

因此数据库字段可以自动映射到 Java 属性：

```text
id_number   -> idNumber
create_time -> createTime
update_time -> updateTime
create_user -> createUser
update_user -> updateUser
```

`selectById` 显式列出字段，而不是使用：

```sql
SELECT *
```

显式字段的优点是：

```text
SQL 返回内容稳定
数据库新增字段不会自动暴露给代码
阅读 Mapper 时能直接看到依赖哪些列
```

### 17. Controller 新增接口

新增接口：

```java
@PostMapping
public Result<Long> create(@Valid @RequestBody EmployeeCreateDTO employeeCreateDTO) {
    return Result.success(employeeService.create(employeeCreateDTO));
}
```

详情接口：

```java
@GetMapping("/{id}")
public Result<EmployeeDetailVO> getById(@PathVariable @Positive Long id) {
    return Result.success(employeeService.getById(id));
}
```

修改接口：

```java
@PutMapping
public Result<Void> update(@Valid @RequestBody EmployeeUpdateDTO employeeUpdateDTO) {
    employeeService.update(employeeUpdateDTO);
    return Result.success(null);
}
```

删除接口：

```java
@DeleteMapping("/{id}")
public Result<Void> delete(@PathVariable @Positive Long id) {
    employeeService.delete(id);
    return Result.success(null);
}
```

### 18. 全局异常扩展

本步继续扩展 `GlobalExceptionHandler`：

```text
ConstraintViolationException
  -> 方法参数校验失败，例如 @Positive、@Min、@Max

HttpMessageNotReadableException
  -> JSON 缺失、格式损坏或无法反序列化
```

它们都返回 HTTP 400。

业务异常仍然使用：

```text
404：员工不存在
409：用户名或身份证号冲突
400：状态值非法
500：本应成功但受影响行数异常
```

### 19. 测试覆盖

本步完成后测试数量从 9 增加到 23。

Service 单元测试：

```text
分页空结果不查询列表
分页参数归一化和 VO 映射
新增员工时密码被加密
重复用户名在插入前被拒绝
修改不存在的员工返回 404
```

Controller 切片测试：

```text
分页成功响应
非法页码
非数字页码
新增员工
新增参数校验失败
查询员工详情
员工不存在返回 404
修改员工
修改状态和删除员工
```

H2 集成测试：

```text
状态过滤和分页
姓名模糊查询
无匹配数据
新增员工并验证 BCrypt
修改员工和状态
删除员工
重复用户名冲突
```

真实 HTTP 全栈测试：

```text
使用 RANDOM_PORT 启动真实 Tomcat
通过 TestRestTemplate 发送 HTTP 请求
新增员工
查询详情
修改员工
修改状态
删除员工
删除后查询返回 404
```

HTTP 全栈测试类：

```text
src/test/java/com/sky/takeout/EmployeeHttpIntegrationTest.java
```

它和 MockMvc 测试的区别：

```text
MockMvc：
  不启动真实 Tomcat
  直接模拟 Servlet 调用
  速度快

TestRestTemplate + RANDOM_PORT：
  启动真实内嵌 Tomcat
  使用真实 HTTP 请求和响应
  覆盖 JSON 序列化、端口监听和完整过滤器链
```

首次手工使用 `spring-boot:run` 激活 `test` profile 时，应用仍然读取了主配置并尝试连接 MySQL。原因不是 profile 名称错误，而是 `src/test/resources/application-test.yml` 和 H2 依赖默认不在 `spring-boot:run` 的运行 classpath 中。

这说明：

```text
profile 被激活
不等于
该 profile 的配置文件一定在运行时 classpath 中
```

因此 HTTP 全栈回归使用 `@SpringBootTest`，让测试框架统一管理 test classpath、profile 和随机端口，避免依赖开发人员手工拼接启动参数。

实际运行结果：

```text
Tests run: 23
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 20. H2 与 MySQL 的验证边界

当前自动化测试使用 H2 内存数据库，它已经验证：

```text
Mapper XML 能被加载
SQL 语法能被 H2 的 MySQL 模式接受
参数绑定和动态 SQL 正常
自增主键能够回填
事务能够包围 Service 写操作
Java 与数据库字段映射正确
```

H2 不能完全证明：

```text
MySQL 8 的排序规则和唯一索引行为
MySQL 对 UPDATE、DELETE 的锁行为
真实连接池与 MySQL 账号权限
`utf8mb4` 中文数据写入
数据库脚本在真实 MySQL 中可执行
```

因此本步在完成 H2 测试后，继续连接了本机真实 MySQL 做第二次验收。

### 21. 真实 MySQL 验收

本机 MySQL：

```text
MySQL version: 26.7.0
Connection: 127.0.0.1:3306
Database: sky_takeout
Character set: utf8mb4
```

执行建库脚本：

```bash
MYSQL_PWD='<password>' mysql \
  --protocol=TCP \
  -h127.0.0.1 \
  -P3306 \
  -uroot \
  < sql/schema.sql
```

真实数据库结构验证结果：

```text
PRIMARY KEY (id)
UNIQUE KEY uk_employee_username (username)
UNIQUE KEY uk_employee_id_number (id_number)
KEY idx_employee_name (name)
KEY idx_employee_status (status)
ENGINE = InnoDB
CHARSET = utf8mb4
```

启动应用连接真实 MySQL：

```bash
DB_URL='jdbc:mysql://127.0.0.1:3306/sky_takeout' \
DB_USERNAME=root \
DB_PASSWORD='<password>' \
./mvnw spring-boot:run
```

完成真实 HTTP 验收：

```text
POST /admin/employee
  -> 200
  -> data = 3

GET /admin/employee/page?page=1&pageSize=10&name=MySQL
  -> 200
  -> total = 1

GET /admin/employee/3
  -> 200
  -> 返回详情且不包含 password

PUT /admin/employee
  -> 200
  -> 修改姓名、手机号和性别

POST /admin/employee/status/0?id=3
  -> 200
  -> status = 0

使用中文姓名再次修改
  -> 200
  -> MySQL 返回中文姓名正常

重复用户名 admin 新增员工
  -> HTTP 409
  -> Username already exists

DELETE /admin/employee/3
  -> 200

删除后 GET /admin/employee/3
  -> HTTP 404
  -> Employee not found
```

直接检查数据库后确认：

```text
employee 表中的密码是 BCrypt 哈希
哈希前缀为 $2a$10$
密码不是明文 secret123
```

验收完成后的数据库状态：

```text
employee_count = 2
mysql-smoke-user count = 0
保留数据：
  id = 1, username = admin
  id = 2, username = operator
```

真实 MySQL 验收证明了：

```text
建库脚本可以在 MySQL 执行
utf8mb4 中文写入和读取正常
MySQL 唯一索引生效
业务冲突被转成 HTTP 409
自增主键能够回填
真实 HikariCP 连接池能够连接 MySQL
真实 HTTP、Jackson、MyBatis 和 MySQL 可以完整贯通
```

验收结束后已关闭 Spring Boot 进程，临时员工数据已经删除，数据库恢复为两条种子数据。

### 22. 自学检查点

1. 为什么密码不能通过解密恢复，而要用 `matches` 比较？
2. 为什么唯一性检查已经写了 Java 查询，数据库仍然需要唯一索引？
3. 为什么 `@Transactional` 放在 Service，而不是 Controller？
4. 为什么详情接口不能直接返回 `Employee` 实体？
5. 为什么修改接口不应同时支持修改密码？
6. `useGeneratedKeys` 和 `keyProperty` 分别解决什么问题？
7. 当前删除员工为什么不能直接等价为最终生产方案？

---

## 2026-09-20：Step 4 - 分类管理

### 1. 本步目标

分类用于给菜品和套餐分组。管理端需要维护分类名称、类型、排序和启停状态。

本步完成以下接口：

```text
GET    /admin/category/page
POST   /admin/category
GET    /admin/category/{id}
GET    /admin/category/list?type={type}
PUT    /admin/category
POST   /admin/category/status/{status}?id={id}
DELETE /admin/category/{id}
```

分类类型约定：

```text
type = 1：菜品分类
type = 2：套餐分类
```

状态约定：

```text
status = 0：禁用
status = 1：启用
```

### 2. 分类表结构

在 `sql/schema.sql` 中新增 `category` 表：

```sql
CREATE TABLE IF NOT EXISTS category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    type TINYINT NOT NULL,
    name VARCHAR(32) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    update_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_type_name (type, name),
    KEY idx_category_type_sort (type, sort),
    KEY idx_category_status (status)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
```

字段作用：

```text
type：分类属于菜品还是套餐
name：显示名称
sort：列表排序值
status：是否启用
create_time / update_time：创建与修改时间
create_user / update_user：审计用户
```

### 3. 为什么唯一索引使用 type + name

业务规则是：

```text
同一个类型下，分类名不能重复
不同业务类型之间，允许出现相同名称
```

所以不能只给 `name` 建唯一索引，而是建立复合唯一索引：

```sql
UNIQUE KEY uk_category_type_name (type, name)
```

允许：

```text
type = 1, name = Beverages
type = 2, name = Beverages
```

不允许：

```text
type = 1, name = Beverages
type = 1, name = Beverages
```

### 4. 新增分类调用链

```text
POST /admin/category
  |
  v
CategoryController
  |
  v
CategoryCreateDTO
  |
  v
Spring Validation
  |
  v
CategoryService.create()
  |
  +---- 清理 name 两端空白
  |
  +---- 查询同类同名的分类是否存在
  |
  +---- 创建 Category
  |
  +---- CategoryMapper.insert()
  |
  v
真实 MySQL
  |
  v
自增分类 ID
  |
  v
Result<Long>
```

### 5. 分类分页查询

请求：

```text
GET /admin/category/page?page=1&pageSize=10&type=1&name=Hot
```

分页查询条件：

```text
type：可选，精确匹配 1 或 2
name：可选，模糊匹配
page / pageSize：分页参数
```

Mapper 动态 SQL：

```xml
<sql id="queryWhere">
    <where>
        <if test="type != null">
            AND type = #{type}
        </if>
        <if test="name != null and name != ''">
            AND name LIKE CONCAT('%', #{name}, '%')
        </if>
    </where>
</sql>
```

排序规则：

```sql
ORDER BY sort ASC, update_time DESC, id DESC
```

含义：

```text
sort 越小越靠前
sort 相同时，最近修改的靠前
sort 和修改时间都相同时，ID 大的靠前
```

最后加入 `id DESC` 是为了保证排序结果稳定，避免两条记录排序值完全相同时数据库返回顺序不确定。

### 6. 按类型查询分类列表

接口：

```text
GET /admin/category/list?type=1
```

该接口主要用于管理端下拉框，例如新增菜品时选择菜品分类。

与分页接口的区别：

```text
/page：
  面向管理表格
  支持分页、名称过滤和类型过滤

/list：
  面向下拉选择
  只按类型查询
  返回完整列表，不分页
```

Service 仍然检查类型：

```java
if (type == null || (type != 1 && type != 2)) {
    throw new BusinessException(400, "Type must be 1 or 2");
}
```

Controller 上的 `@Min`、`@Max` 负责 HTTP 边界，Service 检查负责内部调用安全。

### 7. 新增与修改分类

新增 DTO：

```text
type
name
sort
```

修改 DTO：

```text
id
type
name
sort
```

修改时不修改：

```text
status
create_time
create_user
```

状态通过独立接口修改，创建信息和审计信息由系统维护。

修改分类时，唯一性检查需要排除当前分类自身：

```sql
SELECT COUNT(*)
FROM category
WHERE type = #{type}
  AND name = #{name}
  <if test="excludeId != null">
      AND id != #{excludeId}
  </if>
```

这样未修改名称时，当前分类不会与自身冲突。

### 8. 分类启停

请求：

```text
POST /admin/category/status/0?id=3
```

含义：

```text
id = 3
status = 0
```

执行 SQL：

```sql
UPDATE category
SET status = #{status},
    update_time = CURRENT_TIMESTAMP,
    update_user = #{updateUser}
WHERE id = #{id}
```

当前允许禁用分类，但尚未检查该分类是否已经关联菜品或套餐。

完整的业务规则应在菜品和套餐模块完成后补充：

```text
分类已被使用
-> 禁止删除
-> 禁用前提示影响范围
-> 可能需要同时处理关联数据
```

### 9. 删除分类

接口：

```text
DELETE /admin/category/{id}
```

当前执行物理删除：

```sql
DELETE FROM category
WHERE id = #{id}
```

现阶段还没有 `dish` 和 `setmeal` 表，因此无法检查分类是否已被引用。

后续创建菜品和套餐表后，需要增加：

```text
查询 dish 是否使用该 category_id
查询 setmeal 是否使用该 category_id
如果存在关联，返回 409 并禁止删除
```

因此当前删除功能只完成了分类自身的数据闭环，不是最终业务闭环。

### 10. Service 事务边界

新增、修改、启停和删除分类都使用：

```java
@Transactional
```

以新增分类为例：

```text
检查同类型名称唯一
构造 Category
执行 insert
```

这些步骤需要属于同一个业务操作。

数据库复合唯一索引是并发条件下的最终保障。如果唯一性查询和插入之间发生并发竞争，数据库会抛出冲突，Service 将其转换为：

```text
HTTP 409
Category name already exists for this type
```

### 11. Controller 设计

Controller 保持只处理 HTTP 边界：

```java
@GetMapping("/page")
public Result<PageResult<CategoryVO>> page(
        @Valid CategoryPageQueryDTO query
) {
    return Result.success(categoryService.pageQuery(query));
}
```

新增：

```java
@PostMapping
public Result<Long> create(
        @Valid @RequestBody CategoryCreateDTO categoryCreateDTO
) {
    return Result.success(categoryService.create(categoryCreateDTO));
}
```

列表：

```java
@GetMapping("/list")
public Result<List<CategoryVO>> listByType(
        @RequestParam @Min(1) @Max(2) Integer type
) {
    return Result.success(categoryService.listByType(type));
}
```

Controller 不包含：

```text
SQL
事务
唯一性规则
启停业务判断
```

### 12. 测试覆盖

本步新增四类测试：

```text
CategoryServiceImplTest
  -> 分页、新增、重复、404、非法类型

CategoryControllerTest
  -> 分页、新增、参数错误、列表、404、修改、启停、删除

CategoryMapperIntegrationTest
  -> SQL、分页、CRUD、跨类型同名、排序

CategoryHttpIntegrationTest
  -> 随机端口上的真实 HTTP 生命周期
```

测试过程中发现并修正了一个测试预期错误：

```text
初始类型 1 只有 Hot Dishes 一条数据
新增 HTTP Category 后应为两条
错误断言写成了三条
```

这说明集成测试不仅要验证代码，还要正确表达基础数据状态。

全部测试结果：

```text
Tests run: 40
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 13. 真实 MySQL 验收

再次执行幂等 schema 后，真实数据库新增 `category` 表。

真实表验证：

```text
PRIMARY KEY (id)
UNIQUE KEY uk_category_type_name (type, name)
KEY idx_category_type_sort (type, sort)
KEY idx_category_status (status)
ENGINE = InnoDB
CHARSET = utf8mb4
```

初始种子数据：

```text
id = 1, type = 1, name = Hot Dishes, sort = 10, status = 1
id = 2, type = 2, name = Set Meals,  sort = 10, status = 1
```

真实 HTTP 验收：

```text
新增 type=1, name=MySQL Category, sort=5
  -> 200, id=3

分页查询 type=1, name=MySQL
  -> total=1

按 type=1 查询列表
  -> MySQL Category sort=5 排在 Hot Dishes sort=10 前

查询分类详情
  -> 200

再次新增 type=1, name=MySQL Category
  -> 409

新增 type=2, name=MySQL Category
  -> 200, id=4
  -> 验证跨类型同名允许

修改 id=3
  -> name=MySQL 分类
  -> sort=1
  -> 200

禁用 id=3
  -> status=0
  -> 200

删除 id=3 和 id=4
  -> 200

再次查询 id=3
  -> 404
```

验收结束后：

```text
category_count = 2
temporary_count = 0
保留 Hot Dishes 和 Set Meals
```

Spring Boot 验收进程已关闭，MySQL 服务继续运行。

### 14. 本步设计结论

```text
分类类型使用整数 1 和 2，当前以常量含义记录
分类名只在同一类型内唯一
sort 越小排序越靠前
分页接口和下拉列表接口职责不同
状态修改与资料修改使用不同接口
数据库唯一索引负责并发唯一性
分类删除还需要在菜品和套餐模块完成后增加引用检查
```

### 15. 自学检查点

1. 为什么分类唯一约束是 `(type, name)`，而不是单独的 `name`？
2. 为什么分页接口之外还需要一个不分页的 `/list` 接口？
3. `sort ASC, update_time DESC, id DESC` 中最后的 `id DESC` 有什么作用？
4. 为什么修改分类时需要把当前分类 ID 从唯一性查询中排除？
5. 为什么分类删除目前不能视为最终完成？
6. 分类的 `status` 为什么与普通资料修改分开处理？

---

## 2026-09-20：Step 5 - 菜品与口味管理

### 1. 本步目标

菜品是分类之后的核心业务实体。菜品属于某一个菜品分类，可以包含零到多组口味，并且需要支持上下架。

本步完成以下接口：

```text
GET    /admin/dish/page
POST   /admin/dish
GET    /admin/dish/{id}
PUT    /admin/dish
POST   /admin/dish/status/{status}?id={id}
DELETE /admin/dish/{id}
```

菜品状态：

```text
status = 0：停售
status = 1：起售
```

### 2. 菜品聚合

菜品和口味被当作一个聚合处理：

```text
Dish
  |
  +-- DishFlavor 1
  |
  +-- DishFlavor 2
  |
  +-- DishFlavor N
```

主表：

```text
dish
```

子表：

```text
dish_flavor
```

这意味着新增和修改菜品时，需要同时维护两张表。

### 3. dish 表

核心字段：

```sql
CREATE TABLE IF NOT EXISTS dish (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL,
    category_id BIGINT UNSIGNED NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    image VARCHAR(255) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    update_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_dish_category_id (category_id),
    KEY idx_dish_name (name),
    KEY idx_dish_status (status)
);
```

关键设计：

```text
price 使用 DECIMAL，不使用 double，避免金额精度问题
category_id 建立索引，因为分类筛选是高频查询
name 和 status 建立索引，支持名称搜索和状态筛选
image 当前只保存 URL，文件上传逻辑还未实现
```

### 4. dish_flavor 表

口味值在 Java DTO 中表现为字符串数组：

```json
{
  "name": "Spiciness",
  "value": ["Mild", "Spicy"]
}
```

数据库没有直接使用数组类型，而是把数组序列化为 JSON 字符串保存：

```sql
CREATE TABLE IF NOT EXISTS dish_flavor (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    dish_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(32) NOT NULL,
    value_json VARCHAR(1000) NOT NULL,
    ...
    KEY idx_dish_flavor_dish_id (dish_id)
);
```

第一次实现使用了 `value` 作为列名，但 H2 将 `VALUE` 识别为保留字，导致测试数据库无法建立该列。

最终改为：

```text
物理列名：value_json
Java 属性：value
```

MyBatis 使用显式 `resultMap` 完成映射：

```xml
<resultMap id="dishFlavorResultMap" type="DishFlavor">
    <id column="id" property="id"/>
    <result column="dish_id" property="dishId"/>
    <result column="name" property="name"/>
    <result column="value_json" property="value"/>
    ...
</resultMap>
```

这比依赖别名更清晰，也避免 MySQL 与 H2 对保留字的处理差异。

### 5. 菜品分类约束

菜品只能关联 `type = 1` 的分类：

```java
private Category requireDishCategory(Long categoryId) {
    Category category = categoryMapper.selectById(categoryId);

    if (category == null) {
        throw new BusinessException(400, "Dish category not found");
    }

    if (category.getType() != 1) {
        throw new BusinessException(400, "Category type must be 1");
    }

    return category;
}
```

如果传入套餐分类 ID，接口返回 HTTP 400。

### 6. 新增菜品事务

新增流程：

```text
校验分类存在且 type = 1
构造 Dish
插入 dish 主表
取得自增 ID
把每个口味的 value 序列化为 JSON
批量插入 dish_flavor
```

核心代码：

```java
@Override
@Transactional
public Long create(DishCreateDTO dishCreateDTO) {
    requireDishCategory(dishCreateDTO.getCategoryId());

    Dish dish = new Dish();
    ...
    dishMapper.insert(dish);
    saveFlavors(dish.getId(), dishCreateDTO.getFlavors());
    return dish.getId();
}
```

如果口味插入失败，主表插入也应该回滚，因此 `@Transactional` 放在 Service。

### 7. 修改菜品时替换口味

当前采用简单可靠的口味替换策略：

```text
删除 dish_id 对应的全部旧口味
重新批量插入新口味
```

代码：

```java
dishMapper.deleteFlavorsByDishId(dish.getId());
saveFlavors(dish.getId(), dishUpdateDTO.getFlavors());
```

这种方式容易保证最终状态一致，但会产生新的自增 ID。

后续数据量大时，可以改为差异更新：

```text
新增缺少的口味
更新变化的旧口味
删除不再需要的口味
```

当前阶段不提前优化。

### 8. 菜品分页查询

分页支持：

```text
name：菜品名称模糊查询
categoryId：分类过滤
status：上下架状态过滤
```

菜品列表需要返回分类名称，因此 Mapper 使用 JOIN：

```sql
SELECT d.id,
       d.name,
       d.category_id,
       c.name AS category_name,
       d.price,
       d.image,
       d.status,
       d.update_time
FROM dish d
LEFT JOIN category c ON c.id = d.category_id
WHERE ...
ORDER BY d.update_time DESC, d.id DESC
LIMIT #{pageSize} OFFSET #{offset}
```

结果直接映射到 `DishPageVO`，避免在 Java 循环中逐条查询分类名称。

### 9. 菜品详情

详情返回：

```text
菜品基本信息
分类名称
口味数组
```

口味 JSON 读取：

```java
objectMapper.readValue(value, new TypeReference<>() {
});
```

数据库异常 JSON 会被转换为 HTTP 500，而不是把底层解析细节直接返回给客户端。

### 10. 删除菜品

删除时同时删除口味：

```java
dishMapper.deleteFlavorsByDishId(id);
dishMapper.deleteById(id);
```

整个过程处于同一个事务。

后续套餐模块会建立 `setmeal_dish` 关联，届时还需要增加：

```text
菜品是否被套餐引用
如果已引用，禁止删除
```

### 11. 分类删除保护

菜品模块完成后，分类删除接口增加引用检查：

```sql
SELECT COUNT(*)
FROM dish
WHERE category_id = #{categoryId}
```

如果结果大于 0：

```text
HTTP 409
Category is referenced by dishes
```

这让分类模块从“只维护自身数据”升级为满足跨模块引用约束。

### 12. 测试覆盖

新增测试：

```text
DishServiceImplTest
DishControllerTest
DishMapperIntegrationTest
DishHttpIntegrationTest
```

覆盖内容：

```text
分页空结果
套餐分类不能用于菜品
新增菜品和口味序列化
菜品不存在返回 404
修改菜品时替换口味
删除菜品时删除口味
Controller 参数校验
真实 H2 数据库 CRUD
随机端口真实 HTTP 生命周期
分类被菜品引用时禁止删除
```

修正前的失败：

```text
H2 将 value 识别为保留字
value_json AS value 仍然失败
```

最终修复方式：

```text
使用 resultMap 把 value_json 映射到 Java 的 value 属性
```

修复后结果：

```text
Tests run: 56
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 13. 真实 MySQL 验收

真实数据库新增：

```text
dish
dish_flavor
```

完成的 HTTP 验收：

```text
新增 MySQL Dish
  -> 200
  -> dish id = 1

查询详情
  -> categoryName = Hot Dishes
  -> flavors = ["Mild", "Spicy"]

分页筛选
  -> total = 1

尝试删除分类 id = 1
  -> 409
  -> Category is referenced by dishes

修改菜品
  -> name = MySQL 菜品
  -> price = 24.80
  -> flavors = ["微辣", "中辣"]

上架菜品
  -> status = 1

删除菜品
  -> 200

删除后查询
  -> 404
```

直接检查数据库确认：

```text
dish 主表数据正确
dish_flavor 通过 value_json 保存 JSON 数组
中文字段正常
删除菜品后对应口味被清理
```

验收结束后的数据库状态：

```text
dish_count = 0
flavor_count = 0
category_count = 2
```

Spring Boot 验收进程已关闭。

### 14. 本步设计结论

```text
Dish 和 DishFlavor 是一个聚合
写操作在 Service 中定义事务
价格使用 BigDecimal
分类类型在 Service 中做业务校验
口味数组在数据库中保存为 JSON
MyBatis 使用 resultMap 处理列名与属性名差异
分页查询通过 JOIN 返回分类名称
分类删除需要检查菜品引用
菜品删除还需要在套餐模块完成后增加套餐引用检查
```

### 15. 自学检查点

1. 为什么金额字段使用 `BigDecimal`，而不是 `double`？
2. 为什么新增菜品和新增口味需要放在同一个事务？
3. 修改菜品为什么选择“先删后插”口味，而不是立即做差异更新？
4. 为什么口味数组不直接作为数据库数组保存？
5. `resultMap` 与 `resultType` 的适用场景有什么不同？
6. 为什么分类删除需要查询 `dish` 表？
7. 菜品删除目前在套餐模块完成后还需要补什么约束？

---

## 2026-09-20：Step 6 - 套餐管理

### 1. 本步目标

套餐由多个菜品组成。它和菜品一样可以上下架，但套餐只能关联 `type = 2` 的套餐分类。

本步完成以下接口：

```text
GET    /admin/setmeal/page
POST   /admin/setmeal
GET    /admin/setmeal/{id}
PUT    /admin/setmeal
POST   /admin/setmeal/status/{status}?id={id}
DELETE /admin/setmeal/{id}
```

套餐关系：

```text
category(type=2)
  |
  v
setmeal
  |
  v
setmeal_dish
  |
  v
dish
```

### 2. setmeal 表

```sql
CREATE TABLE IF NOT EXISTS setmeal (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(32) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    description VARCHAR(255) DEFAULT NULL,
    image VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    update_user BIGINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_setmeal_category_name (category_id, name),
    KEY idx_setmeal_category_id (category_id),
    KEY idx_setmeal_status (status)
);
```

套餐名在同一个套餐分类中不能重复，因此唯一索引是：

```text
category_id + name
```

### 3. setmeal_dish 关联表

```sql
CREATE TABLE IF NOT EXISTS setmeal_dish (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    setmeal_id BIGINT UNSIGNED NOT NULL,
    dish_id BIGINT UNSIGNED NOT NULL,
    copies INT NOT NULL DEFAULT 1,
    ...
    UNIQUE KEY uk_setmeal_dish (setmeal_id, dish_id),
    KEY idx_setmeal_dish_dish_id (dish_id)
);
```

字段含义：

```text
setmeal_id：套餐 ID
dish_id：菜品 ID
copies：套餐中包含几份该菜品
```

唯一索引保证同一个套餐不能重复添加同一个菜品。

### 4. 套餐新增流程

```text
校验分类存在且 type = 2
检查套餐名在分类内唯一
检查所有 dishId 是否存在
防止请求中出现重复 dishId
插入 setmeal 主表
获得套餐自增 ID
批量插入 setmeal_dish
```

所有步骤在同一个事务中：

```java
@Transactional
public Long create(SetmealCreateDTO setmealCreateDTO) {
    ...
    setmealMapper.insert(setmeal);
    setmealDishes.forEach(item -> item.setSetmealId(setmeal.getId()));
    insertSetmealDishes(setmealDishes);
    return setmeal.getId();
}
```

如果套餐菜品关联插入失败，套餐主表也会回滚。

### 5. 分类类型保护

套餐分类必须是 `type = 2`：

```java
if (category.getType() != 2) {
    throw new BusinessException(400, "Category type must be 2");
}
```

如果传入菜品分类，真实 HTTP 返回：

```json
{
  "code": 400,
  "message": "Category type must be 2",
  "data": null
}
```

### 6. 菜品存在性校验

Service 遍历请求中的菜品：

```java
Dish dish = dishMapper.selectById(dishDTO.getDishId());
if (dish == null) {
    throw new BusinessException(400, "Dish not found");
}
```

同时使用 `HashSet` 检查重复 `dishId`：

```java
if (!dishIds.add(dishDTO.getDishId())) {
    throw new BusinessException(400, "Setmeal contains duplicate dish IDs");
}
```

Java 层提供更早、更明确的错误信息，数据库唯一索引继续负责并发兜底。

### 7. 修改套餐时替换菜品关系

当前策略：

```text
更新 setmeal 主表
删除 setmeal_id 对应的旧关系
重新插入套餐菜品关系
```

代码：

```java
setmealMapper.update(setmeal);
setmealMapper.deleteDishesBySetmealId(setmeal.getId());
insertSetmealDishes(setmealDishes);
```

这种“先删后插”容易保证套餐当前状态正确，副作用是关联记录会生成新的自增 ID。

### 8. 套餐分页和详情

分页条件：

```text
name：套餐名模糊查询
categoryId：分类过滤
status：状态过滤
```

分页 SQL 通过 JOIN 返回分类名称：

```sql
SELECT s.id,
       s.category_id,
       c.name AS category_name,
       s.name,
       s.price,
       s.status,
       s.description,
       s.image,
       s.update_time
FROM setmeal s
LEFT JOIN category c ON c.id = s.category_id
```

详情返回套餐包含的菜品：

```sql
SELECT sd.dish_id,
       d.name AS dish_name,
       d.price AS dish_price,
       sd.copies
FROM setmeal_dish sd
JOIN dish d ON d.id = sd.dish_id
WHERE sd.setmeal_id = #{setmealId}
```

### 9. 分类删除保护扩展

分类删除现在会检查两种引用：

```text
是否被 dish 引用
是否被 setmeal 引用
```

对应错误：

```text
Category is referenced by dishes
Category is referenced by setmeals
```

这样分类模块不再只考虑菜品，也覆盖套餐分类。

### 10. 菜品删除保护

套餐建立关联后，菜品不能直接删除：

```sql
SELECT COUNT(*)
FROM setmeal_dish
WHERE dish_id = #{dishId}
```

如果结果大于 0：

```text
HTTP 409
Dish is referenced by setmeals
```

正确的处理顺序是：

```text
先从套餐中移除菜品
或者删除引用它的套餐
再删除菜品
```

### 11. 测试覆盖

新增测试：

```text
SetmealServiceImplTest
SetmealControllerTest
SetmealMapperIntegrationTest
SetmealHttpIntegrationTest
```

覆盖内容：

```text
分页空结果
菜品分类不能用于套餐
新增套餐与套餐菜品关联
分类内套餐名唯一
套餐不存在返回 404
删除套餐时删除关联
Controller 参数校验
套餐分页、详情和修改
分类被套餐引用时禁止删除
菜品被套餐引用时禁止删除
真实 HTTP 套餐生命周期
```

全量测试结果：

```text
Tests run: 73
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 12. 真实 MySQL 验收

真实数据库新增：

```text
setmeal
setmeal_dish
```

创建了两个临时菜品：

```text
Setmeal Test Dish A
Setmeal Test Dish B
```

完成验收：

```text
新增套餐
  -> categoryId = 2
  -> dish A copies = 1
  -> dish B copies = 2

套餐详情
  -> categoryName = Set Meals
  -> 返回菜品名、菜品价格和份数

删除套餐分类
  -> 409
  -> Category is referenced by setmeals

删除套餐中的菜品
  -> 409
  -> Dish is referenced by setmeals

修改套餐
  -> name = MySQL 家庭套餐
  -> price = 62.50
  -> 调整菜品份数

上架套餐
  -> status = 1

删除套餐
  -> 200
  -> setmeal_dish 关系同步清理

删除临时菜品
  -> 200
```

验收结束后的状态：

```text
setmeal_count = 0
setmeal_dish_count = 0
dish_count = 0
category_count = 2
```

真实 MySQL 服务继续运行，后端验收进程已重新启动。

### 13. 当前跨模块约束

```text
分类被菜品引用 -> 禁止删除分类
分类被套餐引用 -> 禁止删除分类
菜品被套餐引用 -> 禁止删除菜品
套餐被删除 -> 先删除套餐菜品关系
```

这些约束让模块之间不再孤立，开始形成真实业务系统的一致性规则。

### 14. 本步设计结论

```text
Setmeal 和 SetmealDish 是一个聚合
套餐分类必须 type = 2
套餐名称在分类内唯一
套餐菜品关系使用独立关联表
套餐主表和关联表写入处于同一事务
分页查询通过 JOIN 返回分类名称
详情查询通过 JOIN 返回菜品信息
删除套餐需要清理关联
分类和菜品的删除逻辑需要感知套餐引用
```

### 15. 自学检查点

1. 为什么套餐和菜品之间需要 `setmeal_dish` 关联表？
2. 为什么唯一索引是 `category_id + name`，而不是单独的 `name`？
3. 为什么新增套餐和新增套餐菜品关系需要同一个事务？
4. `copies` 字段表达什么业务含义？
5. 为什么删除套餐时需要先删除 `setmeal_dish`？
6. 为什么菜品被套餐引用后不能直接删除？
7. 当前“先删除再重新插入”的套餐菜品更新方式有什么优缺点？

---

## 2026-09-20：Step 7 - 图片上传

### 1. 本步目标

菜品和套餐目前只保存 `image` URL。本步实现真正的图片上传接口，让前端可以先把文件上传到服务器，再把返回 URL 保存到菜品或套餐。

接口：

```text
POST /admin/common/upload
Content-Type: multipart/form-data
form field: file
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": "/uploads/c3c2c9eac997462983957b426937f320.png"
}
```

### 2. 上传配置

`application.yml`：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 6MB

sky:
  upload:
    path: ${UPLOAD_PATH:./uploads}
    url-prefix: /uploads
    max-file-size: 5242880
```

各部分作用：

```text
spring.servlet.multipart.max-file-size
  -> Servlet 层限制单个文件大小

sky.upload.path
  -> 文件实际保存目录

sky.upload.url-prefix
  -> 文件对外访问 URL 前缀

sky.upload.max-file-size
  -> Service 层再次检查文件大小
```

默认目录是项目根目录下的：

```text
uploads/
```

该目录已加入 `.gitignore`，避免用户上传内容被 Git 跟踪。

### 3. 配置属性类

`UploadProperties`：

```java
@Component
@ConfigurationProperties(prefix = "sky.upload")
public class UploadProperties {

    private String path = "./uploads";
    private String urlPrefix = "/uploads";
    private long maxFileSize = 5 * 1024 * 1024;
}
```

使用 `@ConfigurationProperties` 比在业务代码中直接读取 `application.yml` 更清晰：

```text
配置集中绑定
类型安全
便于测试
不在 Service 中散落 @Value
```

### 4. 静态资源映射

上传后的文件保存在：

```text
./uploads
```

前端访问路径是：

```text
/uploads/文件名
```

两者通过 `UploadResourceConfig` 连接：

```java
Path uploadDirectory = Paths.get(uploadProperties.getPath())
        .toAbsolutePath()
        .normalize();

registry.addResourceHandler(uploadProperties.getUrlPrefix() + "/**")
        .addResourceLocations(uploadDirectory.toUri().toString());
```

调用链：

```text
GET /uploads/xxx.png
  |
  v
Spring MVC ResourceHandler
  |
  v
本地 uploads 目录
  |
  v
返回图片
```

### 5. 上传 Service

接口：

```java
public interface FileStorageService {

    String uploadImage(MultipartFile file);
}
```

实现步骤：

```text
检查文件非空
检查文件大小
检查扩展名
读取图片内容确认是真实图片
生成 UUID 文件名
创建上传目录
复制文件到目标目录
返回 /uploads/文件名
```

### 6. 文件校验

允许的扩展名：

```text
.jpg
.jpeg
.png
.gif
```

没有允许 SVG，因为 SVG 可以包含脚本，直接作为静态资源提供时会带来安全问题。

除了扩展名检查，还使用：

```java
ImageIO.read(inputStream)
```

确认文件内容确实能被解析为图片。

这样即使攻击者把文本文件改名成：

```text
avatar.png
```

仍然会返回：

```text
HTTP 400
File content is not a valid image
```

### 7. UUID 文件名

原始文件名不能直接用于磁盘路径，因为它可能包含：

```text
目录穿越
特殊字符
重名
敏感信息
```

因此只保留扩展名，主体使用 UUID：

```java
String filename = UUID.randomUUID()
        .toString()
        .replace("-", "") + extension;
```

例如：

```text
c3c2c9eac997462983957b426937f320.png
```

上传接口永远根据自己的规则生成路径，不信任客户端原始路径。

### 8. Controller

```java
@PostMapping("/upload")
public Result<String> upload(@RequestParam("file") MultipartFile file) {
    return Result.success(fileStorageService.uploadImage(file));
}
```

Controller 只负责：

```text
接收 multipart 文件
调用 Service
包装统一 Result
```

文件校验、目录和文件名生成都放在 Service。

### 9. 测试覆盖

新增：

```text
FileStorageServiceImplTest
CommonControllerTest
```

覆盖：

```text
正确保存 PNG
返回随机 URL
拒绝不支持的扩展名
拒绝伪装成 PNG 的文本
拒绝超过大小限制的文件
忽略原始路径中的 ../
Controller multipart 参数绑定
```

全量测试：

```text
Tests run: 79
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 10. 真实 HTTP 验收

上传真实 PNG：

```bash
curl -X POST http://127.0.0.1:8080/admin/common/upload \
  -F 'file=@/private/tmp/little-fables-desktop.png;type=image/png'
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": "/uploads/c3c2c9eac997462983957b426937f320.png"
}
```

磁盘检查：

```text
1.4 MB
PNG image data
1440 x 1000
```

访问返回 URL：

```text
HTTP 200
Content-Type: image/png
Content-Length: 1438584
```

不是图片的文件：

```text
HTTP 400
Only JPG, PNG, and GIF images are allowed
```

验收完成后，测试图片已删除。

### 11. 当前限制

当前实现适合单机学习和本地部署，但还缺少：

```text
对象存储
CDN
图片压缩
缩略图
多实例共享存储
文件引用计数或清理策略
上传权限控制
```

多实例部署时，如果每个实例都使用本地 `uploads` 目录，会出现“实例 A 上传，实例 B 读不到”的问题。

后续可以引入：

```text
MinIO
阿里云 OSS
AWS S3
```

### 12. 本步设计结论

```text
上传路径由服务端生成，不信任原始文件名
扩展名和真实图片内容都检查
Servlet 和 Service 两层限制文件大小
上传目录可配置且不进入 Git
文件通过 /uploads/** 静态访问
Controller 与文件存储逻辑分离
当前本地存储方案不等于生产级对象存储方案
```

### 13. 自学检查点

1. 为什么不能直接使用客户端的原始文件名保存文件？
2. 为什么只检查扩展名不够，还要读取图片内容？
3. `spring.servlet.multipart.max-file-size` 和 Service 的大小检查有什么区别？
4. 为什么上传目录要加入 `.gitignore`？
5. `/uploads/**` 为什么还需要资源映射配置？
6. 多实例部署时，本地 `uploads` 目录会产生什么问题？

---

## 2026-09-20：Step 8 - 管理端登录、JWT 与权限边界

### 1. 本步目标

前面所有管理端接口都可以在未登录状态下访问。本步建立最小可用的认证链路：

```text
用户名和密码登录
BCrypt 密码校验
签发 JWT
请求携带 JWT
拦截器校验 JWT
保存当前登录员工 ID
限制删除或禁用当前登录员工
```

### 2. 登录接口

```text
POST /admin/employee/login
Content-Type: application/json
```

请求：

```json
{
  "username": "admin",
  "password": "password"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "Admin",
    "token": "eyJhbGciOiJIUzM4NCJ9..."
  }
}
```

当前本地演示账号：

```text
admin / password
operator / password
```

这些密码只用于本地学习，不应作为生产凭据。

### 3. 登录调用链

```text
EmployeeController.login()
  |
  v
EmployeeServiceImpl.login()
  |
  +---- EmployeeMapper.selectByUsername()
  |
  +---- PasswordEncoder.matches()
  |
  +---- JwtUtil.generateToken()
  |
  v
EmployeeLoginVO
```

登录失败统一返回：

```text
HTTP 401
Invalid username or password
```

无论用户名不存在、密码错误还是账号被禁用，都返回同一条信息，避免向攻击者泄露账号是否存在。

### 4. 密码存储

实际 MySQL 中的密码字段保存 BCrypt 哈希：

```text
$2a$10$...
```

登录时不把数据库密码解密，而是：

```java
passwordEncoder.matches(rawPassword, encodedPassword)
```

BCrypt 会从哈希中读取盐和参数，对输入密码重新计算后比较。

### 5. JWT 依赖

新增：

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

职责：

```text
jjwt-api：编译期 API
jjwt-impl：JWT 解析和签名实现
jjwt-jackson：Claims JSON 序列化
```

### 6. JWT 配置

`application.yml`：

```yaml
sky:
  jwt:
    secret: ${JWT_SECRET:sky-takeout-learning-secret-change-before-production-2026}
    expiration-ms: ${JWT_EXPIRATION_MS:7200000}
  auth:
    enabled: ${AUTH_ENABLED:true}
```

含义：

```text
secret：HMAC 签名密钥
expiration-ms：Token 有效期，默认两小时
auth.enabled：是否启用管理端鉴权
```

生产环境必须通过 `JWT_SECRET` 提供独立高熵密钥，不能使用仓库中的默认值。

### 7. JWT 生成

```java
return Jwts.builder()
        .subject(String.valueOf(employeeId))
        .issuedAt(issuedAt)
        .expiration(expiration)
        .signWith(signingKey)
        .compact();
```

当前 Token 的 `sub` 保存员工 ID。

没有把密码、身份证号、手机号等敏感信息放进 JWT。JWT 只是签名，不是加密，客户端可以读取 Payload。

### 8. JWT 解析

```java
Claims claims = Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();

return Long.valueOf(claims.getSubject());
```

解析会同时验证：

```text
签名是否正确
Token 是否过期
Token 格式是否合法
```

### 9. 请求携带 Token

支持两种方式：

```text
Authorization: Bearer <token>
token: <token>
```

推荐使用标准形式：

```http
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

### 10. 登录拦截器

`LoginInterceptor` 在 Controller 之前执行：

```text
读取 Token
解析员工 ID
写入 CurrentUserContext
请求结束清理 ThreadLocal
```

如果缺少或无法解析 Token：

```text
HTTP 401
Unauthorized
```

拦截范围：

```text
/admin/**
```

排除：

```text
/admin/employee/login
```

所以登录接口可以匿名访问，其余管理端接口需要 Token。

### 11. 当前登录用户上下文

新增：

```java
CurrentUserContext
```

底层使用：

```java
ThreadLocal<Long>
```

请求进入时：

```java
CurrentUserContext.setUserId(employeeId);
```

请求结束时：

```java
CurrentUserContext.clear();
```

为什么必须清理：

```text
Tomcat 线程会被复用
如果不清理，下一个请求可能读到上一个用户的 ID
```

### 12. 审计字段升级

员工、分类、菜品、套餐和关联表写操作不再固定写入用户 1，而是：

```java
CurrentUserContext.getUserIdOrDefault()
```

真实 HTTP 请求中会使用 JWT 对应的员工 ID。

服务层单元测试和内部调用没有 HTTP 上下文时，默认回退到用户 1，避免测试和初始化流程失效。

### 13. 当前用户权限边界

不能禁用当前登录员工：

```text
HTTP 409
Cannot disable current employee
```

不能删除当前登录员工：

```text
HTTP 409
Cannot delete current employee
```

这防止管理员误操作后立刻失去自己的账号。

### 14. 测试策略

普通 HTTP 集成测试：

```text
application-test.yml 中 sky.auth.enabled=false
```

这样可以让已有 Controller 和业务测试专注验证参数、SQL 和响应，而不必为每个请求都准备 Token。

专门鉴权测试：

```text
AuthHttpIntegrationTest
spring property: sky.auth.enabled=true
```

覆盖：

```text
未携带 Token -> 401
正确账号密码 -> 返回 JWT
错误密码 -> 401
Bearer Token -> 访问成功
token Header -> 访问成功
禁用自己 -> 409
删除自己 -> 409
```

JWT 单元测试：

```text
正常生成和解析
过期 Token 被拒绝
```

### 15. 全量测试

```text
Tests run: 88
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 16. 真实 MySQL 和 HTTP 验收

真实 MySQL 中两个演示账号密码字段已更新为 BCrypt。

未登录访问：

```text
GET /admin/employee/page
-> 401 Unauthorized
```

登录：

```text
POST /admin/employee/login
-> 200
-> 返回 JWT
```

错误密码：

```text
POST /admin/employee/login
-> 401 Invalid username or password
```

携带 Token：

```text
GET /admin/category/page
Authorization: Bearer <token>
-> 200
```

禁用自己：

```text
POST /admin/employee/status/0?id=1
-> 409 Cannot disable current employee
```

删除自己：

```text
DELETE /admin/employee/1
-> 409 Cannot delete current employee
```

### 17. 当前限制

还没有实现：

```text
用户端微信登录
Refresh Token
退出登录黑名单
角色和细粒度权限
登录失败次数限制
验证码
Token 主动吊销
```

当前 JWT 是无状态 Token，签发后在过期前默认有效。

### 18. 当前安全边界

```text
密码使用 BCrypt
JWT 使用 HMAC 签名
JWT 只包含员工 ID
管理端接口需要 Token
登录接口匿名访问
当前用户写入审计字段
不能删除或禁用自己
上传图片接口位于 /admin/common/upload，受登录保护
```

### 19. 自学检查点

1. JWT 为什么不能保存密码等敏感数据？
2. 为什么用户名不存在和密码错误要返回相同提示？
3. 为什么拦截器需要使用 `ThreadLocal` 保存当前用户，又必须在请求结束后清理？
4. 为什么测试环境可以关闭鉴权，但仍需要单独的启用鉴权集成测试？
5. 当前 JWT 无状态方案为什么难以主动退出登录？
6. 生产环境为什么必须替换默认 JWT 密钥？

---

## 2026-09-20：Step 9 - 用户端微信登录

### 1. 本步目标

管理端使用用户名和密码登录。用户端通常使用微信小程序登录：

```text
小程序调用 wx.login()
  |
  v
获得临时 code
  |
  v
发送到后端 /user/login
  |
  v
后端请求微信 jscode2session
  |
  v
获得 openid
  |
  v
创建或查询 user_account
  |
  v
返回用户 JWT
```

本步完成除了真实微信凭据联调之外的完整代码链路。

### 2. user_account 表

```sql
CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    openid VARCHAR(64) NOT NULL,
    name VARCHAR(32) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    sex TINYINT DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_account_openid (openid)
);
```

没有把表命名为 `user`，因为 `USER` 在 MySQL 和 H2 中都可能是保留字。`user_account` 语义更明确，也避免数据库方言问题。

索引：

```text
openid 唯一
```

微信身份最重要的事实是：

```text
openid + 小程序 AppID
```

同一个微信用户在同一小程序下拥有稳定 openid。

### 3. 微信登录接口

```text
POST /user/login
```

请求：

```json
{
  "code": "wx.login 返回的临时 code"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "openid": "openid-value",
    "token": "user-jwt"
  }
}
```

### 4. 用户资料接口

```text
GET /user/profile
Authorization: Bearer <用户 Token>
```

当前返回：

```text
id
name
phone
sex
avatar
```

首次登录时这些资料可能为空，后续由用户授权流程逐步补充。

### 5. 微信配置

```yaml
sky:
  wechat:
    app-id: ${WECHAT_APP_ID:}
    app-secret: ${WECHAT_APP_SECRET:}
    api-url: ${WECHAT_API_URL:https://api.weixin.qq.com/sns/jscode2session}
    mock-enabled: ${WECHAT_MOCK_ENABLED:false}
```

真实部署时必须提供：

```text
WECHAT_APP_ID
WECHAT_APP_SECRET
```

仓库不保存真实 AppSecret。

### 6. 微信 API 客户端

`WeChatAuthServiceImpl` 使用 Spring `RestClient` 请求：

```text
GET https://api.weixin.qq.com/sns/jscode2session
```

查询参数：

```text
appid
secret
js_code
grant_type=authorization_code
```

微信成功响应中的关键字段：

```json
{
  "openid": "openid-value",
  "session_key": "session-key"
}
```

失败响应示例：

```json
{
  "errcode": 40029,
  "errmsg": "invalid code"
}
```

当前实现：

```text
errcode 非 0 -> HTTP 401
微信网络异常 -> HTTP 502
AppID 或 Secret 未配置 -> HTTP 503
```

### 7. 本地 mock 模式

真实微信联调需要小程序 AppID、AppSecret 和微信开发者工具。为了让后端逻辑可以独立测试，增加：

```yaml
sky:
  wechat:
    mock-enabled: true
```

启动示例：

```bash
WECHAT_MOCK_ENABLED=true ./mvnw spring-boot:run
```

mock 模式把：

```text
code = manual-demo
```

转换为：

```text
openid = mock-manual-demo
```

该模式只能用于本地开发，不能在生产环境开启。

### 8. 首次登录创建用户

用户登录流程：

```text
交换 code 得到 openid
根据 openid 查询 user_account
  |
  +-- 已存在：直接使用
  |
  +-- 不存在：插入新用户
  |
生成用户 JWT
```

`openid` 数据库唯一索引负责并发下的最终唯一性。

如果并发插入产生 `DuplicateKeyException`，Service 会再次查询已经由其他请求创建的用户。

### 9. 用户 JWT 与员工 JWT 隔离

JWT 增加：

```text
subjectType
```

员工 Token：

```json
{
  "sub": "1",
  "subjectType": "employee"
}
```

用户 Token：

```json
{
  "sub": "1",
  "subjectType": "user"
}
```

即使员工 ID 和用户 ID 都是 1，两者也不能互换使用。

管理端解析：

```java
jwtUtil.parseEmployeeId(token)
```

用户端解析：

```java
jwtUtil.parseUserId(token)
```

如果类型不匹配，拦截器返回：

```text
HTTP 401 Unauthorized
```

### 10. 用户请求拦截器

用户拦截范围：

```text
/user/**
```

排除：

```text
/user/login
```

登录接口匿名访问，其他用户接口需要用户 Token。

当前用户 ID 保存到：

```java
UserContext
```

底层同样使用 `ThreadLocal`，请求结束时必须清理。

### 11. Token 头部解析复用

管理端和用户端都支持：

```text
Authorization: Bearer <token>
token: <token>
```

Token 提取逻辑提取到：

```text
AuthTokenResolver
```

避免员工拦截器和用户拦截器复制相同代码。

### 12. 测试覆盖

新增：

```text
WeChatAuthServiceImplTest
UserServiceImplTest
UserControllerTest
UserHttpIntegrationTest
```

覆盖：

```text
微信 API 成功响应
微信错误码响应
本地 mock 模式
已存在用户登录
首次登录创建用户
用户资料接口
用户 JWT 访问 /user/profile
员工 Token 不能访问用户接口
用户 Token 不能访问管理端接口
```

JWT 单元测试还覆盖了：

```text
员工 Token 不能作为用户 Token
用户 Token 不能作为员工 Token
```

全量测试：

```text
Tests run: 97
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 13. 真实 MySQL 验收

真实数据库新增：

```text
user_account
```

使用：

```text
WECHAT_MOCK_ENABLED=true
```

完成验收：

```text
POST /user/login
code = manual-demo
-> 200
-> openid = mock-manual-demo
-> 创建 user_account id = 1
-> 返回用户 JWT

GET /user/profile
Authorization: Bearer <用户 JWT>
-> 200

再次使用相同 code 登录
-> 仍返回 user id = 1
-> 没有重复创建用户

员工 JWT 访问 /user/profile
-> 401

用户 JWT 访问 /admin/employee/page
-> 401
```

验收完成后已删除临时 mock 用户。

### 14. 当前未完成项

真实微信登录还没有使用线上 AppID/AppSecret 进行联调。

原因是需要：

```text
微信小程序账号
有效 AppID
有效 AppSecret
微信开发者工具或真实小程序
```

当前已完成：

```text
代码调用链
配置项
错误处理
mock 模式
用户落库
用户 JWT
接口身份隔离
```

因此 PLAN 中该项标记为进行中，而不是完成。

### 15. 安全注意事项

```text
AppSecret 不能提交到 Git
生产环境不能开启 mock
session_key 不返回给客户端
openid 不直接代替登录 Token
用户 Token 和管理员 Token 必须区分
```

当前没有保存 `session_key`，因为后续还没实现微信敏感数据解密。

### 16. 自学检查点

1. 微信登录为什么必须先由小程序换取临时 code？
2. `openid` 和 `session_key` 分别有什么作用？
3. 为什么 openid 不直接作为系统登录 Token？
4. 为什么 employee Token 和 user Token 要增加 subjectType？
5. 为什么生产环境不能开启 `WECHAT_MOCK_ENABLED`？
6. AppSecret 为什么不能保存到 Git？

---

## 2026-09-20：Step 10 - 用户端菜品与套餐浏览

### 1. 本步目标

进入 Phase 3 后，先完成用户查看菜单的基础链路：

```text
浏览启用分类
浏览分类下已上架菜品
浏览分类下已上架套餐
查看已上架套餐详情
```

管理端可以查看全部状态数据，用户端只能看到：

```text
category.status = 1
dish.status = 1
setmeal.status = 1
```

### 2. 用户端接口

分类：

```text
GET /user/category/list?type=1
GET /user/category/list?type=2
```

菜品：

```text
GET /user/dish/list?categoryId=1
```

套餐：

```text
GET /user/setmeal/list?categoryId=2
GET /user/setmeal/{id}
```

### 3. 匿名浏览

浏览菜单不需要登录，因此用户拦截器新增排除：

```text
/user/category/**
/user/dish/**
/user/setmeal/**
```

仍然受保护：

```text
/user/profile
```

这样未登录用户可以浏览菜单，只有进入用户资料、购物车和订单流程时才需要用户 Token。

### 4. 分类过滤

新增 Mapper：

```java
List<Category> selectEnabledByType(Integer type);
```

SQL：

```sql
SELECT id,
       type,
       name,
       sort,
       status,
       update_time
FROM category
WHERE type = #{type}
  AND status = 1
ORDER BY sort ASC, update_time DESC, id DESC
```

用户端永远看不到禁用分类。

### 5. 菜品过滤

新增：

```java
List<Dish> selectEnabledByCategoryId(Long categoryId);
```

SQL：

```sql
WHERE category_id = #{categoryId}
  AND status = 1
```

如果分类不存在或已禁用，接口返回：

```text
HTTP 404
Category not found
```

如果分类类型不匹配，例如用套餐分类查询菜品：

```text
HTTP 400
Category type mismatch
```

### 6. 用户菜品 VO

用户端菜品列表返回：

```text
id
name
price
image
description
flavors
```

没有返回：

```text
status
categoryId
createTime
updateTime
createUser
updateUser
```

用户端只接收当前页面真正需要的数据。

### 7. 批量查询口味

如果菜品列表有 20 道菜，逐道查询口味会产生 20 次 SQL，也就是 N+1 查询问题。

本步增加：

```java
List<DishFlavor> selectFlavorsByDishIds(List<Long> dishIds);
```

SQL 使用：

```sql
WHERE dish_id IN
<foreach collection="dishIds" item="dishId" open="(" separator="," close=")">
    #{dishId}
</foreach>
```

Service 一次性查询所有口味，再按 `dishId` 分组：

```text
1 次菜品查询
1 次口味批量查询
```

而不是：

```text
1 次菜品查询
N 次口味查询
```

### 8. 套餐过滤

新增：

```java
List<SetmealPageVO> selectEnabledByCategoryId(Long categoryId);
```

SQL：

```sql
WHERE s.category_id = #{categoryId}
  AND s.status = 1
```

套餐详情同样只返回启用数据。

禁用套餐：

```text
HTTP 404
Setmeal not found
```

### 9. 用户浏览 Service

新增：

```text
UserCatalogService
UserCatalogServiceImpl
UserCatalogController
```

职责：

```text
校验分类类型
过滤禁用分类
过滤停售菜品
过滤停售套餐
批量组装口味
返回用户端 VO
```

没有复用管理端分页接口，因为两端需求不同：

```text
管理端：分页、状态过滤、增删改
用户端：按分类展示启用数据
```

### 10. 测试覆盖

新增：

```text
UserCatalogServiceImplTest
UserCatalogControllerTest
UserCatalogIntegrationTest
UserCatalogHttpIntegrationTest
```

覆盖：

```text
口味批量映射
禁用分类返回 404
禁用套餐详情返回 404
用户端分类列表
用户端菜品列表
用户端套餐列表
只返回已启用菜品和套餐
匿名浏览不需要 Token
/user/profile 仍然需要 Token
```

全量测试：

```text
Tests run: 105
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 11. 真实 MySQL 验收

创建临时数据：

```text
启用菜品
停售菜品
包含启用菜品的启用套餐
停售套餐
```

公开接口验证：

```text
GET /user/category/list?type=1
-> 200
-> 只返回启用分类

GET /user/dish/list?categoryId=1
-> 200
-> 只返回启用菜品

GET /user/setmeal/list?categoryId=2
-> 200
-> 只返回启用套餐

GET /user/setmeal/{enabledId}
-> 200
-> 返回套餐菜品详情

GET /user/setmeal/{disabledId}
-> 404
```

验收完成后清理：

```text
dish_count = 0
flavor_count = 0
setmeal_count = 0
setmeal_dish_count = 0
user_count = 0
```

### 12. 本步设计结论

```text
匿名可以浏览菜单
用户端只展示启用数据
管理端和用户端使用不同查询语义
口味使用批量查询避免 N+1
套餐详情检查套餐状态
分类类型不匹配返回 400
分类禁用或不存在返回 404
```

### 13. 自学检查点

1. 为什么浏览菜单可以匿名，但用户资料需要 Token？
2. 为什么用户端不能直接复用管理端分页接口？
3. 什么是 N+1 查询，批量查询如何解决它？
4. 为什么禁用分类在用户端应表现为 404，而不是继续返回空列表？
5. 用户 VO 为什么不返回数据库审计字段？

---

## 2026-09-20：Step 11 - 用户购物车

### 1. 本步目标

购物车连接“浏览商品”和“下单”：

```text
用户浏览菜品或套餐
  |
  v
加入购物车
  |
  v
修改数量
  |
  v
清空或进入订单
```

本步完成：

```text
添加购物车
查询购物车
减少数量
直接修改数量
清空购物车
登录用户数据隔离
```

### 2. shopping_cart 表

```sql
CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    dish_id BIGINT UNSIGNED DEFAULT NULL,
    setmeal_id BIGINT UNSIGNED DEFAULT NULL,
    quantity INT NOT NULL DEFAULT 1,
    flavor VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_shopping_cart_user_dish (user_id, dish_id),
    UNIQUE KEY uk_shopping_cart_user_setmeal (user_id, setmeal_id),
    KEY idx_shopping_cart_user_id (user_id)
);
```

设计规则：

```text
一个用户对同一菜品最多一条购物车记录
一个用户对同一套餐最多一条购物车记录
dish_id 和 setmeal_id 同时只会有一个有值
quantity 表示购买数量
flavor 保存菜品口味描述
```

### 3. 接口

添加：

```text
POST /user/shoppingCart/add
```

减少：

```text
POST /user/shoppingCart/sub
```

直接修改数量：

```text
POST /user/shoppingCart/update
```

查询：

```text
GET /user/shoppingCart/list
```

清空：

```text
DELETE /user/shoppingCart/clean
```

全部接口都需要用户 JWT。

### 4. 添加请求

菜品：

```json
{
  "dishId": 10,
  "quantity": 2,
  "flavor": "Mild"
}
```

套餐：

```json
{
  "setmealId": 5,
  "quantity": 1
}
```

`dishId` 和 `setmealId` 必须严格二选一：

```text
只有 dishId -> 合法
只有 setmealId -> 合法
两者都没有 -> 400
两者都有 -> 400
```

### 5. 商品状态校验

加入购物车前检查：

```text
菜品存在且 status = 1
或者
套餐存在且 status = 1
```

停售商品不能加入：

```text
HTTP 400
Dish is not available
```

或：

```text
HTTP 400
Setmeal is not available
```

### 6. 重复加购

第一次添加：

```text
不存在记录 -> 插入新记录
```

再次添加同一商品：

```text
存在记录 -> quantity 累加
```

例如：

```text
第一次 quantity = 2
第二次 quantity = 1
结果 quantity = 3
```

数量上限：

```text
99
```

超过上限返回 400。

### 7. 减少数量

```text
quantity > 1 -> quantity - 1
quantity = 1 -> 删除购物车记录
```

不会出现数量为 0 或负数的记录。

### 8. 修改数量

用户可以直接把数量改为指定值：

```json
{
  "id": 1,
  "quantity": 4
}
```

必须同时满足：

```text
购物车记录属于当前登录用户
quantity 在 1 到 99 之间
```

### 9. 当前用户数据隔离

Service 不接收客户端传入的 `userId`，而是从：

```java
UserContext.getRequiredUserId()
```

获取当前用户。

所有查询和修改 SQL 都带：

```sql
WHERE user_id = #{userId}
```

这防止用户通过修改请求参数读取或修改他人的购物车。

### 10. 购物车列表

Mapper 使用 LEFT JOIN 同时读取菜品和套餐：

```sql
SELECT sc.id,
       COALESCE(sc.dish_id, sc.setmeal_id) AS product_id,
       CASE
           WHEN sc.dish_id IS NOT NULL THEN 'dish'
           ELSE 'setmeal'
       END AS product_type,
       COALESCE(d.name, s.name) AS name,
       COALESCE(d.image, s.image) AS image,
       COALESCE(d.price, s.price) AS unit_price,
       sc.quantity,
       sc.flavor
FROM shopping_cart sc
LEFT JOIN dish d ON d.id = sc.dish_id
LEFT JOIN setmeal s ON s.id = sc.setmeal_id
WHERE sc.user_id = #{userId}
```

金额由 Service 计算：

```java
amount = unitPrice * quantity
```

没有把金额持久化到购物车表，因为价格和商品信息已经保存在菜品或套餐表中。

### 11. 事务边界

写操作使用：

```java
@Transactional
```

包括：

```text
添加购物车
减少购物车
修改数量
清空购物车
```

购物车写入需要和商品状态检查保持在同一业务操作中。

### 12. 测试覆盖

新增：

```text
ShoppingCartServiceImplTest
ShoppingCartControllerTest
ShoppingCartIntegrationTest
ShoppingCartHttpIntegrationTest
```

覆盖：

```text
新增购物车
重复加购累加
dishId 和 setmealId 校验
减少到 0 时删除
直接修改数量
金额计算
清空购物车
数据隔离
登录用户 HTTP 购物车生命周期
```

全量测试：

```text
Tests run: 116
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 13. 真实 MySQL 验收

使用 mock 用户登录，并创建临时上架菜品。

完成：

```text
第一次加购 quantity = 2
第二次加购 quantity = 1
购物车 quantity = 3
amount = 78.00

减少一次
quantity = 2

直接修改 quantity = 4
amount = 104.00

清空购物车
列表为空
```

直接检查真实 MySQL：

```text
user_id = 2
dish_id = 6
setmeal_id = NULL
quantity = 4
flavor = Mild
```

验收完成后清理：

```text
shopping_cart = 0
dish = 0
dish_flavor = 0
setmeal = 0
setmeal_dish = 0
user_account = 0
```

### 14. 本步设计结论

```text
购物车绑定 JWT 用户
一个用户对同一商品只有一条记录
重复加购使用数量累加
数量减少到 0 时删除
停售商品不能加入
商品校验和购物车写入使用事务
列表通过 JOIN 同时支持菜品和套餐
金额由单价乘数量动态计算
所有查询都带 user_id 防止越权
```

### 15. 自学检查点

1. 为什么购物车接口不能接受客户端传入 userId？
2. 为什么同一个用户和菜品需要唯一索引？
3. 为什么减少到 0 时删除记录，而不是保存 quantity = 0？
4. 为什么购物车金额不适合直接持久化？
5. 为什么添加购物车前还需要重新检查商品是否上架？
6. `COALESCE` 和 `CASE` 在同时支持菜品与套餐时起什么作用？

---

## 2026-09-20：Step 12 - 用户收货地址

### 1. 本步目标

收货地址属于用户私有数据，主要用于下单时选择配送地址。

本步完成：

```text
新增地址
地址列表
地址详情
修改地址
设置默认地址
查询默认地址
删除地址
```

所有接口都需要用户 JWT。

### 2. address_book 表

```sql
CREATE TABLE IF NOT EXISTS address_book (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    consignee VARCHAR(32) NOT NULL,
    sex TINYINT NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province_name VARCHAR(32) NOT NULL,
    city_name VARCHAR(32) NOT NULL,
    district_name VARCHAR(32) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    label VARCHAR(32) DEFAULT NULL,
    is_default TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_address_book_user_id (user_id),
    KEY idx_address_book_user_default (user_id, is_default)
);
```

字段含义：

```text
user_id：地址所属用户
consignee：收货人
sex：性别
phone：手机号
province_name / city_name / district_name：省市区
detail：详细地址
label：自定义标签，例如 Home 或 Office
is_default：是否为默认地址
```

### 3. 接口

```text
POST   /user/addressBook
GET    /user/addressBook/list
GET    /user/addressBook/default
GET    /user/addressBook/{id}
PUT    /user/addressBook
PUT    /user/addressBook/default/{id}
DELETE /user/addressBook/{id}
```

### 4. 首个地址自动默认

新增地址时：

```text
当前用户没有地址 -> is_default = 1
当前用户已有地址 -> 使用请求中的 is_default
```

这保证用户第一次新增地址后，下单流程可以直接拿到默认地址。

### 5. 默认地址唯一

设置默认地址前，先执行：

```sql
UPDATE address_book
SET is_default = 0
WHERE user_id = #{userId}
  AND is_default = 1
```

然后设置目标地址：

```sql
UPDATE address_book
SET is_default = 1
WHERE id = #{id}
  AND user_id = #{userId}
```

两个 SQL 位于同一个事务，避免出现：

```text
两个默认地址
设置过程中暂时没有默认地址且事务失败
```

### 6. 删除默认地址

删除默认地址后：

```text
查询该用户剩余地址
选择一条作为新的默认地址
```

当前规则是按更新时间倒序选择第一条。

这样不会出现“用户还有地址，但一个默认地址都没有”的状态。

### 7. 用户隔离

所有查询必须同时带：

```sql
WHERE id = #{id}
  AND user_id = #{userId}
```

不能只按地址 ID 查询或修改，否则用户可能越权访问别人的地址。

例如详情、修改、设置默认和删除都调用：

```java
requireAddress(id)
```

内部使用：

```text
id + 当前 JWT userId
```

### 8. 参数校验

主要约束：

```text
收货人不能为空，最多 32 字符
sex 只能为 1 或 2
phone 必须是 11 位手机号
省、市、区不能为空
详细地址不能为空，最多 255 字符
标签最多 32 字符
is_default 只能为 0 或 1
```

### 9. Service 事务

以下操作使用：

```java
@Transactional
```

```text
新增地址
修改地址
设置默认地址
删除地址并提升新默认地址
```

新增和默认切换涉及多条 SQL，不能只依赖单条 Mapper 操作的事务。

### 10. 测试覆盖

新增：

```text
AddressBookServiceImplTest
AddressBookControllerTest
AddressBookIntegrationTest
AddressBookHttpIntegrationTest
```

覆盖：

```text
首条地址自动默认
后续地址保持非默认
访问他人地址返回 404
删除默认地址后提升剩余地址
Controller CRUD
真实 H2 数据库地址生命周期
登录用户 HTTP 地址管理
```

全量测试：

```text
Tests run: 125
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 11. 真实 MySQL 验收

使用 mock 用户登录并创建两条地址：

```text
第一条 Alice，isDefault = 0
-> 自动成为默认

第二条 Bob，isDefault = 1
-> 第一条被清除默认
-> 第二条成为默认

修改第一条地址
-> 默认仍为第二条

将第一条设为默认
-> 第二条失去默认状态

删除第一条
-> 第二条自动提升为默认
```

直接检查数据库后确认：

```text
address_count = 0
user_count = 0
```

验收数据已清理。

### 12. 本步设计结论

```text
地址始终绑定 JWT 用户
所有查询同时使用 id 和 user_id
首个地址自动默认
一个用户最多一个默认地址
默认切换和删除提升使用事务
删除默认地址后自动选择剩余地址
地址表保存用户当前配送信息
```

### 13. 自学检查点

1. 为什么地址查询不能只使用地址 ID？
2. 为什么第一个地址应该自动成为默认地址？
3. 为什么设置默认地址需要先清除旧默认值？
4. 为什么删除默认地址后要重新选择一条默认地址？
5. 为什么地址写操作需要事务？
6. `(user_id, is_default)` 索引对哪些查询有帮助？

---

## 2026-09-20：Step 13 - 订单、支付模拟与状态流转

### 1. 本步目标

本步完成用户端订单闭环：

```text
购物车结算
生成订单和订单明细快照
模拟支付
分页查询历史订单
查询订单详情
取消订单
已支付订单取消时记录退款状态
```

本步完成后，Phase 3 的用户端业务全部具备代码和测试覆盖：

```text
菜品浏览
套餐浏览
购物车
收货地址
下单
支付状态模拟
订单查询与状态流转
```

### 2. 接口

所有订单接口都位于 `/user/order`，并需要用户 JWT：

```text
POST /user/order/submit
PUT  /user/order/payment
GET  /user/order/history
GET  /user/order/detail/{id}
PUT  /user/order/cancel/{id}
```

请求示例：

```json
POST /user/order/submit
{
  "addressBookId": 1,
  "payMethod": 1,
  "remark": "Less spicy"
}
```

`addressBookId` 可以省略。省略时，服务端使用当前用户的默认地址。

支付请求：

```json
PUT /user/order/payment
{
  "orderNumber": "20260920175320520475"
}
```

取消请求：

```json
PUT /user/order/cancel/1
{
  "reason": "Changed plans"
}
```

### 3. 两张表的分工

订单数据拆成两张表：

```text
orders
order_detail
```

`orders` 保存订单主体：

```text
number：业务订单号
status：订单状态
user_id：订单所属用户
address_book_id：下单时使用的地址 ID
order_time：下单时间
checkout_time：支付完成时间
pay_method：支付方式
pay_status：支付状态
amount：订单总金额
remark：订单备注
phone / address / consignee：收货信息快照
cancel_reason / cancel_time：取消信息
```

`order_detail` 保存订单明细快照：

```text
order_id：所属订单
name：下单时的商品名称
image：下单时的商品图片
dish_id：菜品 ID，套餐明细为 NULL
setmeal_id：套餐 ID，菜品明细为 NULL
dish_flavor：下单时选择的口味
number：购买数量
amount：该明细的成交金额
```

这里最重要的设计是订单快照。

订单明细保存的是下单时的名称、图片、口味、单价计算结果和数量，而不是每次查询都重新读取 `dish` 或 `setmeal`。这样即使管理员之后修改菜品名称、价格或图片，历史订单仍然展示用户下单时的内容。

### 4. 状态定义

订单状态 `orders.status`：

```text
1：待支付
2：待接单
6：已取消
```

支付状态 `orders.pay_status`：

```text
0：未支付
1：已支付
2：退款
```

当前支付接口是模拟支付。它不连接微信支付，只执行状态流转：

```text
待支付且未支付 -> 已支付且待接单
```

也就是：

```text
status: 1 -> 2
pay_status: 0 -> 1
checkout_time: NULL -> CURRENT_TIMESTAMP
```

### 5. 下单主流程

`OrderServiceImpl.submit` 的执行顺序：

```text
1. 从 UserContext 获取当前用户 ID
2. 确认收货地址属于当前用户
3. 读取当前用户购物车
4. 购物车为空则返回 400
5. 遍历购物车，重新读取菜品或套餐
6. 检查商品仍然存在且状态为上架
7. 使用数据库中的最新价格重新计算金额
8. 生成订单主体和订单明细快照
9. 写入 orders
10. 回填 order_id 后批量写入 order_detail
11. 清空该用户购物车
```

下单不能直接相信购物车里的旧价格。购物车只保存商品 ID、数量、口味等选择信息，结算时必须以数据库中的最新菜品或套餐价格为准。

### 6. 事务边界

下单方法使用：

```java
@Transactional
```

一次下单至少涉及：

```text
INSERT orders
INSERT order_detail
DELETE shopping_cart
```

如果订单明细写入失败，不能留下一个没有明细的订单，也不能提前清空购物车。因此这些操作必须处于同一个事务。

支付和取消同样使用事务。尤其是取消订单，它同时修改订单状态、支付状态、取消原因和取消时间。

### 7. 订单号生成

订单号由以下内容拼成：

```text
yyyyMMddHHmmss + 6 位随机数
```

示例：

```text
20260920175320520475
```

`orders.number` 有唯一索引。如果极小概率发生订单号冲突，服务层会捕获重复键异常并重新生成，最多尝试三次。

注意事项：

```text
当前方案适合学习和单机开发
高并发生产系统应使用雪花 ID、号段服务或其他全局唯一方案
```

### 8. 支付状态机

支付前先按订单号和当前用户查询：

```sql
SELECT ...
FROM orders
WHERE number = #{number}
  AND user_id = #{userId}
```

只有在以下条件同时满足时才能支付：

```text
status = 1
pay_status = 0
```

真正的更新仍然带条件：

```sql
UPDATE orders
SET status = 2,
    pay_status = 1,
    checkout_time = CURRENT_TIMESTAMP
WHERE id = #{id}
  AND user_id = #{userId}
  AND status = 1
  AND pay_status = 0
```

这叫做条件更新。即使两个支付请求同时到达，数据库也只会让其中一个更新成功。

已经被取消的订单再次支付会返回 HTTP `409 Conflict`。

### 9. 历史订单分页

历史查询参数：

```text
page：从 1 开始
pageSize：默认 10，最大 100
status：可选，1 到 6
```

查询过程分两步：

```text
1. COUNT 当前用户的订单总数
2. 分页查询当前用户订单
3. 批量查询这些订单的全部明细
4. 在内存中按 order_id 分组并组装 OrderVO
```

明细使用一条 `WHERE order_id IN (...)` 查询，而不是对每条订单循环查询一次。这样避免了 N+1 查询问题。

排序规则：

```sql
ORDER BY order_time DESC, id DESC
```

同一秒创建的订单也可以用 `id` 保持稳定顺序。

### 10. 用户隔离

订单是用户私有数据。所有查询和修改都必须同时使用：

```text
订单 ID 或订单号
+ 当前 JWT 中的 userId
```

详情、支付、取消都使用这类条件。

真实数据库验收中，使用另一个 mock 用户访问第一个用户的订单详情，服务端返回 HTTP `404`，证明不能跨用户读取订单。

### 11. 取消与退款模拟

只有以下订单可以取消：

```text
status = 1：待支付
status = 2：待接单
```

取消后统一进入：

```text
status = 6：已取消
```

支付状态按原状态处理：

```text
原 pay_status = 0 -> 新 pay_status = 0
原 pay_status = 1 -> 新 pay_status = 2
```

因此未支付订单取消后是“已取消且未支付”，已支付订单取消后是“已取消且退款”。

取消更新同样使用条件更新，只允许从状态 `1` 或 `2` 转入状态 `6`。

### 12. 核心文件

```text
接口层：
src/main/java/com/sky/takeout/controller/user/OrderController.java

服务接口：
src/main/java/com/sky/takeout/service/OrderService.java

业务实现：
src/main/java/com/sky/takeout/service/impl/OrderServiceImpl.java

数据访问接口：
src/main/java/com/sky/takeout/mapper/OrderMapper.java

MyBatis SQL：
src/main/resources/mapper/OrderMapper.xml

实体：
src/main/java/com/sky/takeout/entity/Order.java
src/main/java/com/sky/takeout/entity/OrderDetail.java

请求对象：
src/main/java/com/sky/takeout/dto/OrderSubmitDTO.java
src/main/java/com/sky/takeout/dto/OrderPaymentDTO.java
src/main/java/com/sky/takeout/dto/OrderCancelDTO.java
src/main/java/com/sky/takeout/dto/OrderPageQueryDTO.java

响应对象：
src/main/java/com/sky/takeout/vo/OrderSubmitVO.java
src/main/java/com/sky/takeout/vo/OrderVO.java
src/main/java/com/sky/takeout/vo/OrderItemVO.java
```

### 13. 自动化测试

本步增加了四层测试：

```text
OrderControllerTest：
验证接口路径、请求校验、参数传递和响应结构。

OrderServiceImplTest：
验证空购物车、订单创建、金额计算、购物车清空、支付和退款状态。

OrderIntegrationTest：
使用 H2 验证完整订单生命周期。

OrderHttpIntegrationTest：
通过随机端口验证 JWT、HTTP 请求、下单、支付和查询。
```

全量测试结果：

```text
Tests run: 135
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 14. 真实 MySQL 验收

先把最新表结构应用到本机 MySQL：

```bash
mysql \
  --protocol=TCP \
  -h127.0.0.1 \
  -P3306 \
  -uroot \
  sky_takeout \
  < sql/schema.sql
```

连接密码通过环境变量传入，不要把明文密码写进仓库：

```bash
export MYSQL_PWD='<your-mysql-password>'
```

使用真实 MySQL 和 mock 微信登录启动：

```bash
DB_URL='jdbc:mysql://127.0.0.1:3306/sky_takeout?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true' \
DB_USERNAME=root \
DB_PASSWORD='<your-mysql-password>' \
WECHAT_MOCK_ENABLED=true \
./mvnw spring-boot:run
```

验收步骤和结果：

```text
管理端登录 admin/password
-> 成功

创建临时上架菜品，单价 18.50
-> 成功

mock 用户登录
-> 成功

创建默认收货地址
-> 成功

加入购物车，数量 2
-> 成功

提交订单
-> 金额 37.00
-> 订单状态 1，待支付
-> 支付状态 0，未支付
-> 订单明细 1 条，数量 2
-> 购物车已清空

模拟支付
-> 订单状态 2，待接单
-> 支付状态 1，已支付
-> checkout_time 已写入

查询历史订单
-> 分页结果包含订单和订单明细

取消已支付订单
-> 订单状态 6，已取消
-> 支付状态 2，退款
-> cancel_reason 已保存

再次支付已取消订单
-> HTTP 409

另一个用户查询该订单
-> HTTP 404
```

直接查询 MySQL 后确认订单和明细正确落盘。验收结束后清理：

```text
order_detail
orders
shopping_cart
address_book
user_account
dish_flavor
dish
```

清理后相关业务表计数全部为 0。

### 15. 本步设计结论

```text
购物车是结算输入，不是订单价格来源
订单主体和订单明细分离
订单明细保存不可变的历史快照
下单、支付、取消都围绕明确的状态机
支付和取消使用条件更新防止并发重复流转
所有订单操作同时绑定当前 JWT 用户
历史查询使用分页和批量明细查询
金额使用 BigDecimal，避免浮点精度问题
```

### 16. 自学检查点

1. 为什么订单明细要保存名称和价格快照，而不是只保存商品 ID？
2. 为什么提交订单时必须重新读取菜品和套餐价格？
3. 为什么 `INSERT orders`、`INSERT order_detail` 和清空购物车必须在同一事务？
4. 条件更新如何防止两个支付请求同时成功？
5. 为什么已支付订单取消后要把支付状态改成退款，而不是直接清零？
6. 订单查询为什么必须同时使用订单 ID 和当前用户 ID？
7. 订单号使用时间加随机数有什么并发风险？生产环境可以采用哪些替代方案？
8. 为什么历史订单要批量查询明细，避免 N+1 查询？

---

## 2026-09-20：Step 14 - 管理端订单闭环

### 1. 本步目标

上一阶段完成了用户侧订单接口。用户能够下单、支付、查询和取消，但门店管理员还没有办法处理订单。

本步补齐管理端订单生命周期：

```text
按条件分页查询订单
查询订单详情
接单
拒单并退款
管理员取消并退款
派送
完成
查询订单统计
```

本步还修复了用户支付和取消并发时可能覆盖支付状态的问题。

### 2. 六个订单状态

管理端接入后，订单状态扩展为：

```text
1：待支付
2：待接单
3：已接单
4：派送中
5：已完成
6：已取消
```

正常履约路径：

```text
1 待支付
-> 支付
2 待接单
-> 管理员接单
3 已接单
-> 派送
4 派送中
-> 完成
5 已完成
```

异常路径：

```text
待支付 -> 管理员取消 -> 已取消
待接单 -> 管理员拒单 -> 已取消
待接单 -> 管理员取消 -> 已取消
已接单 -> 管理员取消 -> 已取消
派送中 -> 管理员取消 -> 已取消
```

支付状态仍然保持：

```text
0：未支付
1：已支付
2：退款
```

当已支付订单进入拒单或管理员取消流程时，`pay_status` 会变成 `2`。

### 3. 管理端接口

```text
GET /admin/order/conditionSearch
GET /admin/order/statistics
GET /admin/order/details/{id}
PUT /admin/order/confirm
PUT /admin/order/rejection
PUT /admin/order/cancel
PUT /admin/order/delivery/{id}
PUT /admin/order/complete/{id}
```

分页查询参数：

```text
page：页码，从 1 开始
pageSize：每页数量，最大 100
number：订单号，模糊匹配
phone：手机号，模糊匹配
status：订单状态，1 到 6
beginTime：下单开始时间
endTime：下单结束时间
```

时间格式：

```text
yyyy-MM-dd HH:mm:ss
```

接单请求：

```json
PUT /admin/order/confirm
{
  "id": 1
}
```

拒单请求：

```json
PUT /admin/order/rejection
{
  "id": 1,
  "reason": "Out of stock"
}
```

管理员取消请求：

```json
PUT /admin/order/cancel
{
  "id": 1,
  "reason": "Store closing"
}
```

派送和完成只需要订单 ID：

```text
PUT /admin/order/delivery/{id}
PUT /admin/order/complete/{id}
```

所有 `/admin/**` 接口都经过管理端 JWT 拦截器。未带 Token 请求统计接口会返回 HTTP `401`。

### 4. 核心文件

```text
Controller：
src/main/java/com/sky/takeout/controller/admin/AdminOrderController.java

Service：
src/main/java/com/sky/takeout/service/AdminOrderService.java
src/main/java/com/sky/takeout/service/impl/AdminOrderServiceImpl.java

Mapper：
src/main/java/com/sky/takeout/mapper/OrderMapper.java
src/main/resources/mapper/OrderMapper.xml

请求 DTO：
src/main/java/com/sky/takeout/dto/AdminOrderPageQueryDTO.java
src/main/java/com/sky/takeout/dto/OrderConfirmDTO.java
src/main/java/com/sky/takeout/dto/OrderRejectionDTO.java
src/main/java/com/sky/takeout/dto/OrderAdminCancelDTO.java

响应 VO：
src/main/java/com/sky/takeout/vo/OrderStatisticsVO.java
```

没有把所有逻辑继续塞进用户侧 `OrderServiceImpl`。管理端查询、筛选和状态流转有独立业务边界，因此新增了 `AdminOrderService`，但两边共用 `OrderMapper` 和订单实体。

### 5. 管理端动态查询

分页查询有两个 SQL：

```text
countByAdminQuery：查询总数
selectAdminPage：查询当前页
```

筛选条件使用 MyBatis `<where>` 和 `<if>`：

```xml
<where>
    <if test="query.number != null and query.number != ''">
        AND number LIKE CONCAT('%', #{query.number}, '%')
    </if>
    <if test="query.phone != null and query.phone != ''">
        AND phone LIKE CONCAT('%', #{query.phone}, '%')
    </if>
    <if test="query.status != null">
        AND status = #{query.status}
    </if>
    <if test="query.beginTime != null">
        AND order_time &gt;= #{query.beginTime}
    </if>
    <if test="query.endTime != null">
        AND order_time &lt;= #{query.endTime}
    </if>
</where>
```

Service 层会先做一次规范化：

```text
page 小于 1 -> 1
pageSize 小于 1 -> 1
pageSize 大于 100 -> 100
number 和 phone 去除首尾空格
空字符串转 NULL
beginTime 晚于 endTime -> HTTP 400
```

### 6. 条件更新

订单状态流转不能只依赖 Service 中的状态判断。因为两个请求可能同时读到同一个旧状态。

真正的状态修改都带前置状态条件。

接单：

```sql
UPDATE orders
SET status = 3
WHERE id = #{id}
  AND status = 2
```

派送：

```sql
UPDATE orders
SET status = 4
WHERE id = #{id}
  AND status = 3
```

完成：

```sql
UPDATE orders
SET status = 5
WHERE id = #{id}
  AND status = 4
```

这三个动作共用一个 Mapper：

```java
int transitionStatus(
        Long id,
        Integer expectedStatus,
        Integer targetStatus
);
```

如果更新影响行数不是 `1`，说明订单不存在或状态已经被其他请求改变，接口返回 HTTP `409 Conflict`。

### 7. 拒单与退款

拒单只允许从状态 `2` 开始：

```sql
UPDATE orders
SET status = 6,
    pay_status = CASE WHEN pay_status = 1 THEN 2 ELSE 0 END,
    cancel_reason = #{reason},
    cancel_time = CURRENT_TIMESTAMP,
    update_time = CURRENT_TIMESTAMP
WHERE id = #{id}
  AND status = 2
```

退款状态直接在 SQL 中根据当前 `pay_status` 计算，而不是在 Java 中先读再写。

这样即使支付请求和拒单请求并发到达，最终支付状态也不会被旧值覆盖。

### 8. 管理员取消

管理员可以取消以下状态：

```text
1 待支付
2 待接单
3 已接单
4 派送中
```

SQL：

```sql
UPDATE orders
SET status = 6,
    pay_status = CASE WHEN pay_status = 1 THEN 2 ELSE 0 END,
    cancel_reason = #{reason},
    cancel_time = CURRENT_TIMESTAMP,
    update_time = CURRENT_TIMESTAMP
WHERE id = #{id}
  AND status IN (1, 2, 3, 4)
```

已完成和已取消订单不能再次取消。

### 9. 修复用户取消的并发覆盖问题

旧实现先读取订单：

```text
读取 pay_status
Java 计算 refundStatus
再执行 UPDATE
```

如果用户取消和支付同时发生，可能出现：

```text
取消请求读到 pay_status = 0
支付请求先完成，pay_status 变为 1
取消请求随后写入 pay_status = 0
结果：已支付订单被错误标记为未支付
```

现在取消 SQL 改为：

```sql
pay_status = CASE WHEN pay_status = 1 THEN 2 ELSE 0 END
```

支付状态以数据库当前值为准，不再使用请求开始时读取的旧值。

同时，支付的条件更新失败时不再返回 HTTP `500`，而是返回 `409 Conflict`：

```text
订单已取消
订单已支付
两个支付请求同时执行
```

这些情况都应该表示“当前状态不允许支付”，而不是服务器内部错误。

### 10. 订单统计

管理端统计接口返回三个数量：

```text
toBeConfirmed：待接单，状态 2
confirmed：已接单，状态 3
deliveryInProgress：派送中，状态 4
```

统计 SQL 使用 `CASE WHEN`：

```sql
SELECT COALESCE(SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END), 0)
           AS to_be_confirmed,
       COALESCE(SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END), 0)
           AS confirmed,
       COALESCE(SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END), 0)
           AS delivery_in_progress
FROM orders
```

这样一次查询就能得到三个状态的数量，不需要执行三次 `COUNT`。

### 11. 自动化测试

本步新增：

```text
AdminOrderServiceImplTest
AdminOrderControllerTest
AdminOrderIntegrationTest
AdminOrderHttpIntegrationTest
```

覆盖：

```text
分页参数规范化
空分页结果
非法时间范围
订单不存在
错误状态流转
接单
拒单
管理员取消
派送
完成
订单统计
管理端 HTTP 查询和状态推进
用户支付条件更新失败
用户取消订单
```

全量测试结果：

```text
Tests run: 152
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### 12. 真实 MySQL 验收

使用真实 MySQL 和 mock 微信登录启动：

```bash
DB_URL='jdbc:mysql://127.0.0.1:3306/sky_takeout?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true' \
DB_USERNAME=root \
DB_PASSWORD='<your-mysql-password>' \
WECHAT_MOCK_ENABLED=true \
./mvnw spring-boot:run
```

验收结果：

```text
管理员登录
-> 成功

创建临时上架菜品，单价 18.50
-> 成功

mock 用户登录并创建默认地址
-> 成功

创建订单 A，购买 2 份，支付 37.00
-> 管理端详情显示状态 2、支付状态 1
-> 按订单号和状态查询，总数 1
-> 统计接口待接单数量增加

管理员接单
-> 状态 3

再次接单
-> HTTP 409

管理员派送
-> 状态 4

管理员完成
-> 状态 5

创建订单 B，支付 18.50
-> 管理员拒单
-> 状态 6、支付状态 2、保存拒单原因

再次拒单
-> HTTP 409

创建订单 C，支付 18.50
-> 管理员取消
-> 状态 6、支付状态 2、保存取消原因

按手机号和已取消状态查询
-> 返回订单 B 和订单 C

不带 Token 查询统计
-> HTTP 401
```

直接查询 MySQL 后确认：

```text
订单 A：status = 5，pay_status = 1，amount = 37.00
订单 B：status = 6，pay_status = 2，cancel_reason = Out of stock acceptance
订单 C：status = 6，pay_status = 2，cancel_reason = Store closing acceptance
每条订单各有一条订单明细
```

验收后清理以下表：

```text
order_detail
orders
shopping_cart
address_book
user_account
dish_flavor
dish
```

清理后相关业务表计数全部为 0。

### 13. 本步设计结论

```text
用户端和管理端订单逻辑使用独立 Service
两边共用订单 Mapper 和订单表
列表查询与详情查询分离
列表默认不读取全部订单明细；传入 `withDetails=true` 时批量读取
每次状态流转都带前置状态条件
支付状态退款计算放在 SQL 内完成
状态冲突返回 409，资源不存在返回 404
统计数量一次查询完成
管理端接口继续复用现有 JWT 登录拦截器
```

### 14. 下一步

管理端订单接口已经完成，但 `little-fables` KDS 仍然使用浏览器 `localStorage`，没有读取真实订单。

下一步应该让 KDS 调用：

```text
GET /admin/order/conditionSearch?status=2
GET /admin/order/conditionSearch?status=3
GET /admin/order/conditionSearch?status=4
PUT /admin/order/confirm
PUT /admin/order/delivery/{id}
PUT /admin/order/complete/{id}
```

并共享管理端 JWT 登录状态，这样厨房看板和 Spring Boot 后端才形成真正的数据闭环。

### 15. 自学检查点

1. 为什么管理端订单列表不应该一次查询所有订单明细？
2. 为什么状态流转必须同时检查当前状态并带条件更新？
3. 为什么拒单和取消时退款状态应当由 SQL 当前值计算？
4. 为什么订单不存在返回 `404`，状态冲突返回 `409`？
5. 为什么统计接口使用 `SUM(CASE WHEN ...)` 比连续执行三次 `COUNT` 更合适？
6. 为什么管理端订单逻辑要从用户侧 `OrderService` 中分离？
7. KDS 接入后端时，为什么不能继续把订单只保存在 `localStorage`？

---

## 2026-09-20：Step 15 - KDS 接入真实订单

### 1. 本步目标

之前 `little-fables` 的厨房看板只读取浏览器 `localStorage`，订单不会进入 Spring Boot，也无法更新数据库。

本步完成真正的数据闭环：

```text
用户下单并支付
-> MySQL 保存订单
-> KDS 轮询真实订单
-> 厨房点击或拖拽
-> 调用管理端订单接口
-> MySQL 更新订单状态
-> KDS 重新加载
```

KDS 只展示活动订单：

```text
2：待接单
3：已接单
4：派送中
```

订单进入状态 `5` 完成后，会自动离开 KDS。

### 2. 整体架构

浏览器不直接请求 `8080`，而是请求前端服务器的 `/api`：

```text
Browser
-> http://127.0.0.1:4173/api/admin/order/...
-> little-fables/server.mjs
-> http://127.0.0.1:8080/admin/order/...
-> Spring Boot
-> MySQL
```

这样做的好处：

```text
浏览器使用同源 /api，不需要 CORS 配置
前端代码不需要写死后端地址
换端口时只需设置 BACKEND_URL
管理端 Token 继续通过 Authorization 请求头转发
```

### 3. 前端 API 代理

`little-fables/server.mjs` 增加：

```javascript
const backendUrl = process.env.BACKEND_URL || "http://127.0.0.1:8080";
```

当路径是 `/api` 或 `/api/*`：

```text
去掉 /api 前缀
保留查询参数
读取请求方法、请求头和请求体
转发到 Spring Boot
把响应状态、Content-Type 和响应体返回浏览器
```

代理必须去掉 `host` 和 `content-length`：

```javascript
headers.delete("host");
headers.delete("content-length");
```

原因：

```text
host 必须匹配目标服务器
请求体经过重新读取后，content-length 可能不再准确
```

### 4. 管理端批量订单明细

KDS 不能只显示订单号，还需要展示商品名称、数量和口味。

管理端分页 DTO 增加：

```text
withDetails=true
```

默认查询：

```text
只回到订单主体，适合普通管理列表
```

传入 `withDetails=true`：

```text
分页查询订单
收集当前页全部 orderId
执行一次 order_id IN (...) 批量查询明细
按 orderId 分组
装入 OrderVO.orderDetailList
```

这避免 KDS 对每条订单再发一次详情请求。

KDS 每个轮询周期只请求三次分页接口：

```http
GET /admin/order/conditionSearch?status=2&withDetails=true&page=1&pageSize=100
GET /admin/order/conditionSearch?status=3&withDetails=true&page=1&pageSize=100
GET /admin/order/conditionSearch?status=4&withDetails=true&page=1&pageSize=100
```

### 5. KDS 管理端登录

KDS 登录仍然使用：

```http
POST /admin/employee/login
```

登录成功后保存：

```text
Token：sessionStorage
管理员名称：sessionStorage
```

使用 `sessionStorage` 而不是 `localStorage` 的原因：

```text
关闭浏览器标签页后自动结束当前 KDS 会话
减少长期保存管理端 Token 的风险
```

请求接口时自动添加：

```http
Authorization: Bearer <token>
```

如果后端返回 `401`：

```text
清除本地会话
回到登录表单
提示管理员重新登录
```

### 6. KDS 轮询

KDS 每 10 秒刷新一次：

```text
只在当前页面是 #kds 且已登录时轮询
离开 KDS 页面后停止请求
手动 Refresh 按钮可以立即刷新
```

轮询结果按订单时间升序排列。最早进入待接单列的订单会显示为 `Next order`。

### 7. 真实订单映射到 KDS 票据

后端订单字段映射：

```text
order.id -> 拖拽和状态操作 ID
order.number -> 订单号
order.consignee -> 收货人
order.phone -> 联系电话
order.amount -> 订单金额
order.orderTime -> 已等待时间
order.remark -> 票据备注
order.orderDetailList -> 商品列表
order.status -> KDS 列
```

订单明细显示：

```text
item.number -> 商品数量
item.name -> 商品名称
item.dishFlavor -> 口味
```

统计栏：

```text
Open tickets：活动订单数量
Items to prepare：当前订单中的商品总数量
Next order：最早的一条待接单订单
```

### 8. KDS 状态操作

按钮和拖拽使用相同的状态机：

```text
待接单 -> 已接单
调用 PUT /admin/order/confirm

已接单 -> 派送中
调用 PUT /admin/order/delivery/{id}

派送中 -> 完成
调用 PUT /admin/order/complete/{id}
```

KDS 不允许把订单拖回之前的状态。

如果直接拖到更后面的列：

```text
待接单 -> 派送中
```

前端会按顺序执行：

```text
confirm
-> delivery
```

如果订单已经被其他管理员处理，后端返回 `409`，前端刷新看板并提示冲突。

### 9. 拖拽实现

票据仍使用 HTML5 拖拽事件：

```text
dragstart：保存订单 ID
dragover：允许放置并高亮目标列
drop：获取目标列状态
```

拖动后不是直接修改本地数组，而是调用后端接口：

```javascript
transitionOrder(order, stage.status)
```

后端成功后才重新加载订单。这样浏览器显示与数据库状态保持一致。

### 10. 响应式验证

桌面端：

```text
1440 x 900
三列看板完整展示
订单票据显示完整
拖拽正常
```

移动端：

```text
390 x 844
工具栏纵向展开
看板横向滚动
文字没有溢出或遮挡
```

浏览器中实际完成：

```text
管理员登录
待接单订单加载
按钮接单
按钮派送
按钮完成
鼠标拖拽到 Accepted
鼠标拖拽到 Delivering
完成订单后从看板移除
```

### 11. 自动化验证

后端全量测试：

```text
Tests run: 152
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

前端语法检查：

```bash
node --check little-fables/app.js
node --check little-fables/server.mjs
```

真实 MySQL 浏览器验收：

```text
订单 #5：KDS 浏览器验收，金额 44.00
-> 按钮完成
-> status = 5

订单 #6：KDS 拖拽验收，金额 22.00
-> 拖拽接单
-> 拖拽派送
-> 按钮完成
-> status = 5
```

验收后清理：

```text
order_detail
orders
shopping_cart
address_book
user_account
dish_flavor
dish
```

相关业务表计数全部为 0。

### 12. 本步设计结论

```text
KDS 不再以 localStorage 作为订单真源
前端服务器代理后端请求，避免 CORS
管理端 Token 只保存在 sessionStorage
withDetails=true 使用批量查询，避免 KDS N+1
KDS 只轮询活动状态 2、3、4
按钮和拖拽使用同一套后端状态流转
前端只在后端成功后重新加载数据
完成后订单自动离开看板
桌面和移动端均完成浏览器验证
```

### 13. 下一步

KDS 已完成主流程接入。后续可以继续做：

```text
KDS 拒单和取消操作
订单明细变化时使用 SSE 或 WebSocket 推送
显示预计送达时间
按派送员筛选
Phase 4 Redis 缓存菜品和套餐
接口文档和部署
```

### 14. 自学检查点

1. 为什么前端服务器代理后端请求可以避免 CORS？
2. 为什么代理转发时要删除 `host` 和 `content-length`？
3. 为什么 KDS 使用 `withDetails=true` 而不是逐条请求订单详情？
4. 为什么管理端 Token 放在 `sessionStorage` 比 `localStorage` 更合适？
5. 为什么拖拽后要等后端成功再刷新看板？
6. 为什么 KDS 只轮询状态 `2`、`3`、`4`？
7. 如果订单被另一个 KDS 同时处理，前端应该如何处理 `409`？
