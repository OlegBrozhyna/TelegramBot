package com.exemple.service.impl;

import com.exemple.dao.AppDocumentDAO;
import com.exemple.dao.BinaryContentDAO;
import com.exemple.entity.AppDocument;
import com.exemple.entity.BinaryContent;
import com.exemple.exception.UploadFileException;
import com.exemple.service.FileService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


@Log4j
@Service
@PropertySource("classpath:telegram.properties")
public class FileServiceImpl implements FileService {
    @Value("${bot.token}")
    private String token;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    private final AppDocumentDAO appDocumentDAO;
    private final BinaryContentDAO binaryContentDAO;


    public FileServiceImpl(AppDocumentDAO appDocumentDAO, BinaryContentDAO binaryContentDAO) {
        this.appDocumentDAO = appDocumentDAO;
        this.binaryContentDAO = binaryContentDAO;
    }


    @SneakyThrows
    @Override
    public AppDocument processDoc(Message telegramMessage) {
        String fileId = telegramMessage.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            if (jsonObject.has("result")) {
                JSONObject resultObject = jsonObject.getJSONObject("result");
                if (resultObject.has("file_patch")) {
                    String filePatch = resultObject.getString("file_patch");
                    byte[] fileInByte = downloadFile(filePatch);
                    BinaryContent transistenBinaryContent = BinaryContent.builder()
                            .fileAsArrayOfBytes(fileInByte)
                            .build();
                    BinaryContent persistentBinaryContent = binaryContentDAO.save(transistenBinaryContent);
                    Document telegramDoc = telegramMessage.getDocument();
                    AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
                    return appDocumentDAO.save(transientAppDoc);
                }
            }
        }
        throw new UploadFileException("Bad response from telegram service: " + response);
    }

    private AppDocument buildTransientAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();

    }

    private byte[] downloadFile(String filePatch) {
        String fullUri = fileStorageUri.replace("{token}", token)
                .replace("{filePath}", filePatch);
        URL urlObj = null;
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }
        //TODO Make optimization
        try (InputStream is = urlObj.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(urlObj.toExternalForm(), e);
        }
    }


    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token, fileId
        );
    }
}
