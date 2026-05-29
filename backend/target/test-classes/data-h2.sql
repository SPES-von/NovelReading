-- Minimal seed data for integration tests

INSERT INTO publisher(id, name, sort_order) VALUES (1, '电击文库', 1);
INSERT INTO author(id, name) VALUES (1, '测试作者');

INSERT INTO tag(id, name) VALUES (1, '科幻');

INSERT INTO novel(
  id, title, author_id, publisher_id, cover_url, synopsis, word_count, rating,
  is_completed, is_featured, is_new_select, new_select_sort,
  view_count, recommend_count, flower_count, favorite_count, is_animated,
  latest_chapter_title, latest_chapter_id
) VALUES (
  1, '测试小说', 1, 1, 'https://example.com/cover.jpg', '简介', 1000, 8.5,
  FALSE, TRUE, TRUE, 1,
  100, 10, 3, 2, 0,
  '第一章', 1
);

INSERT INTO novel_tag(novel_id, tag_id) VALUES (1, 1);

INSERT INTO chapter(id, novel_id, volume, title, sort_order, sort, content, word_count)
VALUES (1, 1, '1', '第一章', 0, 1, 'Hello', 5);

-- one admin account: id=1 password=admin (plain, as current system stores plaintext)
INSERT INTO admin(id, password) VALUES (1, 'admin');

