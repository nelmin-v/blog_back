package com.nelmin.blog.auth.service;

import com.nelmin.blog.auth.dto.ChangeInfoRequestDto;
import com.nelmin.blog.common.bean.UserInfo;
import com.nelmin.blog.common.conf.JwtTokenUtils;
import com.nelmin.blog.auth.dto.AuthResponseDto;
import com.nelmin.blog.auth.dto.BlockedUser;
import com.nelmin.blog.auth.dto.LoginRequestDto;
import com.nelmin.blog.auth.exceptions.UserNotFoundException;
import com.nelmin.blog.common.dto.SuccessDto;
import com.nelmin.blog.common.model.User;
import com.nelmin.blog.common.service.UserService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${auth.max.attempts:3}")
    private Long maxAttempts;

    @Value("${auth.block.time:2}")
    private Long blockTime;

    private final User.Repo userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtils jwtTokenUtils;
    private final Cache cache;
    private final UserService userService;
    private final UserInfo userInfo;

    // TODO защита от перебора
    @Transactional
    public AuthResponseDto authenticate(LoginRequestDto loginRequestDto) {
        var authResponse = new AuthResponseDto(false);

        checkBlock(loginRequestDto, authResponse);

        if (authResponse.hasErrors()) {
            return authResponse;
        }

        var userInfo = userRepository.findUserByUsername(loginRequestDto.login());

        var userNamePasswordToken = new UsernamePasswordAuthenticationToken(loginRequestDto.login(), loginRequestDto.password());
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(userNamePasswordToken);
        } catch (BadCredentialsException exception) {
            countAttempts(loginRequestDto, authResponse);
            return authResponse;
        } catch (Exception exception) {
            log.error("Auth Error", exception);
            authResponse.reject("server_error", "server_error");
            return authResponse;
        }

        var user = userInfo.orElseThrow(UserNotFoundException::new);

        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var token = jwtTokenUtils.generateToken(user);

        authResponse.setToken(token);
        // TODO refresh token
        authResponse.setRefreshToken(token);
        authResponse.setSuccess(true);

        cache.evictIfPresent("blocked_cache_" + loginRequestDto.login());
        log.info("User {} logged", user.getUsername());
        return authResponse;
    }

    @Transactional
    public SuccessDto changeInfo(@NonNull ChangeInfoRequestDto dto) {
        var res = new SuccessDto(false);

        if (!StringUtils.hasText(dto.nickName()) && !StringUtils.hasText(dto.password())) {
            res.reject("invalid", "changeInfo");
            return res;
        }

        try {
            User user = (User) userInfo.getCurrentUser();


            if (StringUtils.hasText(dto.nickName())) {
                userService.changeNickName(user, dto.nickName());
                res.setSuccess(true);
            }

            if (StringUtils.hasText(dto.password())) {
                userService.changePassword(user, dto.password());
                res.setSuccess(true);
            }
        } catch (Exception ex) {
            log.error("Error change info", ex);
            res.setSuccess(false);
        }

        return res;
    }

    private void countAttempts(LoginRequestDto loginRequestDto, AuthResponseDto authResponse) {
        authResponse.setSuccess(false);
        authResponse.reject("invalid", "password");
        authResponse.reject("invalid", "login");

        var blockedUser = cache.get("blocked_cache_" + loginRequestDto.login(), BlockedUser.class);

        if (blockedUser == null) {
            blockedUser = new BlockedUser();
        }

        blockedUser.setAttempts(blockedUser.getAttempts() + 1);

        if (blockedUser.getAttempts().longValue() == maxAttempts) {
            blockedUser.setBlocked(true);
            blockedUser.setBlockDateTime(LocalDateTime.now());
            blockedUser.setReason("Max attempts");
            authResponse.clearErrors();
            authResponse.reject("block", "login", Map.of("time", blockTime));
            log.info("User {} blocked {} minutes", blockedUser.getLogin(), blockTime);
        } else {
            authResponse.reject("attempts", "credentials", Map.of("value", maxAttempts - blockedUser.getAttempts()));
        }

        cache.put("blocked_cache_" + loginRequestDto.login(), blockedUser);
    }


    private void checkBlock(LoginRequestDto loginRequestDto, AuthResponseDto authResponse) {
        var blockedUser = cache.get("blocked_cache_" + loginRequestDto.login(), BlockedUser.class);

        if (blockedUser != null && blockedUser.getBlockDateTime() != null) {
            var blockMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), blockedUser.getBlockDateTime().plusMinutes(blockTime));

            if (blockMinutes <= 0) {
                cache.evictIfPresent("blocked_cache_" + loginRequestDto.login());
            } else {
                authResponse.clearErrors();
                authResponse.reject("blocked", "login", Map.of("time", blockMinutes));
                authResponse.setSuccess(false);
            }
        }
    }
}


