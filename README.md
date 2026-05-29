# 轻小说阅读系统

基于 Spring Boot + Vue.js + MySQL + Redis + JWT + 百度语音合成 API 的轻小说阅读平台。

## 技术栈说明

| 技术 | 功能描述 |
|------|----------|
| **Spring Boot** | 简化 Java EE 开发，实现快速配置和自动化部署，提升系统开发效率 |
| **MySQL** | 关系型数据库，高效存储小说信息、用户数据等核心数据，保障数据查询速度 |
| **Vue.js** | 现代前端框架，实现响应式界面设计，提升阅读页面的交互体验和流畅度 |
| **RESTful API** | 实现前后端分离架构，确保系统各模块独立开发与维护，提升灵活性 |
| **JWT** | 实现用户身份验证和权限管理，保障用户数据安全和系统访问控制 |
| **百度语音合成 API** | 实现小说朗读功能，提供自然流畅的听书体验 |
| **Redis** | 缓存热门小说数据和用户阅读进度，提升系统响应速度 |

## 项目结构

```
NovelReading/
├── backend/          # Spring Boot 后端
├── frontend/         # Vue.js 前端
├── database/         # MySQL 数据库脚本
│   ├── schema.sql    # 表结构及注释
│   └── init-data.sql # 示例数据
└── README.md
```

## 数据库说明

执行 `database/schema.sql` 创建数据库和表，执行 `init-data.sql` 导入示例数据。

表结构及功能注释见 `database/schema.sql` 内各表 COMMENT。

## 启动方式

### 1. 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8+
- Redis（可选，用于缓存）

### 2. 后端

```bash
# 修改 backend/src/main/resources/application.yml 中的数据库连接
cd backend
mvn spring-boot:run
```

后端默认运行在 http://localhost:8080，API 前缀为 `/api`。

### 3. 前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 http://localhost:5173，通过 Vite 代理访问后端 API。

### 4. 无 Redis 模式（可选）

若本地未安装 Redis，可添加启动参数：`--spring.profiles.active=no-redis`，将使用内存缓存。

### 5. 百度语音合成（可选）

在 `application.yml` 或环境变量中配置：
- `BAIDU_SPEECH_API_KEY`
- `BAIDU_SPEECH_SECRET_KEY`

## API 接口

| 接口 | 说明 |
|------|------|
| GET /api/novels/home | 首页汇总数据 |
| GET /api/novels/hot | 强推榜 |
| GET /api/novels/featured | 特色推荐 |
| GET /api/novels/completed | 完本推荐 |
| GET /api/novels/{id} | 小说详情 |
| GET /api/novels/search?keyword= | 搜索 |
| GET /api/publishers | 文库列表 |
| POST /api/auth/login | 登录 |
| POST /api/auth/register | 注册 |
| GET /api/chapters/novel/{novelId} | 章节列表 |
| GET /api/speech/chapter/{chapterId} | 章节语音合成 |
