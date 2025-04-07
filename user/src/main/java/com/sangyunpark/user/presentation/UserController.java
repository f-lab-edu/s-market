package com.sangyunpark.user.presentation;

import com.sangyunpark.user.application.UserService;
import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.dto.response.UserSelectResponseDto;
import com.sangyunpark.user.domain.dto.response.UserSignupResponseDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserSelectResponseDto findUserById(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @GetMapping
    public UserSelectResponseDto findUserByEmail(@RequestParam @Email String email) {
        return userService.findUserByEmail(email);
    }

    @PostMapping
    public UserSignupResponseDto signup(@Valid @RequestBody final UserSignupRequestDto request) {
        Long userId = userService.signup(request);
        return new UserSignupResponseDto(userId);
    }
}
