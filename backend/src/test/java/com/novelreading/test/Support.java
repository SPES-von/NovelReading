package com.novelreading.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class Support {
    private Support() {}

    public static String extractToken(ObjectMapper om, MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        JsonNode node = om.readTree(json);
        JsonNode token = node.get("token");
        if (token == null || token.asText().isBlank()) {
            throw new IllegalStateException("响应中未找到 token 字段: " + json);
        }
        return token.asText();
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }
}

