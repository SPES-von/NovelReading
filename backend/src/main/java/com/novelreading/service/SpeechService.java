package com.novelreading.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 百度语音合成服务
 * 调用百度语音合成 API，将小说文本转为语音，实现听书功能
 * 需配置 app-id、api-key、secret-key 环境变量或 application.yml
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpeechService {

    @Value("${baidu.speech.app-id:}")
    private String appId;

    @Value("${baidu.speech.api-key:}")
    private String apiKey;

    @Value("${baidu.speech.secret-key:}")
    private String secretKey;

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String TTS_URL = "https://tsn.baidu.com/text2audio";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取 Access Token（百度 API 需要）
     */
    public String getAccessToken() {
        if (apiKey == null || apiKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            log.warn("百度语音 API 未配置，请设置 BAIDU_SPEECH_API_KEY 和 BAIDU_SPEECH_SECRET_KEY");
            return null;
        }
        String url = TOKEN_URL + "?grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey;
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
            JsonNode node = objectMapper.readTree(resp.getBody());
            return node.path("access_token").asText();
        } catch (Exception e) {
            log.error("获取百度 Token 失败", e);
            return null;
        }
    }

    /**
     * 文本转语音
     * @param text 要合成的文本
     * @return 音频二进制数据，或 null 表示失败
     */
    public byte[] textToSpeech(String text) {
        String token = getAccessToken();
        if (token == null) return null;
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = TTS_URL + "?tex=" + encodedText + "&tok=" + token + "&cuid=novel_reading&ctp=1&lan=zh&spd=5&pit=5&vol=5&per=1&aue=3";
            ResponseEntity<byte[]> resp = restTemplate.getForEntity(url, byte[].class);
            return resp.getBody();
        } catch (Exception e) {
            log.error("语音合成失败", e);
            return null;
        }
    }
}
