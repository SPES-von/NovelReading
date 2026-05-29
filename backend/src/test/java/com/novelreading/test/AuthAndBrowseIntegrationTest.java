package com.novelreading.test;

import com.novelreading.service.SpeechService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "no-redis"})
class AuthAndBrowseIntegrationTest {

    @Autowired MockMvc mvc;
    @MockBean SpeechService speechService;

    /** 功能点：注册输入校验 + 重复注册错误提示 */
    @Test
    void registerValidation_andErrorMessages_shouldWork() throws Exception {
        // 注册校验：用户名为空
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"p1\",\"nickname\":\"n1\",\"email\":\"u2@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("没有填写用户名"));

        // 注册校验：密码为空
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u2\",\"password\":\"\",\"nickname\":\"n1\",\"email\":\"u2@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("密码没有填写"));

        // 先成功注册
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u2\",\"password\":\"p2\",\"nickname\":\"n2\",\"email\":\"u2@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // 再次注册同用户名 -> 错误提示
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u2\",\"password\":\"p2\",\"nickname\":\"n2\",\"email\":\"u2b@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户名已经被注册"));

        System.out.println("[PASS] 登录/注册校验测试成功：注册参数校验与重复注册错误提示正确");
    }

    /** 功能点：登录错误提示（密码错误、用户不存在） */
    @Test
    void loginValidation_andErrorMessages_shouldWork() throws Exception {
        // 先注册一个可登录用户
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u3\",\"password\":\"p3\",\"nickname\":\"n3\",\"email\":\"u3@test.com\"}"))
                .andExpect(status().isOk());

        // 密码错误
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u3\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("密码错误"));

        // 用户不存在
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"not_exist\",\"password\":\"p\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("用户不存在"));

        System.out.println("[PASS] 登录错误提示测试成功：密码错误与用户不存在提示正确");
    }

    /** 功能点：搜索结果正确 + 分类筛选结果正确 */
    @Test
    void searchAndBrowse_shouldReturnExpectedResults() throws Exception {
        // 搜索：按标题关键字命中“测试小说”
        mvc.perform(get("/novels/search").param("keyword", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("测试小说"));

        // 分类筛选：tagId=1 + under30 + isAnimated=0 应命中种子小说
        mvc.perform(get("/novels/browse")
                        .param("tagId", "1")
                        .param("isAnimated", "0")
                        .param("wordCountRange", "under30")
                        .param("sort", "flower"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));

        System.out.println("[PASS] 搜索/分类筛选测试成功：结果命中预期小说");
    }
}

