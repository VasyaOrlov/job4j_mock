package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.telegram.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgUserService;

import java.util.Optional;

@AllArgsConstructor
public class CheckAction implements Action {

    TgUserService tgUserService;
    @Override
    public BotApiMethod<Message> handle(Message message) {
        long chatId = message.getChatId();
        Optional<TgUser> user = tgUserService.findByChatId(chatId);
        String text;
        String ls = System.lineSeparator();
        if (user.isEmpty()) {
            text = "Вы еще не зарегистрированы." + ls
                    + "Для регистрации введите команду /new .";
            return new SendMessage(String.valueOf(chatId), text);
        }
        text = "Ваши регистрационные данные: " + ls
                + "имя пользователя - " + user.get().getUsername() + ls
                + "email - " + user.get().getEmail();
        return new SendMessage(String.valueOf(chatId), text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
