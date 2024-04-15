package ru.checkdev.notification.telegram.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.telegram.domain.TgUser;
import ru.checkdev.notification.telegram.repository.TgUserRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TgUserService {
    private final TgUserRepository tgUserRepository;

    public boolean checkExist(Long chatId) {
        return tgUserRepository.existsByChatId(chatId);
    }

    public void save(TgUser tgUser) {
        tgUserRepository.save(tgUser);
    }

    public Optional<TgUser> findByChatId(long chatId) {
        return tgUserRepository.findByChatId(chatId);
    }

    public boolean deleteByChatId(long chatId) {
        return tgUserRepository.deleteByChatId(chatId) > 0;
    }
}
