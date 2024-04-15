package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import ru.checkdev.notification.telegram.service.TgUserService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegActionTest {
    @Mock
    private TgUserService tgUserServiceMock;
    @Mock
    private TgAuthCallWebClint tgAuthCallWebClientMock;

    private final String siteUrlMock = "URL";
    private RegAction action;
    private Message message;

    @BeforeEach
    void setUp() {
        action = new RegAction(tgAuthCallWebClientMock, tgUserServiceMock, siteUrlMock);
        message = new Message();
        message.setChat(new Chat(1L, "private"));
        message.setFrom(new User(1L, "user", false));
    }

    @Test
    void whenHandleButUserExist() {
        when(tgUserServiceMock.checkExist(message.getChatId())).thenReturn(true);

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Вы уже зарегистрированы")
                .contains("Получить данные о пользователе: /check");
    }

    @Test
    void whenHandleAndUserNotExist() {
        when(tgUserServiceMock.checkExist(message.getChatId())).thenReturn(false);

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText()).contains("Введите email для регистрации:");
    }

    @Test
    void whenCallbackButEmailNotCorrect() {
        message.setText("not correct email");

        SendMessage rsl = (SendMessage) action.callback(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Email: " + message.getText() + " не корректный.")
                .contains("попробуйте снова.");
    }

    @Test
    void whenCallbackButExceptionWhenPostWebClient() {
        message.setText("email@mail.ru");
        when(tgAuthCallWebClientMock.doPost(any(String.class), any(PersonDTO.class)))
                .thenThrow(WebClientResponseException.class);

        SendMessage rsl = (SendMessage) action.callback(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText()).contains("Сервис не доступен попробуйте позже");
    }

    @Test
    void whenCallbackButPostWebClientReturnError() {
        message.setText("email@mail.ru");
        when(tgAuthCallWebClientMock.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "Error Info";
                    }
                }));

        SendMessage rsl = (SendMessage) action.callback(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Ошибка регистрации: Error Info");
    }

    @Test
    void whenCallback() {
        message.setText("email@mail.ru");
        when(tgAuthCallWebClientMock.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public Object getPerson() {
                        return new Object() {
                            public int getId() {
                                return 1;
                            }
                        };
                    }
                }));
        Mockito.doNothing().when(tgUserServiceMock).save(any(TgUser.class));

        SendMessage rsl = (SendMessage) action.callback(message);


        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Вы зарегистрированы: ")
                .contains("Логин: email@mail.ru")
                .contains("Пароль: tg/")
                .contains("URL");
    }
}