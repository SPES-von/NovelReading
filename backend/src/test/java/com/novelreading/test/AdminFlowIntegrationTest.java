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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "no-redis"})
class AdminFlowIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean SpeechService speechService;

    private String adminLoginAndGetToken() throws Exception {
        // 功能点：管理员登录并拿到 JWT（后台接口鉴权）
        var result = mvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adminId\":1,\"password\":\"admin\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return extractToken(om, result);
    }

    /** 功能点：后台接口权限控制（无管理员凭证时应拒绝访问） */
    @Test
    void adminEndpoints_shouldRequireAdminToken() throws Exception {
        mvc.perform(get("/admin/novels"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin/comments"))
                .andExpect(status().isForbidden());

        System.out.println("[PASS] 后台权限控制测试成功：无管理员凭证访问后台接口被正确拒绝");
    }

    /** 功能点：上架小说 + 新增章节 + 更新章节（小说内容维护） */
    @Test
    void adminCoreFlow_shouldWork() throws Exception {
        String token = adminLoginAndGetToken();

        // 1) 列表
        mvc.perform(get("/admin/novels").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        // 2) 创建新小说（需要 authorId/publisherId 存在：data-h2.sql 已提供 id=1）
        var created = mvc.perform(post("/admin/novels")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"后台新书\",\"synopsis\":\"s\",\"authorId\":1,\"publisherId\":1,\"coverUrl\":\"u\",\"wordCount\":123,\"volume\":\"1\",\"chapterTitle\":\"第一章\",\"chapterContent\":\"内容\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        long novelId = om.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // 3) 为该书新增章节
        mvc.perform(post("/admin/novels/" + novelId + "/chapters")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"volume\":\"1\",\"title\":\"第二章\",\"content\":\"more\",\"wordCount\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("第二章"));

        long chapterId = om.readTree(
                mvc.perform(post("/admin/novels/" + novelId + "/chapters")
                                .header("Authorization", bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"volume\":\"1\",\"title\":\"第三章\",\"content\":\"need update\",\"wordCount\":12}"))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString()
        ).get("id").asLong();

        // 4) 更新章节（视作小说内容更新）
        mvc.perform(put("/admin/novels/chapters/" + chapterId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"volume\":\"1\",\"title\":\"第三章-更新\",\"content\":\"updated content\",\"wordCount\":20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("第三章-更新"));

        // 5) 用户/评论列表（至少能访问）
        mvc.perform(get("/admin/users").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mvc.perform(get("/admin/comments").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        System.out.println("[PASS] 后台内容管理测试成功：上架小说、新增章节、更新章节、用户/评论管理接口可用");
    }
}

