# TakeOut 餐饮订单和支付系统

一个基于 Spring Boot 3 + Vue 3 的全栈外卖餐饮管理系统，前后端分离架构。提供完整的用户端和管理端功能，支持菜品管理、套餐管理、订单处理、支付集成、实时通信等核心能力。

------

# 后端说明

```
* 订单状态：
* 1 待支付：下单未付款
* 2 待商家接单：已付款，商家还没接单
* 3 商家接单，制作中：商家确认接单，正在做菜
* 4 待骑手取餐：商家出餐完成，骑手还没到店
* 5 骑手已取餐，配送中：骑手拿到餐，在路上，实时看定位
* 6 骑手已送达：骑手点送达，等待用户确认
* 7 订单已完成：系统自动确认收货
* 8 订单已取消：未接单退款、商家拒单、超时取消、售后全额退款
```

**支付流程**：

| 集成到order  | ![支付](说明/支付功能/支付宝1.png) |
| ------------ | ---------------------------------- |
| 支付过程     | ![支付](说明/支付功能/支付宝2.png) |
| 同步支付成功 | ![支付](说明/支付功能/支付宝3.png) |
| 异步检验     | ![支付](说明/支付功能/支付宝4.png) |

------



## 一、用户与员工认证模块

### 需求阶段

**需求背景**：项目需要双端认证系统，支持用户端（消费者）和管理端（员工/管理员）的登录、注册、权限控制。

**痛点**：
- 用户和管理员需要独立的认证体系和权限管理
- 分布式环境下 Session 共享困难
- 需要支持单点登录和 Token 过期失效
- 密码明文存储不安全

### 设计阶段

**设计思路**：

Q：为什么用户端和管理端使用两套独立的 JWT 配置？
> A：用户和管理员的业务场景不同，Token 有效期、密钥、权限校验逻辑都需要独立配置。使用两套 JWT 配置可以更好地控制双端的认证策略，避免互相影响。

Q：为什么不用 Session 而用 JWT？
> A：Session 需要在服务端维护会话状态，集群部署时需要 Session 共享。JWT 是无状态的，Token 本身携带用户信息，服务端只需要验证签名即可，更适合分布式架构。

**架构设计**：
```
用户请求 → JwtTokenUserInterceptor → JwtUtil校验Token → Controller → Service → Mapper → MySQL
管理员请求 → JwtTokenAdminInterceptor → JwtUtil校验Token → Controller → Service → Mapper → MySQL
                      ↓
              Redis（存储Token）
```

### 编码阶段

**核心代码实现**：

```java
// UserController.java - 用户登录逻辑
@PostMapping("/login")
public Result login(@RequestBody UserLogInDTO userLogInDTO) {
    User user = userService.login(userLogInDTO);
    if (user == null) {
        return Result.error(ErrorConstant.PASSWORD_ERROR);
    }
    // 构建Token载荷
    Map<String, Object> claims = new HashMap<>();
    claims.put(JwtClaimsConstant.USER_ID, user.getId());
    claims.put(JwtClaimsConstant.USERNAME, user.getUserName());
    ThreadLocalContextHolder.set(claims);
    
    // 生成Token（使用用户端密钥）
    String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
    
    // 存入Redis
    stringRedisTemplate.opsForValue().set(KEY_PREFIX + user.getId(), token, jwtProperties.getUserTtl(), TimeUnit.SECONDS);
    return Result.success(user.getId() + "::" + token);
}
```

```java
// AdminEmployeeController.java - 管理员登录逻辑
@PostMapping("/login")
public Result login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
    Employee employee = employeeService.login(employeeLoginDTO);
    if (employee == null) {
        return Result.error(ErrorConstant.PASSWORD_ERROR);
    }
    // 构建Token载荷
    Map<String, Object> claims = new HashMap<>();
    claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
    claims.put(JwtClaimsConstant.EMPNAME, employee.getUserName());
    ThreadLocalContextHolder.set(claims);
    
    // 生成Token（使用管理端密钥）
    String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);
    
    // 存入Redis
    stringRedisTemplate.opsForValue().set(KEY_PREFIX + employee.getId(), token, jwtProperties.getAdminTtl(), TimeUnit.SECONDS);
    return Result.success(token);
}
```

### 问题修复阶段

**问题**：双端拦截器路径配置冲突

**修复方案**：在 WebMVCConfiguration 中为用户端和管理端配置独立的拦截器，通过路径前缀区分（`/user/**` 和 `/admin/**`）

---

## 二、菜品管理模块

### 需求阶段

**需求背景**：需要支持菜品的 CRUD 操作，菜品信息包含口味等关联数据，查询频率高。

**痛点**：
- 菜品查询频繁，需要缓存优化
- 菜品与口味是一对多关系，查询时需要关联查询
- 菜品上下架状态需要实时更新
- 缓存与数据库一致性问题

### 设计阶段

**设计思路**：

Q：为什么用手动 Redis 缓存而不是 Spring Cache 注解？
> A：手动控制 Redis 操作更灵活，可以自定义缓存策略、处理空值缓存、精确控制缓存失效时机。菜品查询需要关联口味数据，手动缓存可以一次性缓存完整的 DishVO 对象。

Q：菜品缓存为什么设置 50 分钟过期时间？
> A：菜品信息相对稳定，不会频繁变更。50 分钟的过期时间可以有效减少数据库查询压力，同时在菜品更新时主动删除缓存保证数据一致性。

**缓存策略流程图**：
```
查询菜品
    ↓
缓存存在？
    ├─ 是 → 直接返回缓存数据（DishVO包含口味信息）
    └─ 否 → 查询数据库（菜品+口味）→ 设置缓存（50分钟）→ 返回数据

更新/删除菜品 → 主动删除缓存
```

### 编码阶段

**核心代码实现**：

```java
// AdminDishController.java - 带缓存的菜品查询
@GetMapping("/{id}")
public Result getDishById(@PathVariable Long id) {
    // 1. 拼接完整的 Redis Key
    String key = DISH_CACHE_KEY + id;
    
    // 2. 先从 Redis 查询缓存
    Object cached = redisTemplate.opsForValue().get(key);
    if (cached != null) {
        if (NULL_PLACEHOLDER.equals(cached)) {
            redisTemplate.delete(key);
        }
        if (cached instanceof DishVO dishVO) {
            return Result.success(dishVO);
        }
    }
    
    // 3. 缓存未命中，查询数据库
    Dish dish = dishService.getById(id);
    if (dish == null) {
        return Result.error("菜品不存在");
    }
    
    // 4. 关联查询口味信息
    LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(DishFlavor::getDishId, id).select(DishFlavor::getName, DishFlavor::getValue);
    List<DishFlavor> dishFlavorList = dishFlavorService.list(wrapper);
    
    // 5. 组装 VO 对象
    DishVO dishVO = BeanUtil.toBean(dish, DishVO.class);
    dishVO.setFlavors(dishFlavorList);
    
    // 6. 设置缓存（50分钟）
    redisTemplate.opsForValue().set(key, dishVO, EXISTS_TIME, TimeUnit.MINUTES);
    return Result.success(dishVO);
}
```

```java
// AdminDishController.java - 更新菜品并删除缓存
@PutMapping
public Result updateDish(@RequestBody DishDTO dishDTO) {
    // 更新菜品信息
    Dish dish = Dish.builder()
            .id(dishDTO.getId())
            .name(dishDTO.getName()).categoryId(dishDTO.getCategoryId()).price(dishDTO.getPrice())
            .image(dishDTO.getImage()).description(dishDTO.getDescription())
            .status(dishDTO.getStatus())
            .build();
    dishService.updateById(dish);
    
    // 删除旧口味，保存新口味
    LambdaQueryWrapper<DishFlavor> flavorRemove = new LambdaQueryWrapper<>();
    flavorRemove.eq(DishFlavor::getDishId, dish.getId());
    dishFlavorService.remove(flavorRemove);
    List<DishFlavor> flavorEntities = dishDTO.getFlavors().stream()
            .map(f -> DishFlavor.builder()
                    .dishId(dish.getId())
                    .name(f.getName())
                    .value(f.getValue())
                    .build())
            .toList();
    dishFlavorService.saveBatch(flavorEntities);
    
    // 删除菜品缓存，下次查询时重新缓存
    redisTemplate.delete(DISH_CACHE_KEY + dish.getId());
    return Result.success("updateDish::" + dish.getId());
}
```

### 问题修复阶段

**问题**：已启用的菜品无法删除

**修复方案**：在删除前检查菜品状态，只有停售状态的菜品才能删除

```java
@DeleteMapping
public Result deleteDish(@RequestParam List<Long> ids) {
    LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.in(Dish::getId, ids).eq(Dish::getStatus, StatusConstant.ENABLE);
    List<Dish> dishList = dishService.list(queryWrapper);
    if (!dishList.isEmpty()) {
        return Result.error("不能删除已启用的菜品");
    }
    // 删除菜品口味和缓存...
}
```

---

## 三、套餐管理模块

### 需求阶段

**需求背景**：套餐是多个菜品的组合，需要支持套餐的创建、查询、上下架和删除。

**痛点**：
- 套餐与菜品是多对多关系，需要中间表维护
- 套餐查询频率高，需要缓存优化
- 套餐状态变更需要同步更新缓存

### 设计阶段

**设计思路**：

Q：为什么套餐用 Spring Cache 注解而菜品用手动 Redis？
> A：套餐查询逻辑相对简单，使用 `@Cacheable` 和 `@CacheEvict` 注解可以减少样板代码。套餐数据量相对较小，注解方式足够灵活。而菜品查询需要关联口味数据，手动控制可以更好地处理复杂的缓存逻辑。

Q：套餐删除时为什么用 `allEntries = true` 清除所有缓存？
> A：套餐删除可能影响多个缓存条目（套餐详情、套餐列表等），使用 `allEntries = true` 可以确保所有相关缓存都被清除，避免数据不一致。

**架构设计**：
```
查询套餐 → @Cacheable → Redis缓存 → 返回数据
添加/更新/删除套餐 → @CacheEvict → 删除缓存
```

### 编码阶段

**核心代码实现**：

```java
// AdminSetmealController.java - 添加套餐（自动清除缓存）
@PostMapping
@CacheEvict(cacheNames = "setmeal", allEntries = true)
public Result addSetmeal(@RequestBody SetmealDTO setmealDTO) {
    // 检查套餐名称是否已存在
    LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
    setmealWrapper.eq(Setmeal::getName, setmealDTO.getName());
    Setmeal check_setmeal = setmealService.getOne(setmealWrapper);
    if (check_setmeal != null) {
        return Result.error("套餐名称已存在");
    }
    
    // 保存套餐主表
    Setmeal setmeal = BeanUtil.toBean(setmealDTO, Setmeal.class);
    setmeal.setStatus(StatusConstant.ENABLE);
    setmealService.save(setmeal);
    
    // 保存套餐菜品关联数据
    List<SetmealDish> setmealDishesList = setmealDTO.getSetmealDishes().stream()
            .map(setmealDish -> SetmealDish.builder()
                    .setmealId(setmeal.getId())
                    .copies(setmealDish.getCopies())
                    .dishId(setmealDish.getDishId())
                    .name(setmealDish.getName())
                    .build())
            .collect(Collectors.toList());
    setmealDishService.saveBatch(setmealDishesList);
    return Result.success("addSetmeal::" + setmeal.getId());
}
```

```java
// AdminSetmealController.java - 查询套餐详情（自动缓存）
@GetMapping("/{id}")
@Cacheable(cacheNames = "setmeal", key = "#id")
public Result readSetmeal(@PathVariable Long id) {
    Setmeal setmeal = setmealService.getById(id);
    if (setmeal == null) {
        return Result.error("套餐不存在");
    }
    
    // 关联查询套餐包含的菜品
    LambdaQueryWrapper<SetmealDish> setmealDishWrapper = new LambdaQueryWrapper<>();
    setmealDishWrapper.eq(SetmealDish::getSetmealId, id);
    List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishWrapper);
    return Result.success(setmealDishList);
}
```

### 问题修复阶段

**问题**：套餐状态变更后缓存未更新

**修复方案**：使用 `@CacheEvict` 注解在状态变更时清除对应缓存

```java
@PutMapping("/{id}/status/{status}")
@CacheEvict(cacheNames = "setmeal", key = "#id")
public Result updateStatus(@PathVariable Long id, @PathVariable Long status) {
    Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
    setmealService.updateById(setmeal);
    return Result.success(id + "::" + (status == StatusConstant.ENABLE ? "启用成功" : "停用成功"));
}
```

---

## 四、购物车模块

### 需求阶段

**需求背景**：用户在下单前需要将菜品或套餐加入购物车，支持数量调整和清空操作。

**痛点**：
- 同一用户同一菜品同一口味需要去重
- 购物车数据需要实时更新金额
- 需要区分菜品和套餐的处理逻辑

### 设计阶段

**设计思路**：

Q：为什么购物车不用 Redis 缓存？
> A：购物车数据属于用户会话数据，每个用户的购物车内容不同，且数据量较小。直接存储在数据库可以保证数据持久化，避免用户重新登录后购物车数据丢失。

Q：购物车去重逻辑为什么要同时匹配用户ID、菜品ID和口味？
> A：同一菜品的不同口味是不同的商品（比如"麻辣火锅"和"清汤火锅"），需要作为独立的购物车条目处理。只匹配用户ID和菜品ID会导致不同口味的菜品被合并，不符合业务需求。

**架构设计**：
```
添加商品 → 判断是菜品还是套餐 → 检查是否已存在（同用户+同商品+同口味）
    ├─ 已存在 → 更新数量和金额
    └─ 不存在 → 新增购物车记录
```

### 编码阶段

**核心代码实现**：

```java
// UserShoppingCartController.java - 添加购物车（支持菜品和套餐）
@PostMapping
public Result createShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
    ShoppingCart shoppingCart = new ShoppingCart();
    Map<String, Object> claims = ThreadLocalContextHolder.get();
    Long userId = Long.parseLong(claims.get(JwtClaimsConstant.USER_ID).toString());
    shoppingCart.setUserId(userId);
    
    // 判断是菜品还是套餐
    if (shoppingCartDTO.getSetmealId() == null) {
        // 菜品处理逻辑
        shoppingCart.setSetmealId(0L);
        Dish dish = dishService.getById(shoppingCartDTO.getDishId());
        shoppingCart.setDishId(shoppingCartDTO.getDishId());
        shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
        shoppingCart.setImage(dish.getImage());
        shoppingCart.setName(dish.getName());
        
        // 去重查询：同用户+同菜品+同口味
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getDishId, shoppingCartDTO.getDishId())
                .eq(ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor());
        ShoppingCart same_shoppingCart = shoppingCartService.getOne(wrapper);
        
        if (same_shoppingCart != null) {
            // 已存在，更新数量和金额
            Long newNumber = same_shoppingCart.getNumber() + shoppingCartDTO.getNumber();
            BigDecimal newAmount = same_shoppingCart.getAmount().add(dish.getPrice().multiply(BigDecimal.valueOf(shoppingCartDTO.getNumber())));
            same_shoppingCart.setNumber(newNumber);
            same_shoppingCart.setAmount(newAmount);
            shoppingCartService.updateById(same_shoppingCart);
        } else {
            // 不存在，新增记录
            shoppingCart.setNumber(shoppingCartDTO.getNumber());
            shoppingCart.setAmount(dish.getPrice().multiply(BigDecimal.valueOf(shoppingCartDTO.getNumber())));
            shoppingCartService.save(shoppingCart);
        }
        return Result.success("createShoppingCart::" + shoppingCart.getId());
    }
    
    // 套餐处理逻辑（类似菜品）
    // ...
}
```

```java
// UserShoppingCartController.java - 清空购物车
@DeleteMapping("/all")
public Result clean() {
    Map<String, Object> map = ThreadLocalContextHolder.get();
    Long userId = Long.parseLong(map.get(JwtClaimsConstant.USER_ID).toString());
    LambdaQueryWrapper<ShoppingCart> Wrapper = new LambdaQueryWrapper<>();
    Wrapper.eq(ShoppingCart::getUserId, userId);
    shoppingCartService.remove(Wrapper);
    return Result.success("clean");
}
```

### 问题修复阶段

**问题**：购物车金额计算精度丢失

**修复方案**：使用 BigDecimal 进行金额计算，避免浮点数精度问题

---

## 五、订单与支付模块

### 需求阶段

**需求背景**：实现完整的订单流程，包括下单、支付、接单、配送、完成等状态流转，支持支付宝支付和退款。

**痛点**：

- 订单状态流转复杂，需要严格控制状态转换
- 支付回调需要处理异步通知
- 退款需要与支付平台交互
- 需要支持订单重复提交

### 设计阶段

**设计思路**：

Q：为什么订单状态用枚举类管理？
> A：订单状态是有限且固定的集合，使用枚举类可以避免魔法数字，提高代码可读性和可维护性。枚举类还可以封装状态转换逻辑。

Q：为什么支付成功后需要更新订单状态？
> A：支付是订单流程的关键节点，支付成功后订单状态从"待支付"变为"待商家接单"，触发后续的业务流程。

**订单状态流转**：
```
1 待支付 → 2 待商家接单 → 3 制作中 → 4 待骑手取餐 → 5 配送中 → 6 已送达 → 7 已完成
        	↓                                      
   8 已取消（未接单退款、商家拒单，售后全额退款）
```

### 编码阶段

**核心代码实现**：

```java
// UserOrderController.java - 下单功能
@PostMapping("/submit")
public Result submitOrder(@RequestBody OrdersDTO orderDTO) {
    Orders orders = BeanUtil.toBean(orderDTO, Orders.class);
    orders.setStatus(OrderStatusEnum.PENDING_PAYMENT);
    orderService.save(orders);
    
    // 保存订单明细
    Long orderId = orders.getId();
    List<OrderDetail> orderDetailList = orderDTO.getOrderDetails()
                .stream()
                .map(orderDetail -> {
                    OrderDetail orderDetail1 = BeanUtil.toBean(orderDetail, OrderDetail.class);
                    orderDetail1.setOrderId(orderId);
                    return orderDetail1;
                })
                .toList();
    orderDetailService.saveBatch(orderDetailList);
    return Result.success("submitOrder::" + orders.getId());
}
```

```java
// UserOrderController.java - 支付宝支付
@GetMapping("/pay/{id}")
public void payOrder(@PathVariable Long id, HttpServletResponse response) throws Exception {
    Orders payOrder = orderService.getById(id);
    // 更新订单状态为待商家接单
    payOrder.setStatus(OrderStatusEnum.PENDING_MERCHANT_ACCEPT);
    payOrder.setPayMethod(1L); // 1->支付宝，2是微信
    orderService.updateById(payOrder);
    
    // 构建支付参数
    PayDTO payDTO = new PayDTO(payOrder.getId().toString(), payOrder.getAmount(), payOrder.getConsignee());
    String form = alipayService.createPagePayForm(payDTO);
    
    // 返回支付宝支付表单
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType("text/html;charset=UTF-8");
    response.getWriter().write(form);
    response.getWriter().flush();
}
```

```java
// AlipayController.java - 支付接口
@GetMapping("/order")
public void orderPay(PayDTO payDTO, HttpServletResponse response) throws Exception {
    String form = alipayService.createPagePayForm(payDTO);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType("text/html;charset=UTF-8");
    response.getWriter().write(form);
    response.getWriter().flush();
}

@PostMapping("/refund")
public AlipayTradeRefundResponse refundOrder(RefundDTO refundDTO) throws Exception {
    return alipayService.refund(refundDTO);
}
```

### 问题修复阶段

**问题**：订单取消后无法退款

**修复方案**：在取消订单时调用支付宝退款接口

```java
@PutMapping("/cancel/{id}")
public Result cancelOrder(@PathVariable Long id) {
    Orders orders = orderService.getById(id);
    orders.setStatus(OrderStatusEnum.CANCELLED);
    orders.setCancelReason("用户取消");
    orderService.updateById(orders);
    
    // 调用支付宝退款接口
    try {
        RefundDTO refundDTO = new RefundDTO(
                orders.getId().toString(),
                orders.getAmount(),
                LocalDateTime.now().toString(),
                orders.getCancelReason()
        );
        alipayService.refund(refundDTO);
    } catch (AlipayApiException e) {
        throw new RuntimeException(e);
    }
    return Result.success("cancelOrder" + id);
}
```

---

## 六、WebSocket 实时通信模块

### 需求阶段

**需求背景**：实现订单状态实时推送，用户可以实时查看订单进度，商家可以实时接收新订单通知。

**痛点**：
- 轮询方式效率低，服务器压力大
- 需要支持多客户端同时在线
- 连接断开后需要重新连接

### 设计阶段

**设计思路**：

Q：为什么用 WebSocket 而不是轮询？
> A：轮询需要客户端定时发送请求，效率低且浪费资源。WebSocket 是长连接，服务器可以主动推送消息，实时性更好，服务器压力更小。

Q：为什么用 Map 存储 Session？
> A：需要根据用户 ID 定向推送消息，使用 Map 可以快速查找对应客户端的 Session 对象。

**架构设计**：
```
客户端连接 → WebSocketServer.onOpen → sessionMap存储Session
服务器推送 → WebSocketServer.sendToAllClient / session.getBasicRemote().sendText
客户端断开 → WebSocketServer.onClose → sessionMap移除Session
```

### 编码阶段

**核心代码实现**：

```java
// WebSocketServer.java - WebSocket服务
@Component
@ServerEndpoint("/websocket/{id}")
public class WebSocketServer {
    
    // 存放会话对象，key为客户端ID
    private static Map<String, Session> sessionMap = new HashMap();
    
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id) {
        System.out.println("客户端：" + id + "建立连接");
        sessionMap.put(id, session);
    }
    
    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, @PathParam("id") String id) {
        System.out.println("收到来自客户端：" + id + "的信息:" + message);
    }
    
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("id") String id) {
        System.out.println("连接断开:" + id);
        sessionMap.remove(id);
    }
    
    /**
     * 群发消息
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
```

### 问题修复阶段

**问题**：WebSocket 连接在多实例部署时无法跨节点推送

**修复方案**：引入消息队列（如 RabbitMQ），当订单状态变更时，通过消息队列通知所有节点，每个节点再向自己的客户端推送消息。

---

## 七、文件管理模块+excel读写模块

### 需求阶段

**需求背景**：实现图片上传功能，支持菜品图片、套餐图片、用户头像等文件的上传和访问,实现excel，员工信息，菜品信息，套餐信息导出excel对于分析数据，财务报表等

**痛点**：

- 本地存储在多实例部署时文件不一致
- 需要支持阿里云 OSS 云存储
- 文件命名需要避免冲突

### 设计阶段

**设计思路**：

Q：为什么提供两种文件存储方式？
> A：本地存储用于开发测试环境，初始化存储，完成初始化使用，搭建环境使用；阿里云 OSS 用于生产环境，支持高可用和 CDN 加速。

Q：文件命名为什么用 UUID？
> A：UUID 全局唯一，避免文件名冲突，同时增加安全性（防止文件遍历攻击）。

**架构设计**：
```
文件上传 → LocalFileController（本地）/ OSSFileController（阿里云OSS）→ 返回文件访问URL
文件下载 → LocalFileController（本地）/ OSSFileController（阿里云OSS）→ 返回文件流
```

### 编码阶段

**核心代码实现**：

```java
// OSSFileController.java - 阿里云OSS文件上传（示例）
@PostMapping("/upload")
public Result upload(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    // 使用UUID生成唯一文件名
    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    String objectName = UUID.randomUUID().toString() + extension;
    
    // 调用AliOssUtil上传到阿里云OSS
    String url = aliOssUtil.uploadFile(objectName, file.getInputStream());
    
    Map<String, String> fileResult = new HashMap<>();
    fileResult.put("url", url);
    fileResult.put("filename", originalFilename);
    return Result.success(fileResult);
}
```

### 问题修复阶段

**问题**：文件下载中文文件名乱码

**修复方案**：使用 URLEncoder 编码文件名，同时设置 Content-Disposition 响应头

```java
response.setHeader("Content-Disposition", "attachment;filename=" + 
    URLEncoder.encode(fileName, StandardCharsets.UTF_8));
```

---

## 八、AI 热线模块（ai-hotline）

### 需求阶段

**需求背景**：用户想通过拍照的方式快速找到心仪的套餐。比如在餐厅看到别人桌上的菜，拍下来就能识别出是什么菜，并推荐对应的套餐。

**痛点**：
- 传统搜索需要用户输入文字，体验不够直观
- 用户不知道菜名时无法搜索
- 图片识别后需要与数据库菜品信息关联匹配

### 设计阶段

**设计思路**：

Q：为什么用 Spring AI + 图工作流（Graph）来处理？
> A：整个流程可以拆解为"视觉识别 → 工具查询"两个独立步骤。使用 Alibaba Cloud AI Graph 的状态图引擎，可以清晰编排每个节点，State 在节点间自动传递，方便扩展（比如未来加入推荐排序节点）。Spring AI 提供了统一的 ChatClient 抽象，对接 OpenAI 兼容 API 只需配置 base-url 和 api-key。

Q：为什么 ai-hotline 是独立服务而不是集成到主应用中？
> A：ai-hotline 依赖 Spring AI 和 Spring Boot 3.5.x 的较新版本，与主应用的 3.3.8 版本有冲突。独立部署还能让 AI 服务的扩缩容独立于业务服务，避免偶发的高延迟影响核心下单流程。

**工作流程**：
```
用户请求（图片+问题）
    ↓
POST /ai/see → 图片转 Base64 → 传入 StateGraph
    ↓
node1 - VisualFunction（视觉识别节点）
    ├─ 调用视觉 ChatClient（Qwen3.5 多模态模型）
    ├─ Prompt: "识别有哪些食物,饮料？"
    └─ 输出 → State.visualResult = "鱼、虾、啤酒..."
    ↓
node2 - ToolFunction（工具查询节点）
    ├─ 调用工具 ChatClient（带 SetmealTool）
    ├─ 将 visualResult 作为输入传给大模型
    ├─ 大模型自动选择合适的 Tool 方法查询数据库
    └─ 输出 → State.toolResult = 匹配的套餐列表
    ↓
返回结果 → "==>1.visual>识别结果 ==>2.tool>套餐列表"
```

**Q&A**：

Q：SetmealTool 提供了哪些查询能力？
> A：提供了 6 种查询方式：
> 1. `queryById` — 按套餐ID精确查询
> 2. `queryByName` — 按套餐名称模糊查询
> 3. `queryByCategoryId` — 按分类ID精确查询
> 4. `queryByPriceRange` — 按价格区间查询
> 5. `queryByStatus` — 按售卖状态查询
> 6. `queryByDescription` — **按图片识别的食材/饮品关键词模糊匹配套餐描述**
> 7. `queryByMultiCondition` — 多条件组合查询

Q：为什么需要 queryByDescription 这个工具方法？
> A：这是视觉识别的核心衔接方法。视觉模型会从图片中提取"鱼、虾、牛蛙、啤酒"等食材关键词，通过模糊匹配套餐的 description 字段（菜品描述），就可以找到包含这些食材的套餐。例如用户拍了一张水煮鱼的图片，视觉识别出"鱼、辣椒"，就能匹配到 description 包含"鱼"的套餐。

**聊天记忆设计**：
```
@Bean
public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
    return MessageWindowChatMemory.builder()
            .chatMemoryRepository(chatMemoryRepository)
            .maxMessages(20)  // 保留最近20条消息
            .build();
}
```
> 使用 Redis 作为聊天记忆存储后端，保留最近 20 条消息上下文，支持多轮对话中保持连贯性。

### 编码阶段

**核心代码实现**：

```java
// SeeController.java - AI 热线入口
@RestController
@RequestMapping("/ai")
public class SeeController {
    
    @PostMapping("/see")
    public Object flow(@RequestParam String question,
                       @RequestParam MultipartFile file) throws Exception {
        CompiledGraph compiledGraph = nodeLink.toSee();
        // 将文件转为 Base64 字符串再传入 state
        String fileBase64 = Base64.getEncoder().encodeToString(file.getBytes());
        return compiledGraph.invoke(Map.of("question", question, "file", fileBase64))
                .map(overAllState -> "==>1.visual>" + overAllState.value("visualResult").orElse("null") +
                        "==>2.tool>" + overAllState.value("toolResult").orElse("null"))
                .orElse("执行失败");
    }
}
```

```java
// VisualFunction.java - 视觉识别节点
@Service
public class VisualFunction implements NodeAction {
    @Resource(name = "visualChatClient")
    private ChatClient visualClient;

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
}
```

```java
// ToolFunction.java - 工具查询节点
@Service
public class ToolFunction implements NodeAction {
    @Resource(name = "toolClient")
    private ChatClient toolClient;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String input = state.value("visualResult").toString();
        String result = toolClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text(input))
                .call()
                .content();
        return Map.of("toolResult", result != null ? result : "没有查询到内容");
    }
}
```

```java
// SetmealTool.java - AI 工具函数（核心查询能力）
@Component
public class SetmealTool {

    @Tool(description = "按套餐ID精确查询，返回单个套餐信息")
    public Setmeal queryById(@ToolParam(description = "套餐ID（必填，精确匹配）") Long id) {
        return setmealMapper.selectById(id);
    }

    @Tool(description = "按套餐名称模糊查询，支持关键词匹配")
    public List<Setmeal> queryByName(@ToolParam(description = "套餐名称关键词") String name) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Setmeal::getName, name);
        return setmealMapper.selectList(wrapper);
    }

    @Tool(description = "根据图片识别出的食材、饮品关键词，模糊匹配套餐的菜品描述字段")
    public List<Setmeal> queryByDescription(SetmealToolParam setmealToolParam) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        String key = setmealToolParam.getDescription().trim();
        if (!key.isEmpty()) {
            wrapper.like(Setmeal::getDescription, key);
        }
        return setmealMapper.selectList(wrapper);
    }
    // 更多查询方法：queryByCategoryId, queryByPriceRange, queryByStatus, queryByMultiCondition...
}
```

```java
// NodeLink.java - 工作流编排
@Configuration
public class NodeLink {
    @Bean("see")
    public CompiledGraph toSee() {
        StateGraph graph = new StateGraph("see", strategyFactory);
        graph.addNode("node1", AsyncNodeAction.node_async(visualFunction));
        graph.addNode("node2", AsyncNodeAction.node_async(toolFunction));
        graph.addEdge(StateGraph.START, "node1");
        graph.addEdge("node1", "node2");
        graph.addEdge("node2", StateGraph.END);
        return graph.compile();
    }
}
```

```java
// ToolConfiguration.java - 工具 ChatClient 配置
@Configuration
public class ToolConfiguration {
    @Bean
    public ChatClient toolClient(OpenAiChatModel model, Advisor loggerAdvisor,
                                 Advisor messageMemoryAdvisor, SetmealTool setmealTool) {
        return ChatClient.builder(model)
                .defaultAdvisors(loggerAdvisor, messageMemoryAdvisor)
                .defaultTools(setmealTool)  // 注册 AI 工具函数
                .build();
    }
}
```

### 问题修复阶段

**问题**：Graph 框架无法直接处理 byte[] 类型文件数据

**修复方案**：在上传时将 MultipartFile 转为 Base64 字符串再传入 State，避免序列化问题

```java
// 将文件转为 Base64 字符串再传入 state，避免 graph 框架无法处理 byte[]
String fileBase64 = Base64.getEncoder().encodeToString(file.getBytes());
```

**问题**：图片识别关键词可能为空，导致全表查询

**修复方案**：在 queryByDescription 中添加非空校验，防止空字符串触发的无筛选查询

```java
if (!key.isEmpty()) {
    wrapper.like(Setmeal::getDescription, key);
}
```

**问题**：ai-hotline 与主应用 Spring Boot 版本不兼容（3.5.x vs 3.3.8）

**修复方案**：ai-hotline 作为独立 Maven 模块运行，不加入父工程的 module 列表，使用独立的 Spring Boot parent 版本

---

# 核心组件设计

### 1. JWT 认证组件（JwtUtil）

**设计思路**：

Q：为什么用户端和管理端使用不同的密钥？
> A：用户端和管理端是独立的认证体系，使用不同的密钥可以提高安全性。如果一个密钥泄露，不会影响另一个端的认证。

Q：为什么 Token 要存储在 Redis 中？
> A：存储在 Redis 中可以实现单点登录（同一账号在多个设备登录时，只有最后一个有效），同时支持 Token 主动失效（如用户退出登录）。

**代码实现**：
```java
// JwtUtil.java - JWT工具类（核心方法）
public class JwtUtil {
    
    /**
     * 生成JWT令牌
     * @param secretKey 签名密钥
     * @param ttlMillis 过期时间（毫秒）
     * @param claims 载荷数据
     * @return JWT令牌
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 指定签名算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        
        // 生成过期时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);
        
        // 构建JWT
        return Jwts.builder()
                .setClaims(claims)
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                .setExpiration(exp)
                .compact();
    }
    
    /**
     * 解析JWT令牌
     * @param secretKey 签名密钥
     * @param token JWT令牌
     * @return 载荷数据（Claims对象）
     */
    public static Claims parseJWT(String secretKey, String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
```

### 2. AOP 日志组件（ServiceInterceptAspect）

**设计思路**：

Q：为什么用 AOP 记录日志？
> A：使用 AOP 可以在不侵入业务代码的情况下记录方法执行信息，包括执行耗时、参数、返回值等。这样可以保持业务代码的简洁性，同时实现统一的日志记录。

**代码实现**：
```java
// ServiceInterceptAspect.java - AOP切面
@Aspect
@Component
public class ServiceInterceptAspect {
    
    @Around("@annotation(start.annotation.Info)")
    public Object info(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();
        
        // 记录方法参数
        log.info("方法 {} 开始执行，参数：{}", methodName, JSONUtil.toJsonStr(args));
        
        // 执行目标方法
        Object result = joinPoint.proceed();
        
        // 记录执行耗时和返回值
        long endTime = System.currentTimeMillis();
        log.info("方法 {} 执行完成，耗时：{}ms，返回值：{}", 
                methodName, endTime - startTime, JSONUtil.toJsonStr(result));
        
        return result;
    }
}
```

---

# 依赖说明

### 用户与员工认证功能依赖
| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| Spring Boot | 3.3.8 | 应用框架，自动配置数据源、Redis等基础设施 |
| Spring Boot Starter Web | 3.3.8 | UserController/AdminEmployeeController提供REST接口（注册、登录、信息修改） |
| MyBatis Plus | 3.5.9 | UserMapper/EmployeeMapper实现用户、员工数据CRUD；AutoMetaObjectHandler自动填充元数据字段 |
| JJWT API/Impl/Jackson | 0.12.6 | JwtUtil生成登录Token，JwtTokenUserInterceptor/JwtTokenAdminInterceptor验证Token |
| Spring Boot Starter Data Redis | 3.3.8 | 存储用户Token（`user:{userId}`）和员工Token（`emp:{empId}`） |
| Spring Boot Starter Validation | 3.3.8 | @NotNull、@Size等注解校验注册和登录参数的合法性 |
| Hutool All | 5.8.26 | BeanUtil对象属性拷贝；StrUtil判空；JSONUtil序列化/反序列化 |

### 菜品管理功能依赖
| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| MyBatis Plus | 3.5.9 | DishMapper/DishFlavorMapper实现菜品和口味数据CRUD |
| Spring Boot Starter Data Redis | 3.3.8 | **缓存策略**：AdminDishController使用RedisTemplate手动缓存菜品数据（50分钟过期），更新/删除后主动删除缓存 |
| Spring Boot Starter Validation | 3.3.8 | 参数校验支持 |
| Aliyun SDK OSS | 3.17.4 | 菜品图片上传到阿里云OSS |
| Hutool All | 5.8.26 | BeanUtil进行对象属性拷贝 |

### 套餐管理功能依赖
| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| MyBatis Plus | 3.5.9 | SetmealMapper/SetmealDishMapper实现套餐和套餐菜品数据CRUD |
| Spring Boot Starter Cache | 3.3.8 | **缓存策略**：AdminSetmealController使用@Cacheable/@CacheEvict注解实现套餐数据缓存 |
| Spring Boot Starter Data Redis | 3.3.8 | Redis作为Spring Cache的缓存实现 |
| Hutool All | 5.8.26 | BeanUtil进行对象属性拷贝 |

### 购物车功能依赖
| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| MyBatis Plus | 3.5.9 | ShoppingCartMapper实现购物车数据CRUD；支持去重查询 |
| Spring Boot Starter Validation | 3.3.8 | 参数校验支持 |

### 订单与支付功能依赖
| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| MyBatis Plus | 3.5.9 | OrderMapper/OrderDetailMapper实现订单和订单明细数据CRUD |
| 支付宝 SDK (alipay-sdk-java) | 4.40.658.ALL | AlipayController集成支付宝支付、退款、回调处理 |
| 微信支付 SDK (wechatpay-apache-httpclient) | 0.6.0 | WeChatPayUtil实现微信支付集成 |
| EasyExcel | 3.3.2 | ExcelReportController导出订单数据报表 |
| Spring Boot Starter WebSocket | 3.3.8 | WebSocketServer实现订单状态实时推送 |

### WebSocket实时通信功能依赖
| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| Spring Boot Starter WebSocket | 3.3.8 | WebSocketServer实现订单状态实时推送、消息通知 |

### 文件管理功能依赖
| 依赖 | 版本 | 功能支撑 |
| :--- | :--- | :--- |
| Aliyun SDK OSS | 3.17.4 | OSSFileController实现图片上传到阿里云OSS，返回CDN访问URL |
| Spring Boot Starter Web | 3.3.8 | LocalFileController实现本地文件上传/下载 |
| com.alibaba.easyexcel | -- | 实现excel，员工信息，菜品信息，套餐信息的读写 |

---

### 支付流程说明

支付逻辑位于 `AlipayController` 和 `UserOrderController` 中，核心流程如下：

1. **用户下单**：通过 `POST /user/orders/submit` 接口提交订单，订单状态为"待支付"

2. **发起支付**：通过 `GET /user/orders/pay/{id}` 接口发起支付宝支付
   - 更新订单状态为"待商家接单"
   - 构建支付宝支付表单
   - 返回支付页面

3. **用户支付**：用户在支付宝页面完成支付

4. **同步回调**：支付成功后跳转到 `GET /pay/return` 接口

5. **异步通知**：支付宝服务器调用 `POST /pay/notify` 接口（需公网可访问）
   - 验签（核心安全步骤）
   - 校验订单金额
   - 更新订单状态
   - 返回处理结果

6. **退款**：通过 `PUT /user/orders/cancel/{id}` 接口取消订单并发起退款
   - 更新订单状态为"已取消"
   - 调用支付宝退款接口

---

### 对比分析

**问题1：双端认证共用一套拦截器**
```java
// 错误写法 - 不推荐！
@Component
public class JwtInterceptor implements HandlerInterceptor {
    // 用户和管理员共用一套逻辑
}
```
> 本项目改进：使用 `JwtTokenUserInterceptor` 和 `JwtTokenAdminInterceptor` 两套独立拦截器，分别处理用户端和管理端的认证逻辑。

**问题2：购物车去重只匹配用户和商品**
```java
// 错误写法 - 不推荐！
wrapper.eq(ShoppingCart::getUserId, userId)
        .eq(ShoppingCart::getDishId, dishId);
// 没有匹配口味，会导致不同口味的菜品被合并
```
> 本项目改进：同时匹配 `userId + dishId + dishFlavor`，确保不同口味的菜品作为独立条目处理。

**问题3：套餐删除不清除缓存**
```java
// 错误写法 - 不推荐！
@DeleteMapping
public Result deleteSetmeal(List<Long> ids) {
    setmealService.removeByIds(ids);
    // 没有清除缓存，可能返回旧数据
}
```
> 本项目改进：使用 `@CacheEvict(cacheNames = "setmeal", allEntries = true)` 自动清除所有套餐缓存。

---

# 项目结构

```
take-out/
├── backend-spring-takeout/           # 后端代码
├── database-sql/                     # 数据库脚本目录
│   ├── sql.txt                       # 数据库初始化SQL
│   └── 数据库设计文档.md              # 完整的数据库设计说明
├── frontend-vue-admin-takeout/       # 前端管理端（Vue 3）
└── 说明/                             # 项目说明文档
    ├── 原型功能/                     # 前端原型截图
    ├── 支付功能/                     # 支付流程截图
    ├── 苍穹外卖-用户端接口.html       # 用户端API接口文档
    └── 苍穹外卖-管理端接口.html       # 管理端API接口文档
```

# 环境要求

- JDK 17+
- Spring Boot 3+
- Node.js 20.19.0+ 或 22.12.0+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.8+

---

# 前端说明

## 管理端界面

| 功能页面 | 截图 |
| :--- | :--- |
| 登录页面 | ![管理端登录](说明/原型功能/admin服务端1.png) |
| 工作台首页 | ![工作台首页](说明/原型功能/admin服务端2.png) |
| 菜品管理 | ![菜品管理](说明/原型功能/admin服务端3.png) |
| 套餐管理 | ![套餐管理](说明/原型功能/admin服务端4.png) |
| 订单管理 | ![订单管理](说明/原型功能/admin服务端5.png) |
| 分类管理 | ![分类管理](说明/原型功能/admin服务端6.png) |
| 员工管理 | ![员工管理](说明/原型功能/admin服务端7.png) |

---