package com.github.mpacala00.supportportal.utility;

import com.auth0.jwt.JWT;
import com.github.mpacala00.supportportal.domain.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.github.mpacala00.supportportal.constant.SecurityConstant.*;

public class JwtTokenProvider {

    //very important to keep this secret!
    @Value("${jwt.secret}")
    private String secret;

    //generate this when user passes authentication
    public String generateJwtToken(UserPrincipal userPrincipal) {
        //claims: what user can do
        String[] claims = getClaimsForUser(userPrincipal);

        //static import of SecurityConstants allows to not use class name before every const
        return JWT.create().withIssuer(GET_ARRAYS)
                .withAudience(GET_ARRAYS_ADMINISTRATION) //for whom token is being created
                .withIssuedAt(new Date()) //when was the token genereted
                .withSubject(userPrincipal.getUsername()) //Subject is the actual user: id/username/name, usually id
                .withArrayClaim(AUTHORITIES, claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) //when will the token expire
                .sign(HMAC512(secret.getBytes())); //signature with selected alg + bytes from secret
    }

    private String[] getClaimsForUser(UserPrincipal userPrincipal) {
        return null;
    }
}
