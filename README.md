# 校园活动信息管理 

> **面试题目**：题目 3 —— 校园活动信息管理（小程序 / 网页）
> **实现方式**：Spring Boot + Vue 3 前后端分离
> **仓库地址**：https://github.com/Leafboss/CampusActivity

一个校园活动信息管理系统：访客可以**浏览活动列表、按关键词和状态筛选、查看活动详情**；管理端可以**新增 / 编辑 / 删除活动，并上传活动图片**。

---

## 一、项目简介

后台提供活动的增删改查接口与本地图片上传接口，前端是三个页面的单页应用（活动列表 / 活动详情 / 活动管理）。

题目原文对本题的要求是「活动列表展示（名称、时间、地点、简介），可点击查看活动详情，可自行设计样式」，技术栈不限、只做静态页面也可以。本项目在满足全部要求之外，主动补齐了一条**完整的读写链路**：数据库持久化、参数校验、统一响应体与全局异常处理、图片上传与文件生命周期管理，用来展示「一个功能真正跑通」需要哪些环节。

---

## 二、技术栈

| 层次 | 技术 | 版本 | 用途 |
| --- | --- | --- | --- |
| 后端 | Java | 17+（开发环境 JDK 21） | 运行环境 |
| | Spring Boot | 4.0.3 | Web 框架（注意：4.x 的 Web 起步依赖叫 `spring-boot-starter-webmvc`） |
| | MyBatis | mybatis-spring-boot-starter 4.0.1 | 持久层 |
| | MySQL | 8.x | 数据库 |
| | spring-boot-starter-validation | 随 Boot 版本 | 参数校验（`@Valid` + `@NotBlank` / `@Size` 等） |
| | Lombok | 随 Boot 版本 | 省去 getter/setter 样板代码 |
| 前端 | Vue | 3.5.43 | 前端框架（组合式 API + `<script setup>`） |
| | Vue Router | 4.6.4 | 路由（history 模式） |
| | axios | 1.20.0 | 发起 HTTP 请求 |
| | Vite | 8.3.1 | 开发服务器与打包 |

---

## 三、功能清单

| # | 功能 | 前端入口 | 后端接口 |
| --- | --- | --- | --- |
| 1 | 活动列表（含统计信息） | 首页 `/` | `GET /api/activities` |
| 2 | 关键词搜索 + 状态筛选 | 首页搜索框 / 状态按钮 | `GET /api/activities?keyword=&status=` |
| 3 | 活动详情 | 首页点卡片 → `/activity/:id` | `GET /api/activities/{id}` |
| 4 | 管理端活动列表 | 管理页 `/manage` | `GET /api/activities` |
| 5 | 新增活动 | 管理页「新增活动」 | `POST /api/activities` |
| 6 | 编辑活动（含更换图片） | 管理页「编辑」 | `PUT /api/activities/{id}` |
| 7 | 删除活动（连带删除其图片文件） | 管理页「删除」 | `DELETE /api/activities/{id}` |
| 8 | 上传活动图片 | 新增 / 编辑弹层里的「选择图片」 | `POST /api/upload` |

补充说明：

- 第 2 项是**后端条件查询**（MyBatis 动态 SQL `<where>` + `<if>`），不是前端本地过滤。
- 活动状态用 `1 = 报名中 / 0 = 已结束` 存储，界面上的中文文案由前端 `src/utils/status.js` 统一翻译。
- 图片有两类：仓库自带的占位图 `/images/xxx.jpg`（前端静态资源），以及用户上传的 `/uploads/xxx.png`（存在后端磁盘上）。

---

## 四、项目结构

```
一轮面试/
├── backend/                                  # Spring Boot 后端（端口 4987）
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/campus_activity/
│       │   ├── CampusActivityApplication.java    # 启动类
│       │   ├── config/WebConfig.java             # 把 upload.path 目录映射成 /uploads/** 静态资源
│       │   ├── controller/
│       │   │   ├── ActivityController.java       # 活动增删改查接口
│       │   │   └── FileUploadController.java     # 图片上传接口
│       │   ├── exception/GlobalExceptionHander.java  # 全局异常处理（参数校验 / JSON 解析失败）
│       │   ├── mapper/ActivityMapper.java        # MyBatis 接口（注解 SQL）
│       │   ├── pojo/Activity.java                # 活动实体 + 字段校验注解
│       │   ├── pojo/Result.java                  # 统一响应体 {code, msg, data}
│       │   └── service/                          # 业务层（默认图片、删除图片文件等规则）
│       └── resources/
│           ├── application.yml                   # 配置（密码等敏感项走环境变量）
│           ├── com/example/campus_activity/mapper/ActivityMapper.xml  # 动态 SQL
│           └── sql/
│               ├── init.sql                      # 建库建表 + 种子数据（会删库重建）
│               └── migrate-v2-status-time.sql    # 已有数据时的表结构升级脚本
└── frontend/                                 # Vue 3 前端（开发端口 5173）
    ├── vite.config.js                            # 开发代理：/api、/uploads → 4987
    ├── public/images/                            # 占位图与示例配图（随仓库分发）
    └── src/
        ├── api/activity.js                       # 全部后端请求都收在这一个文件（6 个函数）
        ├── router/index.js                       # 三个路由
        ├── views/                                # HomeView / DetailView / AdminView
        ├── components/                           # 卡片、页头页脚、统计条等展示组件
        ├── utils/                                # 状态文案、日期格式化
        └── assets/main.css                       # 全局样式
```

---

## 五、快速开始

### 5.1 环境要求

| 依赖 | 版本要求 | 说明 |
| --- | --- | --- |
| JDK | 17 或以上 | 本项目在 JDK 21 上开发与运行 |
| Maven | 3.8 或以上 | 项目**没有**附带 `mvnw` 包装器，需要本机安装 Maven，或直接用 IDEA 内置的 |
| MySQL | 8.x | 需要能创建数据库、执行 SQL 脚本 |
| Node.js | `^20.19.0` 或 `>=22.12.0` | 这是 Vite 8 的硬性要求，版本过低前端起不来（开发环境为 v24.20.0） |

### 5.2 创建数据库

**方式 A：全新初始化（推荐首次运行使用）**

```bash
mysql -u root -p < backend/src/main/resources/sql/init.sql
```

脚本会创建 `campus_activity` 库、`activity` 表，并写入 6 条示例活动。

> ⚠️ 注意：`init.sql` 开头是 `DROP DATABASE IF EXISTS campus_activity`，会**清空同名库**。库里已经有数据时不要跑它。

**方式 B：已有数据的库，升级表结构**

```bash
mysql -u root -p campus_activity < backend/src/main/resources/sql/migrate-v2-status-time.sql
```

这个脚本把表升级到当前结构，对应两处改动：

- `status` 由字符串（`upcoming` / `ended`）改成 `TINYINT` 的 `1` / `0`
- `time` 由 `VARCHAR` 改成真正的 `DATETIME`

脚本是分段执行的，包含「先备份表 → 体检脏数据 → 改列类型 → 验收查询」，可以直接整段按顺序执行，也可以在 Navicat 里逐步跑并观察每一步的输出。

### 5.3 配置后端（环境变量）

数据库密码等敏感信息**不写在配置文件里**，`application.yml` 中只有 `${环境变量:默认值}` 占位符。

Windows PowerShell（只在当前窗口有效）：

```powershell
$env:DB_PASSWORD = "你的 MySQL 密码"
$env:UPLOAD_PATH = "D:/campus-activity-uploads/"
```

Windows 永久生效（`setx` 写注册表，**需要新开一个终端**才读得到）：

```cmd
setx DB_PASSWORD "你的 MySQL 密码"
setx UPLOAD_PATH "D:/campus-activity-uploads/"
```

macOS / Linux：

```bash
export DB_PASSWORD="你的 MySQL 密码"
export UPLOAD_PATH="/Users/你的用户名/campus-activity-uploads/"
```

用 IDEA 运行时：`Run / Debug Configurations` → 选中启动配置 → `Environment variables` 里按 `键=值` 填同样的内容，用分号分隔多个。

### 5.4 启动后端

```bash
cd backend
mvn spring-boot:run
```

或者用 IDEA 直接运行 `CampusActivityApplication`。

启动成功的标志是控制台出现：

```
Tomcat started on port 4987 (http)
Started CampusActivityApplication in x.xxx seconds
```

打包成可执行 jar：

```bash
mvn clean package
java -jar target/campus-activity-1.0.0.jar
```

### 5.5 启动前端

```bash
cd frontend
npm install
npm run dev
```

启动后终端会打印本地地址（默认 `http://localhost:5173`）。

生产构建：

```bash
npm run build     # 产物在 frontend/dist/
npm run preview   # 本地预览构建产物
```

若要用 Nginx 托管 `dist/`，需要把 `/api` 和 `/uploads` 反向代理到后端 `4987`，并配置 history 模式回退（`try_files $uri $uri/ /index.html`），否则刷新子路由会 404。

### 5.6 打开页面

| 页面 | 地址 |
| --- | --- |
| 活动列表（首页） | http://localhost:5173/ |
| 活动详情 | http://localhost:5173/activity/1 |
| 活动管理（增删改 + 传图） | http://localhost:5173/manage |

开发期前端把 `/api` 和 `/uploads` 都代理到 `http://localhost:4987`（见 `frontend/vite.config.js`），所以**不存在跨域问题**，不需要后端配置 CORS。

---

## 六、配置项与环境变量一览

全部配置都在 `backend/src/main/resources/application.yml`：

| 配置项 | 环境变量 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `spring.datasource.url` | `DB_URL` | `jdbc:mysql://localhost:3306/campus_activity?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai` | 数据库连接串 |
| `spring.datasource.username` | `DB_USERNAME` | `root` | 数据库用户名 |
| `spring.datasource.password` | `DB_PASSWORD` | 空字符串 | **必须设置**，否则连不上数据库 |
| `upload.path` | `UPLOAD_PATH` | `${user.home}/campus-activity-uploads/` | 上传图片的落盘目录，需要读写权限 |
| `server.port` | 无 | `4987` | 后端端口；临时改端口可加启动参数 `--server.port=8000` |

两点说明：

- **`upload.path` 一份配置、三处读取**：`FileUploadController`（写文件）、`ActivityServiceImpl`（删除换图/删活动时的旧文件）、`WebConfig`（把该目录映射成 `/uploads/**` 静态资源，浏览器才能访问）。三者必须指向同一个目录，否则会出现「传上去了但显示不出来」。
- 默认值用的是 `${user.home}` 而不是相对路径，这样无论从哪个工作目录启动，解析出的绝对路径都一致。

---

## 七、接口说明

### 7.1 统一响应体

所有接口都返回同一个结构，**业务失败也返回 HTTP 200**，前端统一按 `code` 判断：

```json
{ "code": 1, "msg": "success", "data": null }
```

| 字段 | 含义 |
| --- | --- |
| `code` | `1` = 成功，`0` = 失败 |
| `msg` | 提示信息；失败时是给用户看的具体原因 |
| `data` | 业务数据，失败时为 `null` |

### 7.2 接口列表

| # | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 1 | GET | `/api/activities` | 活动列表；两个参数都可选：`keyword`（匹配名称/地点/简介）、`status`（`0` 或 `1`）。都不传等于查全部，按时间倒序 |
| 2 | GET | `/api/activities/{id}` | 活动详情；不存在时返回 `code=0, msg="活动不存在"` |
| 3 | POST | `/api/activities` | 新增活动，请求体为活动 JSON |
| 4 | PUT | `/api/activities/{id}` | 修改活动；`id` 以路径为准，请求体里带 `id` 会被覆盖 |
| 5 | DELETE | `/api/activities/{id}` | 删除活动，并连带删除它引用的上传图片文件 |
| 6 | POST | `/api/upload` | 上传图片（`multipart/form-data`，字段名 `file`），返回可访问的路径 |

### 7.3 请求与响应示例

**查询列表（带筛选）**

```bash
curl "http://localhost:4987/api/activities?keyword=篮球&status=1"
```

**新增活动**

```bash
curl -X POST http://localhost:4987/api/activities \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"校园歌手大赛·复赛\",\"time\":\"2026-10-20 19:00\",\"location\":\"大学生活动中心\",\"summary\":\"复赛名单已公布\",\"detail\":\"欢迎到场观看\",\"image\":\"\",\"status\":1}"
```

请求体字段：

| 字段 | 类型 | 必填 | 限制（与实体类上的校验注解一致） |
| --- | --- | --- | --- |
| `name` | String | 是 | 不超过 100 字 |
| `time` | String | 是 | 格式固定为 `yyyy-MM-dd HH:mm`，例如 `2026-10-20 19:00` |
| `location` | String | 是 | 不超过 100 字 |
| `summary` | String | 是 | 不超过 255 字 |
| `detail` | String | 否 | 不超过 5000 字 |
| `image` | String | 否 | 不超过 255 字；留空时后端自动填默认占位图 `/images/placeholder.jpg` |
| `status` | Integer | 是 | 只能是 `0`（已结束）或 `1`（报名中） |

> ⚠️ `time` 的月、日、时、分**都要写成两位**。`2026-10-1 19:00` 这种写法会被判为格式错误。
> 另外 `time` 一旦格式不对，整个 JSON 都无法被解析成对象，会由全局异常处理返回「请求参数格式不正确」；而字段为空、超长、状态超范围这类问题，走的是另一条分支，会返回**具体哪个字段不合规**。两者的区别见 `GlobalExceptionHander`。

**上传图片**

```bash
curl -X POST http://localhost:4987/api/upload -F "file=@D:/photo.png"
# {"code":1,"data":"/uploads/755f0aac2ca64b87ac1f24e09d48b723.png","msg":"success"}
```

**参数不合规时的返回**

```json
{ "code": 0, "msg": "活动名称不能为空", "data": null }
```

---

## 八、图片上传与存储

### 8.1 一次「新增带图活动」其实发了两次请求

| 步骤 | 请求 | 做的事 |
| --- | --- | --- |
| 1 | `POST /api/upload` | 文件立刻落盘到 `upload.path`，返回路径 `/uploads/xxx.png`，前端把它存进表单的 `image` 字段 |
| 2 | `POST /api/activities` | 点「保存」时，把 `image` 连同其它字段一起作为 JSON 提交，写进数据库 |

也就是说，**后端之间没有任何「拿图片」的动作**，图片地址是前端从第一个响应里取出来、再放进第二个请求体里的。数据库里存的只是这根字符串，图片和活动记录之间没有外键关系。

### 8.2 上传接口做了什么

1. 空文件直接拒绝
2. **后缀白名单**：把文件名后缀转小写后精确匹配 `.jpg` / `.jpeg` / `.png` / `.gif` / `.webp`
3. **UUID 重命名**：落盘文件名整体换成 UUID，避免多人上传同名文件互相覆盖，同时消除文件名带来的路径风险
4. 目录不存在则自动创建，再把文件写入 `upload.path`
5. 返回 `/uploads/文件名`

### 8.3 图片什么时候被删除

- **删除活动**时：先删数据库记录，再删磁盘文件。顺序不能反 —— 万一删库失败而文件已删，就会出现「记录还在、图片没了」的裂图；现在最坏情况只是多留一个没人引用的文件。
- **编辑活动换图**时：更新成功后，比对旧图与新图，不同则删掉旧文件。
- 删除前有两道保险：只处理 `/uploads/` 开头的路径（仓库自带的 `/images/` 是前端静态资源，不能碰），以及**引用计数检查**（还有别的活动在用同一张图就保留）。
- 删文件失败只记日志，不影响接口返回成功。

### 8.4 关于 `uploads/` 目录

`uploads/` 是**运行时产生**的目录，已加入 `.gitignore`，**不在仓库里**。从仓库克隆之后：

- 跑 `init.sql` 得到的 6 条示例活动，图片用的是仓库自带的 `/images/activity-1.jpg` ~ `/images/activity-6.jpg`，可以正常显示；
- 你自己上传的图片会落到 `UPLOAD_PATH` 指向的目录，与仓库无关。

---

## 九、参考与借鉴来源声明

按题目要求（作品要求第 6 条），此处注明参考来源与借鉴内容：

| # | 来源 | 借鉴内容 |
| --- | --- | --- |
| 1 | 面试题目文档《技术开发2026秋一面题目》 | 项目需求、功能范围与验收标准（题目 3） |
| 2 | 本人此前学习阶段完成的 Spring Boot + Vue 练习项目（**本人自己编写，非第三方开源代码**） | 项目分层结构：`Controller / Service / Impl / Mapper / Mapper.xml`、`Result` 统一响应体、`GlobalExceptionHander` 全局异常处理、`WebConfig` 静态资源映射。本项目在此结构基础上按题目需求重新设计了表结构与业务逻辑 |
| 3 | MyBatis 官方文档 | 动态 SQL 的 `<where>` + `<if>` 写法；`Mapper.xml` 与接口「同包同名」的约定；`@Options(useGeneratedKeys)` 主键回填 |
| 4 | Spring Boot / Spring Framework 官方文档 | 配置文件占位符 `${VAR:默认值}`、`spring-boot-starter-validation` 参数校验、`MultipartFile` 文件上传、静态资源映射 |
| 5 | Vue 3 / Vue Router / Vite / axios 官方文档 | 组合式 API（`ref` / `reactive` / `computed`）、`v-model` 与指令用法、路由配置、开发代理配置、请求封装 |
| 6 | 图片素材 | 首页横幅与示例活动配图：学校官网下载 |

除上述来源外，本项目为**个人独立完成**，没有直接复制任何第三方项目的完整代码或页面。

---

## 十、关于 AI 工具的使用说明

题目允许使用 AI 工具辅助开发（考核规则第 2.3 条），但要求作者本人对作品负责。本项目在开发过程中使用 AI 工具（ChatGPT / Codex）协助完成了：需求拆解、代码评审、错误信息排查、配置文件与文档整理。

所有提交的代码均经过本人逐段阅读、运行验证与调试，能够说明每一处的作用与设计原因，并可按评委要求现场修改功能或定位修复缺陷。

---

## 十一、作者与提交信息

| 项 | 内容 |
| --- | --- |
| 作者 | 潘宇衡 |
| 年级:大三 / 专业:软件工程 | 
| 提交时间 | 2026-10-05 |
| 联系方式 | 手机号:18825839818 |