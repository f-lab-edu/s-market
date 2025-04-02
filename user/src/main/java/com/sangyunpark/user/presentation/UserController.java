package com.sangyunpark.user.presentation;

import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.dto.response.UserSignupResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @PostMapping
    public ResponseEntity<UserSignupResponseDto> signup(@RequestBody UserSignupRequestDto request) {
        return ResponseEntity.ok(new UserSignupResponseDto());
    }
}
