package com.github.mpacala00.supportportal.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.github.mpacala00.supportportal.domain.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    //for accessing information; check user's authorities
    public List<GrantedAuthority> getAuthorities(String token) {
        String[] claims = getClaimsFromToken(token);
        return Stream.of(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    //returning list of user claims from the token
    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = getJWTVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    //get the JWTVerifier by passing in the algorithm used to encode jwt with secret passed as arg
    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = HMAC512(secret);
            verifier = JWT.require(algorithm).withIssuer(GET_ARRAYS).build();
        }
        catch(JWTVerificationException exception) {
            //throwing new exception to not expose inner workings of the app
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }

        return verifier;
    }

    private String[] getClaimsForUser(UserPrincipal userPrincipal) {
        List<String> authorities = new ArrayList<>();
        for(GrantedAuthority grantedAuthority : userPrincipal.getAuthorities()) {
            authorities.add(grantedAuthority.getAuthority());
        }

        return authorities.toArray(new String[authorities.size()]);
    }
}
