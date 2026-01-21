package com.video_downloader.controller;

import com.video_downloader.model.DownloadSession;
import com.video_downloader.model.DownloadStatus;
import com.video_downloader.service.YtDlpService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ContentDisposition;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

record DownloadRequest(@NotBlank String url, String selector, String audioFormat, Integer audioQuality, String ext) {}

@RestController
@RequestMapping("/api/download")
public class DownloadController {
    private final YtDlpService svc;
    public DownloadController(YtDlpService svc) { this.svc = svc; }

    @PostMapping("/session")
    public ResponseEntity<DownloadSession> create(@RequestBody DownloadRequest req) throws Exception {
        DownloadSession s = svc.startDownload(req.url(), req.selector(), req.audioFormat(), req.audioQuality(), req.ext());
        return ResponseEntity.ok(s);
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<DownloadSession> progress(@PathVariable String id) {
        DownloadSession s = svc.getSession(id);
        return ResponseEntity.ok(s);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<FileSystemResource> file(@PathVariable String id) {
        DownloadSession s = svc.getSession(id);
        if (s == null || s.status != DownloadStatus.DONE || s.outputFile == null) return ResponseEntity.notFound().build();
        File f = s.outputFile.toFile();
        FileSystemResource r = new FileSystemResource(f);
        ContentDisposition cd = ContentDisposition.attachment().filename(f.getName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(f.length())
                .body(r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        svc.cleanup(id);
        return ResponseEntity.noContent().build();
    }
}
