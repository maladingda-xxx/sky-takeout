# Sky Takeout 测试指南

这份文档面向"想亲手把项目所有功能跑一遍"的场景，按 环境准备 → 启动 → 自动化测试 → 接口手工测试 → 前端验证 的顺序组织。

## 1. 需要准备的环境

```text
JDK 21            java -version
Maven Wrapper     项目自带 ./mvnw，不需要单独装 Maven
MySQL 8            本地 3306，需要有建库权限的账号
Redis              本地 6379，缓存功能需要
Node.js            只跑 little-fables 前端时才需要
```

检查 Redis 和 MySQL：

```bash
redis-cli ping                 # 期望 PONG
mysql -uroot -p -e "select version();"
```

## 2. 初始化数据库

```bash
mysql -uroot -p < sql/schema.sql
```

脚本会建库 `sky_takeout`、建全部表，并写入种子数据：

```text
员工账号    admin / password      角色：管理员
            operator / password   角色：操作员
分类        id=1 菜品分类 Hot Dishes
            id=2 套餐分类 Set Meals
```

密码在库里是 BCrypt 密文，登录时用上面的明文。

## 3. 启动顺序

```bash
# 1) MySQL 和 Redis 保持运行
# 2) 后端（默认 8080）
./mvnw spring-boot:run

# 3) 可选：前端（默认 4173，自动代理 /api 到 8080）
cd little-fables
npm run dev
```

后端启动的常用环境变量：

```text
DB_URL / DB_USERNAME / DB_PASSWORD    数据库连接
REDIS_HOST / REDIS_PORT               缓存连接
CACHE_ENABLED=false                   关掉缓存（排查缓存问题时用）
AUTH_ENABLED=false                    关掉登录校验（只想调业务时用）
WECHAT_MOCK_ENABLED=true              用户端登录走 mock，不需要微信账号
UPLOAD_PATH=./uploads                 图片落盘目录
```

自检：

```bash
curl http://127.0.0.1:8080/api/hello
# Sky Takeout is running
```

## 4. 自动化测试

```bash
# 默认套件：缓存关闭，不依赖 Redis
./mvnw -o test
# 期望 Tests run: 169, Failures: 0, Errors: 0, Skipped: 9

# 打开真实 Redis 用例（需要本机 Redis 在跑）
REDIS_INTEGRATION_TEST=true ./mvnw -o test
# 期望 Tests run: 169, Failures: 0, Errors: 0, Skipped: 0

# 只跑某个模块
./mvnw -o test -Dtest=OrderHttpIntegrationTest
./mvnw -o test -Dtest=CatalogCacheIntegrationTest,RedisCacheBehaviorIntegrationTest
```

测试分层：`*ServiceImplTest` 是单元测试（Mockito），`*IntegrationTest` 走 H2 + Spring 上下文，`*HttpIntegrationTest` 起真实端口发 HTTP 请求。

## 5. 接口手工测试

### 5.1 拿 Token

管理端：

```bash
TOKEN=$(curl -s -X POST http://127.0.0.1:8080/admin/employee/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password"}' | jq -r '.data.token')
```

用户端（需要 `WECHAT_MOCK_ENABLED=true`）：

```bash
UTOKEN=$(curl -s -X POST http://127.0.0.1:8080/user/login \
  -H 'Content-Type: application/json' \
  -d '{"code":"manual-test-user"}' | jq -r '.data.token')
```

鉴权规则：

```text
/admin/**   需要 token，只有 /admin/employee/login 例外
/user/**    需要 token，/user/login、/user/category/**、/user/dish/**、/user/setmeal/** 例外
请求头      Authorization: Bearer <token>
```

### 5.2 员工管理

```bash
curl -H "Authorization: Bearer $TOKEN" \
  'http://127.0.0.1:8080/admin/employee/page?page=1&pageSize=10'

curl -X POST http://127.0.0.1:8080/admin/employee \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Tester","username":"tester","password":"123456","phone":"13800000003","sex":1,"idNumber":"110101199003030033"}'

curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/admin/employee/3

curl -X PUT http://127.0.0.1:8080/admin/employee \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"id":3,"name":"Tester2","username":"tester","phone":"13800000004","sex":1,"idNumber":"110101199003030033"}'

curl -X POST -H "Authorization: Bearer $TOKEN" \
  'http://127.0.0.1:8080/admin/employee/status/0?id=3'

curl -X DELETE -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/admin/employee/3
```

重点验证：重复用户名/身份证返回 400；不能删除或禁用当前登录的 admin。

### 5.3 分类管理

```bash
curl -H "Authorization: Bearer $TOKEN" \
  'http://127.0.0.1:8080/admin/category/page?page=1&pageSize=10&type=1'

curl -H "Authorization: Bearer $TOKEN" \
  'http://127.0.0.1:8080/admin/category/list?type=1'

curl -X POST http://127.0.0.1:8080/admin/category \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"type":1,"name":"Drinks","sort":20}'

curl -X POST -H "Authorization: Bearer $TOKEN" \
  'http://127.0.0.1:8080/admin/category/status/0?id=3'

curl -X DELETE -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/admin/category/3
```

重点验证：同类型下重名报错；分类下还有菜品时不允许删除；禁用分类后用户端看不到该分类。

### 5.4 菜品管理与图片上传

```bash
curl -X POST http://127.0.0.1:8080/admin/dish \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Kung Pao Chicken","categoryId":1,"price":28.00,"image":"/images/kungpao.jpg","description":"Classic","status":1,"flavors":[{"name":"Spiciness","value":["Mild","Spicy"]}]}'

curl -H "Authorization: Bearer $TOKEN" 'http://127.0.0.1:8080/admin/dish/page?page=1&pageSize=10&name=Kung'
curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/admin/dish/1
curl -X POST -H "Authorization: Bearer $TOKEN" 'http://127.0.0.1:8080/admin/dish/status/0?id=1'
curl -X DELETE -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/admin/dish/1

# 上传图片（multipart 字段名 file）
curl -X POST http://127.0.0.1:8080/admin/common/upload \
  -H "Authorization: Bearer $TOKEN" -F 'file=@/path/to/picture.jpg'
# 返回 data 就是可访问的 URL，例如 /uploads/xxxx.jpg
curl -I http://127.0.0.1:8080/uploads/xxxx.jpg
```

重点验证：菜品分类必须是 type=1；口味值是数组；上传超过 5MB、扩展名不允许、或者文件内容不是真实图片时都要报错（服务端会用 ImageIO 实际解析一遍）。

### 5.5 套餐管理

```bash
curl -X POST http://127.0.0.1:8080/admin/setmeal \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"categoryId":2,"name":"Lunch Combo","price":38.00,"status":1,"description":"Combo","dishes":[{"dishId":1,"copies":2}]}'

curl -H "Authorization: Bearer $TOKEN" 'http://127.0.0.1:8080/admin/setmeal/page?page=1&pageSize=10'
curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/admin/setmeal/1
curl -X POST -H "Authorization: Bearer $TOKEN" 'http://127.0.0.1:8080/admin/setmeal/status/0?id=1'
```

重点验证：套餐分类必须是 type=2；菜品明细里的 dishId 必须存在，且同一次提交不能出现重复 dishId。

### 5.6 用户端浏览（不需要 token）

```bash
curl 'http://127.0.0.1:8080/user/category/list?type=1'
curl 'http://127.0.0.1:8080/user/dish/list?categoryId=1'
curl 'http://127.0.0.1:8080/user/setmeal/list?categoryId=2'
curl 'http://127.0.0.1:8080/user/setmeal/1'
```

用错误 id 访问（例如 `categoryId=999999`）应返回 404，连续请求第二次不会打到数据库——这就是负缓存。

### 5.7 购物车

```bash
curl -X POST http://127.0.0.1:8080/user/shoppingCart/add \
  -H "Authorization: Bearer $UTOKEN" -H 'Content-Type: application/json' \
  -d '{"dishId":1,"quantity":2}'

curl -H "Authorization: Bearer $UTOKEN" http://127.0.0.1:8080/user/shoppingCart/list

curl -X POST http://127.0.0.1:8080/user/shoppingCart/sub \
  -H "Authorization: Bearer $UTOKEN" -H 'Content-Type: application/json' \
  -d '{"dishId":1}'

curl -X DELETE -H "Authorization: Bearer $UTOKEN" http://127.0.0.1:8080/user/shoppingCart/clean
```

重点验证：dishId 和 setmealId 必须二选一；数量减到 0 时记录被删除；购物车只能看到自己的数据。

### 5.8 收货地址

```bash
curl -X POST http://127.0.0.1:8080/user/addressBook \
  -H "Authorization: Bearer $UTOKEN" -H 'Content-Type: application/json' \
  -d '{"consignee":"Tester","sex":1,"phone":"13800000009","provinceName":"Shanghai","cityName":"Shanghai","districtName":"Pudong","detail":"Test Road 1","label":"Home","isDefault":1}'

curl -H "Authorization: Bearer $UTOKEN" http://127.0.0.1:8080/user/addressBook/list
curl -H "Authorization: Bearer $UTOKEN" http://127.0.0.1:8080/user/addressBook/default
curl -X PUT -H "Authorization: Bearer $UTOKEN" http://127.0.0.1:8080/user/addressBook/default/1
curl -X DELETE -H "Authorization: Bearer $UTOKEN" http://127.0.0.1:8080/user/addressBook/1
```

重点验证：设置新默认地址后旧默认地址自动取消；不能操作别人的地址。

### 5.9 下单、支付与查询

```bash
# 下单：addressBookId 不传时使用默认地址
curl -X POST http://127.0.0.1:8080/user/order/submit \
  -H "Authorization: Bearer $UTOKEN" -H 'Content-Type: application/json' \
  -d '{"payMethod":1,"remark":"less spicy"}'

# 用返回的 orderNumber 支付
curl -X PUT http://127.0.0.1:8080/user/order/payment \
  -H "Authorization: Bearer $UTOKEN" -H 'Content-Type: application/json' \
  -d '{"orderNumber":"<上一步返回的订单号>"}'

curl -H "Authorization: Bearer $UTOKEN" 'http://127.0.0.1:8080/user/order/history?page=1&pageSize=10'
curl -H "Authorization: Bearer $UTOKEN" http://127.0.0.1:8080/user/order/detail/1
curl -X PUT -H "Authorization: Bearer $UTOKEN" http://127.0.0.1:8080/user/order/cancel/1
```

重点验证：购物车为空时不能下单；下单成功后购物车被清空；已支付订单取消走退款模拟；只有本人的订单能查。

### 5.10 管理端订单闭环

```bash
curl -H "Authorization: Bearer $TOKEN" \
  'http://127.0.0.1:8080/admin/order/conditionSearch?page=1&pageSize=10&status=2'
curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/admin/order/statistics
curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/admin/order/details/1

curl -X PUT http://127.0.0.1:8080/admin/order/confirm \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"id":1}'
curl -X PUT -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/admin/order/delivery/1
curl -X PUT -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8080/admin/order/complete/1

# 拒单（已支付会走退款模拟）
curl -X PUT http://127.0.0.1:8080/admin/order/rejection \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"id":2,"rejectionReason":"out of stock"}'

# 管理员取消
curl -X PUT http://127.0.0.1:8080/admin/order/cancel \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"id":3,"cancelReason":"customer request"}'
```

订单状态：`1 待付款 2 待接单 3 已接单 4 派送中 5 已完成 6 已取消`。每一次流转都带前置状态校验，对同一个订单重复接单应该返回 409 而不是覆盖状态。

## 6. 缓存验证

```bash
# 第一次请求会写缓存
curl 'http://127.0.0.1:8080/user/dish/list?categoryId=1'
redis-cli --scan --pattern 'sky:cache:*'
redis-cli ttl 'sky:cache:catalogDishes:1'          # 600~660 秒之间，带抖动

# 管理端改数据后，对应缓存被清空
curl -X POST -H "Authorization: Bearer $TOKEN" 'http://127.0.0.1:8080/admin/dish/status/0?id=1'
redis-cli exists 'sky:cache:catalogDishes:1'       # 期望 0

# 负缓存：不存在的分类
curl -i 'http://127.0.0.1:8080/user/dish/list?categoryId=999999'   # 404
redis-cli ttl 'sky:cache:catalogMiss:dish-category:999999'          # 0~180 秒
```

## 7. 前端验证

```bash
cd little-fables
npm run dev
# http://127.0.0.1:4173        餐厅首页与预订
# http://127.0.0.1:4173/#kds   厨房看板
```

KDS 用管理端账号登录（`admin` / `password`），看板只展示状态 2、3、4 的订单。验证点：按钮接单/派送/完成能改状态；拖拽能把订单推进到下一列；完成的订单离开看板；Token 过期时提示重新登录。

注意：首页的预订只存在浏览器 `localStorage`，没有后端接口，刷新浏览器仍在，换浏览器就没了。

## 8. 全功能验收清单

```text
[ ] 员工 分页、新增、详情、修改、启用禁用、删除、重名与当前登录人保护
[ ] 分类 分页、新增、详情、按类型列表、修改、启用禁用、删除、重名校验
[ ] 菜品 分页、新增（含口味）、详情、修改、上下架、删除、分类类型校验
[ ] 套餐 分页、新增（含菜品明细）、详情、修改、上下架、删除
[ ] 图片 上传成功、返回 URL 可访问、超限与非法扩展名被拒绝
[ ] 登录 管理端登录、用户端 mock 登录、Token 校验、401 处理
[ ] 用户端浏览 分类、菜品、套餐、套餐详情、停用分类不可见
[ ] 购物车 加、减、改、查、清空、商品二选一校验
[ ] 地址 新增、列表、默认地址、修改、删除、用户隔离
[ ] 订单 下单、支付、历史分页、详情、取消、本人可见
[ ] 管理端订单 条件查询、统计、详情、接单、拒单、取消、派送、完成
[ ] 缓存 命中、写操作失效、负缓存、TTL 抖动、CACHE_ENABLED=false 可关闭
[ ] KDS 登录、轮询、按钮流转、拖拽、完成离场
[ ] 异常 参数错误返回 400、未登录返回 401、重复操作返回 409
```

## 9. 常见问题

```text
401 未登录                检查请求头是不是 "Authorization: Bearer <token>"，管理端和用户端 token 不通用
400 参数错误              看 GlobalExceptionHandler 返回的 message，参数名和类型按文档对齐
503 WeChat login is not configured
                          WECHAT_MOCK_ENABLED 没开，或者 app-id/app-secret 为空
浏览接口读不到新数据       管理端写操作才会清缓存；直接用 SQL 改库不会清，等 TTL 过期或 redis-cli del
Redis 连不上               缓存开关打开时浏览接口依赖 Redis；排查时可设 CACHE_ENABLED=false 启动
上传后 404                UPLOAD_PATH 目录下确实要有文件，路径要和 /uploads 映射一致
测试里 Mockito 报错        用项目自带 ./mvnw 跑，JDK 版本用 21
```
