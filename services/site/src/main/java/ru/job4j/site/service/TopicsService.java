package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.CategoryDTO;
import ru.job4j.site.dto.TopicDTO;
import ru.job4j.site.dto.TopicLiteDTO;
import ru.job4j.site.dto.TopicIdNameDTO;

import java.util.Calendar;
import java.util.List;

@Service
public class TopicsService {
    @Value("${service.desc}")
    private String descUrl;

    public List<TopicDTO> getByCategory(int id) throws JsonProcessingException {
        var text = new RestAuthCall(String.format("%s/topics/%d", descUrl, id)).get();
        var mapper = new ObjectMapper();
        return mapper.readValue(text, new TypeReference<>() {
        });
    }

    public TopicDTO getById(int id) throws JsonProcessingException {
        var text = new RestAuthCall(String.format("%s/topic/%d", descUrl, id)).get();
        var mapper = new ObjectMapper();
        return mapper.readValue(text, new TypeReference<>() {
        });
    }

    public TopicDTO create(String token, TopicLiteDTO topicLite) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var topic = new TopicDTO();
        topic.setName(topicLite.getName());
        topic.setPosition(topicLite.getPosition());
        topic.setText(topicLite.getText());
        var category = new CategoryDTO();
        category.setId(topicLite.getCategoryId());
        topic.setCategory(category);
        var out = new RestAuthCall(String.format("%s/topic/", descUrl)).post(
                token,
                mapper.writeValueAsString(topic)
        );
        return mapper.readValue(out, TopicDTO.class);
    }

    public void update(String token, TopicDTO topic) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        topic.setUpdated(Calendar.getInstance());
        var json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(topic);
        new RestAuthCall(String.format("%s/topic/", descUrl)).update(
                token,
                json
        );
    }

    public void delete(String token, int id) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var topic = new TopicDTO();
        topic.setId(id);
        new RestAuthCall(String.format("%s/topic/", descUrl)).delete(
                token,
                mapper.writeValueAsString(topic)
        );
    }

    public String getNameById(int id) {
        return new RestAuthCall(String.format("%s/topic/name/%d", descUrl, id)).get();
    }

    public List<TopicIdNameDTO> getTopicIdNameDtoByCategory(int categoryId)
            throws JsonProcessingException {
        var text = new
                RestAuthCall(String.format("%s/topics/getByCategoryId/%d", descUrl,
                categoryId)).get();
        var mapper = new ObjectMapper();
        return mapper.readValue(text, new TypeReference<>() {
        });
    }
}
