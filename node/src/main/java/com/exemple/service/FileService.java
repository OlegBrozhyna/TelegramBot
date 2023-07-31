package com.exemple.service;

import com.exemple.entity.AppDocument;
import com.exemple.entity.AppPhoto;
import com.exemple.exception.UploadFileException;
import com.exemple.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;


public interface FileService {
    AppDocument processDoc(Message telegramMessage);

    AppPhoto processPhoto (Message telegramMessage);
    String generateLink(Long docId, LinkType linkType);
}
