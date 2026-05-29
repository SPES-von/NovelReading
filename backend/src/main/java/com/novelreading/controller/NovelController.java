package com.novelreading.controller;

import com.novelreading.dto.NovelDTO;
import com.novelreading.dto.NovelSimpleDTO;
import com.novelreading.dto.PublisherNovelDTO;
import com.novelreading.entity.User;
import com.novelreading.repository.UserRepository;
import com.novelreading.service.NovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小说控制器
 * 提供首页各模块数据及小说详情、搜索接口
 */
@RestController
@RequestMapping("/novels")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;
    private final UserRepository userRepository;

    /** 首页汇总数据：特色推荐、强推榜、热点榜、首页推荐榜6本、完本推荐、热门小说等 */
    @GetMapping("/home")
    public ResponseEntity<Map<String, Object>> getHomeData() {
        Map<String, Object> data = new HashMap<>();
        data.put("featured", novelService.getFeaturedNovel());
        data.put("strongRecommendList", novelService.getStrongRecommendList());
        data.put("hotChartList", novelService.getHotChartList());
        data.put("hotList", novelService.getHotNovels());
        data.put("recommendList", novelService.getRecommendList());
        data.put("homeRecommendList", novelService.getHomeRecommendList());
        data.put("newSelectList", novelService.getNewSelectList());
        data.put("newBookChart", novelService.getNewBookChart());
        data.put("completedRecommend", novelService.getCompletedRecommend());
        data.put("classicCompleted", novelService.getClassicCompleted());
        data.put("classicCompletedList", novelService.getClassicCompletedList());
        data.put("publisherNovels", novelService.getPublisherNovels());
        data.put("hotNovelsList", novelService.getHotNovelsList());
        return ResponseEntity.ok(data);
    }

    /** 强推榜/热点榜 */
    @GetMapping("/hot")
    public ResponseEntity<List<NovelDTO>> getHotList() {
        return ResponseEntity.ok(novelService.getHotNovels());
    }

    /** 特色推荐 */
    @GetMapping("/featured")
    public ResponseEntity<NovelDTO> getFeatured() {
        return ResponseEntity.ok(novelService.getFeaturedNovel());
    }

    /** 完本推荐 */
    @GetMapping("/completed")
    public ResponseEntity<List<NovelSimpleDTO>> getCompletedRecommend() {
        return ResponseEntity.ok(novelService.getCompletedRecommend());
    }

    /** 各文库热门小说 */
    @GetMapping("/by-publisher")
    public ResponseEntity<List<PublisherNovelDTO>> getPublisherNovels() {
        return ResponseEntity.ok(novelService.getPublisherNovels());
    }

    /** 小说详情 */
    @GetMapping("/{id}")
    public ResponseEntity<NovelDTO> getById(@PathVariable Long id) {
        NovelDTO dto = novelService.getNovelById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    /** 送鲜花：鲜花数 +1 */
    @PostMapping("/{id}/flower")
    public ResponseEntity<Void> flower(@PathVariable Long id) {
        novelService.incrementFlower(id);
        return ResponseEntity.ok().build();
    }

    /** 加入书架：需登录，将用户ID与小说ID写入用户书架表，并更新小说收藏数
     * 返回 { added: true } 表示新增成功，{ added: false } 表示书架中已存在此书 */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<Map<String, Object>> favorite(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }
        String username = auth.getName();
        User user = userRepository.findByPk_Username(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        boolean added = novelService.addToBookshelf(user.getId(), id);
        return ResponseEntity.ok(Map.of("added", added));
    }

    /** 搜索：支持标题、简介、标签名模糊查询，分页返回 */
    @GetMapping("/search")
    public ResponseEntity<Page<NovelDTO>> search(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(novelService.search(keyword, page, size));
    }

    /** 排行榜页：字数榜、评分榜、热度榜、推荐榜、鲜花榜、收藏榜，各前10条 */
    @GetMapping("/rankings")
    public ResponseEntity<Map<String, List<NovelDTO>>> getRankings() {
        return ResponseEntity.ok(novelService.getRankings());
    }

    /** 全书页：完本书籍榜单，is_completed=1，按鲜花/评分/热度/收藏排序取前15 */
    @GetMapping("/completed-chart")
    public ResponseEntity<List<NovelDTO>> getCompletedChart(
            @RequestParam(required = false, defaultValue = "viewCount") String sort) {
        return ResponseEntity.ok(novelService.getCompletedChart(sort));
    }

    /** 筛选页：作品主题、是否动画化、字数、更新时间筛选，鲜花数/收藏数/入库时间/更新时间排序 */
    @GetMapping("/browse")
    public ResponseEntity<Page<NovelDTO>> browse(
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Integer isAnimated,
            @RequestParam(required = false) String wordCountRange,
            @RequestParam(required = false) Integer updatedWithinDays,
            @RequestParam(required = false, defaultValue = "flower") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(novelService.browse(tagId, isAnimated, wordCountRange, updatedWithinDays, sort, page, size));
    }
}
