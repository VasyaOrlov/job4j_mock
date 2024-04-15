package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import ru.checkdev.notification.telegram.service.TgUserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForgetActionTest {

    @Mock
    private TgUserService tgUserServiceMock;
    @Mock
    private TgAuthCallWebClint tgAuthCallWebClientMock;

    private final String siteUrlMock = "URL";
    private Message message;
    private ForgetAction action;

    @BeforeEach
    void setUp() {
        action = new ForgetAction(tgAuthCallWebClientMock, tgUserServiceMock, siteUrlMock);
        message = new Message();
        message.setChat(new Chat(1L, "private"));
    }

    @Test
    void whenHandleButUserNotExist() {
        when(tgUserServiceMock.findByChatId(message.getChatId())).thenReturn(Optional.empty());

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Вы еще не зарегистрированы.")
                .contains("Для регистрации введите команду /new.");
    }

    @Test
    void whenHandleButExceptionWhenPostWebClient() {
        TgUser user = new TgUser(1, "username", "e@mail", message.getChatId(), 1);
        when(tgUserServiceMock.findByChatId(message.getChatId())).thenReturn(Optional.of(user));
        when(tgAuthCallWebClientMock.doPost(any(String.class), any(PersonDTO.class)))
                .thenThrow(WebClientResponseException.class);

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText()).contains("Сервис не доступен попробуйте позже");
    }

    @Test
    void whenHandleButPostWebClientReturnError() {
        TgUser user = new TgUser(1, "username", "e@mail", message.getChatId(), 1);
        when(tgUserServiceMock.findByChatId(message.getChatId())).thenReturn(Optional.of(user));
        when(tgAuthCallWebClientMock.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "Error Info";
                    }
                }));

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Ошибка при сбросе пароля: ")
                .contains("Error Info");
    }

    @Test
    void whenHandleOk() {
        TgUser user = new TgUser(1, "username", "e@mail", message.getChatId(), 1);
        when(tgUserServiceMock.findByChatId(message.getChatId())).thenReturn(Optional.of(user));
        when(tgAuthCallWebClientMock.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getOk() {
                        return "ok";
                    }
                }));

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Ваши данные для входа: ")
                .contains("Логин: " + user.getEmail())
                .contains("Пароль: tg/")
                .contains(siteUrlMock);
    }
}