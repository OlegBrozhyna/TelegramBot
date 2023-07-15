package com.exemple.service;

import com.exemple.entity.AppDocument;
import com.exemple.exception.UploadFileException;
import org.telegram.telegrambots.meta.api.objects.Message;


public interface FileService {
    AppDocument processDoc(Message externalMessage)
            throws UploadFileException;
}
