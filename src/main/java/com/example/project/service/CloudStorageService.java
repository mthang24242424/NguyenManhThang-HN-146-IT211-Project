package com.example.project.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudStorageService {

    Map<String, String> upload(MultipartFile file);

    void delete(String publicId);
}
