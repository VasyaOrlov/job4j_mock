package ru.job4j.site.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.job4j.site.dto.CategoryDTO;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.dto.ProfileDTO;
import ru.job4j.site.dto.TopicDTO;
import ru.job4j.site.service.*;

import javax.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

import static ru.job4j.site.controller.RequestResponseTools.getToken;

@Controller
@AllArgsConstructor
@Slf4j
public class IndexController {
    private final CategoriesService categoriesService;
    private final InterviewsService interviewsService;
    private final AuthService authService;
    private final NotificationService notifications;
    private final ProfilesService profilesService;
    private final TopicsService topicsService;

    @GetMapping({"/", "index"})
    public String getIndexPage(Model model, HttpServletRequest req) throws JsonProcessingException {
        RequestResponseTools.addAttrBreadcrumbs(model,
                "Главная", "/"
        );
        List<CategoryDTO> categories = categoriesService.getMostPopular();
        try {
            model.addAttribute("categories", categories);
            var token = getToken(req);
            if (token != null) {
                var userInfo = authService.userInfo(token);
                model.addAttribute("userInfo", userInfo);
                model.addAttribute("userDTO", notifications.findCategoriesByUserId(userInfo.getId()));
                RequestResponseTools.addAttrCanManage(model, userInfo);
            }
        } catch (Exception e) {
            log.error("Remote application not responding. Error: {}. {}, ", e.getCause(), e.getMessage());
        }
        List<InterviewDTO> interviews = interviewsService.getByType(1);
        model.addAttribute("new_interviews", interviews);
        Set<ProfileDTO> profiles = interviews.stream()
                .flatMap(interview -> profilesService.getAllProfile()
                        .stream()
                        .filter(profile -> profile.getId() == interview.getSubmitterId()))
                .collect(Collectors.toSet());
        model.addAttribute("profiles", profiles);
        List<TopicDTO> topics = new ArrayList<>();
        for (CategoryDTO category : categories) {
            topics.addAll(topicsService.getByCategory(category.getId()));
        }
        HashMap<Integer, Integer> valueInterview = new HashMap<>();
        for (InterviewDTO interview : interviews) {
            for (TopicDTO topic : topics) {
                if (topic.getId() == interview.getTopicId()) {
                    int key = topic.getCategory().getId();
                    valueInterview.put(key, valueInterview.getOrDefault(key, 0) + 1);
                }
            }
        }
        model.addAttribute("value_interview", valueInterview);
        return "index";
    }
}