package com.video_downloader.controller;

import com.video_downloader.model.MetadataResponse;
import com.video_downloader.service.YtDlpService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

record MetadataRequest(@NotBlank String url) {}

@RestController
@RequestMapping("/api")
public class MetadataController {
    private final YtDlpService svc;
    public MetadataController(YtDlpService svc) { this.svc = svc; }

    @PostMapping("/metadata")
    public ResponseEntity<MetadataResponse> metadata(@RequestBody MetadataRequest req) {
        MetadataResponse res = svc.fetchMetadata(req.url());
        return ResponseEntity.ok(res);
    }
}

