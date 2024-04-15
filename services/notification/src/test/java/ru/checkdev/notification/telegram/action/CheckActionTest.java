package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.telegram.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgUserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckActionTest {
    @Mock
    private TgUserService tgUserServiceMock;

    private Message message;
    private CheckAction action;

    @BeforeEach
    void setUp() {
        action = new CheckAction(tgUserServiceMock);
        message = new Message();
        message.setChat(new Chat(1L, "private"));
    }

    @Test
    void whenHandlerButUserNotExist() {
        when(tgUserServiceMock.findByChatId(message.getChatId())).thenReturn(Optional.empty());

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Вы еще не зарегистрированы.")
                .contains("Для регистрации введите команду /new .");
    }

    @Test
    void whenHandlerAndUserExist() {
        TgUser user = new TgUser(1, "username", "e@mail", message.getChatId(), 1);
        when(tgUserServiceMock.findByChatId(message.getChatId())).thenReturn(Optional.of(user));

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Ваши регистрационные данные: ")
                .contains("имя пользователя - " + user.getUsername())
                .contains("email - " + user.getEmail());
    }
}