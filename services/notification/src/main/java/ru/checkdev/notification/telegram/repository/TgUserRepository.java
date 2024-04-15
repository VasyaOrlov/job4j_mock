package ru.checkdev.notification.telegram.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.checkdev.notification.telegram.domain.TgUser;

import java.util.Optional;

public interface TgUserRepository extends CrudRepository<TgUser, Integer> {
    boolean existsByChatId(Long chatId);

    Optional<TgUser> findByChatId(long chatId);

    @Transactional
    Integer deleteByChatId(long chatId);

}
