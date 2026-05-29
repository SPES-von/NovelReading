# 第 4 章 系统设计 — 图表清单与快速成图方法

本目录下存放毕业论文第 4 章所需的 PlantUML 源文件。所有图均需在正式排版时**导出为图片**（如 PNG/SVG）后插入 Word。

## 图表清单

| 图号 | 文件名 | 说明 |
|------|--------|------|
| 图4-1 | 图4-1-系统架构图.puml | 系统总体架构设计图（客户端→通信层→服务端各层→数据层） |
| 图4-2 | 图4-2-功能模块图.puml | 系统功能模块图（读者端 6 模块 + 管理端 6 模块） |
| 图4-3 | 图4-3-用户登录活动图.puml | 用户登录业务流程活动图（泳道图） |
| 图4-3-2 | 图4-3-2-用户阅读小说时序图.puml | 用户阅读小说时序图（用户/系统泳道分支） |
| 图4-3-5 | 图4-3-5-管理员管理用户信息时序图.puml | 管理员管理用户信息时序图 |
| 图4-3-6 | 图4-3-6-管理员上架更新小说时序图.puml | 管理员上架/更新小说时序图 |
| 图4-5 | 图4-5-加入书架活动图.puml | 加入书架业务流程活动图 |
| 图4-6 | 图4-6-听书时序图.puml | 听书功能关键业务流程时序图 |
| 图4-7 | 图4-7-数据库总体E-R图.puml | 数据库总体 E-R 图（概要） |
| 图4-8 | 图4-8-小说与章节E-R图.puml | 小说、作者、文库、标签、章节 实体 E-R 图 |
| 图4-9 | 图4-9-用户与书架进度E-R图.puml | 用户、书架、阅读进度、阅读偏好 实体 E-R 图 |

## 第 5 章 图表

| 图号 | 文件名 | 说明 |
|------|--------|------|
| 图5-1 | 图5-1-系统部署结构图.puml | 系统部署结构图（浏览器、Nginx、前端静态、Spring Boot、MySQL、Redis） |

## 快速生成图片的三种方式

### 方式一：VS Code + PlantUML 插件（推荐）

1. 安装 [PlantUML](https://marketplace.visualstudio.com/items?itemName=jebbs.plantuml) 插件。
2. 本机安装 [Java](https://www.java.com/) 与 [Graphviz](https://graphviz.org/)（或使用插件自带的服务器渲染）。
3. 在 VS Code 中打开对应 `.puml` 文件，按 `Alt+D` 预览。
4. 右键画布 → **Export Current Diagram** → 选择 PNG 或 SVG，保存到所需目录后插入论文。

### 方式二：在线 PlantUML

1. 打开 https://www.plantuml.com/plantuml/uml 。
2. 将 `.puml` 文件内容复制粘贴到网页编辑器。
3. 页面会自动生成图，右键图片另存为 PNG/SVG。

### 方式三：命令行批量导出

1. 下载 [plantuml.jar](https://plantuml.com/zh/download)。
2. 在项目根目录执行（需已安装 Java）：
   ```bash
   java -jar plantuml.jar docs/图4-*.puml
   ```
3. 图片将生成在与 `.puml` 同目录下，同名但扩展名为 `.png`。

## 其他工具（可选）

- **Draw.io / diagrams.net**：若需手绘风格或与 Word 协同，可按照论文中的图说明，在 draw.io 中重绘架构图、时序图、E-R 图，再导出 PNG/PDF。
- **数据库 E-R 图**：可使用 MySQL Workbench 的 EER 反向工程（从 `database/schema.sql` 建表后反向生成），或 [dbdiagram.io](https://dbdiagram.io/) 根据表结构绘制。

## 用例图（第 3 章）

第 3 章用例图源文件也在本目录：

- 用例图-轻小说阅读系统.puml（图3-1）
- 用例图-访客.puml
- 用例图-注册用户.puml

导出方式同上。
