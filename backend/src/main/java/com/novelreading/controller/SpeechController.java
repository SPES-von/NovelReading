package com.novelreading.controller;

import com.novelreading.service.ChapterService;
import com.novelreading.service.SpeechService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 语音合成控制器
 * 调用百度语音合成 API，实现小说听书功能
 */
@RestController
@RequestMapping("/speech")
@RequiredArgsConstructor
public class SpeechController {

    private final SpeechService speechService;
    private final ChapterService chapterService;

    /**
     * 将指定章节内容转为语音
     * @param chapterId 章节ID
     * @return 音频流 (MP3)
     */
    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<byte[]> textToSpeech(@PathVariable Long chapterId) {
        var chapter = chapterService.getChapterById(chapterId);
        if (chapter == null || chapter.getContent() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] audio = speechService.textToSpeech(chapter.getContent());
        if (audio == null) {
            return ResponseEntity.internalServerError().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentLength(audio.length);
        return ResponseEntity.ok().headers(headers).body(audio);
    }

    /**
     * 自定义文本转语音
     */
    @PostMapping("/tts")
    public ResponseEntity<byte[]> textToSpeech(@RequestBody String text) {
        byte[] audio = speechService.textToSpeech(text);
        if (audio == null) {
            return ResponseEntity.internalServerError().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentLength(audio.length);
        return ResponseEntity.ok().headers(headers).body(audio);
    }
}
