package org.microserviceteam.microlink_content.service.impl;

import org.microserviceteam.microlink_content.service.FileStorageService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Primary
public class LocalStorageServiceImpl implements FileStorageService {

    @Override
    public String storeFile(MultipartFile file) {
        // Just return a dummy URL for testing purposes
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        System.out.println("Simulating file upload to local storage: " + fileName);
        return "http://localhost:8082/media/" + fileName;
    }
}
