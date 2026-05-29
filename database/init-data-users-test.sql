-- ============================================
-- 用户表测试数据（20 条不重复用户）
-- 密码均为 123456（BCrypt 与 init-data 中 von 相同）
-- 执行前请确保已执行 schema 与 init-data，本脚本在已有 user 基础上追加
-- ============================================
USE novel_reading;

-- 密码 123456 的 BCrypt 哈希；显式指定 is_banned 避免“无默认值”错误
INSERT INTO `user` (username, password, nickname, email, avatar_url, is_banned) VALUES
('alice_01', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '爱丽丝', 'alice01@test.com', NULL, 0),
('bob_reader', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', 'Bob', 'bob.reader@test.com', NULL, 0),
('cat_novel', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '小喵', 'cat.novel@test.com', NULL, 0),
('david_li', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '大卫', 'david.li@test.com', NULL, 0),
('emma_zhang', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '艾玛', 'emma.zhang@test.com', NULL, 0),
('frank_wang', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '王小明', 'frank.wang@test.com', NULL, 0),
('grace_chen', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '陈小美', 'grace.chen@test.com', NULL, 0),
('henry_zhou', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '周亨利', 'henry.zhou@test.com', NULL, 0),
('iris_lin', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '林小艾', 'iris.lin@test.com', NULL, 0),
('jack_huang', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '黄杰克', 'jack.huang@test.com', NULL, 0),
('kate_liu', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '刘凯特', 'kate.liu@test.com', NULL, 0),
('leo_xu', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '徐里奥', 'leo.xu@test.com', NULL, 0),
('mia_sun', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '孙米娅', 'mia.sun@test.com', NULL, 0),
('nick_ma', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '马尼克', 'nick.ma@test.com', NULL, 0),
('olivia_zhao', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '赵奥莉', 'olivia.zhao@test.com', NULL, 0),
('paul_gao', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '高保罗', 'paul.gao@test.com', NULL, 0),
('qin_luo', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '罗琴', 'qin.luo@test.com', NULL, 0),
('ryan_he', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '何瑞恩', 'ryan.he@test.com', NULL, 0),
('sara_guo', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '郭莎拉', 'sara.guo@test.com', NULL, 0),
('tom_tang', '$2a$10$ZteaDELUr8W4l8r8noUOueh.056srL/7/o1OSpi3cZVqorNk6maS2', '唐汤姆', 'tom.tang@test.com', NULL, 0);

-- ============================================
-- 评论表测试数据（必须先执行 schema + init-data，保证存在 user id=1 和 novel id=1,2）
-- 仅使用 user_id=1（init-data 中的 von），避免外键约束失败
-- ============================================
INSERT INTO novel_comment (user_id, novel_id, content, like_count, dislike_count, audit, created_at) VALUES
(1, 1, '这本催眠APP的设定太有意思了，期待后续！', 5, 0, 1, '2024-01-15 10:30:00'),
(1, 2, '败北女角们太可爱了，温水好惨哈哈。', 12, 0, 1, '2024-02-20 14:00:00'),
(1, 1, '用户评论：剧情节奏不错，就是有点短。', 3, 1, 0, '2024-03-01 09:15:00'),
(1, 2, '八奈见和烧盐我都喜欢，难以抉择。', 8, 0, 1, '2024-03-05 16:45:00'),
(1, 1, '来打卡～轻小说赛高！', 0, 0, 0, '2024-03-10 11:20:00'),
(1, 2, '温水的吐槽太真实了，笑死。', 20, 2, 1, '2024-03-12 08:00:00'),
(1, 1, '希望作者多写点催眠应用的桥段。', 1, 0, 0, '2024-03-15 13:30:00'),
(1, 2, '败犬女主这个名字起得好。', 6, 0, 1, '2024-03-18 17:00:00');
