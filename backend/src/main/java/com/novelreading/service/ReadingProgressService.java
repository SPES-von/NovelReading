package com.novelreading.service;

import com.novelreading.entity.Chapter;
import com.novelreading.entity.Novel;
import com.novelreading.entity.ReadingProgress;
import com.novelreading.entity.User;
import com.novelreading.repository.ChapterRepository;
import com.novelreading.repository.ReadingProgressRepository;
import com.novelreading.repository.UserRepository;
import com.novelreading.repository.NovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 阅读进度/书签服务
 * 根据书 id、卷、章节解析出 chapter_id，按用户+书籍唯一：存在则更新 chapter_id，不存在则插入
 */
@Service
@RequiredArgsConstructor
public class ReadingProgressService {

    private final ReadingProgressRepository readingProgressRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;

    /**
     * 保存或更新书签：根据书籍 id 与章节 id 获取 chapter，写入或更新 reading_progress。
     * 记录当前阅读位置 progress_position、书签 bookmark；若存在相同 user_id + novel_id 则更新（含 chapter_id，支持不同卷/章）。
     */
    @Transactional
    public ReadingProgress saveOrUpdateBookmark(Long userId, Long novelId, Long chapterId,
                                                Integer progressPosition, Integer bookmark) {
        Chapter chapter = chapterRepository.findByIdAndNovel_Id(chapterId, novelId).orElse(null);
        if (chapter == null) {
            throw new IllegalArgumentException("章节不存在或不属于该书籍");
        }
        User user = userRepository.findByInternalId(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            throw new IllegalArgumentException("书籍不存在");
        }

        int pos = progressPosition != null && progressPosition >= 0 ? progressPosition : 0;

        Optional<ReadingProgress> existing = readingProgressRepository.findByUser_InternalIdAndNovel_Id(userId, novelId);
        if (existing.isPresent()) {
            ReadingProgress progress = existing.get();
            progress.setChapter(chapter);
            progress.setProgressPosition(pos);
            progress.setBookmark(bookmark != null && bookmark >= 0 ? bookmark : null);
            return readingProgressRepository.save(progress);
        }
        ReadingProgress progress = new ReadingProgress();
        progress.setUser(user);
        progress.setNovel(novel);
        progress.setChapter(chapter);
        progress.setProgressPosition(pos);
        progress.setBookmark(bookmark != null && bookmark >= 0 ? bookmark : null);
        return readingProgressRepository.save(progress);
    }

    /**
     * 查询当前用户对某本书的阅读进度（书签），若有则返回章节 id、标题、阅读位置与书签位置
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> getProgressForNovel(Long userId, Long novelId) {
        return readingProgressRepository.findByUser_InternalIdAndNovel_Id(userId, novelId)
                .map(p -> {
                    Chapter ch = p.getChapter();
                    Map<String, Object> map = new HashMap<>();
                    map.put("chapterId", ch.getId());
                    map.put("chapterTitle", ch.getTitle() != null ? ch.getTitle() : "");
                    map.put("progressPosition", p.getProgressPosition() != null ? p.getProgressPosition() : 0);
                    map.put("bookmark", p.getBookmark());
                    return map;
                });
    }

    /**
     * 当前用户阅读历史列表：reading_progress 全表该用户记录，按最后阅读时间倒序，每条含书籍与当前章节信息
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listAllByUser(Long userId) {
        if (userId == null) return List.of();
        return readingProgressRepository.findByUser_InternalIdOrderByLastReadAtDesc(userId).stream()
                .map(p -> {
                    Novel n = p.getNovel();
                    Chapter ch = p.getChapter();
                    Map<String, Object> map = new HashMap<>();
                    map.put("novelId", n != null ? n.getId() : null);
                    map.put("title", n != null ? n.getTitle() : "");
                    map.put("coverUrl", n != null ? n.getCoverUrl() : null);
                    map.put("chapterId", ch != null ? ch.getId() : null);
                    map.put("chapterTitle", ch != null ? ch.getTitle() : "");
                    map.put("bookmark", p.getBookmark());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
