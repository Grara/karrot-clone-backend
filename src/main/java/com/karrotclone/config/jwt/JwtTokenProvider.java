package com.karrotclone.config.jwt;

import com.karrotclone.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    public static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 30;

    public static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 30;

    //추후 value로 때체
    //@Value("${jwt.accessToken}")
    public static final String ACCESS_TOKEN_NAME = "carrotMarketClone";

    //@Value("${jwt.refreshToken}")
    public static final String REFRESH_TOKEN_NAME = "carrotMarketCloneRefresh";

    private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    //@Value("${jwt.secretKey}")
    private String SECRET_KEY = "secretKey";

    //redis 쓸까...


    private Key getSigningKey(String secretKey){
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public Claims extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder().setSigningKey(getSigningKey(SECRET_KEY))
                .build().parseClaimsJws(token).getBody();
    }

    public String getUsername(String token){
        return extractAllClaims(token).get("username", String.class);
    }

    public Boolean isTokenExpired(String token){
        final Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    //UserPrincipal 생성이 우선
    public String generateAccessToken(Member member){
        return doGenerateToken(member.getUsername(),ACCESS_TOKEN_EXPIRE_TIME);
        //토큰 생성 로직 생성 후 대체.
    }

    public String generateRefreshToken(Member member){
        return doGenerateToken(member.getUsername(), REFRESH_TOKEN_EXPIRE_TIME);
    }

    public String doGenerateToken(String username, long expireTime){
        Claims claims = Jwts.claims();
        claims.put("username", username);

        String jwt = Jwts.builder().setClaims(claims).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(getSigningKey(SECRET_KEY), SignatureAlgorithm.HS256)
                .compact();

        return jwt;
    }

    public Long getExpireTime(String token){
        return extractAllClaims(token).getExpiration().getTime();
    }

    public boolean validateToken(String token){
        //redis 안쓰면 어떻게 판별할 지 생각.
        return true;
    }
}