package cn.lizongyi.shareaccount.util;

import cn.lizongyi.shareaccount.services.BaseHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

// 自己登录的token，不是微信小程序的token
@Component
public class JwtUtil {

    @Autowired
    private BaseHandler baseHandler;
    
    private SecretKey secretKey;

    private SecretKey getSecretKey() {
        if (secretKey == null) {
            String secretBase64Encoded = baseHandler.getJwtSecret();
            this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64Encoded));
        }
        return secretKey;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                   .setSigningKey(getSecretKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String subject, long expirationTimeInSeconds) {
        return createToken(subject, expirationTimeInSeconds);
    }

    private String createToken(String subject, long expirationTimeInSeconds) {
        return Jwts.builder()
                   .setSubject(subject)
                   .setIssuedAt(new Date(System.currentTimeMillis()))
                   .setExpiration(new Date(System.currentTimeMillis() + expirationTimeInSeconds * 1000L))
                   .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                   .compact();
    }

    // JwtUtil.java
    public String extractOpenid(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Boolean validateToken(String token) {
        final String extractedOpenid = extractOpenid(token);
        return !isTokenExpired(token) && extractedOpenid != null && !extractedOpenid.isEmpty();
    }
}