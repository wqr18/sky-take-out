package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.QiniuUtil;
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
    private QiniuUtil qiniuUtil;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        
        // 文件类型白名单校验（仅允许图片类型）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            log.warn("文件名无效: {}", originalFilename);
            return Result.error("文件名无效");
        }
        // 文件大小限制（5MB）
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            log.warn("文件大小超出限制: {}MB / 最大{}MB", file.getSize() / (1024 * 1024), maxSize / (1024 * 1024));
            return Result.error("文件大小超出限制（最大5MB）");
        }
        
        try {

            String url = qiniuUtil.upload(file, "img");  // QiniuUtil 内部生成唯一文件名
            log.info("文件上传成功: {}", url);
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }

    @PostMapping("/token")
    public String getToken(){
        return qiniuUtil.getUploadToken();
    }
}