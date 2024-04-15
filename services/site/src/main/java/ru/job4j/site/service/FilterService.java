package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.FilterDTO;

@Service
public class FilterService {
    @Value("${service.mock}")
    private String mockUrl;

    public FilterDTO save(String token, FilterDTO filter) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var out = new RestAuthCall(String.format("%s/filter/", mockUrl)).post(
                token,
                mapper.writeValueAsString(filter)
        );
        return mapper.readValue(out, FilterDTO.class);
    }

    public FilterDTO getByUserId(String token, int userId) throws JsonProcessingException {
        var text = new RestAuthCall(String.format("%s/filter/%d", mockUrl, userId))
                .get(token);
        return new ObjectMapper().readValue(text, new TypeReference<>() {
        });
    }

    public void deleteByUserId(String token, int userId) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        new RestAuthCall(String.format("%s/filter/delete/%d", mockUrl, userId)).delete(
                token,
                mapper.writeValueAsString(userId)
        );
    }
}
