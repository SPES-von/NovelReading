package com.novelreading.service;

import com.novelreading.dto.NovelDTO;
import com.novelreading.dto.NovelSimpleDTO;
import com.novelreading.dto.PublisherNovelDTO;
import com.novelreading.entity.HomeRecommend;
import com.novelreading.entity.Novel;
import com.novelreading.entity.NewBookChart;
import com.novelreading.entity.HotNovel;
import com.novelreading.entity.CompletedNovel;
import com.novelreading.entity.StrongRecommendChart;
import com.novelreading.entity.HotChart;
import com.novelreading.entity.User;
import com.novelreading.entity.UserBookshelf;
import com.novelreading.repository.HomeRecommendRepository;
import com.novelreading.repository.NovelRepository;
import com.novelreading.repository.UserBookshelfRepository;
import com.novelreading.repository.UserRepository;
import com.novelreading.repository.NewBookChartRepository;
import com.novelreading.repository.HotNovelRepository;
import com.novelreading.repository.CompletedNovelRepository;
import com.novelreading.repository.StrongRecommendChartRepository;
import com.novelreading.repository.HotChartRepository;
import com.novelreading.repository.PublisherRepository;
import com.novelreading.repository.ChapterRepository;
import com.novelreading.entity.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小说业务服务
 * 提供首页各模块数据：特色推荐、强推榜、完本推荐、文库列表等
 * 使用 @Cacheable 缓存热门数据到 Redis
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NovelService {

    private final NovelRepository novelRepository;
    private final PublisherRepository publisherRepository;
    private final HomeRecommendRepository homeRecommendRepository;
    private final NewBookChartRepository newBookChartRepository;
    private final HotNovelRepository hotNovelRepository;
    private final CompletedNovelRepository completedNovelRepository;
    private final StrongRecommendChartRepository strongRecommendChartRepository;
    private final HotChartRepository hotChartRepository;
    private final UserBookshelfRepository userBookshelfRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;

    /** 排行榜：6 个 ORDER BY 取前10条 */
    public Map<String, List<NovelDTO>> getRankings() {
        Map<String, List<NovelDTO>> map = new LinkedHashMap<>();
        map.put("wordCount", novelRepository.findTop10ByOrderByWordCountDesc().stream().map(this::toDTO).collect(Collectors.toList()));
        map.put("rating", novelRepository.findTop10ByOrderByRatingDesc().stream().map(this::toDTO).collect(Collectors.toList()));
        map.put("viewCount", novelRepository.findTop10ByOrderByViewCountDesc().stream().map(this::toDTO).collect(Collectors.toList()));
        map.put("recommendCount", novelRepository.findTop10ByOrderByRecommendCountDesc().stream().map(this::toDTO).collect(Collectors.toList()));
        map.put("flowerCount", novelRepository.findTop10ByOrderByFlowerCountDesc().stream().map(this::toDTO).collect(Collectors.toList()));
        map.put("favoriteCount", novelRepository.findTop10ByOrderByFavoriteCountDesc().stream().map(this::toDTO).collect(Collectors.toList()));
        return map;
    }

    /** 全书页完本榜单：is_completed=1，按 sort 排序取前15。sort: flower|rating|viewCount|favorite，默认 viewCount */
    public List<NovelDTO> getCompletedChart(String sort) {
        List<Novel> list;
        if ("flower".equals(sort)) {
            list = novelRepository.findTop15ByIsCompletedTrueOrderByFlowerCountDesc();
        } else if ("rating".equals(sort)) {
            list = novelRepository.findTop15ByIsCompletedTrueOrderByRatingDesc();
        } else if ("favorite".equals(sort)) {
            list = novelRepository.findTop15ByIsCompletedTrueOrderByFavoriteCountDesc();
        } else {
            list = novelRepository.findTop15ByIsCompletedTrueOrderByViewCountDesc();
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** 强推榜：优先使用配置表顺序，否则按热度降序取前10 */
    public List<NovelDTO> getStrongRecommendList() {
        List<StrongRecommendChart> chart = strongRecommendChartRepository.findAllByOrderByPositionAsc();
        if (!chart.isEmpty()) {
            return chart.stream()
                    .map(c -> c.getNovel())
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        return novelRepository.findTop10ByOrderByViewCountDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** 热点榜：优先使用配置表顺序，否则按热度降序取前10 */
    public List<NovelDTO> getHotChartList() {
        List<HotChart> chart = hotChartRepository.findAllByOrderByPositionAsc();
        if (!chart.isEmpty()) {
            return chart.stream()
                    .map(c -> c.getNovel())
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        return novelRepository.findTop10ByOrderByViewCountDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** 强推榜/热点榜（兼容旧接口，返回强推榜） */
    @Cacheable(value = "hotNovels", key = "'list'")
    public List<NovelDTO> getHotNovels() {
        return getStrongRecommendList();
    }

    /** 特色推荐小说（首页大图） */
    @Cacheable(value = "featuredNovel", key = "'single'")
    public NovelDTO getFeaturedNovel() {
        return novelRepository.findByIsFeaturedTrue().stream()
                .findFirst()
                .map(this::toDTO)
                .orElseGet(() -> novelRepository.findAll(PageRequest.of(0, 1))
                        .stream().findFirst().map(this::toDTO).orElse(null));
    }

    /** 横向推荐列表（按热度） */
    public List<NovelDTO> getRecommendList() {
        return novelRepository.findTop10ByOrderByViewCountDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** 首页推荐榜6本（固定顺序，与图片完全一致） */
    public List<NovelDTO> getHomeRecommendList() {
        return homeRecommendRepository.findAllByOrderByPositionAsc()
                .stream()
                .map(hr -> hr.getNovel())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** 热门小说模块6本（固定顺序，用于轮播展示） */
    public List<NovelDTO> getHotNovelsList() {
        return hotNovelRepository.findAllByOrderByPositionAsc()
                .stream()
                .map(hn -> hn.getNovel())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** 新书精选（按 new_select_sort 排序） */
    public List<NovelSimpleDTO> getNewSelectList() {
        return novelRepository.findByNewSelectSortGreaterThanOrderByNewSelectSortAsc(0)
                .stream()
                .map(this::toSimpleDTO)
                .collect(Collectors.toList());
    }

    /** 新书榜：优先使用配置表顺序，否则按推荐数降序取前10 */
    public List<NovelDTO> getNewBookChart() {
        List<NewBookChart> chart = newBookChartRepository.findAllByOrderByPositionAsc();
        if (!chart.isEmpty()) {
            return chart.stream()
                    .map(nbc -> nbc.getNovel())
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        return novelRepository.findTop10ByOrderByRecommendCountDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** 完本推荐（使用配置表，返回后6条） */
    @Cacheable(value = "completedNovels", key = "'list'")
    public List<NovelSimpleDTO> getCompletedRecommend() {
        List<CompletedNovel> completedNovels = completedNovelRepository.findAllByOrderByPositionAsc();
        if (!completedNovels.isEmpty() && completedNovels.size() >= 6) {
            // 返回后6条（position 4-9）
            return completedNovels.stream()
                    .skip(3) // 跳过前3条
                    .map(cn -> cn.getNovel())
                    .map(this::toSimpleDTO)
                    .collect(Collectors.toList());
        }
        // 回退到原来的逻辑
        return novelRepository.findByIsCompletedTrueOrderByViewCountDesc(PageRequest.of(0, 20))
                .stream()
                .map(this::toSimpleDTO)
                .collect(Collectors.toList());
    }

    /** 经典完本列表（使用配置表，返回前3条，用于轮播展示） */
    @Cacheable(value = "classicCompletedList", key = "'list'")
    public List<NovelDTO> getClassicCompletedList() {
        List<CompletedNovel> completedNovels = completedNovelRepository.findAllByOrderByPositionAsc();
        if (!completedNovels.isEmpty() && completedNovels.size() >= 3) {
            // 返回前3条（position 1-3）
            return completedNovels.stream()
                    .limit(3) // 只取前3条
                    .map(cn -> cn.getNovel())
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        // 回退到原来的逻辑
        return novelRepository.findByIsCompletedTrueOrderByViewCountDesc(PageRequest.of(0, 3))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** 各文库下的热门小说（热门小说板块） */
    @Cacheable(value = "publisherNovels", key = "'all'")
    public List<PublisherNovelDTO> getPublisherNovels() {
        return publisherRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(p -> {
                    PublisherNovelDTO dto = new PublisherNovelDTO();
                    dto.setPublisherId(p.getId());
                    dto.setPublisherName(p.getName());
                    dto.setNovels(novelRepository.findByPublisherIdOrderByViewCountDesc(p.getId(), PageRequest.of(0, 5))
                            .stream().map(this::toSimpleDTO).collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /** 经典完本（单本突出展示，保留用于兼容） */
    public NovelDTO getClassicCompleted() {
        List<NovelDTO> list = getClassicCompletedList();
        return list.isEmpty() ? null : list.get(0);
    }

    /** 文库列表（分类下拉菜单） */
    @Cacheable(value = "publishers", key = "'list'")
    public List<com.novelreading.entity.Publisher> getPublishers() {
        return publisherRepository.findAllByOrderBySortOrderAsc();
    }

    /** 小说详情（含该书章节数） */
    public NovelDTO getNovelById(Long id) {
        NovelDTO dto = novelRepository.findById(id).map(this::toDTO).orElse(null);
        if (dto != null) {
            dto.setChapterCount((int) chapterRepository.countByNovelId(id));
        }
        return dto;
    }

    /** 送鲜花：鲜花数 +1 */
    @Transactional
    public void incrementFlower(Long novelId) {
        novelRepository.findById(novelId).ifPresent(n -> {
            n.setFlowerCount((n.getFlowerCount() != null ? n.getFlowerCount() : 0) + 1);
            novelRepository.save(n);
        });
    }

    /** 收藏：收藏数 +1（无用户时仅更新小说收藏数，有用户时同时写入用户书架表） */
    @Transactional
    public void incrementFavorite(Long novelId) {
        novelRepository.findById(novelId).ifPresent(n -> {
            n.setFavoriteCount((n.getFavoriteCount() != null ? n.getFavoriteCount() : 0) + 1);
            novelRepository.save(n);
        });
    }

    /** 加入书架：将用户ID与小说ID写入用户书架表，并更新小说收藏数
     * @return true-新增成功，false-书架中已存在此书 */
    @Transactional
    public boolean addToBookshelf(Long userId, Long novelId) {
        if (userId == null || novelId == null) return false;
        if (userBookshelfRepository.existsByUser_InternalIdAndNovel_Id(userId, novelId)) {
            return false;
        }
        User user = userRepository.findByInternalId(userId).orElse(null);
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (user == null || novel == null) return false;
        UserBookshelf ub = new UserBookshelf();
        ub.setUser(user);
        ub.setNovel(novel);
        userBookshelfRepository.save(ub);
        novel.setFavoriteCount((novel.getFavoriteCount() != null ? novel.getFavoriteCount() : 0) + 1);
        novelRepository.save(novel);
        return true;
    }

    /** 获取用户书架中的小说列表（按加入时间倒序） */
    public List<NovelDTO> getBookshelf(Long userId) {
        if (userId == null) return List.of();
        return userBookshelfRepository.findByUser_InternalIdOrderByCreatedAtDesc(userId).stream()
                .map(ub -> ub.getNovel())
                .filter(n -> n != null)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** 从书架移除：删除用户书架记录并更新小说收藏数
     * @return true-移除成功，false-书架中不存在此书 */
    @Transactional
    public boolean removeFromBookshelf(Long userId, Long novelId) {
        if (userId == null || novelId == null) return false;
        UserBookshelf ub = userBookshelfRepository.findByUser_InternalIdAndNovel_Id(userId, novelId).orElse(null);
        if (ub == null) return false;
        Novel novel = ub.getNovel();
        userBookshelfRepository.delete(ub);
        if (novel != null && novel.getFavoriteCount() != null && novel.getFavoriteCount() > 0) {
            novel.setFavoriteCount(novel.getFavoriteCount() - 1);
            novelRepository.save(novel);
        }
        return true;
    }

    /** 搜索：支持标题、简介、标签名模糊查询，返回分页。总数用独立 native 查询保证准确。 */
    public Page<NovelDTO> search(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return Page.empty(PageRequest.of(page, size));
        }
        String kw = keyword.trim();
        var pageable = PageRequest.of(page, size);
        var content = novelRepository.searchByKeywordContent(kw, pageable);
        long total = novelRepository.countSearchByKeyword(kw);
        return new PageImpl<>(content.stream().map(this::toDTO).toList(), pageable, total);
    }

    /**
     * 筛选页：按作品主题(tag)、是否动画化、字数区间、更新时间区间筛选，并支持多种排序
     * @param tagId 作品主题标签ID，null 表示不限
     * @param isAnimated 是否动漫化：null-不限，0-未动漫化，1-已动漫化
     * @param wordCountRange 字数区间：under30/30_50/50_100/over100，null 表示不限
     * @param updatedWithinDays 更新在N日内：3/7/15/30，null 表示不限
     * @param sort 排序：flower-鲜花数,favorite-收藏数,created-入库时间,updated-更新时间
     */
    public Page<NovelDTO> browse(Long tagId, Integer isAnimated, String wordCountRange,
                                  Integer updatedWithinDays, String sort, int page, int size) {
        Sort order = buildBrowseSort(sort);
        Specification<Novel> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (tagId != null) {
                Join<Novel, Tag> tagJoin = root.join("tags");
                predicates.add(cb.equal(tagJoin.get("id"), tagId));
                if (query.getResultType() != Long.class) {
                    query.distinct(true);
                }
            }
            if (isAnimated != null) {
                predicates.add(cb.equal(root.get("isAnimated"), isAnimated));
            }
            if (wordCountRange != null && !wordCountRange.isEmpty()) {
                switch (wordCountRange) {
                    case "under30" -> predicates.add(cb.lt(root.get("wordCount"), 300000));
                    case "30_50" -> {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("wordCount"), 300000));
                        predicates.add(cb.lt(root.get("wordCount"), 500000));
                    }
                    case "50_100" -> {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("wordCount"), 500000));
                        predicates.add(cb.lt(root.get("wordCount"), 1000000));
                    }
                    case "over100" -> predicates.add(cb.greaterThanOrEqualTo(root.get("wordCount"), 1000000));
                    default -> {}
                }
            }
            if (updatedWithinDays != null && updatedWithinDays > 0) {
                LocalDateTime since = LocalDateTime.now().minusDays(updatedWithinDays);
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), since));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return novelRepository.findAll(spec, PageRequest.of(page, size, order))
                .map(this::toDTO);
    }

    private Sort buildBrowseSort(String sort) {
        if (sort == null) sort = "flower";
        return switch (sort) {
            case "favorite" -> Sort.by(Sort.Direction.DESC, "favoriteCount");
            case "created" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "updated" -> Sort.by(Sort.Direction.DESC, "updatedAt");
            default -> Sort.by(Sort.Direction.DESC, "flowerCount");
        };
    }

    private NovelDTO toDTO(Novel n) {
        NovelDTO dto = new NovelDTO();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setAuthorName(n.getAuthor() != null ? n.getAuthor().getName() : "");
        dto.setAuthorId(n.getAuthor() != null ? n.getAuthor().getId() : null);
        dto.setPublisherName(n.getPublisher() != null ? n.getPublisher().getName() : "");
        dto.setPublisherId(n.getPublisher() != null ? n.getPublisher().getId() : null);
        dto.setCoverUrl(n.getCoverUrl());
        dto.setSynopsis(n.getSynopsis());
        dto.setWordCount(n.getWordCount());
        dto.setRating(n.getRating());
        dto.setIsCompleted(n.getIsCompleted());
        dto.setViewCount(n.getViewCount());
        dto.setRecommendCount(n.getRecommendCount());
        dto.setFlowerCount(n.getFlowerCount() != null ? n.getFlowerCount() : 0);
        dto.setFavoriteCount(n.getFavoriteCount() != null ? n.getFavoriteCount() : 0);
        dto.setIsAnimated(n.getIsAnimated() != null ? n.getIsAnimated() : 0);
        dto.setCreatedAt(n.getCreatedAt());
        dto.setUpdatedAt(n.getUpdatedAt());
        dto.setLatestChapterTitle(n.getLatestChapterTitle());
        if (n.getTags() != null) {
            dto.setTagNames(n.getTags().stream().map(t -> t.getName()).collect(Collectors.toList()));
        }
        return dto;
    }

    private NovelSimpleDTO toSimpleDTO(Novel n) {
        NovelSimpleDTO dto = new NovelSimpleDTO();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setAuthorName(n.getAuthor() != null ? n.getAuthor().getName() : "");
        dto.setPublisherName(n.getPublisher() != null ? n.getPublisher().getName() : "");
        dto.setCoverUrl(n.getCoverUrl());
        dto.setSynopsis(n.getSynopsis());
        if (n.getTags() != null && !n.getTags().isEmpty()) {
            dto.setTagName(n.getTags().get(0).getName());
        } else {
            dto.setTagName(n.getPublisher() != null ? n.getPublisher().getName() : "");
        }
        return dto;
    }
}
