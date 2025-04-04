package com.sangyunpark.user.presentation;

import com.sangyunpark.user.application.UserService;
import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.dto.response.UserSignupResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserSignupResponseDto signup(@Valid @RequestBody final UserSignupRequestDto request) {
        Long userId = userService.signup(request);
        return new UserSignupResponseDto(userId);
    }
}
