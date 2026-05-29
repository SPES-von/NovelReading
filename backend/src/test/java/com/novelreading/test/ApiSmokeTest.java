package com.novelreading.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novelreading.service.SpeechService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "no-redis"})
class ApiSmokeTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean SpeechService speechService;

    /** 功能点：公开接口可用性冒烟测试（访客可访问） */
    @Test
    void publicEndpoints_shouldReturn200() throws Exception {
        mvc.perform(get("/tags"))
                .andExpect(status().isOk());

        mvc.perform(get("/publishers"))
                .andExpect(status().isOk());

        mvc.perform(get("/authors"))
                .andExpect(status().isOk());

        mvc.perform(get("/novels/home"))
                .andExpect(status().isOk());

        mvc.perform(get("/novels/1"))
                .andExpect(status().isOk());

        mvc.perform(get("/novels/search").param("keyword", "测试"))
                .andExpect(status().isOk());

        mvc.perform(get("/chapters/novel/1"))
                .andExpect(status().isOk());

        mvc.perform(get("/chapters/1"))
                .andExpect(status().isOk());

        mvc.perform(get("/novels/1/comments"))
                .andExpect(status().isOk());

        System.out.println("[PASS] 公开接口可用性测试成功：标签/文库/作者/首页/详情/搜索/章节/评论接口可正常访问");
    }

    /** 功能点：受保护接口鉴权测试（未登录应返回 401） */
    @Test
    void protectedEndpoints_withoutToken_shouldReturn401() throws Exception {
        mvc.perform(get("/bookshelf"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/user/profile"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/reading-progress").param("novelId", "1"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/novels/1/favorite"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/novels/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hi\"}"))
                .andExpect(status().isUnauthorized());

        System.out.println("[PASS] 受保护接口鉴权测试成功：未登录访问书架/个人中心/阅读进度/收藏/发评均被拦截");
    }

    /** 功能点：听书接口可用性（TTS 返回音频流） */
    @Test
    void speechEndpoints_shouldReturnAudio_whenServiceOk() throws Exception {
        when(speechService.textToSpeech(anyString())).thenReturn("mp3".getBytes());

        mvc.perform(post("/speech/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"hello\""))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/mpeg"));

        mvc.perform(get("/speech/chapter/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/mpeg"));

        System.out.println("[PASS] 听书功能测试成功：TTS 接口可返回音频流");
    }
}

