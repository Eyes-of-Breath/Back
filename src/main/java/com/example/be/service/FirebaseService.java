package com.example.be.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FirebaseService {

    public String uploadFile(MultipartFile file) throws IOException {
        Bucket bucket = StorageClient.getInstance().bucket("your-firebase-storage-bucket-name"); // 👈 Firebase Storage 버킷 이름
        InputStream content = file.getInputStream();
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename(); // 파일 이름 중복 방지

        Blob blob = bucket.create(fileName, content, file.getContentType());

        // 업로드된 파일의 공개 URL 반환 (버킷 권한 설정에 따라 다를 수 있음)
        return blob.getMediaLink();
    }
}