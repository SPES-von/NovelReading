-- ============================================
-- 轻小说阅读系统 - MySQL 数据库架构
-- 功能说明：存储小说、用户、阅读进度等核心业务数据
-- ============================================

-- 先删除数据库再重新创建，确保干净环境
DROP DATABASE IF EXISTS novel_reading;

-- 创建数据库
CREATE DATABASE novel_reading
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE novel_reading;

-- ============================================
-- 1. 文库/出版社表 (publisher)
-- 功能：存储轻小说文库分类，如电击文库、角川文库等
-- 用于首页"分类"、"文库"下拉菜单的数据源
-- ============================================
CREATE TABLE IF NOT EXISTS publisher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文库唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '文库名称，如电击文库、富士见文库',
    sort_order INT DEFAULT 0 COMMENT '排序序号，数值越小越靠前',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文库/出版社表';

-- ============================================
-- 2. 作者表 (author)
-- 功能：存储小说作者信息
-- 关联小说表的作者字段
-- ============================================
CREATE TABLE IF NOT EXISTS author (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '作者唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '作者姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作者表';

-- ============================================
-- 3. 小说表 (novel)
-- 功能：存储小说核心信息，包括标题、封面、简介、字数等
-- 用于首页特色推荐、热门小说、完本推荐等模块展示
-- ============================================
CREATE TABLE IF NOT EXISTS novel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '小说唯一标识',
    title VARCHAR(255) NOT NULL COMMENT '小说标题',
    author_id BIGINT NOT NULL COMMENT '作者ID，关联author表',
    publisher_id BIGINT NOT NULL COMMENT '文库ID，关联publisher表',
    cover_url VARCHAR(500) COMMENT '封面图片URL',
    synopsis TEXT COMMENT '小说简介',
    word_count INT DEFAULT 0 COMMENT '字数统计',
    rating DECIMAL(3,1) DEFAULT 0 COMMENT '评分，如8.5',
    is_completed TINYINT(1) DEFAULT 0 COMMENT '是否完本：0-连载中，1-已完本',
    is_featured TINYINT(1) DEFAULT 0 COMMENT '是否特色推荐：0-否，1-是',
    is_new_select TINYINT(1) DEFAULT 0 COMMENT '是否新书精选：0-否，1-是',
    new_select_sort INT DEFAULT 0 COMMENT '新书精选排序序号，数值越小越靠前',
    view_count BIGINT DEFAULT 0 COMMENT '阅读量/热度分数',
    recommend_count INT DEFAULT 0 COMMENT '推荐人数',
    flower_count INT DEFAULT 0 COMMENT '鲜花数',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    is_animated TINYINT(1) DEFAULT 0 COMMENT '是否动漫化：0-未动漫化，1-已动漫化',
    latest_chapter_title VARCHAR(255) COMMENT '最新章节标题',
    latest_chapter_id BIGINT COMMENT '最新章节ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_author (author_id),
    INDEX idx_publisher (publisher_id),
    INDEX idx_completed (is_completed),
    INDEX idx_featured (is_featured),
    INDEX idx_new_select (is_new_select),
    INDEX idx_new_select_sort (new_select_sort),
    INDEX idx_recommend_count (recommend_count),
    INDEX idx_view_count (view_count),
    INDEX idx_rating (rating),
    INDEX idx_flower_count (flower_count),
    INDEX idx_favorite_count (favorite_count),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at),
    INDEX idx_is_animated (is_animated),
    INDEX idx_word_count (word_count),
    FOREIGN KEY (author_id) REFERENCES author(id),
    FOREIGN KEY (publisher_id) REFERENCES publisher(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说表';

-- ============================================
-- 4. 小说标签表 (tag)
-- 功能：存储小说分类标签，如科幻、校园青春、后宫等
-- 用于小说详情页的类型展示
-- ============================================
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签唯一标识',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说标签表';

-- ============================================
-- 5. 小说-标签关联表 (novel_tag)
-- 功能：多对多关联小说与标签
-- ============================================
CREATE TABLE IF NOT EXISTS novel_tag (
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (novel_id, tag_id),
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说-标签关联表';

-- ============================================
-- 6. 章节表 (chapter)
-- 功能：存储小说的章节信息
-- 用于阅读页面的章节列表和内容加载
-- ============================================
CREATE TABLE IF NOT EXISTS chapter (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '章节唯一标识',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    volume VARCHAR(50) NOT NULL DEFAULT '1' COMMENT '卷标识，如第一卷、第二卷、第8.5巻',
    title VARCHAR(255) NOT NULL COMMENT '章节标题',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '章节排序序号',
    sort INT DEFAULT 0 COMMENT '卷在书籍详情页作者信息下按钮的排序位置',
    content LONGTEXT COMMENT '章节正文内容，支持百度语音合成朗读',
    word_count INT DEFAULT 0 COMMENT '章节字数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_novel (novel_id),
    INDEX idx_sort (novel_id, sort_order),
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节表';

-- 注：novel.latest_chapter_id 与 chapter 表存在循环引用，故不建外键，由应用层维护

-- ============================================
-- 7. 用户表 (user)
-- 功能：存储用户账户信息，用于登录、注册、JWT 认证
-- 主键为 (username, email)，id 仅用于外键引用
-- ============================================
CREATE TABLE IF NOT EXISTS user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户内部ID，供外键引用',
    username VARCHAR(50) NOT NULL COMMENT '用户名，登录用',
    email VARCHAR(100) NOT NULL COMMENT '邮箱，与用户名共同唯一标识用户',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密存储）',
    nickname VARCHAR(50) COMMENT '昵称/显示名',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_banned TINYINT(1) DEFAULT 0 COMMENT '是否封禁：0-否，1-是',
    banned_at DATETIME DEFAULT NULL COMMENT '封禁开始时间',
    banned_until DATETIME DEFAULT NULL COMMENT '封禁到期时间',
    PRIMARY KEY (username, email),
    UNIQUE KEY uk_id (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 8. 用户阅读进度表 (reading_progress)
-- 功能：记录用户对每本小说的阅读进度，当前阅读章节由 chapter_id 表示
-- 数据可缓存至 Redis 以提升响应速度
-- ============================================
CREATE TABLE IF NOT EXISTS reading_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录唯一标识',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    chapter_id BIGINT NOT NULL COMMENT '当前阅读章节ID，对应 chapter.id',
    progress_position INT DEFAULT 0 COMMENT '章节内阅读位置（字符偏移）',
    bookmark INT DEFAULT NULL COMMENT '书签：章节内字符偏移，可为空',
    last_read_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后阅读时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次阅读时间',
    UNIQUE KEY uk_user_novel (user_id, novel_id),
    INDEX idx_user (user_id),
    INDEX idx_novel (novel_id),
    INDEX idx_chapter_id (chapter_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapter(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户阅读进度表';

-- ============================================
-- 9. 首页推荐榜配置表 (home_recommend)
-- 功能：存储首页6本推荐书籍的固定顺序（支持同一本书多次出现）
-- ============================================
CREATE TABLE IF NOT EXISTS home_recommend (
    position INT PRIMARY KEY COMMENT '推荐位序号 1-6',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页推荐榜配置';

-- ============================================
-- 10. 新书榜配置表 (new_book_chart)
-- 功能：存储新书榜10本书籍的固定顺序及推荐数
-- ============================================
CREATE TABLE IF NOT EXISTS new_book_chart (
    position INT PRIMARY KEY COMMENT '排名 1-10',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新书榜配置';

-- ============================================
-- 10a. 强推榜配置表 (strong_recommend_chart)
-- 功能：存储强推榜固定顺序，与 new_book_chart 结构一致
-- ============================================
CREATE TABLE IF NOT EXISTS strong_recommend_chart (
    position INT PRIMARY KEY COMMENT '排名 1-10',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='强推榜配置';

-- ============================================
-- 10b. 热点榜配置表 (hot_chart)
-- 功能：存储热点榜固定顺序，与 new_book_chart 结构一致
-- ============================================
CREATE TABLE IF NOT EXISTS hot_chart (
    position INT PRIMARY KEY COMMENT '排名 1-10',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热点榜配置';

-- ============================================
-- 11. 热门小说模块配置表 (hot_novels)
-- 功能：存储热门小说模块6本小说的固定顺序（用于轮播展示）
-- ============================================
CREATE TABLE IF NOT EXISTS hot_novels (
    position INT PRIMARY KEY COMMENT '推荐位序号 1-6',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门小说模块配置';

-- ============================================
-- 11a. 用户书架表 (user_bookshelf)
-- 功能：存储用户加入书架的小说，记录用户ID与小说ID的对应关系
-- ============================================
CREATE TABLE IF NOT EXISTS user_bookshelf (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录唯一标识',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY uk_user_novel (user_id, novel_id),
    INDEX idx_user (user_id),
    INDEX idx_novel (novel_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户书架表';

-- ============================================
-- 11b. 用户阅读偏好表 (user_read_preferences)
-- 功能：存储用户个性化阅读设置，如字体大小、背景主题等
-- ============================================
CREATE TABLE IF NOT EXISTS user_read_preferences (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录唯一标识',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    font_size INT DEFAULT 16 COMMENT '字体大小（px）',
    line_height DECIMAL(3,1) DEFAULT 1.8 COMMENT '行高倍数',
    theme VARCHAR(20) DEFAULT 'default' COMMENT '阅读主题：default/sepia/night',
    page_width INT DEFAULT 720 COMMENT '阅读区域宽度（px），0表示自适应',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户阅读偏好表';

-- ============================================
-- 11c. 评论表 (novel_comment)
-- 功能：存储用户对某本小说的评论，支持点赞数、踩击数；踩击数>10的评论不展示；审核=1显示，=0不显示
-- ============================================
CREATE TABLE IF NOT EXISTS novel_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论唯一标识',
    user_id BIGINT NOT NULL COMMENT '用户ID，关联user.id',
    novel_id BIGINT NOT NULL COMMENT '小说ID，关联novel.id',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    dislike_count INT DEFAULT 0 COMMENT '踩击数',
    audit TINYINT(1) DEFAULT 0 COMMENT '审核：0-不显示，1-显示',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    INDEX idx_novel (novel_id),
    INDEX idx_audit (audit),
    INDEX idx_user (user_id),
    INDEX idx_like_count (like_count),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说评论表';

-- 若 novel_comment 表已存在且无 audit 字段，可执行：
-- ALTER TABLE novel_comment ADD COLUMN audit TINYINT(1) DEFAULT 0 COMMENT '审核：0-不显示，1-显示';
-- ALTER TABLE novel_comment ADD INDEX idx_audit (audit);

-- ============================================
-- 12. 完本推荐模块配置表 (completed_novels)
-- 功能：存储完本推荐模块9本小说的固定顺序（前3条是经典完书模块，后6条是完书推荐模块）
-- ============================================
CREATE TABLE IF NOT EXISTS completed_novels (
    position INT PRIMARY KEY COMMENT '推荐位序号 1-9（1-3为经典完本，4-9为完本推荐）',
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='完本推荐模块配置';

-- ============================================
-- 管理员表 (admin)
-- 功能：系统管理员账号，仅存储管理员 id 与密码，用于后台登录
-- ============================================
CREATE TABLE IF NOT EXISTS admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员唯一标识，同时作为登录账号',
    password VARCHAR(255) NOT NULL COMMENT '密码（明文存储，不加密）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ============================================
-- 初始化文库数据（对应首页分类下拉菜单）
-- ============================================
INSERT INTO publisher (name, sort_order) VALUES
('电击文库', 1),
('富士见文库', 2),
('角川文库', 3),
('MF文库J', 4),
('Fami通文库', 5),
('GA文库', 6),
('HJ文库', 7),
('一迅社', 8),
('集英社', 9),
('小学馆', 10),
('讲谈社', 11),
('少女文库', 12),
('其他文库', 13),
('华文轻小说', 14),
('原生幻想', 15);

-- ============================================
-- 初始化标签数据
-- ============================================
INSERT INTO tag (name) VALUES
('科幻'), ('校园青春'), ('后宫'), ('欢乐向'),
('青春日常'), ('恋爱'), ('奇幻'), ('冒险');


