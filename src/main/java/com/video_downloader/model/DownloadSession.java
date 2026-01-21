package com.video_downloader.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class DownloadSession {
    public String id = UUID.randomUUID().toString();
    public String url;
    public String selector;
    public DownloadStatus status = DownloadStatus.PENDING;
    public double progress;
    public String speed;
    public String eta;
    public Path outputFile;
    public Path dir;
    public Instant createdAt = Instant.now();
    public Process process;
    public String message;
}
