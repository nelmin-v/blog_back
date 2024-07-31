package com.nelmin.my_log.auth.controller;

import com.nelmin.my_log.auth.dto.ChangeInfoRequestDto;
import com.nelmin.my_log.common.bean.UserInfo;
import com.nelmin.my_log.common.conf.JwtTokenUtils;
import com.nelmin.my_log.auth.dto.AuthResponseDto;
import com.nelmin.my_log.auth.dto.LoginRequestDto;
import com.nelmin.my_log.auth.dto.StateResponseDto;
import com.nelmin.my_log.auth.service.AuthService;
import com.nelmin.my_log.common.dto.SuccessDto;
import com.nelmin.my_log.common.dto.UserInfoDto;
import com.nelmin.my_log.common.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserInfo userInfo;

    @PostMapping(value = "/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        var result = authService.authenticate(loginRequestDto);

        if (result.getErrors().isEmpty()) {
            HttpHeaders headers = jwtTokenUtils.createTokenHeaders(result.getToken());
            result.setToken(null);
            result.setRefreshToken(null);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .body(result);
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(result);
        }
    }

    @GetMapping(value = "/state")
    public ResponseEntity<StateResponseDto> state() {
        return ResponseEntity.ok(new StateResponseDto(userInfo.isAuthorized()));
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/info")
    public ResponseEntity<UserInfoDto> info() {
        var response = userService.info(userInfo.getId());

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_PREMIUM_USER")
    @GetMapping(value = "/premium-info")
    public ResponseEntity<UserInfoDto> premiumInfo() {
        var response = userService.info(userInfo.getId());

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @GetMapping(value = "/info/{nickname}")
    public ResponseEntity<UserInfoDto> info(@Valid @PathVariable String nickname) {
        var response = userService.publicInfo(nickname);
        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @GetMapping(value = "/infos/{nickname}")
    public ResponseEntity<List<UserInfoDto>> infos(@Valid @PathVariable String nickname) {
        var response = userService.publicInfos(nickname);
        return ResponseEntity
                .status(response.isEmpty() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/change-nickname")
    public ResponseEntity<SuccessDto> changeNickname(@Valid @RequestBody ChangeInfoRequestDto dto) {
        var response = authService.changeNickname(dto);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/change-description")
    public ResponseEntity<SuccessDto> changeDescription(@Valid @RequestBody ChangeInfoRequestDto dto) {
        var response = authService.changeDescription(dto);

        return ResponseEntity
                .status(response.hasErrors() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}