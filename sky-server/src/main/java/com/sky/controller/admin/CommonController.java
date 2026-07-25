package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.GithubUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    @Autowired
    private GithubUtil githubUtil;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            log.warn("文件名无效: {}", originalFilename);
            return Result.error("文件名无效");
        }
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            log.warn("文件大小超过限制: {}MB / 最大{}MB", file.getSize() / (1024 * 1024), maxSize / (1024 * 1024));
            return Result.error("文件大小超过限制(最大5MB)");
        }

        try {
            String url = githubUtil.upload(file, "img");
            log.info("文件上传成功: {}", url);
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }

    @PostMapping("/token")
    public String getToken(){
        return githubUtil.getUploadToken();
    }
}