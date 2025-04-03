package com.sangyunpark.user.presentation;

import com.sangyunpark.user.application.UserService;
import com.sangyunpark.user.domain.dto.response.UserSelectResponseDto;
import com.sangyunpark.user.domain.dto.resquest.UserSelectByEmailRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserSelectResponseDto> findUserById(@PathVariable Long id) {
        UserSelectResponseDto responseDto = userService.findUserById(id);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/email")
    public ResponseEntity<UserSelectResponseDto> findUserByEmail(@Valid @RequestBody UserSelectByEmailRequestDto userSelectByEmailRequestDto) {
        UserSelectResponseDto responseDto = userService.findUserByEmail(userSelectByEmailRequestDto);
        return ResponseEntity.ok(responseDto);
    }
}
