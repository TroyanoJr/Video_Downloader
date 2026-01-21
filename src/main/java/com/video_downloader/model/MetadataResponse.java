package com.video_downloader.model;

import java.util.List;

public class MetadataResponse {
    public String title;
    public String thumbnail;
    public List<FormatDto> videoFormats;
    public List<FormatDto> audioFormats;
    public List<FormatDto> otherFormats;
    public boolean available;
    public String message;
}

