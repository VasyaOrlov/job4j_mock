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
        model.addAttribute("profiles", profilesService.getProfilesByInterview(interviews));
        HashMap<Integer, Integer> topicCategory = new HashMap<>();
        for (CategoryDTO category : categories) {
            int idCategory = category.getId();
            topicsService.getByCategory(idCategory)
                            .forEach(e -> topicCategory.put(e.getId(), idCategory));
        }
        HashMap<Integer, Integer> valueInterview = new HashMap<>();
        for (InterviewDTO interview : interviews) {
            Integer idCategory = topicCategory.get(interview.getTopicId());
            if (idCategory != null) {
                valueInterview.put(idCategory, valueInterview.getOrDefault(idCategory, 0) + 1);
            }
        }
        model.addAttribute("value_interview", valueInterview);
        return "index";
    }
}