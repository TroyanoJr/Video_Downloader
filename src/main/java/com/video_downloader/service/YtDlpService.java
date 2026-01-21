package com.video_downloader.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.video_downloader.model.DownloadSession;
import com.video_downloader.model.DownloadStatus;
import com.video_downloader.model.FormatDto;
import com.video_downloader.model.MetadataResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration; // NUEVO
import java.time.Instant;  // NUEVO
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit; // NUEVO
import java.util.stream.Collectors;

@Service
public class YtDlpService {
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${tools.ytdlpPath:yt-dlp}")
    private String ytdlpPath;

    @Value("${tools.ffmpegPath:ffmpeg}")
    private String ffmpegPath;

    @Value("${app.downloadDir:downloads}")
    private String downloadDir;

    private final Map<String, DownloadSession> sessions = new ConcurrentHashMap<>();

    public MetadataResponse fetchMetadata(String url) {
        MetadataResponse res = new MetadataResponse();
        res.available = false;
        try {
            List<String> cmd = Arrays.asList(ytdlpPath, "-J", "--no-warnings", "--ignore-errors", url);
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            String json = readAll(p.getInputStream());
            int exit = p.waitFor();
            if (exit != 0 || json == null || json.isBlank()) {
                res.message = "URL no disponible";
                return res;
            }
            JsonNode root = mapper.readTree(json);
            res.title = optText(root, "title");
            res.thumbnail = optText(root, "thumbnail");
            List<FormatDto> formats = parseFormats(root.path("formats"));
            List<FormatDto> videoMp4 = formats.stream()
                    .filter(f -> "video".equals(f.type) && "mp4".equalsIgnoreCase(f.ext) && f.height != null && f.height >= 144 && f.height <= 1100)
                    .sorted(Comparator.comparing((FormatDto f) -> f.height).reversed())
                    .collect(Collectors.toList());

            List<FormatDto> otherVideo = formats.stream()
                    .filter(f -> "video".equals(f.type) && !"mp4".equalsIgnoreCase(f.ext) && f.height != null && f.height >= 144 && f.height <= 1100)
                    .sorted(Comparator.comparing((FormatDto f) -> f.height).reversed())
                    .collect(Collectors.toList());

            List<FormatDto> audio = formats.stream()
                    .filter(f -> "audio".equals(f.type))
                    .collect(Collectors.toList());

            FormatDto bestM4a = audio.stream().filter(a -> "m4a".equalsIgnoreCase(a.ext)).max(Comparator.comparing(a -> Optional.ofNullable(a.abr).orElse(0))).orElse(null);
            FormatDto bestWebmAudio = audio.stream().filter(a -> "webm".equalsIgnoreCase(a.ext)).max(Comparator.comparing(a -> Optional.ofNullable(a.abr).orElse(0))).orElse(null);

            List<FormatDto> videoOut = buildSelectorsByHeight(videoMp4, bestM4a, "mp4");
            List<FormatDto> otherOut = buildSelectorsByHeight(otherVideo, bestWebmAudio, "other");
            res.videoFormats = videoOut.stream().limit(6).collect(Collectors.toList());
            res.otherFormats = otherOut.stream().limit(6).collect(Collectors.toList());

            // Audio options
            List<FormatDto> audioOut = new ArrayList<>();
            audioOut.add(makeAudioOption("mp3", 320));
            audioOut.add(makeAudioOption("mp3", 240));
            res.audioFormats = audioOut;

            res.available = !res.videoFormats.isEmpty() || !res.audioFormats.isEmpty() || !res.otherFormats.isEmpty();
            if (!res.available) res.message = "No hay formatos disponibles";
            return res;
        } catch (Exception e) {
            res.message = "Error obteniendo metadatos";
            return res;
        }
    }

    private List<FormatDto> parseFormats(JsonNode arr) {
        List<FormatDto> list = new ArrayList<>();
        if (arr == null || !arr.isArray()) return list;
        for (JsonNode f : arr) {
            FormatDto dto = new FormatDto();
            dto.formatId = optText(f, "format_id");
            dto.ext = optText(f, "ext");
            dto.height = f.has("height") && f.get("height").isInt() ? f.get("height").asInt() : null;
            dto.abr = f.has("abr") && f.get("abr").isNumber() ? f.get("abr").asInt() : null;
            dto.filesize = f.has("filesize") && f.get("filesize").isNumber() ? f.get("filesize").asLong() : null;
            String ac = optText(f, "acodec");
            String vc = optText(f, "vcodec");
            dto.type = vc != null && !"none".equals(vc) ? "video" : "audio";
            list.add(dto);
        }
        return list;
    }

    private List<FormatDto> buildSelectorsByHeight(List<FormatDto> videos, FormatDto bestAudio, String typeLabel) {
        Map<Integer, List<FormatDto>> byHeight = videos.stream().filter(v -> v.height != null).collect(Collectors.groupingBy(v -> v.height));
        List<Integer> heights = new ArrayList<>(byHeight.keySet());
        heights.sort(Comparator.reverseOrder());
        List<FormatDto> out = new ArrayList<>();
        for (Integer h : heights) {
            List<FormatDto> vlist = byHeight.get(h);
            if (vlist == null || vlist.isEmpty()) continue;
            FormatDto v = vlist.get(0);
            FormatDto dto = new FormatDto();
            dto.ext = v.ext;
            dto.height = h;
            dto.type = "video".equals(typeLabel) ? "video" : "other";
            if (v.formatId != null && bestAudio != null && !"none".equals(bestAudio.formatId)) {
                dto.selector = v.formatId + "+" + bestAudio.formatId;
            } else if (v.formatId != null) {
                dto.selector = v.formatId;
            } else {
                String extFilter = v.ext;
                dto.selector = "bestvideo[ext=" + extFilter + "][height=" + h + "]" + (bestAudio != null ? "+bestaudio[ext=" + bestAudio.ext + "]" : "/best[ext=" + extFilter + "][height=" + h + "]");
            }
            dto.label = h + "p (" + v.ext + ")";
            out.add(dto);
        }
        return out;
    }

    private FormatDto makeAudioOption(String ext, int kbps) {
        FormatDto dto = new FormatDto();
        dto.type = "audio";
        dto.ext = ext;
        dto.abr = kbps;
        dto.label = ext.toUpperCase() + " - " + kbps + "kbps";
        dto.selector = "bestaudio";
        return dto;
    }

    public DownloadSession startDownload(String url, String selector, String audioFormat, Integer audioQuality, String ext) throws IOException {
        Path outDir = Paths.get(downloadDir);
        Files.createDirectories(outDir);
        DownloadSession s = new DownloadSession();
        s.url = url;
        s.selector = selector;
        sessions.put(s.id, s);
        Path sessionDir = outDir.resolve(s.id);
        Files.createDirectories(sessionDir);
        s.dir = sessionDir;
        List<String> cmd = new ArrayList<>();
        cmd.add(ytdlpPath);
        if (selector != null && !selector.isBlank()) {
            cmd.add("-f");
            cmd.add(selector);
        }
        if (audioFormat != null && audioFormat.equalsIgnoreCase("mp3")) {
            cmd.add("--extract-audio");
            cmd.add("--audio-format");
            cmd.add("mp3");
            if (audioQuality != null) {
                cmd.add("--audio-quality");
                cmd.add(audioQuality + "K");
            }
        } else {
            if (ext != null) {
                cmd.add("--merge-output-format");
                cmd.add(ext);
            }
        }
        cmd.add("--newline");
        cmd.add("-o");
        String template;
        if (audioFormat != null && audioFormat.equalsIgnoreCase("mp3")) {
            template = sessionDir.toAbsolutePath() + "/%(title)s-%(abr)sK.%(ext)s";
        } else {
            template = sessionDir.toAbsolutePath() + "/%(title)s-%(height)s.%(ext)s";
        }
        cmd.add(template);
        cmd.add(url);
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        s.process = p;
        s.status = DownloadStatus.RUNNING;
        new Thread(() -> trackProgress(s)).start();
        return s;
    }

    private void trackProgress(DownloadSession s) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(s.process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("[download]") && line.contains("%")) {
                    double pct = parsePercent(line);
                    s.progress = pct;
                }
                if (line.startsWith("[Merger]") || line.contains("has already been downloaded")) {
                    s.progress = 100.0;
                }
            }
            int exit = s.process.waitFor();
            s.status = exit == 0 ? DownloadStatus.DONE : DownloadStatus.FAILED;
            s.outputFile = findLatestFile(s.dir);
        } catch (Exception e) {
            s.status = DownloadStatus.FAILED;
            s.message = "Fallo en descarga";
        }
    }

    private Path findLatestFile(Path dir) throws IOException {
        try (var stream = Files.list(dir)) {
            return stream.filter(Files::isRegularFile).max(Comparator.comparingLong(p -> p.toFile().lastModified())).orElse(null);
        }
    }

    private double parsePercent(String line) {
        try {
            int i = line.indexOf('%');
            int j = i;
            while (j > 0 && Character.isDigit(line.charAt(j - 1))) j--;
            return Double.parseDouble(line.substring(j, i));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String optText(JsonNode n, String f) {
        return n.has(f) && !n.get(f).isNull() ? n.get(f).asText() : null;
    }

    private String readAll(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String l;
            while ((l = br.readLine()) != null) sb.append(l);
            return sb.toString();
        }
    }

    public DownloadSession getSession(String id) { return sessions.get(id); }

    public void cleanup(String id) {
        DownloadSession s = sessions.remove(id);
        if (s != null && s.outputFile != null) {
            try { Files.deleteIfExists(s.outputFile); } catch (IOException ignored) {}
        }
    }
}
