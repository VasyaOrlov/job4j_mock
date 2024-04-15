package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.telegram.service.TgUserService;

@AllArgsConstructor
@Slf4j
public class UnsubscribeAction implements Action {
    private final TgUserService tgUserService;
    @Override
    public BotApiMethod<Message> handle(Message message) {
        long chatId = message.getChatId();
        String text;
        String ls = System.lineSeparator();

        if (!tgUserService.checkExist(chatId)) {
            text = "Вы еще не зарегистрированы." + ls
                    + "Для регистрации введите команду /new.";
            return new SendMessage(String.valueOf(chatId), text);
        }

        tgUserService.deleteByChatId(chatId);
        text = "Аккаунт телеграм успешно отвязан.";
        return new SendMessage(String.valueOf(chatId), text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }
}
