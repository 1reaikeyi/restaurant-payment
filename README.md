# Restaurant-payment 餐饮和支付系统

restaurant-payment：B2C经营模式，一个餐馆卖家，多个买家。餐馆服务由店长，店员和客户组成。

一个由Spring Boot 3 + Vue 3 的前后端分离架构，中间件使用redis，主业务为餐饮订单和支付的全栈系统，同时Spring AI（这里使用spring-ai-starter-model-openai） 作为单独服务接入，通过菜品识别对应菜单。

------

# 后端说明

**订单状态流转**：

```
1 待支付 → 2 待商家接单 → 3 制作中 → 4 待骑手取餐 → 5 配送中 → 6 已送达 → 7 已完成
        	↓                                      
   8 已取消（未接单退款、商家拒单、超时取消、售后全额退款）
```

**支付流程**：

| 步骤 | 截图 |
| :--: | :--: |
| 集成到订单 | <img src="说明/支付功能结果/支付宝1.png" alt="支付" style="zoom:25%;" /> |
| 支付过程 | <img src="说明/支付功能结果/支付宝2.png" alt="支付" style="zoom: 50%;" /> |
| 同步支付成功 | <img src="说明/支付功能结果/支付宝3.png" alt="支付" style="zoom: 50%;" /> |
| 异步检验 | <img src="说明/支付功能结果/支付宝4.png" alt="支付" style="zoom: 50%;" /> |

**第三方授权登录流程图**

| 1    | <img src="说明\支付宝，qq授权登录\ali1.png" alt="支付宝" style="zoom:25%;" /> |
| ---- | ------------------------------------------------------------ |
|      | <img src="说明\支付宝，qq授权登录\ali2.png" alt="支付宝" style="zoom:50%;" /> |
|      | <img src="说明\支付宝，qq授权登录\ali3.png" alt="支付宝" style="zoom:50%;" /> |



# 项目结构

```
restaurant-payment/
├── backend-spring-restaurant/            # 后端代码（Spring Boot 3 多模块）
│   ├── common/                           # 公共模块（常量/异常/工具/JwtProperties/AliOssProperties）
│   │
│   ├── model/                            # 实体与数据传输对象
│   │
│   ├── mapper/                           # 数据访问层（MyBatis-Plus）
│   │
│   ├── service/                          # 业务逻辑模块
│   │
│   ├── start/                            # 主业务启动模块
│   │   ├── aop/                          # Info/OperationLogging 注解 + ServiceInterceptAspect 切面
│   │   ├── config/                       # Config 等
│   │   ├── controller/                   # 按职责分目录
│   │   │   ├── admin/                    # 管理端
│   │   │   ├── user/                     # 用户端
│   │   │   ├── login/                    # 登录入口
│   │   │   ├── file/                     # 文件上传与Excel
│   │   │   ├── websocket/                # WebSocket 推送
│   │   │   ├── timetask/                 # 定时任务（订单超时取消等）
│   │   │   └── 支付宝/                   # 支付宝支付/退款/回调/OAuthLogin
│   │   ├── filter/                       # 安全过滤器链
│   │   │   ├── UserRefreshRequestFilter          # user Token 校验/滑动刷新
│   │   │   ├── EmployeeRefreshRequestFilter      # emp  Token 校验/滑动刷新
│   │   │   └── InformationRequestFilter          # 兜底认证拦截
│   │   ├── security/                     # Spring Security 主体与上下文工具
│   │   ├── exceptionHandle/             # GlobalExceptionHandler 全局异常
│   │   ├── metaHandler/                 # AutoMetaObjectHandler 自动填充 createTime/updateTime
│   │   ├── oparation/                   # OperationType 操作类型枚举
│   │   └── img/                         # 初始化图片资源
│   │
│   └── ai-see/                           # AI视觉识别服务（独立服务）
│
├── frontend-vue-admin-restaurant/        # 前端管理端（Vue 3）
│   ├── src/
│   │   ├── api/                          # API接口封装
│   │   ├── views/                        # 页面视图
│   │   ├── layout/                       # 布局组件
│   │   ├── router/                       # 路由配置
│   │   ├── stores/                       # 状态管理（Pinia）
│   │   └── utils/                        # 工具函数
│   └── package.json
│
├── database-sql/                         # 数据库脚本目录
│   ├── sql.txt                           # 数据库create table
│	├── sql插入数据.txt                    # 数据库初始化SQL
│   └── 数据库设计文档.md                   # 数据库设计说明
│
└── 说明/                                 # 项目说明文档
    ├── 原型功能/                         # 前端原型截图
    ├── 第三方授权登录/                    # 支付宝，qq,微信
    ├── 支付功能结果/                      # 支付流程截图
    ├── employee的api文档.md              # 管理端
    └── user的api文档.md                  # 用户端
```

# 环境要求

- JDK 17+
- Spring Boot 3+
- Spring AI 1.1+
- Node.js 20.19.+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.8+
- 以上为最低配置

---

## 一、用户与员工双端登录认证模块

### 需求阶段

需求背景：项目需要同时支撑「管理端员工」和「用户端客户」两套登录体系，且两端的权限、Token、Redis Key 必须互不干扰。

- 传统 Session 认证在前后端分离 + 分布式部署下不好扩展

- 员工端（店长/店员）与用户端（客户）需隔离，避免权限串扰

- 密码明文存储不安全，Token 固定过期会让活跃用户被强制下线

- 第三方登录，目前主流支付宝、qq、微信等，可扩展其他平台

### 策略流程图

```java
员工注册 → AdminEmployeeController/register() → BCrypt加密密码 → MySQL保存 → 返回注册成功
用户注册 → UserController/register()          → BCrypt加密密码 → MySQL保存 → 返回注册成功

员工登录 → AdminEmployeeController/login()
        → 构造 principal="emp:{username}" → AuthenticationManager.authenticate()
        → MultiLoginAuthenticationProvider 按 prefix 区分 emp/user → 查员工表 + BCrypt.matches
        → 生成 JWT(claims: EMP_ID/EMP_NAME/TYPE="emp") → Redis存储("restaurant:emp:{id}")
        → 返回 Token
用户登录 → UserController/login()
        → 构造 principal="user:{username}" → AuthenticationManager.authenticate()
        → MultiLoginAuthenticationProvider 按 prefix 区分 → 查用户表 + BCrypt.matches
        → 生成 JWT(claims: USER_ID/USER_NAME/TYPE="user") → Redis存储("restaurant:user:{id}")
        → 返回 Token

请求拦截 → EmployeeRefreshRequestFilter（仅处理 TYPE=emp，否则放行给 UserFilter）
        → UserRefreshRequestFilter（仅处理 TYPE=user，否则放行）
        → InformationRequestFilter（兜底：未认证返回 401）
        → Redis 校验 Token 一致性 → 滑动过期刷新 → 设置 SecurityContext（ROLE_ADMIN/ROLE_USER）
```
### 编码阶段

```java
// MultiLoginAuthenticationProvider.java - 双端登录认证核心
// 通过 principal 前缀 "emp:" / "user:" 区分员工与用户，复用同一套 AuthenticationManager
@Override
public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String principal = authentication.getName();
    String password = (String) authentication.getCredentials();

    int idx = principal.indexOf(':');
    String type = idx > 0 ? principal.substring(0, idx) : "user";  // 默认按 user 处理
    String username = idx > 0 ? principal.substring(idx + 1) : principal;

    // ===== emp 员工登录 =====
    if ("emp".equals(type)) {
        Employee employee = employeeService.findEmployeename(username);
        if (employee == null || !passwordEncoder.matches(password, employee.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        return new UsernamePasswordAuthenticationToken(
                new LoginPrincipal(employee.getId(), employee.getUsername()),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
    // ===== user 普通用户登录 =====
    User user = userService.findUsername(username);
    if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
        throw new BadCredentialsException("用户名或密码错误");
    }
    return new UsernamePasswordAuthenticationToken(
            new LoginPrincipal(user.getId(), user.getUsername()),
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
}
```

```java
// UserRefreshRequestFilter.java - user Token 校验与滑动过期（emp Token 直接放行）
// 关键：通过 claims 中的 TYPE 字段区分 token 归属，互不干扰
Map<String, Object> claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
String type = claims.get(JwtConstant.TYPE) != null
        ? claims.get(JwtConstant.TYPE).toString() : "user";
if (!"user".equals(type)) {
    // 不是 user 的 token（emp），交给 EmployeeRefreshRequestFilter 处理
    filterChain.doFilter(request, response);
    return;
}
Long userId = Long.parseLong(claims.get(JwtConstant.USER_ID).toString());
// Redis 校验：防止 Token 被盗用后多端登录
String standardToken = stringRedisTemplate.opsForValue()
        .get(RedisPrefixConstant.USER_AUTHHEADER_PREFIX + userId);
if (!token.equals(standardToken)) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return;
}
// 设置 SecurityContext（ROLE_USER）
SecurityContextHolder.getContext().setAuthentication(authentication);
// 滑动过期：每次请求刷新 Redis 中 Token 有效期
stringRedisTemplate.expire(RedisPrefixConstant.USER_AUTHHEADER_PREFIX + userId,
        jwtProperties.getUserTtl(), TimeUnit.SECONDS);
```

### 问题修复阶段

Q：双端登录认证时，如何避免两个 RefreshRequestFilter 都尝试解析同一个 Token 导致重复验证？

> **过滤器短路执行机制**：关键是利用 `filterChain.doFilter()` 的短路特性。在 `EmployeeRefreshRequestFilter` 中，先解析 Token 的 TYPE 字段：如果是 `emp` 类型，执行完整验证流程并设置 SecurityContext；如果不是，直接调用 `filterChain.doFilter()` 放行给下一个过滤器。`UserRefreshRequestFilter` 同样逻辑处理 `user` 类型。只有当两个过滤器都处理完，`InformationRequestFilter` 兜底检查 SecurityContext 是否为空。这种设计的关键是**每个过滤器只处理自己关心的 Token 类型，不做拒绝判断**，避免了 if-else 嵌套和 Token 类型转换错误。

Q：BCryptPasswordEncoder.matches() 方法内部是如何处理盐值的？为什么不需要单独存储 salt 字段？

> **盐值内嵌设计**：BCrypt 的密文格式为 `$2a$10$<22位salt><31位hash>`，盐值直接嵌在密文中间。`matches()` 方法解析密文时，按 `$` 分割后自动提取出盐值（第 3 段前 22 位），然后用提取出的盐值对传入的明文密码进行哈希运算，最后比较结果是否匹配。这种设计的好处是**盐值与密文绑定存储**，不需要额外的盐字段，验证时自动提取。代价是每个用户的盐值不可变——如果想更换盐值，必须重新哈希整个密码，这也是为什么登录接口中密码验证后如果成功就直接放行，不会重新生成新盐值的原因。

Q：滑动过期策略下，长时间不活跃的用户会被强制下线。系统如何检测并处理已下线用户的后续请求？

> **SecurityContext 缺失检测**：当用户 Token 过期后，`Redis` 中的 Key 自动过期。此时 `RefreshRequestFilter` 解析 Token 成功（JWT 本身未过期），但 Redis 查询返回 null。代码中通过 `stringRedisTemplate.hasKey()` 判断 Token 是否存在，不存在则直接返回 401 错误。这种设计的核心是**双重检查**：JWT 签名验证 + Redis Token 存在性校验。即使 JWT 本身未过期，但只要 Redis 中的 Token 被删除（过期或主动登出），用户就会被强制下线。这为"管理员强制踢出"提供了实现基础——只需从 Redis 删除指定用户的 Token，该用户的所有后续请求都会被拒绝。

---

## 二、分类管理模块

### 需求阶段

需求背景：菜品和套餐都需要分类管理，分类数据相对稳定但访问频繁，是典型的「读多写少」场景。

- 分类数量较少但查询频率高，每次查库浪费
- 菜品分类（type=1）与套餐分类（type=2）共用一张 `restaurant_category` 表，需按 type 区分缓存
- 分类修改后需要及时同步到缓存，避免脏数据

### 策略流程图

```java
查询分类（按type） → CategoryController/readByType()
    ├─ @Cacheable(key="#type") 命中缓存 → 直接返回 Redis 数据
    └─ 未命中 → 查询 MySQL → 结果写入 Redis（cacheName::type） → 返回
新增分类 → AdminCategoryController/create() → 写 MySQL → 返回
更新分类 → AdminCategoryController/update() → @CacheEvict(allEntries=true) 写 MySQL → 清空整个缓存
删除分类 → AdminCategoryController/delete() → @CacheEvict(allEntries=true) 删 MySQL → 清空整个缓存
```

### 编码阶段

```java
// AdminCategoryController.java - Spring Cache 声明式缓存
// @CacheConfig 在类级别统一声明 cacheNames，方法级只用 key
@RestController
@RequestMapping("/admin/category")
@CacheConfig(cacheNames = "restaurantCategory:type")
public class AdminCategoryController {

    // 按 type 查询：结果缓存，key 用 SpEL 引用方法参数 #type
    @Cacheable(key = "#type", unless = "#result == null")
    @GetMapping
    public Result readByType(@RequestParam("type") Long type) {
        List<RestaurantCategory> list = restaurantCategoryService.lambdaQuery()
                .eq(RestaurantCategory::getType, type).list();
        return Result.success(list);
    }

    // 更新：写库后清空该 cacheName 下所有缓存，保证一致性
    @CacheEvict(allEntries = true)
    @PutMapping
    public Result update(@RequestBody RestaurantCategoryDTO dto) {
        RestaurantCategory entity = BeanUtil.toBean(dto, RestaurantCategory.class);
        restaurantCategoryService.updateById(entity);
        return Result.success(OperationEnum.UPDATE + "--" + entity.getId());
    }

    // 删除：同样清空所有缓存（因为可能影响按 type 查询的列表）
    @CacheEvict(allEntries = true)
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        restaurantCategoryService.removeByIds(ids);
        return Result.success(OperationEnum.DELETE + "--" + ids);
    }
}
```

```java
// RedisConfig.java - 缓存管理器配置
// 统一 TTL 30 分钟，Key 用 String 序列化，Value 用 Jackson JSON 序列化
@Bean
public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer(redisMapper)))
            .entryTtl(Duration.ofMinutes(30));  // 默认缓存 30 分钟
    return RedisCacheManager.builder(connectionFactory).cacheDefaults(defaultConfig).build();
}
```

### 问题修复阶段

Q：@CacheEvict(allEntries=true) 底层是如何实现批量删除的？它和逐 key 删除相比有哪些性能差异？

> **Spring Cache 的清除策略**：`allEntries=true` 底层调用 `RedisCacheManager.getCache()` 获取对应的 Cache 实例，然后调用 `Cache.clear()` 方法。对于 Redis 实现，`clear()` 内部执行的是 `SCAN` + `DEL` 命令（Spring Cache 3.x 版本），而不是 `KEYS` + `DEL`。逐 key 删除的问题是：你需要知道确切的缓存 Key 格式才能定位，而 `allEntries=true` 直接清空整个命名空间（`restaurantCategory:type` 下的所有 Key）。性能上，`allEntries=true` 对命名空间内 Key 数量敏感——如果 Key 数量少（如分类只有几个 type），几乎无开销；如果 Key 数量巨大（如按用户 ID 缓存），则可能阻塞 Redis。

Q：Spring Cache 的 @Cacheable 注解中 unless 表达式 `unless = "#result == null"` 的作用是什么？

> **防止空值缓存**：Spring Cache 默认行为是无论查询结果是否为 null，都会将结果缓存。这意味着如果某个 ID 查询不到数据，会在 Redis 中缓存一个 null 值，下次查询直接返回 null（称为"缓存穿透保护"）。但分类查询用 `unless = "#result == null"` 的意思是**只有当结果不为 null 时才缓存**。这是因为分类数据中，null 值通常意味着"该分类不存在"，而这个信息本身不需要缓存——用户反复查询一个不存在的分类，每次都应该返回"不存在"而不是被 null 缓存短路。这在数据字典类场景下很有用，但在需要防穿透的场景（如热点商品 ID 查询），应该去掉 unless 让 null 也缓存。

---

## 三、菜品管理模块

### 需求阶段

需求背景：菜品是餐馆核心商品，需要支持菜品 CRUD、菜品口味（多口味）、按分类查询、起售/停售等。

- 菜品数据量大，分页查询性能要求高
- 一个菜品对应多个口味（DishDetail），需主子表关联
- 菜品与分类关联，按分类检索是高频查询

### 策略流程图

```java
【新增流程】
用户请求 → AdminDishController/create()
    → DishDTO 转 Dish 实体 → dishService.save(dish) → MySQL 保存主表
    → DishDetail DTO 列表转实体 → dishDetailService.saveBatch() → MySQL 批量保存口味
    → @Transactional 事务提交 → 返回成功

【查询流程】
用户请求 → DishController/readAll()
    → LambdaQueryWrapper 构建查询（状态=启用 + 名称模糊匹配）
    → new Page() 分页参数 → dishService.page() → 返回分页数据

详情查询 → DishController/readById()
    → dishService.readCache(id) → Redis 查缓存
    ├─ 缓存存在且逻辑未过期 → 直接返回缓存数据
    ├─ 缓存存在但逻辑过期 → 异步线程池获取分布式锁 → 刷新缓存 → 返回旧数据
    └─ 缓存不存在 → 查询数据库 → 写入 RedisData → 返回

【更新流程】
用户请求 → AdminDishController/update()
    → dishService.updateCache(dishDTO)
    → BeanUtil 转实体 → super.updateById() → 更新 MySQL
    → stringRedisTemplate.delete() → 主动删除缓存（保证一致性）
    → 清空旧口味 → 批量保存新口味 → @Transactional 提交

【删除流程】
用户请求 → AdminDishController/delete()
    → super.removeByIds(ids) → 删除主表
    → dishDetailService.remove() → 批量删除关联口味
    → for 循环 delete() → 删除所有缓存 Key
    → 返回成功
```

### 编码阶段

```java
// AdminDishController.java - 管理端菜品 CRUD
// 核心设计：主表+明细表在同一事务中保存，缓存主动删除保证一致性
@RestController
@RequestMapping("/admin/dish")
public class AdminDishController {

    // 新增菜品：主表+明细表事务保存
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result create(@RequestBody DishDTO dishDTO) {
        Dish dish = BeanUtil.toBean(dishDTO, Dish.class);
        dishService.save(dish);
        // 批量保存口味列表（saveBatch 比循环 insert 性能高数十倍）
        List<DishDetail> dishDetailList = dishDTO.getDishDetails().stream()
                .map(dishDetail -> BeanUtil.toBean(dishDetail, DishDetail.class))
                .toList();
        dishDetailService.saveBatch(dishDetailList);
        return Result.success(OperationEnum.CREATE + "--" + dish.getId());
    }

    // 分页查询：支持状态筛选+名称模糊搜索
    @GetMapping("/all")
    public Result readAll(DishPageDTO dishPageDTO) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, StatusConstant.ENABLE)
                .like(dishPageDTO.getName() != null, Dish::getName, dishPageDTO.getName());
        IPage<Dish> page = new Page<>(dishPageDTO.getPage(), dishPageDTO.getPageSize());
        IPage<Dish> dishIPage = dishService.page(page, queryWrapper);
        return Result.success(dishIPage);
    }

    // 详情查询：缓存优先，逻辑过期+异步刷新
    @GetMapping
    public Result readById(@RequestParam Long id) {
        Dish dish = dishService.readCache(id);  // 带缓存的读取
        List<DishDetail> dishDetailList = dishDetailService.lambdaQuery()
                .eq(DishDetail::getDishId, dish.getId()).list();
        return Result.success(dish + "::" + dishDetailList);
    }
}
```

```java
// DishServiceImpl.java - 核心缓存策略：逻辑过期 + 异步刷新 + 分布式锁
// RedisData 结构：{ data: 实际数据, expireTime: 逻辑过期时间 }
// Redis 物理 TTL 设为 24 小时（FINAL_TTL），逻辑过期设为 30 秒（PLUS_TTL）
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    private static final long RENEW_THRESHOLD = 10L;  // 续期阈值（秒）
    private static final long PLUS_TTL = 30L;          // 逻辑过期时间
    private static final long FINAL_TTL = 86400L;       // Redis 物理 TTL（24小时）

    // 核心查询方法：三级缓存策略
    public Dish readCache(Long id) {
        String value = stringRedisTemplate.opsForValue().get(KEY + id);

        // 1. 缓存不存在 → 查库并设置缓存
        if (value == null) {
            return getDish(id);
        }

        RedisData redisData = JSONUtil.toBean(value, RedisData.class);

        // 2. 缓存为空值（防穿透）→ 查库
        if (redisData.getData() == null) {
            return getDish(id);
        }

        // 3. 缓存存在且逻辑未过期 → 检查是否临近过期，异步续期
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            long remainSec = Duration.between(LocalDateTime.now(), redisData.getExpireTime()).getSeconds();
            if (remainSec < RENEW_THRESHOLD) {
                // 临近过期（<10秒），同步续期 expireTime，防止即将过期时大量请求触发刷新
                redisData.setExpireTime(LocalDateTime.now().plusSeconds(PLUS_TTL));
                stringRedisTemplate.opsForValue().set(KEY + id, JSONUtil.toJsonStr(redisData),
                        FINAL_TTL, TimeUnit.SECONDS);
            }
            return BeanUtil.toBean(redisData.getData(), Dish.class);
        }

        // 4. 缓存逻辑过期 → 异步获取分布式锁刷新，同步返回旧数据
        Dish dish = getDishCache(id);  // 异步刷新（非阻塞）
        if (dish == null) {
            // 异步刷新可能尚未完成，兜底返回旧数据
            return BeanUtil.toBean(redisData.getData(), Dish.class);
        }
        return dish;
    }

    // 异步刷新：线程池 + 分布式锁 + 双重检查
    private Dish getDishCache(Long id) {
        DISH_EXECUTOR.submit(() -> {
            RLock redissonLock = redissonClient.getLock("dish:lock:" + id);
            try {
                if (!redissonLock.tryLock(0, TimeUnit.SECONDS)) {
                    return;  // 获取锁失败，说明其他线程正在刷新
                }
                // 双重检查：防止多个异步任务同时进入后重复刷新
                String latestVal = stringRedisTemplate.opsForValue().get(KEY + id);
                RedisData latestData = JSONUtil.toBean(latestVal, RedisData.class);
                if (latestData != null && latestData.getData() != null
                        && latestData.getExpireTime().isAfter(LocalDateTime.now())) {
                    return;  // 已有其他线程刷新过，跳过
                }
                getDish(id);  // 刷新缓存
            } finally {
                if (redissonLock.isHeldByCurrentThread()) {
                    redissonLock.unlock();
                }
            }
        });
        return null;  // 立即返回，不等待刷新完成
    }

    // 写操作：更新数据库后主动删除缓存
    @Transactional(rollbackFor = Exception.class)
    public void updateCache(DishDTO dishDTO) {
        Dish dish = BeanUtil.toBean(dishDTO, Dish.class);
        super.updateById(dish);                        // 1. 更新数据库
        stringRedisTemplate.delete(KEY + dish.getId()); // 2. 删除缓存（Cache Aside 模式）
        // 3. 重建口味：先删后插
        dishDetailService.remove(new LambdaQueryWrapper<DishDetail>()
                .eq(DishDetail::getDishId, dish.getId()));
        dishDetailService.saveBatch(dishDTO.getDishDetails());
    }
}
```

### 问题修复阶段

Q：缓存逻辑过期方案中，RedisData 结构的物理 TTL 设为 24 小时但逻辑过期设为 30 秒，这种"双过期时间"的设计有什么潜在风险？

> **内存占用与一致性风险**：双过期时间的设计初衷是"物理不过期，逻辑控制过期"，但有两个风险：① **内存泄漏风险**：如果某个菜品被删除后，Redis 中的缓存 Key 会在 24 小时后才被物理清理，期间一直占用内存。改进方式是在删除菜品时主动 `delete` 缓存 Key，配合较短的逻辑过期时间，让数据尽快过期；② **数据不一致窗口**：逻辑过期后到物理过期前，系统返回的是旧数据，如果菜品价格/状态在这段时间内发生变化，用户看到的是过时信息。解决方案是写操作时主动删除缓存（Cache Aside 模式），读操作时即使逻辑过期也能通过异步刷新拿到最新数据。

Q：异步刷新缓存用 `synchronized` 或 `ReentrantLock` 替代 Redisson 分布式锁可行吗？它们的核心区别是什么？

> **单机锁 vs 分布式锁的适用场景**：`synchronized` 和 `ReentrantLock` 都是 JVM 级别的单机锁，只能在单实例内生效。如果部署多个 Spring Boot 实例，每个实例都有自己的锁，可能多个实例同时触发缓存刷新。Redisson 分布式锁基于 Redis 实现，跨 JVM 实例生效，确保全局只有一个线程刷新。选择依据是**部署架构**：单实例部署用 `ReentrantLock` 即可，无需引入 Redisson 依赖；多实例部署必须用分布式锁。项目预留 Redisson 是为了未来横向扩展，但当前单实例部署下可以用 `synchronized` 简化实现。

Q：DishServiceImpl 中的线程池配置为核心线程 5、队列 100、拒绝策略 CallerRunsPolicy。如果菜品缓存大量失效，这个配置会导致什么问题？

> **线程池参数调优**：当缓存大量失效时，每个请求都会提交一个缓存刷新任务到线程池。如果队列满（超过 100 个等待任务），`CallerRunsPolicy` 会让**调用线程（即处理 HTTP 请求的 Tomcat 线程）自己执行刷新任务**。这会导致两个严重问题：① Tomcat 线程被阻塞在数据库查询上，无法处理其他请求，造成整个应用响应变慢；② 如果请求本身需要等待刷新结果（同步路径），用户延迟会从 <1ms 涨到数百毫秒。改进方案是：增大核心线程数（按 CPU 核数 × 2 设置）、减小队列长度（让任务尽快拒绝而非排队）、使用自定义拒绝策略（如记录日志后直接丢弃非关键任务）。

---

## 四、套餐管理模块

### 需求阶段

需求背景：套餐是餐馆的核心商品组合，需要支持套餐 CRUD、套餐菜品关联、按分类查询等功能。套餐由多个菜品组成，一个套餐可包含多种菜品，每个菜品可设定份数。

- 套餐数据需要与菜品表关联，支持多菜品组合
- 套餐状态需要支持启用/停用切换
- 套餐查询需要支持分页和按名称搜索

### 策略流程图

```java
【新增流程】
用户请求 → AdminPlanController/create()
    → PlanDTO 转 Plan 实体 → planService.save(plan) → MySQL 保存主表
    → PlanDetail DTO 列表转实体 → planDetailService.saveBatch() → MySQL 批量保存菜品明细
    → @Transactional 事务提交 → 返回成功

【查询流程】
分页查询 → PlanController/readAll()
    → LambdaQueryWrapper 构建查询（状态=启用 + 名称模糊匹配）
    → new Page() 分页参数 → planService.page() → 返回分页数据

详情查询 → AdminPlanController/readById()
    → planService.readCache(id) → Redis 查缓存
    → planDetailService 查询关联菜品明细 → 返回主表+明细

【更新流程】
用户请求 → AdminPlanController/update()
    → planService.updateCache(planDTO)
    → BeanUtil 转实体 → super.updateById() → 更新 MySQL
    → 清空旧菜品明细 → 批量保存新菜品明细 → @Transactional 提交

【删除流程】
用户请求 → AdminPlanController/delete()
    → super.removeByIds(ids) → 删除主表
    → planDetailService.remove() → 批量删除关联菜品明细
    → 返回成功
```

### 编码阶段

```java
// 实体关系：Plan（套餐主表） 1:N PlanDetail（套餐菜品明细子表）
// Plan.java 对应 plan 表，PlanDetail.java 对应 plan_detail 表
// 通过 plan_id 外键关联，一个套餐可包含多个菜品（如：米饭x2、红烧肉x1、青菜x1）
```

```java
// AdminPlanController.java - 套餐管理核心接口
@RestController
@RequestMapping("/admin/plan")
public class AdminPlanController {

    // 新增套餐：主表+明细表在同一事务中保存
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result create(@RequestBody PlanDTO planDTO) {
        Plan plan = BeanUtil.toBean(planDTO, Plan.class);
        planService.save(plan);
        // 批量保存套餐菜品明细
        List<PlanDetail> planDetailList = planDTO.getPlanDetails().stream()
                .map(planDetail -> BeanUtil.toBean(planDetail, PlanDetail.class))
                .toList();
        planDetailService.saveBatch(planDetailList);
        return Result.success(OperationEnum.CREATE + "--" + plan.getId());
    }

    // 分页查询：按状态和名称筛选
    @GetMapping("/all")
    public Result readAll(DishPageDTO dishPageDTO) {
        LambdaQueryWrapper<Plan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Plan::getStatus, StatusConstant.ENABLE)  // 只查启用状态
                .like(dishPageDTO.getName() != null, Plan::getName, dishPageDTO.getName());
        IPage<Plan> page = new Page<>(dishPageDTO.getPage(), dishPageDTO.getPageSize());
        IPage<Plan> planPage = planService.page(page, queryWrapper);
        return Result.success(planPage);
    }

    // 查询套餐详情：主表+菜品明细
    @GetMapping
    public Result readById(@RequestParam Long id) {
        Plan plan = planService.readCache(id);  // 缓存读取
        List<PlanDetail> planDetailList = planDetailService.lambdaQuery()
                .eq(PlanDetail::getPlanId, plan.getId()).list();
        return Result.success(plan + "::" + planDetailList);
    }
}
```

### 问题修复阶段

Q：更新套餐时采用"先删后插"全量替换明细的方式，如果在高并发场景下两个请求同时更新会导致什么问题？

> **并发更新的竞态条件**：假设请求 A 和请求 B 同时更新同一个套餐：① 请求 A 删除旧明细 → 请求 B 也删除旧明细（无影响，因为已删除）→ 请求 A 插入新明细 → 请求 B 插入新明细。此时会出现**数据错乱**——两个请求的明细数据混合在一起。解决方案：① **乐观锁**——在 Plan 表增加 version 字段，更新时检查 version 是否匹配，不匹配则拒绝；② **分布式锁**——`RedissonClient.getLock("plan:update:" + id)` 锁定套餐，串行化更新操作；③ **唯一约束**——在 plan_detail 表增加 (plan_id, dish_id) 联合唯一索引，防止重复插入。

Q：PlanDetailService.saveBatch() 批量保存时，如果中间某条保存失败，会导致数据不一致吗？

> **事务边界与批量保存**：不会导致不一致，因为 `@Transactional(rollbackFor = Exception.class)` 注解了整个方法。`saveBatch()` 默认在一个事务中执行所有 INSERT，任何一条失败都会触发回滚。但要注意 `rollbackFor = Exception.class` 的必要性——Spring 默认只在 `RuntimeException` 时回滚，如果业务代码抛出受检异常（如 `IOException`），不会回滚。显式指定 `rollbackFor = Exception.class` 确保所有异常都触发回滚。另一个注意点是 `saveBatch()` 的默认批量大小（默认 1000），如果明细超过 1000 条，会分批执行，但仍在同一个事务中。

---

## 五、购物车管理模块

### 需求阶段

需求背景：用户浏览菜品和套餐后，可以将商品加入购物车，支持数量调整、删除、清空等操作。购物车是下单前的暂存区域，需要支持菜品和套餐混合存储。

- 购物车数据需要与用户关联，每个用户独立
- 支持菜品和套餐两种商品类型
- 数量调整需要实时计算金额

### 策略流程图

```java
【加入购物车流程】
用户请求 → ShoppingController/add()
    → 根据 dishId/setmealId 查询商品信息（菜品或套餐）
    → 检查购物车是否已存在该商品
    ├─ 已存在 → 数量+1，重新计算金额 → 更新记录
    └─ 不存在 → 创建新 OrderShopping 记录 → 计算金额（单价×数量）→ 保存
    → 返回成功

【查询购物车流程】
用户请求 → ShoppingController/list()
    → 获取当前用户 ID（从 SecurityContext）
    → OrderShoppingService 按 userId 查询所有购物车记录
    → 返回购物车列表（含商品信息、数量、金额）

【修改数量流程】
用户请求 → ShoppingController/update()
    → 按 ID 查询购物车记录 → 更新 number 字段
    → 重新计算 amount（单价×数量）→ 更新记录
    → 返回成功

【删除商品流程】
用户请求 → ShoppingController/delete()
    → 按 ID 删除 OrderShopping 记录
    → 返回成功

【清空购物车流程】
用户请求 → ShoppingController/clean()
    → 获取当前用户 ID → 批量删除该用户的所有购物车记录
    → 返回成功
```

### 编码阶段

```java
// OrderShopping.java - 购物车实体类
// 对应 order_shopping 表，存储用户选购的商品
@Data
@TableName("order_shopping")
public class OrderShopping implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String name;           // 商品名称（冗余存储）
    private Long userId;          // 用户ID
    private Long dishId;          // 菜品ID（菜品商品）
    private Long setmealId;       // 套餐ID（套餐商品）
    private String dishFlavor;    // 菜品口味
    private Long number;          // 数量
    private BigDecimal amount;    // 金额（单价 × 数量）
    private String image;         // 商品图片
    private LocalDateTime createTime;  // 创建时间
}
```

### 问题修复阶段

Q：OrderShopping 表中同时存在 dishId 和 setmealId 两个字段，这种设计在查询时如何处理空值判断？

> **多态关联查询的实现方式**：查询购物车列表时，需要根据 `dishId` 或 `setmealId` 分别查询对应的商品信息。如果字段为 null 就跳过查询。MyBatis-Plus 实现中通常用嵌套查询或 JOIN 关联：① 嵌套查询——先查 OrderShopping 列表，然后遍历每个记录，根据 dishId/setmealId 查询对应商品，这种 N+1 查询在购物车商品多时性能差；② JOIN 关联——用 SQL LEFT JOIN 关联 dish 和 plan 表，一次查询返回所有数据，性能更好。项目中选择方式①是因为购物车商品数量有限（通常 <20），N+1 查询影响可接受。

Q：购物车中商品金额 amount 字段是冗余存储的，如果菜品价格变动如何保持同步？

> **冗余字段的一致性维护**：冗余存储 amount 的目的是**减少 JOIN 查询**——展示购物车时不需要再关联菜品表查询价格。但冗余数据需要保持同步，有两种策略：① **实时计算**——查询购物车时实时计算 `单价 × 数量`，不存 amount 字段，缺点是每次查询都需要关联菜品表；② **写时同步**——用户加入购物车时计算并存储 amount，菜品价格变动时通过事件机制（如 @EventListener 监听菜品更新事件）批量更新购物车中的对应商品。项目选择策略①（冗余存储），但缺少价格同步机制——如果菜品涨价，用户购物车中的价格不会自动更新，需要用户手动删除后重新添加。

Q：购物车操作（加入、修改数量）如何防止并发问题？

> **并发控制策略**：假设用户在两个设备上同时修改同一商品的数量：设备 A 读到数量 2，设备 B 也读到数量 2，设备 A 更新为 3，设备 B 也更新为 3。结果应该是 4 但实际是 3（丢失更新）。解决方案：① **乐观锁**——在 OrderShopping 表增加 version 字段，更新时检查版本号，不匹配则重试；② **原子操作**——用 `UPDATE SET number = number + 1 WHERE id = ? AND user_id = ?` 的原子 SQL 代替先读后写；③ **Redis 缓存**——将购物车数据存在 Redis 的 Hash 结构中，用 `HINCRBY` 原子操作增加数量。项目当前实现是"先读后写"，在用户量不大的场景下可接受，但生产环境建议用原子 SQL 或 Redis。

---

## 六、订单管理模块

### 需求阶段

需求背景：订单是餐饮系统的核心业务闭环，从用户下单到骑手配送完成，需要完整的订单状态流转管理。订单涉及支付、退款、配送等多个环节。

- 订单状态流转：待支付 → 待接单 → 制作中 → 待取餐 → 配送中 → 已送达 → 已完成/已取消
- 需要支持订单超时自动取消
- 需要支持订单退款流程

### 订单状态流转

```
1 待支付 → 2 待商家接单 → 3 制作中 → 4 待骑手取餐 → 5 配送中 → 6 已送达 → 7 已完成
         ↓                                      
   8 已取消（未接单退款、商家拒单、超时取消、售后全额退款）
```

> **注意**：订单管理模块的 Controller 层尚未实现，以下为已实现的实体类、枚举和定时任务。

### 编码阶段

```java
// OrderStatusEnum.java - 订单状态枚举
public enum OrderStatusEnum {
    PENDING_PAYMENT(1, "待支付"),
    PENDING_ACCEPT(2, "待商家接单"),
    PREPARING(3, "制作中"),
    PENDING_RIDER_PICK(4, "待骑手取餐"),
    DELIVERING(5, "配送中"),
    DELIVERED(6, "已送达"),
    COMPLETED(7, "已完成"),
    CANCELLED(8, "已取消");
    
    private Integer value;
    private String description;
}
```

```java
// OrderTask.java - 订单超时处理定时任务
@Component
public class OrderTask {
    @Autowired
    private OrderService orderService;

    // 每小时检查待骑手取餐订单，提醒骑手取餐
    @Scheduled(cron = "0 0 * * * ?")
    public void processTimeoutOrder() {
        // 1. 查询待骑手取餐状态的订单
        List<Order> pendingOrders = orderService.lambdaQuery()
                .eq(Order::getStatus, OrderStatusEnum.PENDING_RIDER_PICK).list();
        if (!pendingOrders.isEmpty()) {
            log.info("有待处理订单：{}", pendingOrders);
        }
        // 2. 查询配送中且已超过开始配送时间的订单
        LocalDateTime now = LocalDateTime.now();
        List<Order> deliveringOrders = orderService.lambdaQuery()
                .eq(Order::getDeliveryStatusEnum, DeliveryStatusEnum.NOW).list();
        for (Order order : deliveringOrders) {
            if (order.getStartDeliveryTime() != null && now.isAfter(order.getStartDeliveryTime())) {
                log.info("id为{}订单需要开始派送了", order.getId());
            }
        }
    }
}
```

> **注意**：订单管理模块的问题修复阶段将在 Controller 层实现后补充。

---

## 七、支付系统模块

### 需求阶段

需求背景：订单支付是餐饮系统的资金入口，需对接第三方支付。项目选择支付宝电脑网站支付（沙箱环境），支持下单、查询、退款、关单、OAuth 授权登录。

- 支付流程涉及同步跳转（用户可见）和异步通知（服务端验签），两者职责不同
- 异步通知必须验签，防止伪造请求篡改订单状态
- 退款是逆向流程，需独立的退款查询接口确认到账

> **注意**：支付系统模块的支付 Controller（创建订单、支付、退款等）尚未实现，OAuth 授权登录已实现。

### 策略流程图（支付部分待实现）

```
【OAuth 授权登录 - 已实现】
GET /oauth/authorize?redirectUri= → 302 跳转支付宝授权页
GET /oauth/callback?auth_code= → auth_code 换 access_token → 拉取用户资料

【电脑网站支付 - 待实现】
→ AlipayService.createPagePayForm() → 构造 AlipayTradePagePayRequest
→ 设置 notifyUrl（异步）+ returnUrl（同步）+ timeout_express=60m
→ 返回支付宝收银台 HTML 表单

【异步通知 - 待实现】
→ POST /pay/notify（必须公网可访问）
→ AlipaySignature.rsaCheckV1 验签 → 校验金额 → 更新订单状态

【退款/关单 - 待实现】
→ POST /pay/refund → AlipayService.refund()
→ POST /pay/order/close → 超时未支付关闭交易
```

### 编码阶段

```java
// OAuthLogin.java - 支付宝授权登录（已实现）
// GET /oauth/authorize → 302 跳转支付宝授权页
@GetMapping("/authorize")
public void authorize(@RequestParam String redirectUri, HttpServletResponse response) throws IOException {
    String authorizeUrl = AUTHORIZE_URL
            + "?app_id=" + alipayProperties.getAppId()
            + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
            + "&scope=auth_user";
    response.sendRedirect(authorizeUrl);
}

// GET /oauth/callback?auth_code= → 换取 access_token + 获取用户资料
@GetMapping("/callback")
public Map<String, Object> callback(@RequestParam("auth_code") String authCode) throws AlipayApiException {
    // 1. auth_code 换取 access_token
    AlipaySystemOauthTokenRequest tokenReq = new AlipaySystemOauthTokenRequest();
    tokenReq.setCode(authCode);
    tokenReq.setGrantType("authorization_code");
    AlipaySystemOauthTokenResponse tokenResp = client.execute(tokenReq);

    String accessToken = tokenResp.getAccessToken();
    String openId = tokenResp.getOpenId();

    // 2. access_token 获取用户资料（昵称、头像、性别、邮箱）
    AlipayUserInfoShareRequest userReq = new AlipayUserInfoShareRequest();
    userReq.putOtherTextParam("auth_token", accessToken);
    AlipayUserInfoShareResponse userResp = client.execute(userResp);

    Map<String, Object> result = new HashMap<>();
    result.put("openId", openId);
    result.put("accessToken", accessToken);
    result.put("nickName", userResp.getNickName());
    result.put("avatar", userResp.getAvatar());
    return result;
}
```

### 问题修复阶段

Q：异步通知处理中，如何保证接口的幂等性——如果同一笔支付收到多次回调该怎么处理？

> **状态机校验实现幂等**：支付宝异步通知可能因为网络重试、用户重复点击等原因发送多次。处理流程：① 查询订单当前状态 → ② 检查订单是否已经处理过（如状态已是"已支付"）→ ③ 如果已处理，直接返回 "success"（告知支付宝已收到，不再重试）→ ④ 如果未处理，更新订单状态为"已支付"。这种"先查状态再操作"的模式是支付回调的标准幂等方案。更进一步，可以用数据库的**唯一约束**——在订单表增加 `pay_notified` 标记，处理前先检查标记，已标记则跳过。

Q：异步通知超时未返回 "success"，支付宝会重试通知。如果重试次数超过上限会怎样？

> **重试机制与最终一致性**：支付宝默认重试 8 次，间隔递增（15s → 15s → 30s → 3m → 10m → 20m → 30m → 30m）。如果全部失败，支付宝认为通知失败，但**支付实际是成功的**。此时商户需要主动查询订单状态进行对账。实现方式：① **定时对账**——每隔 1 小时查询"待支付但已超过 5 分钟"的订单，调用 `AlipayTradeQueryRequest` 查询支付宝侧状态；② **补偿机制**——如果对账发现支付宝已支付但本地订单未更新，手动更新订单状态。这是**最终一致性**的典型实现——通过主动查询弥补被动通知的不可靠性。

---

## 八、文件上传与 Excel 导出模块

### 需求阶段

需求背景：菜品图片、员工头像、用户头像需要上传；运营需要导出用户数据为 Excel 报表。

- 本地存储在多实例部署时文件不一致
- 大文件上传需限制大小（10MB）
- Excel 导出需支持流式下载，避免内存溢出

### 策略流程图

```
文件上传（本地） → POST /local → UUID生成文件名 → 保存到 ku/image 目录 → 返回本地路径
文件上传（OSS）  → POST /oss  → UUID生成文件名 → AliOssUtil上传到阿里云 → 返回 CDN URL
文件下载（本地） → GET /local?fileName= → 读取本地文件 → Content-Disposition → 返回流
文件下载（OSS）  → GET /oss?url= → 从 OSS URL 拉取字节流 → 返回 ResponseEntity<byte[]>
Excel 导出       → POST /report/excel/write → EasyExcel.write → 写入 report.xlsx
Excel 读取       → POST /report/excel/read  → EasyExcel.read → AnalysisEventListener 解析
Excel 下载       → GET /report/excel/download → 流式下载
```

### 编码阶段

```java
// FileController.java - 本地文件上传（UUID 防冲突 + URLEncoder 防中文乱码）
private static final String PATH = "ku/image";

@PostMapping
public Result upload(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    File path = new File(PATH);
    if (!path.exists()) path.mkdirs();
    // UUID + 原扩展名，避免文件名冲突和文件遍历攻击
    String saveName = UUID.randomUUID().toString() +
            originalFilename.substring(originalFilename.lastIndexOf("."));
    file.transferTo(new File(path, saveName));
    return Result.success(path + "::" + saveName);
}

@GetMapping
public void download(String fileName, HttpServletResponse response) {
    File path = new File(PATH, fileName);
    response.setContentType("application/octet-stream");
    // 中文文件名用 URLEncoder 编码，避免下载乱码
    String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    response.setHeader("Content-Disposition", "attachment;filename=" + encoded);
    try (InputStream in = new FileInputStream(path)) {
        StreamUtils.copy(in, response.getOutputStream());
    }
}
```

```java
// AliOssUtil.java - 阿里云 OSS 上传
public String uploadFile(String objectName, InputStream inputStream) {
    OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    try {
        ossClient.putObject(bucketName, objectName, inputStream);
    } finally {
        ossClient.shutdown();  // 必须关闭，否则连接泄漏
    }
    // 返回 CDN 访问 URL：https://{bucket}.{endpoint}/{objectName}
    return "https://" + bucketName + "." + endpoint + "/" + objectName;
}
```

### 问题修复阶段

Q：阿里云 OSS 上传时，为什么要配置 Bucket 的 ACL 为公共读？如果用私有读会有什么问题？

> **访问控制与资源暴露**：Bucket ACL 有三种类型：`private`（私有读）、`public-read`（公共读）、`public-read-write`（公共读写）。项目用 `public-read` 是因为菜品图片需要展示给所有用户——如果用 `private`，每次访问图片都需要生成临时签名 URL（有效期 15 分钟），前端无法直接展示。安全风险是任何人知道 URL 就能访问图片，但 OSS 的 URL 是随机生成的，且不含敏感信息，风险可控。如果有敏感文件（如身份证照片），必须用 `private` + 临时签名 URL 的方案。

Q：EasyExcel 导出大量数据时，如何避免 OOM（OutOfMemoryError）？

> **流式写入与分批次处理**：EasyExcel 的 `write()` 方法底层使用**流式写入**，而不是一次性加载所有数据到内存。实现步骤：① 用 `pageHelper.startPage()` 分页查询数据，每次查 1000 条；② 遍历分页结果，调用 `easyExcel.write()` 的 `doWrite()` 方法逐行写入；③ 每写完一批释放内存。关键配置是设置 `inMemory(false)`——让 EasyExcel 使用文件临时存储而非内存缓存。如果数据量超过 100 万行，需要考虑异步导出（导出到 OSS 后返回下载链接），同步导出会导致 HTTP 超时。

---


## 九、AOP 操作日志模块

### 需求阶段

需求背景：管理端的增删改查操作需记录审计日志，便于追溯谁在什么时间做了什么操作。同时业务方法需记录执行耗时用于性能监控。

- 操作日志需自动记录，不能侵入业务代码
- 需区分操作类型（CREATE/READ/UPDATE/DELETE）
- 需记录操作人、操作结果（成功/失败）、入参

### 策略流程图

```
业务方法标注 @OperationLogging(operation=CREATE)
    → ServiceInterceptAspect 拦截 → 执行目标方法
    ├─ 成功 → OperationType.ok(operation, args) → log.info
    └─ 异常 → OperationType.error(operation, args) → log.info → 异常继续抛出

业务方法标注 @Info(desc="描述")
    → ServiceInterceptAspect 拦截 → 记录目标类/方法/入参/耗时/返回值/异常
```

| 人、操作结果，入参 | <img src="D:\a.github\restaurant-payment\说明\原型功能\日志记录.png" style="zoom:75%;" /> |
| ------------------ | ------------------------------------------------------------ |



### 编码阶段

```java
// OperationLogging.java - 操作日志注解
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLogging {
    // 操作类型：CREATE / READ / UPDATE / DELETE
    OperationEnum operation() default OperationEnum.CREATE;
}

// Info.java - 方法信息注解
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Info {
    String desc() default "未描述";        // 方法描述
    boolean recordCostTime() default true; // 是否记录耗时
}
```

```java
// ServiceInterceptAspect.java - 双切面：@Info 耗时监控 + @OperationLogging 操作日志
@Aspect
@Component
public class ServiceInterceptAspect {

    // @OperationLogging 切面：自动记录操作日志
    @Around("@annotation(start.aop.OperationLogging)")
    public Object interceptOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationLogging annotation = signature.getMethod().getAnnotation(OperationLogging.class);
        String operation = annotation.operation().name();  // CREATE/READ/UPDATE/DELETE
        String methodArgs = Arrays.toString(joinPoint.getArgs());
        try {
            Object result = joinPoint.proceed();  // 执行业务方法
            OperationType.ok(operation, methodArgs);  // 成功日志
            return result;
        } catch (Exception e) {
            OperationType.error(operation, methodArgs);  // 失败日志
            throw e;  // 异常继续抛，保证全局异常处理器能处理
        }
    }
}
```

```java
// OperationType.java - 操作日志实体（用 log.info 输出，确保默认可见）
public static OperationType ok(String operation, Object message) {
    OperationType op = new OperationType();
    op.operation = operation;
    op.id = SecurityContextParam.getCurrentUserId();  // 从 SecurityContext 取操作人
    op.status = "SUCCESS";
    op.message = message;
    log.info("用户ID:" + op.id + ", 执行操作:" + op.operation + ", " + message + ", " + op.status);
    return op;
}
```

### 问题修复阶段

Q：@OperationLogging 注解中定义的 name 和 method 字段，在 Spring AOP 中是如何传递给切面处理逻辑的？

> **自定义注解的运行时获取**：自定义注解的处理流程：① 在切面类用 `@Around("@annotation(operationLogging)")` 匹配所有标注了 `@OperationLogging` 的方法，`operationLogging` 参数就是注解实例；② 通过 `operationLogging.name()` 获取注解中定义的值；③ 如果注解需要动态参数（如操作人、操作时间），可以从切点信息（`ProceedingJoinPoint`）中获取方法参数，结合注解值构造日志内容。这种设计的好处是**编译期检查**——如果注解不存在或参数缺失，编译时报错。相比在方法调用前手动记录日志（如 `log.info("用户" + userId + "执行了" + methodName)`），注解方式将日志逻辑与业务逻辑完全解耦。

Q：AOP 切面中获取方法参数时，如何处理参数中的敏感信息（如密码、Token）？

> **参数脱敏策略**：`ProceedingJoinPoint.getArgs()` 返回方法的所有参数，可能包含敏感数据（如 `login(UserLoginDTO dto)` 中的 `password` 字段）。如果直接打印，会导致敏感信息泄露。处理方式：① **反射获取字段**——通过 `args[i].getClass().getDeclaredField("password")` 获取字段，判断是否为敏感字段后进行脱敏（如只显示前 2 位 + `***`）；② **注解标记**——定义 `@Sensitive` 注解标记敏感字段，切面检测到标记后自动脱敏；③ **白名单打印**——只打印非敏感字段，敏感字段直接标记为 `[FILTERED]`。生产环境建议用方式②，通过注解显式标记，避免硬编码字段名。

---

## 十、AI 视觉识别服务模块（ai-see 独立服务）

### 需求阶段

需求背景：用户上传菜品图片，系统自动识别图片中的食物/饮料，并匹配对应套餐推荐。这是项目的差异化能力，用 Spring AI + 视觉模型 + Function Calling 实现。

- 单次调用需串联「视觉识别」和「套餐查询」两个步骤，是多节点编排
- 套餐查询需支持多条件组合（ID/名称/分类/价格/状态/描述）
- 需要对话记忆，支持多轮交互

### 策略流程图

```
用户上传图片+问题 → POST /ai/see
    → 图片转 Base64 → CompiledGraph.invoke({question, file})
    → node1: VisualFunction（视觉识别节点）
        → Media(image/jpeg, base64) → visualChatClient.prompt("识别有哪些食物,饮料？")
        → 输出 visualResult（如"鱼、啤酒、豆腐"）
    → node2: ToolFunction（工具查询节点）
        → toolClient.prompt(visualResult) → 触发 @Tool 方法
        → SetmealTool.queryByDescription(关键词) → LambdaQueryWrapper 模糊匹配
        → 输出 toolResult（匹配的套餐列表）
    → 返回 "visualResult + toolResult"
```

### 编码阶段

```java
// NodeLink.java - Spring AI Alibaba Graph 流程编排
// 定义 StateGraph，串联 visualFunction → toolFunction 两个节点
@Bean("see")
public CompiledGraph toSee() {
    KeyStrategyFactory strategyFactory = () -> Map.of(
            "visualResult", new ReplaceStrategy(),  // 视觉识别结果（覆盖策略）
            "toolResult", new ReplaceStrategy()     // 工具查询结果（覆盖策略）
    );
    StateGraph graph = new StateGraph("see", strategyFactory);
    // 节点
    graph.addNode("node1", AsyncNodeAction.node_async(visualFunction));  // 视觉识别
    graph.addNode("node2", AsyncNodeAction.node_async(toolFunction));    // 工具查询
    // 边：START → node1 → node2 → END
    graph.addEdge(StateGraph.START, "node1");
    graph.addEdge("node1", "node2");
    graph.addEdge("node2", StateGraph.END);
    return graph.compile();
}
```

```java
// VisualFunction.java - 视觉识别节点（NodeAction 实现）
// 从 state 取 Base64 图片，构造 Media 调用多模态模型识别食物
@Override
public Map<String, Object> apply(OverAllState state) throws Exception {
    String base64 = (String) state.value("file").orElse("文件为空");
    Media media = new Media(MimeTypeUtils.IMAGE_JPEG,
            URI.create("data:image/jpeg;base64," + base64));
    String result = visualClient.prompt()
            .user(promptUserSpec -> promptUserSpec.text("识别有哪些食物,饮料？").media(media))
            .call()
            .content();
    return Map.of("visualResult", result != null ? result : "没有识别到内容");
}
```

```java
// SetmealTool.java - Function Calling 工具（@Tool 注解，AI 自动调用）
@Tool(description = "根据图片识别出的食材、饮品关键词，模糊匹配套餐的菜品描述字段")
public List<Setmeal> queryByDescription(@ToolParam(description = "食物、饮料关键词") SetmealToolParam param) {
    LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
    String key = param.getDescription() == null ? "" : param.getDescription().trim();
    if (!key.isEmpty()) {
        wrapper.like(Setmeal::getDescription, key);  // 模糊匹配描述
    }
    wrapper.like(Setmeal::getName, key);  // 同时匹配名称
    return setmealMapper.selectList(wrapper);
}
```

```java
// SpringAiConfig.java - 对话记忆配置（MessageWindowChatMemory 滑动窗口）
@Bean
public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
    return MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(20)  // 保留最近 20 条消息，超出自动淘汰
            .build();
}
```

### 问题修复阶段

Q：Function Calling（工具调用）的底层机制是如何实现的？LLM 如何知道调用哪个函数？

> **Schema 定义与意图识别**：Function Calling 的实现分三步：① **定义工具 Schema**——用 `ToolCallbackDefinition.builder().name("getPlanList").description("获取套餐列表").inputSchema(...)` 定义工具名称、描述、输入参数的 JSON Schema；② **发送给 LLM**——调用 `chatClient.prompt()...tools(planTools)` 时，Spring AI 自动将 Schema 注入到 System Prompt 中，告知 LLM 可用的工具列表；③ **LLM 识别并调用**——LLM 根据用户意图判断需要调用的工具，返回 `ToolCall` 对象（包含工具名和参数），Spring AI 自动反射调用对应方法并将结果回填到对话上下文。关键是**描述的准确性**——`description` 字段直接影响 LLM 的工具选择准确率。

Q：多节点 Graph 编排中，如果 node1（视觉识别）执行超时或失败，node2（工具查询）如何处理？

> **状态机的容错机制**：Graph 框架的容错策略：① **超时配置**——为每个节点配置 `timeout` 参数，超时后节点抛出 `NodeTimeoutException`；② **异常传播**——节点异常会导致 Graph 执行中断，返回错误状态；③ **回退策略**——可以在 Graph 中配置 `fallback` 节点，当主节点失败时执行回退逻辑。项目中如果视觉识别失败，可以返回"无法识别菜品，请手动选择"的提示，而不是直接报错。实现方式：在 `VisualFunction` 中 try-catch 捕获异常，设置 `OverAllState.errorMessage` 字段，然后在 node2 之前增加条件边，判断是否有错误信息。

Q：Spring AI 的 ChatClient 中，system、user、assistant 三种消息角色有什么区别？

> **对话历史的角色分工**：三种角色对应 LLM 对话中的不同语义层次：① **System**——系统提示词，定义 AI 的行为规则（如"你是一个菜品识别助手"），在整个对话中保持不变；② **User**——用户输入，来自用户的问题或指令（如"识别这张图片的菜品"）；③ **Assistant**——AI 回复，包括文本回复和工具调用结果。Spring AI 的 `ChatClient` 通过 `.system(prompt)`、`.user(message)`、`.assistant(response)` 方法区分角色。多轮对话时，Spring AI 自动维护消息历史（`ChatMemory`），将前一轮的 Assistant 回复作为上下文传递给下一轮。

---


## 十一、定时任务模块

### 需求阶段

需求背景：餐饮订单存在超时未支付自动取消、配送时间到点自动提醒等场景，需要定时任务周期性扫描数据库，处理超时订单和触发业务逻辑。

- 订单状态流转需要定时检测（如待骑手取餐超时提醒）
- 配送时间到达时需要自动推送通知
- 定时任务应轻量执行，避免对数据库造成压力

### 策略流程图

```
Spring Scheduled 定时触发 → @Scheduled(cron = "0 0 * * * ?") 每小时执行
    → OrderTask.processTimeoutOrder()
    → 查询 PENDING_RIDER_PICK 状态的订单
    → 查询配送状态为 NOW 且已超过开始配送时间的订单
    → log.info 输出提醒日志（实际项目中可扩展为 WebSocket 推送）
```

### 编码阶段

```java
// OrderTask.java - 订单超时处理定时任务
// @Scheduled cron 表达式：秒 分 时 日 月 周（每小时整点执行）
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0 0 * * * ?")  // 每小时执行一次
    public void processTimeoutOrder() {
        // 1. 待骑手取餐状态的订单（PENDING_RIDER_PICK）
        List<Order> pendingOrders = orderService.lambdaQuery()
                .eq(Order::getStatus, OrderStatusEnum.PENDING_RIDER_PICK).list();
        if (!pendingOrders.isEmpty()) {
            log.info("有待处理订单：{}", pendingOrders);
        }

        // 2. 配送中且已超过开始配送时间的订单
        List<Order> deliveringOrders = orderService.lambdaQuery()
                .eq(Order::getDeliveryStatusEnum, DeliveryStatusEnum.NOW).list();
        LocalDateTime now = LocalDateTime.now();
        for (Order order : deliveringOrders) {
            if (order.getStartDeliveryTime() != null && now.isAfter(order.getStartDeliveryTime())) {
                log.info("id为{}订单需要开始派送了", order.getId());
            }
        }
    }
}
```

### 问题修复阶段

Q：@Scheduled 定时任务在集群部署场景下如何避免多个实例重复执行？

> **分布式锁与调度方案**：如果部署多个 Spring Boot 实例，每个实例都会独立执行定时任务，导致重复处理。解决方案：① **分布式锁**——在任务方法开头用 `RedissonClient.getLock("order:timeout:lock").lock()` 获取锁，只有获取锁的实例执行任务，其他实例跳过；② **ShedLock**——第三方库，基于数据库实现分布式锁，自动处理锁的获取和释放；③ **定时调度中间件**——将定时任务交给 XXL-JOB、Elastic-Job 等分布式调度框架，由中心调度器统一分发任务。项目单实例部署下不需要此处理，但未来多实例部署时必须引入。

Q：Cron 表达式 "0 0 * * * ?" 每小时执行一次，如果任务执行时间超过 1 小时会发生什么？

> **任务重叠与线程池竞争**：Spring 的 `ThreadPoolTaskScheduler` 支持并发执行定时任务——如果上一个任务还没执行完，下一个任务会在新线程中启动。这会导致：① 数据库查询压力倍增（多个线程同时扫描订单表）；② 业务逻辑重复执行（同一订单可能被多次处理）。解决方案：① **串行化任务**——配置 `spring.task.scheduling.pool.size=1`，确保定时任务串行执行；② **分布式锁**——即使并发执行，通过 Redis 锁保证同一订单只被处理一次；③ **优化任务耗时**——将批量操作改为增量操作，如用 `UPDATE status = 8 WHERE status = 1 AND create_time < NOW() - INTERVAL 30 MINUTE` 一条 SQL 完成，避免全表扫描。

---

## 十二、店铺状态管理模块

### 需求阶段

需求背景：餐饮系统需要支持营业/打烊状态切换，管理端可手动设置店铺状态，用户端查询店铺状态决定是否允许下单。

- 店铺状态存储在 Redis 中，读写快速
- 管理端可随时切换状态（营业中/已打烊）
- 用户端只读，根据状态显示下单入口或关闭提示

### 策略流程图

```
管理端设置状态 → AdminShoppingController.updateStatus()
    → POST /admin/shop/{status}
    → 1=营业中, 0=已打烊
    → 写入 Redis（key=SHOP_STATUS）
用户端查询状态 → ShoppingController.read()
    → GET /user/shop
    → 读取 Redis 中的营业状态
    → 返回给前端展示
```

### 编码阶段

```java
// AdminShoppingController.java - 管理端店铺状态控制
@RestController
@RequestMapping("/admin/shop")
public class AdminShoppingController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 切换营业状态：1=营业中，0=已打烊
    @PostMapping("{status}")
    public Result updateStatus(@PathVariable Long status) {
        String statusText = status == 1 ? "营业中" : "已打烊";
        stringRedisTemplate.opsForValue().set(ShopConstant.SHOP_STATUS, statusText);
        return Result.success(OperationEnum.CREATE + statusText);
    }

    // 查询当前营业状态
    @GetMapping
    public Result read() {
        String status = stringRedisTemplate.opsForValue().get(ShopConstant.SHOP_STATUS);
        if (status == null) {
            status = "已打烊";  // 默认打烊状态
        }
        return Result.success(OperationEnum.READ + "--" + status);
    }
}
```

### 问题修复阶段

Q：店铺状态存储在 Redis 中，如果 Redis 缓存穿透（key 不存在）或雪崩（Redis 宕机），系统如何降级处理？

> **降级策略实现**：Redis 存储的店铺状态有两个风险场景：① **缓存穿透**——每次查询 key 都不存在（如 Redis 刚启动），请求直接穿透到 MySQL。解决方案是在 `AdminShopController` 的 `read()` 方法中增加 MySQL 兜底查询：`if (shopStatus == null) { shopStatus = shopMapper.selectOne(...); }`；② **缓存雪崩**——Redis 宕机，所有请求都穿透到 MySQL。解决方案是用 Spring 的 `@Cacheable` + `sync = true` 配置，当缓存不可用时走同步读取，或用 Sentinel/Cluster 模式保证 Redis 高可用。更进一步，可以用本地缓存（如 `ConcurrentHashMap`）做二级缓存，当 Redis 不可用时从本地缓存读取。

---

# 核心组件设计

### 1. 双端认证过滤器链（SecurityFilterChain）

```java
// SecurityConfig.java - 三层过滤器链设计
http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/user/register", "/user/login").permitAll()      // 用户端登录注册放行
        .requestMatchers("/admin/register", "/admin/login").permitAll()    // 管理端登录注册放行
        .requestMatchers("/admin/**", "/user/**").authenticated()          // 两端业务接口需认证
        .anyRequest().permitAll())                                         // 其他放行
    // 过滤器顺序：user → emp → 兜底
    .addFilterBefore(userRefreshRequestFilter, UsernamePasswordAuthenticationFilter.class)
    .addFilterBefore(employeeRefreshRequestFilter, UsernamePasswordAuthenticationFilter.class)
    .addFilterBefore(informationRequestFilter, UsernamePasswordAuthenticationFilter.class);
```

Q：自定义过滤器如何获取当前请求的 HttpServletRequest 和 HttpServletResponse？

> **过滤器的生命周期与参数注入**：自定义过滤器实现 `OncePerRequestFilter` 接口后，Spring 自动将 `HttpServletRequest` 和 `HttpServletResponse` 作为方法参数注入到 `doFilterInternal()` 方法中。这是因为 `OncePerRequestFilter` 继承自 `GenericFilterBean`，在 `doFilter()` 方法中解析 Servlet API 参数并传递给 `doFilterInternal()`。获取当前请求的方式：① 方法参数——直接用 `request.getHeader("Authorization")` 获取请求头；② 静态方法——用 `ServletRequestAttributes.getRequest()` 获取（需在 RequestContextHolder 开启线程绑定）。推荐用方式①，因为更清晰且避免线程安全问题。

Q：InformationRequestFilter 作为兜底过滤器，它的执行时机和核心职责是什么？

> **最终防线与异常处理**：`InformationRequestFilter` 注册在最后，职责是**检查前两个过滤器是否已设置 SecurityContext**。执行时机：前两个 RefreshRequestFilter 处理完后，如果 SecurityContext 为空（说明没有匹配的用户类型 Token），`InformationRequestFilter` 返回 401 错误。核心职责：① **认证结果校验**——如果前两个过滤器都未处理该 Token（类型不匹配或 Token 无效），说明请求未认证；② **统一错误响应**——返回 JSON 格式的 401 响应（`Result.error(401, "未登录")`），而不是 Spring Security 默认的 HTML 重定向；③ **日志记录**——记录认证失败的请求 IP 和路径，便于安全审计。这种设计确保了即使前两个过滤器都"放过"了请求，最后仍有一道防线兜底。

Q：多个过滤器的执行顺序如何保证？

> **Servlet Filter 的链状执行模型**：Spring Security 的过滤器链基于 Servlet Filter 的标准机制——请求按注册顺序依次经过每个过滤器，每个过滤器决定是继续传递（`filterChain.doFilter()`）还是中断返回错误。项目中 `UserRefreshRequestFilter` 先执行，如果 Token 类型不匹配（不是 user），直接 `doFilter()` 放行给下一个；`EmployeeRefreshRequestFilter` 同样逻辑；最后 `InformationRequestFilter` 兜底检查。这种"接力式"设计的关键是**每个过滤器只做自己的判断，不做决策**，决策留给下游（要么是 Controller，要么是 401 拒绝）。

### 2. Spring Cache 缓存抽象

```java
// 项目统一用 Spring Cache 声明式注解，底层切换 Redis
// @Cacheable：查询时缓存（key 用 SpEL，如 #type）
// @CacheEvict：写操作后清除缓存（allEntries=true 清整个命名空间）
// @CacheConfig：类级别统一 cacheNames，方法级只写 key
```

Q：@Cacheable 注解的 key 属性支持 SpEL 表达式，如何实现动态缓存 Key？

> **SpEL 表达式解析与动态 Key 生成**：`@Cacheable(key = "#type")` 中的 `#type` 是 SpEL 表达式，Spring 在运行时解析方法参数并生成缓存 Key。例如 `getCategoryByType(Integer type)` 方法中，`#type` 会被替换为实际参数值（1 或 2），生成 Key 为 `restaurantCategory:type::1` 或 `restaurantCategory:type::2`。更复杂的动态 Key 可以用 T（类）调用静态方法：`key = "#id + '_' + T(java.util.UUID).randomUUID()"`。注意 SpEL 表达式中，方法参数用 `#` 前缀，变量用 `#result`（返回值）、`#root.args[0]`（第一个参数）等内置变量。

Q：@CacheEvict(allEntries=true) 和 @CacheEvict(key = "#id") 两种清除策略的底层实现有什么区别？

> **精确清除 vs 批量清除**：`key = "#id"` 精确清除会生成完整的缓存 Key（如 `dishCache::123`），底层调用 Redis 的 `DEL` 命令同步删除单个 Key。`allEntries=true` 批量清除则调用 `SCAN` + `DEL` 遍历并删除命名空间下的所有 Key。选择依据：① 如果能精确知道受影响的 Key（如只更新单个菜品），用 `key` 指定精确清除，避免误删其他缓存；② 如果更新会影响整个列表（如添加/删除分类），用 `allEntries=true` 批量清除，强制下次查询从数据库重建。项目中分类管理用 `allEntries=true`，因为修改任何分类都会影响整个分类列表的完整性。

### 3. 多模态 AI 编排（StateGraph）

```java
// Spring AI Alibaba Graph 的 StateGraph 模型
// 节点（NodeAction）+ 边（Edge）+ 状态（OverAllState）
// 适合多步骤、有状态流转的 AI 工作流
```

---

# 依赖说明

### admin功能依赖

| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| Spring Boot Starter Web | 3.3.8 | AdminDishController/AdminPlanController/AdminOrderController 提供管理端 REST 接口；MultipartFile 文件上传支持 |
| Spring Boot Starter Security | 3.3.8 | SecurityConfig 配置认证规则；MultiLoginAuthenticationProvider 双端登录认证；BCryptPasswordEncoder 密码加密 |
| MyBatis Plus | 3.5.9 | DishMapper/PlanMapper/OrderMapper 实现菜品、套餐、订单数据 CRUD；AutoMetaObjectHandler 自动填充 createTime/updateTime |
| Spring Boot Starter Data Redis | 3.3.8 | @Cacheable/@CacheEvict 声明式缓存分类和套餐数据；RedisCacheManager 统一 TTL 配置 |
| JJWT API/Impl/Jackson | 0.12.6 | JwtUtil 生成员工 Token（TYPE=emp）；EmployeeRefreshRequestFilter 校验 Token 并实现滑动过期 |
| Hutool All | 5.8.26 | BeanUtil DTO 转实体；StrUtil 判空；JSONUtil 序列化/反序列化 |
| Redisson | 3.52.0 | 分布式锁支持（预留扩展） |
| Spring Boot Starter AOP | 3.3.8 | ServiceInterceptAspect 切面实现 @OperationLogging 操作日志和 @Info 耗时监控 |

### user功能依赖

| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| Spring Boot Starter Web | 3.3.8 | DishController/PlanController/ShoppingController 提供用户端 REST 接口；WebSocketServer 实时消息推送 |
| Spring Boot Starter Data Redis | 3.3.8 | 存储用户 Token（`restaurant:user:{userId}`）；存储店铺营业状态（`SHOP_STATUS`）；Spring Cache 缓存分类和套餐数据 |
| MyBatis Plus | 3.5.9 | UserMapper/OrderShoppingMapper 实现用户、购物车数据 CRUD；UserService 查询用户信息 |
| JJWT API/Impl/Jackson | 0.12.6 | JwtUtil 生成用户 Token（TYPE=user）；UserRefreshRequestFilter 校验 Token 并实现滑动过期 |
| Hutool All | 5.8.26 | BeanUtil 对象属性拷贝；BooleanUtil 布尔值判断；UUID 生成文件名 |
| Aliyun SDK OSS | 3.17.4 | UserController 实现用户头像上传到阿里云 OSS，返回 CDN 访问 URL |



### 支付功能依赖

| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| Alipay SDK Java | 4.40.658.ALL | AlipayService 实现电脑网站支付（AlipayTradePagePayRequest）、交易查询、退款、退款查询、关单；OAuthLogin 实现 auth_code 换 access_token |
| Spring Boot Starter Web | 3.3.8 | 提供 HttpServletResponse 流式输出支付宝收银台 HTML；@ConfigurationProperties 绑定 AlipayProperties |

### AI 视觉识别功能依赖

| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| Spring AI BOM | 1.1.0 | 统一管理 Spring AI 版本 |
| spring-ai-starter-model-openai | 1.1.0 | OpenAiChatModel 提供多模态对话能力；对接硅基流动 Qwen 模型 |
| spring-ai-alibaba-graph-core | 1.1.0.0 | StateGraph 编排视觉识别节点和工具查询节点；OverAllState 在节点间传递状态 |
| Spring AI Advisors Vector Store | 1.1.0 | MessageChatMemoryAdvisor 对话记忆；SimpleLoggerAdvisor 日志审计 |

### 文件上传功能依赖

| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| Aliyun SDK OSS | 3.17.4 | AliOssUtil 实现文件上传到阿里云 OSS，返回 CDN 访问 URL |
| Spring Boot Starter Web | 3.3.8 | MultipartFile 文件上传支持；StreamUtils 流式下载 |
| EasyExcel | 3.3.2 | ExcelReportController 实现 Excel 读写下载（当前代码已注释，待启用） |

### AOP 操作日志功能依赖

| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| Spring Boot Starter AOP | 3.3.8 | ServiceInterceptAspect 切面拦截 @Info/@OperationLogging 注解；@Around 环绕通知记录耗时和操作日志 |
| Spring Boot Starter Security | 3.3.8 | OperationType 通过 SecurityContextParam.getCurrentUserId() 获取操作人 ID |

---

### 对比分析

**问题1：单过滤器处理双端 Token**

```java
// 其他人写法 - 混乱！
public class SingleFilter extends OncePerRequestFilter {
    protected void doFilterInternal(...) {
        // 一个过滤器里 if-else 判断 user/emp，逻辑耦合，难以维护
        if (type.equals("user")) { ... }
        else if (type.equals("emp")) { ... }
    }
}
```
> 本项目改进：拆为 `UserRefreshRequestFilter` + `EmployeeRefreshRequestFilter` 两个过滤器，各自只处理自己类型的 Token，非自己类型的直接放行。职责单一，互不干扰。

**问题2：操作日志，aop切入定位接口那些成功那些失败，方便运维**

```java
// 其他人写法 - 看不到日志！
public static OperationType ok(String operation, Object message) {
    log.debug("操作:" + operation);  // 默认 INFO 级别，debug 不输出
    return op;
}
```
> 本项目改进：改为 `log.info(...)`，确保默认配置下操作日志可见。也可在 `application.yml` 开启 `logging.level.start.oparation: debug` 替代。

**问题3：异步通知不验签**

```java
// 其他人写法 - 危险！
@PostMapping("/notify")
public String notify(HttpServletRequest request) {
    String outTradeNo = request.getParameter("out_trade_no");
    orderService.updateStatus(outTradeNo, PAID);  // 不验签直接更新，可被伪造
    return "success";
}
```
> 本项目改进：异步通知必须 `AlipaySignature.rsaCheckV1` 验签，校验 `app_id`、金额一致性，做幂等检查（订单是否已处理）。验签失败返回 `failure` 让支付宝重试，避免丢失通知。

---

# 前端说明

## 管理端界面

技术栈：Vue 3 + Element Plus + Pinia + Vue Router + Vite

| 功能页面 | 截图 |
| :--: | :--: |
| 登录页面 | <img src="说明/原型功能/admin服务端1.png" alt="管理端登录" style="zoom: 25%;" /> |
| 菜谱分类 | <img src="说明/原型功能/admin服务端2.png" alt="首页" style="zoom: 25%;" /> |
| 员工管理 | <img src="说明/原型功能/admin服务端3.png" alt="首页" style="zoom: 25%;" /> |
| 工作台 | <img src="说明/原型功能/admin服务端4.png" alt="套餐管理" style="zoom:25%;" /> |
| 菜品管理 | <img src="说明/原型功能/admin服务端5.png" alt="首页" style="zoom:25%;" /> |
| 套餐管理 | <img src="说明/原型功能/admin服务端6.png" alt="首页" style="zoom:25%;" /> |
| 订单作台 | <img src="说明/原型功能/admin服务端7.png" alt="首页" style="zoom:25%;" /> |
| 店铺管理 | <img src="说明/原型功能/admin服务端8.png" alt="首页" style="zoom:25%;" /> |

## 用户端说明



---
