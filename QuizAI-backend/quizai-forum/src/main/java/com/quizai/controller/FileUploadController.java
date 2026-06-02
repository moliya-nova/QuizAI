package com.quizai.controller;

import com.quizai.domain.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("api/file")
public class FileUploadController {

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    @Value("${upload.url-prefix:http://localhost:8080}")
    private String urlPrefix;

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024;
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    private static final String[] ALLOWED_VIDEO_TYPES = {"video/mp4", "video/quicktime", "video/x-msvideo"};

    @PostMapping("upload")
    public R<String> upload(@RequestParam("file") MultipartFile file,
                            @RequestParam(value = "type", defaultValue = "image") String type) {
        if (file.isEmpty()) {
            return R.error("文件不能为空");
        }

        String contentType = file.getContentType();
        long maxSize;

        if ("video".equals(type)) {
            maxSize = MAX_VIDEO_SIZE;
            if (!isAllowedType(contentType, ALLOWED_VIDEO_TYPES)) {
                return R.error("不支持的视频格式，仅支持 MP4/MOV/AVI");
            }
        } else {
            maxSize = MAX_IMAGE_SIZE;
            if (!isAllowedType(contentType, ALLOWED_IMAGE_TYPES)) {
                return R.error("不支持的图片格式，仅支持 JPG/PNG/GIF/WebP");
            }
        }

        if (file.getSize() > maxSize) {
            return R.error("文件大小超过限制");
        }

        try {
            Path dirPath = Paths.get(uploadDir);
            Files.createDirectories(dirPath);

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

            Path targetPath = dirPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String url = urlPrefix + "/api/file/view/" + newFilename;
            return R.success(url);
        } catch (IOException e) {
            return R.error("文件保存失败");
        }
    }

    @GetMapping("view/{filename}")
    public void view(@PathVariable String filename,
                     jakarta.servlet.http.HttpServletResponse response) throws IOException {
        Path filePath = Paths.get(uploadDir, filename);
        if (!Files.exists(filePath)) {
            response.setStatus(404);
            return;
        }
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "max-age=86400");
        Files.copy(filePath, response.getOutputStream());
    }

    private boolean isAllowedType(String contentType, String[] allowed) {
        if (contentType == null) return false;
        for (String t : allowed) {
            if (t.equals(contentType)) return true;
        }
        return false;
    }
}
