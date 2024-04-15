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
import ru.checkdev.notification.telegram.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import ru.checkdev.notification.telegram.service.TgUserService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscribeActionTest {
    @Mock
    private TgUserService tgUserServiceMock;
    @Mock
    private TgAuthCallWebClint tgAuthCallWebClientMock;

    private SubscribeAction action;
    private Message message;

    @BeforeEach
    void setUp() {
        action = new SubscribeAction(tgAuthCallWebClientMock, tgUserServiceMock);
        message = new Message();
        message.setChat(new Chat(1L, "private"));
    }

    @Test
    void whenHandleButUserExistAndSubscribe() {
        when(tgUserServiceMock.checkExist(message.getChatId())).thenReturn(true);

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText()).contains("Ваш аккаунт телеграм уже привязан к аккаунту сайта.")
                .contains("Чтобы узнать регистрационные данные используйте /check")
                .contains("Чтобы отвязать аккаунт используйте /unsubscribe");
    }

    @Test
    void whenHandleOk() {
        when(tgUserServiceMock.checkExist(message.getChatId())).thenReturn(false);

        SendMessage rsl = (SendMessage) action.handle(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Введите email и пароль через пробел.")
                .contains("Пример: \"example@mail.ru password\"");
    }

    @Test
    void whenCallbackButBadData() {
        message.setText("pass");

        SendMessage rsl = (SendMessage) action.callback(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Пожалуйста, введите пару email и пароль как указано в примере выше.");
    }

    @Test
    void whenCallbackButWrongEmail() {
        message.setText("email.ru pass");

        SendMessage rsl = (SendMessage) action.callback(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Email: email.ru не корректный.")
                .contains("попробуйте снова");
    }

    @Test
    void whenCallbackButExceptionWhenTokenWebClient() {
        message.setText("e@mail.ru pass");
        TgUser user = new TgUser(1, "username", "e@mail", message.getChatId(), 1);
        when(tgAuthCallWebClientMock.token(anyMap())).thenThrow(WebClientResponseException.class);

        SendMessage rsl = (SendMessage) action.callback(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText()).contains("Сервис авторизации не доступен, попробуйте позже.");
    }

    @Test
    void whenCallbackButTokenWebClientNotReturnToken() {
        message.setText("e@mail.ru pass");
        when(tgAuthCallWebClientMock.token(anyMap()))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "error";
                    }
                }));

        SendMessage rsl = (SendMessage) action.callback(message);

        assertThat(rsl.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(rsl.getText())
                .contains("Введенные email или пароль не верны.")
                .contains("Попробуйте ещё раз /subscribe");
    }


    /**
    @Test
    @SuppressWarnings("checkstyle:methodname")
    void whenCallbackOk() {
        message.setText("email@mail.ru password");
        when(tgAuthCallWebClientMock.token(anyMap()))
                .thenReturn(Mono.just(new Object() {
                    public String getAccess_token() {
                        return "token";
                    }
                }));
        when(tgAuthCallWebClientMock.doGet(any(String.class), any()))
                .thenReturn(Mono.just(new Object() {
                    public int getId() {
                        return 1;
                    }
                    public String getUserName() {
                        return "username";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Аккаунт с почтой: email@mail.ru - успешно привязан.");
    }
    */
}