package ru.checkdev.auth.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.checkdev.auth.dto.ProfileDTO;
import ru.checkdev.auth.service.ProfileService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * CheckDev пробное собеседование
 * ProfileController контроллер отправки и приема DTO модели ProfileDTO
 *
 * @author Dmitry Stepanov
 * @version 22.09.2023T23:49
 */
@RestController
@RequestMapping("/profiles")
@Slf4j
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Обрабатывает get запрос на получение профиля пользователя по запрошенному ID.
     *
     * @param id ID ProfileDTO
     * @return ResponseEntity
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfileDTO> getProfileById(@PathVariable int id) {
        var profileDTO = profileService.findProfileByID(id);
        return new ResponseEntity<>(
                profileDTO.orElse(new ProfileDTO()),
                profileDTO.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK);
    }

    /**
     * Отправляет все профили пользователей
     *
     * @return ResponseEntity
     */
    @GetMapping("/")
    public ResponseEntity<List<ProfileDTO>> getAllProfilesOrderByCreateDesc() {
        var profiles = profileService.findProfilesOrderByCreatedDesc();
        return new ResponseEntity<>(
                profiles,
                profiles.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK);
    }

    @PostMapping(path = "/byInterview/", consumes = "application/json")
    public ResponseEntity<List<ProfileDTO>> getProfilesByIdsSubmitterInterview(@RequestBody(required = false)
                                                                               Set<Integer> ids) {
        System.out.println(ids);
        List<ProfileDTO> profiles = new ArrayList<>();
        if (!ids.isEmpty()) {
            profiles = profileService.findProfilesByIdsSubmitterInterview(ids);
        }
        return new ResponseEntity<>(
                profiles,
                profiles.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK);
    }
}
