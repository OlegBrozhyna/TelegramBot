package com.exemple.service;

import com.exemple.entity.AppDocument;
import com.exemple.entity.AppPhoto;
import com.exemple.entity.BinaryContent;
import org.springframework.core.io.FileSystemResource;

public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);
    FileSystemResource getFileSystemResource(BinaryContent binaryContent);
}
