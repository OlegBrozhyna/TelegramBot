package com.exemple.service.impl;

import com.exemple.dao.AppUserDAO;
import com.exemple.dao.RawDataDAO;
import com.exemple.entity.AppUser;
import com.exemple.entity.RawData;
import com.exemple.entity.enums.UserState;
import com.exemple.service.MainService;
import com.exemple.service.ProducerService;
import lombok.Builder;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.exemple.entity.enums.UserState.BASIC_STATE;

@Builder
@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;

    private final AppUserDAO appUserDAO;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, AppUserDAO appUserDAO) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.appUserDAO = appUserDAO;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
// save telegramUser
        var textMessage = update.getMessage();
        var telegramUser=textMessage.getFrom();
        var appUser = findOrSaveAppUser(telegramUser);


        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from NODE");
        producerService.producerAnswer(sendMessage);
    }
    // save telegramUser
    private AppUser findOrSaveAppUser(User telegramUser) {
        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO change the default value after adding registration
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser;
    }


    private void saveRawData(Update update) {

        RawData rawData = RawData.builder().
                event(update)
                .build();
        rawDataDAO.save(rawData);

    }

    @Override
    public void processDocMessage(Update update) {

    }

    @Override
    public void processPhotoMessage(Update update) {

    }
}
