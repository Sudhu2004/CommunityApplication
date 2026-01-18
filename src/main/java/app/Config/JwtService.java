package app.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "990HiFbZCwYrtsyYl6qwO9jYC3wNYrC18qlTkkzRYUF".getBytes()
    );

    private final long JWT_TOKEN_VALIDITY = 1000 * 60 * 60 * 10; // 10 hours

    public String generateToken(String userEmail) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userEmail);
    }

    public String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(SECRET_KEY)
                .compact();
    }

    public String getUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Date extracExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenValid(String token) {
        return extracExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, String userEmail) {
        final String extractedUserEmail = getUserEmail(token);
        return extractedUserEmail.equals(userEmail) && !isTokenValid(token);
    }
}
