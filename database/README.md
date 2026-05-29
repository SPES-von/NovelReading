# 轻小说阅读系统 - 数据库说明

## 数据库架构概览

数据库 `novel_reading` 用于存储小说、用户、阅读进度等核心业务数据。  
**字符集**：utf8mb4，**排序规则**：utf8mb4_unicode_ci。  
建库脚本会先 `DROP DATABASE IF EXISTS novel_reading` 再创建，确保干净环境。

---

## 表结构及功能说明

### 1. publisher（文库/出版社表）
- **功能**：存储轻小说文库分类，如电击文库、角川文库等
- **用途**：首页「分类」「文库」下拉菜单的数据源
- **主要字段**：id（自增主键）, name, sort_order（排序序号）, created_at, updated_at

### 2. author（作者表）
- **功能**：存储小说作者信息
- **用途**：关联小说表的作者字段
- **主要字段**：id（自增主键）, name, created_at, updated_at

### 3. novel（小说表）
- **功能**：存储小说核心信息（标题、封面、简介、字数、评分、完本/推荐/新书精选/动漫化等）
- **用途**：首页特色推荐、热门小说、完本推荐、新书精选、筛选与排行等
- **主要字段**：id, title, author_id, publisher_id, cover_url, synopsis, word_count, rating, is_completed, is_featured, is_new_select, new_select_sort, view_count, recommend_count, flower_count, favorite_count, is_animated, latest_chapter_title, latest_chapter_id, created_at, updated_at
- **说明**：latest_chapter_id 与 chapter 表存在逻辑关联，未建外键，由应用层维护

### 4. tag（小说标签表）
- **功能**：存储小说分类标签，如科幻、校园青春、后宫等
- **用途**：小说详情页类型展示；分类/筛选页的作品主题筛选
- **主要字段**：id（自增主键）, name, created_at

### 5. novel_tag（小说-标签关联表）
- **功能**：多对多关联小说与标签
- **主要字段**：novel_id, tag_id（联合主键），外键均 ON DELETE CASCADE

### 6. chapter（章节表）
- **功能**：存储小说的章节信息及正文内容
- **用途**：阅读页章节列表与内容加载；章节内容支持语音合成朗读
- **主要字段**：id, novel_id, title, sort_order, content（LONGTEXT）, word_count, created_at, updated_at

### 7. user（用户表）
- **功能**：存储用户账户信息，用于登录、注册、JWT 认证
- **说明**：业务主键为 **(username, email)**，id 为自增唯一键，仅供外键引用；username、email 各自唯一
- **主要字段**：id（UNIQUE）, username, email, password（BCrypt）, nickname, avatar_url, created_at, updated_at

### 8. reading_progress（用户阅读进度表）
- **功能**：记录用户对每本小说的阅读进度（当前章节、章节内位置）
- **用途**：恢复阅读位置；可缓存至 Redis 提升响应速度
- **主要字段**：id, user_id, novel_id, chapter_id, progress_position, last_read_at, created_at；唯一约束 (user_id, novel_id)

### 9. home_recommend（首页推荐榜配置表）
- **功能**：存储首页 6 本推荐书籍的固定顺序（支持同一本书多次出现）
- **主要字段**：position（主键 1–6）, novel_id, created_at

### 10. new_book_chart（新书榜配置表）
- **功能**：存储新书榜 10 本书籍的固定顺序
- **主要字段**：position（主键 1–10）, novel_id, created_at

### 10a. strong_recommend_chart（强推榜配置表）
- **功能**：存储强推榜 10 本书籍的固定顺序
- **主要字段**：position（主键 1–10）, novel_id, created_at

### 10b. hot_chart（热点榜配置表）
- **功能**：存储热点榜 10 本书籍的固定顺序
- **主要字段**：position（主键 1–10）, novel_id, created_at

### 11. hot_novels（热门小说模块配置表）
- **功能**：存储热门小说模块 6 本小说的固定顺序（用于轮播展示）
- **主要字段**：position（主键 1–6）, novel_id, created_at

### 11a. user_bookshelf（用户书架表）
- **功能**：存储用户加入书架的小说，记录用户与小说的收藏关系
- **用途**：个人中心「收藏列表」、我的书架；加入/移出书架时同步更新 novel.favorite_count
- **主要字段**：id, user_id, novel_id, created_at；唯一约束 (user_id, novel_id)

### 11b. user_read_preferences（用户阅读偏好表）
- **功能**：存储用户个性化阅读设置（字体大小、行高、主题、阅读区域宽度等）
- **用途**：个人中心「设置个性」；阅读页应用字体、行高、主题、区域宽度
- **主要字段**：id, user_id（UNIQUE）, font_size, line_height, theme（如 default/sepia/night）, page_width, created_at, updated_at

### 12. completed_novels（完本推荐模块配置表）
- **功能**：存储完本推荐模块 9 本小说的固定顺序（前 3 条为经典完本，后 6 条为完本推荐）
- **主要字段**：position（主键 1–9）, novel_id, created_at

---

## 初始化数据说明（init-data.sql）

`init-data.sql` 在 `schema.sql` 执行后执行，用于演示首页各模块及分类/筛选效果。

### 基础数据（schema.sql 内已包含）
- **文库（publisher）**：电击文库、富士见文库、角川文库、MF 文库 J、Fami 通文库、GA 文库、HJ 文库、一迅社、集英社、小学馆、讲谈社、少女文库、其他文库、华文轻小说、原生幻想等
- **标签（tag）**：科幻、校园青春、后宫、欢乐向、青春日常、恋爱、奇幻、冒险

### init-data.sql 主要内容
| 内容 | 说明 |
|------|------|
| **示例用户** | 用户 von（邮箱 von@example.com），密码为 123456 的 BCrypt 加密；对应 user_id=1 |
| **用户阅读偏好** | user_id=1 的默认偏好：font_size=16, line_height=1.8, theme=default, page_width=720 |
| **作者** | 多条作者记录（id 约 1–47），供小说关联 |
| **小说** | 多本小说数据，包含首页推荐、强推榜、完本推荐、新书精选、新书榜、热点榜、热门小说等所需条目；含 cover_url、synopsis、word_count、rating、is_completed、is_featured、is_new_select、view_count、recommend_count、flower_count、favorite_count、is_animated、latest_chapter_title 等 |
| **首页推荐** | home_recommend 共 6 个位置 |
| **新书榜** | new_book_chart 共 10 个位置 |
| **强推榜** | strong_recommend_chart 共 10 个位置 |
| **热点榜** | hot_chart 共 10 个位置 |
| **热门小说** | hot_novels 共 6 个位置 |
| **完本推荐** | completed_novels 共 9 个位置（前 3 经典完本，后 6 完本推荐） |
| **用户书架** | 示例用户 von 的书架：user_bookshelf 中 user_id=1 的若干 novel_id |
| **标签扩展** | 在 schema 的 8 个标签基础上，增加校园、百合、转生、异世界等更多 tag，并写入 novel_tag 关联 |
| **分类测试数据** | 为小说设置差异化 flower_count、favorite_count、created_at、updated_at，便于测试鲜花数/收藏数排序及「更新时间」筛选；部分作品设置 is_animated=1 |

---

## 执行顺序

1. **执行 `schema.sql`**：创建数据库 `novel_reading`、所有表结构及索引/外键，并插入文库与初始 8 个标签
2. **执行 `init-data.sql`**：导入示例用户、作者、小说、各榜单配置、小说-标签关联、用户书架及分类测试数据（可选，用于本地/演示环境）

---

## 图片存储说明

封面不存二进制，仅存 **URL 或路径**，由 Web 服务器或 CDN 提供访问。

| 表名 | 列名 | 类型 | 说明 |
|------|------|------|------|
| novel | cover_url | VARCHAR(500) | 小说封面 URL 或相对路径（如 /img/xxx.jpg） |

- 特色推荐、推荐榜、强推榜、完本推荐、热门小说等均从 `novel.cover_url` 取图，前端按需缩放。
- 实际图片文件建议放在静态目录或对象存储（如 OSS/COS），库中只存访问地址。

---

## 技术说明

- **MySQL 8+** 推荐
- **字符集**：utf8mb4，**排序规则**：utf8mb4_unicode_ci
- **存储引擎**：InnoDB
- 所有与 novel/user 相关的配置表及关联表外键均为 ON DELETE CASCADE，删除小说或用户时从表数据一并清理
