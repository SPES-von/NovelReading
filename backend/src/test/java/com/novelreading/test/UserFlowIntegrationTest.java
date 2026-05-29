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

import static com.novelreading.test.Support.bearer;
import static com.novelreading.test.Support.extractToken;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "no-redis"})
class UserFlowIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    // 避免任何测试启动时触发真实外部调用
    @MockBean SpeechService speechService;

    private String registerAndGetToken() throws Exception {
        // 功能点：注册成功后返回 token（后续受保护接口鉴权使用）
        var result = mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u1\",\"password\":\"p1\",\"nickname\":\"n1\",\"email\":\"u1@test.com\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return extractToken(om, result);
    }

    /** 功能点：用户主流程（加入/移除书架、阅读进度、偏好、评论） */
    @Test
    void endToEnd_userCoreFlow_shouldWork() throws Exception {
        when(speechService.textToSpeech(anyString())).thenReturn("mp3".getBytes());

        String token = registerAndGetToken();

        // 1) 送鲜花（公开）
        mvc.perform(post("/novels/1/flower"))
                .andExpect(status().isOk());

        // 2) 加入书架（需登录）
        mvc.perform(post("/novels/1/favorite")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.added").exists());

        // 3) 查询书架
        mvc.perform(get("/bookshelf")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // 4) 保存书签/阅读进度
        mvc.perform(post("/reading-progress/bookmark")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novelId\":1,\"chapterId\":1,\"progressPosition\":10,\"bookmark\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        // 5) 获取某本书进度
        mvc.perform(get("/reading-progress").param("novelId", "1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // 6) 获取阅读历史列表
        mvc.perform(get("/reading-progress/list")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // 7) 获取/更新个人中心偏好
        mvc.perform(get("/user/preferences")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mvc.perform(put("/user/preferences")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fontSize\":18,\"lineHeight\":1.9,\"theme\":\"night\",\"pageWidth\":720}"))
                .andExpect(status().isOk());

        // 8) 发布评论（需登录）
        mvc.perform(post("/novels/1/comments")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"很好看\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        // 9) 从书架移除
        mvc.perform(delete("/bookshelf/1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.removed").exists());

        System.out.println("[PASS] 用户主流程测试成功：加入/移除书架、阅读进度、偏好设置、评论发布均可用");
    }
}

