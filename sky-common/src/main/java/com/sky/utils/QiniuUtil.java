package com.sky.utils;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.sky.properties.QiniuProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
public class QiniuUtil {

    @Autowired
    private QiniuProperties qiniuProp;

    // 上传管理器单例
    private UploadManager getUploadManager() {
        Configuration cfg = new Configuration();
        return new UploadManager(cfg);
    }

    // 获取鉴权对象
    private Auth getAuth() {
        return Auth.create(qiniuProp.getAccessKey(), qiniuProp.getSecretKey());
    }

    /**
     * 文件上传
     *
     * @param file   前端传过来的文件
     * @param folder 存放目录，如 img/avatar
     * @return 文件完整访问URL
     */
    public String upload(MultipartFile file, String folder) {
        // 1. 生成唯一文件名，防止重名覆盖
        String originalName = file.getOriginalFilename();
        String suffix = originalName.substring(originalName.lastIndexOf("."));
        String fileName = folder + "/" + UUID.randomUUID() + suffix;

        // 2. 生成上传凭证
        String upToken = getAuth().uploadToken(qiniuProp.getBucketName());

        try {
            // 3. 执行上传
            Response response = getUploadManager().put(file.getBytes(), fileName, upToken);
            DefaultPutRet ret = response.jsonToObject(DefaultPutRet.class);
            // 拼接访问地址
            return qiniuProp.getCdnDomain() + "/" + ret.key;
        } catch (QiniuException e) {
            throw new RuntimeException("七牛上传失败：" + e.response.toString());
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件完整url
     */
    public void deleteFile(String fileUrl) throws QiniuException {
        String key = fileUrl.replace(qiniuProp.getCdnDomain() + "/", "");
        com.qiniu.storage.BucketManager bucketManager = new com.qiniu.storage.BucketManager(getAuth(), new Configuration());
        bucketManager.delete(qiniuProp.getBucketName(), key);
    }

    /**
     * 私有空间：生成临时访问链接（默认1小时有效期）
     *
     * @param fileKey 文件key
     * @return 带签名的临时url
     */
    public String getPrivateUrl(String fileKey) {
        String rawUrl = qiniuProp.getCdnDomain() + "/" + fileKey;
        // 3600秒有效期
        return getAuth().privateDownloadUrl(rawUrl, 3600);
    }

    /**
     * 前端直传：获取上传Token（前后端分离场景）
     */
    /**
     * 前端直传：获取上传Token（前后端分离场景）
     */
    public String getUploadToken() {
        // 单参数重载，直接传bucket名称，默认1小时有效期
        return getAuth().uploadToken(qiniuProp.getBucketName());
    }
}