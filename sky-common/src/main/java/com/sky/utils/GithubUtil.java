package com.sky.utils;

import com.sky.properties.GithubProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class GithubUtil {

    @Autowired
    private GithubProperties githubProp;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String upload(MultipartFile file, String folder) throws Exception {
        log.info("=== GitHub 配置信息 ===");
        log.info("Token: {}", githubProp.getToken() != null ? "已配置 (长度: " + githubProp.getToken().length() + ")" : "未配置");
        log.info("Owner: {}", githubProp.getOwner());
        log.info("Repo: {}", githubProp.getRepo());
        log.info("Branch: {}", githubProp.getBranch());
        log.info("Path: {}", githubProp.getPath());
        log.info("=== 配置信息结束 ===");

        if (githubProp.getToken() == null || githubProp.getToken().isEmpty()) {
            throw new RuntimeException("请先配置 GitHub Token (application.yml 中的 sky.github.token)");
        }
        if (githubProp.getOwner() == null || githubProp.getOwner().isEmpty()) {
            throw new RuntimeException("请先配置 GitHub 用户名 (application.yml 中的 sky.github.owner)");
        }
        if (githubProp.getRepo() == null || githubProp.getRepo().isEmpty()) {
            throw new RuntimeException("请先配置 GitHub 仓库名 (application.yml 中的 sky.github.repo)");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;
        String filePath = folder + "/" + filename;

        String base64Content = Base64.getEncoder().encodeToString(file.getBytes());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("message", "upload image: " + filename);
        requestBody.put("branch", githubProp.getBranch());
        requestBody.put("content", base64Content);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + githubProp.getToken());
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        String apiUrl = String.format("https://api.github.com/repos/%s/%s/contents/%s",
                githubProp.getOwner(), githubProp.getRepo(), filePath);

        log.info("上传文件到GitHub: owner={}, repo={}, path={}", githubProp.getOwner(), githubProp.getRepo(), filePath);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("content") && jsonNode.get("content").has("download_url")) {
                String cdnUrl = String.format("https://cdn.jsdelivr.net/gh/%s/%s@%s/%s/%s",
                        githubProp.getOwner(), githubProp.getRepo(), githubProp.getBranch(), folder, filename);
                log.info("文件上传成功: {}", cdnUrl);
                return cdnUrl;
            }

            throw new RuntimeException("GitHub API 返回格式不正确: " + response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("GitHub API 错误: 状态码={}, 响应体={}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("GitHub 认证失败，请检查 Token 是否有效且具有 repo 权限");
            }
            throw new RuntimeException("GitHub 上传失败: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        }
    }

    public String getUploadToken() {
        return githubProp.getToken();
    }
}