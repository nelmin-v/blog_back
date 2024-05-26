package com.nelmin.blog.common.conf;

import com.nelmin.blog.common.abstracts.IUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenUtils {
    @Value("${jwttoken.secret:mambasupersecrettokenmambasupersecrettokenmambasupersecrettoken}")
    private String jwtSecret;

    @Value("${jwttoken.expiration:7200000}")
    private Long jwtExpiration;

    public String generateToken(IUser user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Boolean validateToken(String token) {

        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            getParser().parse(token);
            return true;
        } catch (Exception ex) {
            log.error("Ошибка валидации токена", ex);
        }

        return false;
    }

    public String extractUserName(String token) {
        Claims claims = getParser()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private JwtParser getParser() {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build();
    }

    public HttpHeaders createTokenHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", createTokenCookieValue(token));
        headers.add("Set-Cookie", createRefreshTokenCookieValue(token));
        return headers;
    }

    public String createTokenCookieValue(String token) {
        return "Authorization=Bearer_" + token + "; Max-Age=86400; SameSite=None; Path=/; Secure; HttpOnly";
    }

    public String createRefreshTokenCookieValue(String token) {
        return "Refresh=" + token + "; SameSite=None; Path=/; Secure; HttpOnly";
    }
}