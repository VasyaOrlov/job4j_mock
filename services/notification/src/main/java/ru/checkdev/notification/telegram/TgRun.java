package ru.checkdev.notification.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.checkdev.notification.telegram.action.*;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;
import ru.checkdev.notification.telegram.service.TgUserService;

import java.util.List;
import java.util.Map;

/**
 * 3. Мидл
 * Инициализация телеграм бот,
 * username = берем из properties
 * token = берем из properties
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@Component
@Slf4j
public class TgRun {
    private final TgAuthCallWebClint tgAuthCallWebClint;
    private final TgUserService tgUserService;
    @Value("${tg.username}")
    private String username;
    @Value("${tg.token}")
    private String token;
    @Value("${server.site.url.login}")
    private String urlSiteAuth;

    public TgRun(TgAuthCallWebClint tgAuthCallWebClint, TgUserService tgUserService) {
        this.tgAuthCallWebClint = tgAuthCallWebClint;
        this.tgUserService = tgUserService;
    }

    @Bean
    public void initTg() {
        Map<String, Action> actionMap = Map.of(
                "/start", new InfoAction(
                        List.of(
                                "/start - напечатать список доступных команд",
                                "/new - регистрация нового пользователя",
                                "/check - выдать ФИО и почту, привязанную к этому аккаунту",
                                "/forget - восстановление пароля",
                                "/subscribe - подписка",
                                "/unsubscribe - отписка")
                ),
                "/new", new RegAction(tgAuthCallWebClint, tgUserService, urlSiteAuth),
                "/check", new CheckAction(tgUserService),
                "/forget", new ForgetAction(tgAuthCallWebClint, tgUserService, urlSiteAuth),
                "/subscribe", new SubscribeAction(tgAuthCallWebClint, tgUserService),
                "/unsubscribe", new UnsubscribeAction(tgUserService)
        );
        try {
            BotMenu menu = new BotMenu(actionMap, username, token);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(menu);
        } catch (TelegramApiException e) {
            log.error("Telegram bot: {}, ERROR {}", username, e.getMessage());
        }
    }
}
