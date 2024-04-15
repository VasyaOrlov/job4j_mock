package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import ru.checkdev.notification.telegram.service.TgUserService;

import java.util.Map;

@AllArgsConstructor
@Slf4j
public class SubscribeAction implements Action {

    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgAuthCallWebClint authCallWebClint;
    private final TgUserService tgUserService;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        long chatId = message.getChatId();
        String text;
        String ls = System.lineSeparator();
        if (tgUserService.checkExist(chatId)) {
            text = "Ваш аккаунт телеграм уже привязан к аккаунту сайта." + ls
                    + "Чтобы узнать регистрационные данные используйте /check" + ls
                    + "Чтобы отвязать аккаунт используйте /unsubscribe";
            return new SendMessage(String.valueOf(chatId), text);
        }
        text = "Введите email и пароль через пробел." + ls
                + "Пример: \"example@mail.ru password\"";
        return new SendMessage(String.valueOf(chatId), text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        String chatId = message.getChatId().toString();
        String[] emailAndPassword = message.getText().split(" ");
        String text;
        if (emailAndPassword.length != 2) {
            text = "Пожалуйста, введите пару email и пароль как указано в примере выше.";
            return new SendMessage(chatId, text);
        }
        String email = emailAndPassword[0];
        String password = emailAndPassword[1];
        String sl = System.lineSeparator();

        if (!tgConfig.isEmail(email)) {
            text = "Email: " + email + " не корректный." + sl
                    + "попробуйте снова /subscribe";
            return new SendMessage(chatId, text);
        }

        Object tokenAuth;
        try {
            Map<String, String> params = Map.of(
                    "username", email,
                    "password", password
            );
            tokenAuth = authCallWebClint.token(params).block();
        } catch (Exception e) {
            log.error("Get token from service Auth error: {}", e.getMessage());
            text = "Сервис авторизации не доступен, попробуйте позже.";
            return new SendMessage(chatId, text);
        }

        var tokenMap = tgConfig.getObjectToMap(tokenAuth);
        if (!tokenMap.containsKey("access_token")) {
            text = "Введенные email или пароль не верны." + sl
                    + "Попробуйте ещё раз /subscribe";
            return new SendMessage(chatId, text);
        }
        var token = (String) tokenMap.get("access_token");

        Object profileAuth;
        try {
            profileAuth = authCallWebClint.doGet("/person/current", token).block();
        } catch (Exception e) {
            log.error("WebClient doGet error: {}", e.getMessage());
            text = "Сервис данных о профиле не доступен, попробуйте позже.";
            return new SendMessage(chatId, text);
        }
        var profileResultMap = tgConfig.getObjectToMap(profileAuth);
        TgUser tgUser = new TgUser(0, (String) profileResultMap.get("username"), email,
                message.getChatId(), (int) profileResultMap.get("id"));
        tgUserService.save(tgUser);
        text = "Аккаунт с почтой: " + email + " - успешно привязан.";
        return new SendMessage(chatId, text);
    }
}
