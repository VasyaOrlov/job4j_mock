package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import ru.checkdev.notification.telegram.service.TgUserService;

import java.util.Calendar;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
public class ForgetAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String URL_FORGOT_PASSWORD = "/forgot";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint authCallWebClint;
    private final TgUserService tgUserService;
    private final String urlSiteAuth;
    @Override
    public BotApiMethod<Message> handle(Message message) {
        long chatId = message.getChatId();
        Optional<TgUser> user = tgUserService.findByChatId(chatId);
        String text;
        String ls = System.lineSeparator();
        if (user.isEmpty()) {
            text = "Вы еще не зарегистрированы." + ls
                    + "Для регистрации введите команду /new.";
            return new SendMessage(String.valueOf(chatId), text);
        }
        TgUser tgUser = user.get();
        String userName = tgUser.getUsername();
        String email = tgUser.getEmail();
        String pass = tgConfig.getPassword();
        PersonDTO personDTO = new PersonDTO(userName, email, pass, false, null, Calendar.getInstance());
        Object rsl;
        try {
            rsl = authCallWebClint.doPost(URL_FORGOT_PASSWORD, personDTO).block();
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + ls
                    + "/start";
            return new SendMessage(String.valueOf(chatId), text);
        }

        var mapObject = tgConfig.getObjectToMap(rsl);
        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка при сбросе пароля: " + mapObject.get(ERROR_OBJECT);
            return new SendMessage(String.valueOf(chatId), text);
        }
        text = "Ваши данные для входа: " + ls
                + "Логин: " + email + ls
                + "Пароль: " + pass + ls
                + urlSiteAuth;
        return new SendMessage(String.valueOf(chatId), text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
