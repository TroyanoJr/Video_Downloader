package com.video_downloader.model;

public class FormatDto {
    public String formatId;
    public String ext;
    public Integer height;
    public Integer abr;
    public Long filesize;
    public String type; // video|audio|other
    public String selector; // yt-dlp -f selector
    public String label;
}

