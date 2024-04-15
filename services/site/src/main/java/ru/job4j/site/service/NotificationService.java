package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.SubscribeCategory;
import ru.job4j.site.dto.SubscribeTopicDTO;
import ru.job4j.site.dto.UserDTO;
import ru.job4j.site.dto.UserTopicDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    @Value("${service.notification}")
    private String notificationUrl;
    @NonNull
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void addSubscribeCategory(int userId, int categoryId) {
        SubscribeCategory subscribeCategory = new SubscribeCategory(userId, categoryId);
        kafkaTemplate.send("add_category", subscribeCategory);
    }

    public void deleteSubscribeCategory(int userId, int categoryId) {
        SubscribeCategory subscribeCategory = new SubscribeCategory(userId, categoryId);
        kafkaTemplate.send("delete_category", subscribeCategory);
    }

    public UserDTO findCategoriesByUserId(int id) throws JsonProcessingException {
        var text = new RestAuthCall(String.format("%s/subscribeCategory/%d", notificationUrl, id)).get();
        var mapper = new ObjectMapper();
        List<Integer> list = mapper.readValue(text, new TypeReference<>() {
        });
        return new UserDTO(id, list);
    }

    public void addSubscribeTopic(int userId, int topicId) {
        SubscribeTopicDTO subscribeTopicDTO = new SubscribeTopicDTO(userId, topicId);
        kafkaTemplate.send("add_topic", subscribeTopicDTO);
    }

    public void deleteSubscribeTopic(int userId, int topicId) {
        SubscribeTopicDTO subscribeTopic = new SubscribeTopicDTO(userId, topicId);
        kafkaTemplate.send("delete_topic", subscribeTopic);
    }

    public UserTopicDTO findTopicByUserId(int id) throws JsonProcessingException {
        var text = new RestAuthCall(String.format("%s/subscribeTopic/%d", notificationUrl, id)).get();
        var mapper = new ObjectMapper();
        List<Integer> list = mapper.readValue(text, new TypeReference<>() {
        });
        return new UserTopicDTO(id, list);
    }
}