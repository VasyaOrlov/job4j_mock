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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnsubscribeActionTest {
    @Mock
    private TgUserService tgUserServiceMock;

    private Message message;
    private UnsubscribeAction action;

    @BeforeEach
    void setUp() {
        action = new UnsubscribeAction(tgUserServiceMock);
        message = new Message();
        message.setChat(new Chat(1L, "private"));
    }

    @Test
    void whenHandleButUserNotExist() {
        when(tgUserServiceMock.checkExist(message.getChatId())).thenReturn(false);

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Вы еще не зарегистрированы.")
                .contains("Для регистрации введите команду /new.");
    }

    @Test
    void whenHandleOk() {
        TgUser user = new TgUser(1, "username", "e@mail", message.getChatId(), 1);
        when(tgUserServiceMock.checkExist(message.getChatId())).thenReturn(true);

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText()).contains("Аккаунт телеграм успешно отвязан.");
    }
}