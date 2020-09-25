package com.github.mpacala00.supportportal.filter;

import com.github.mpacala00.supportportal.utility.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.github.mpacala00.supportportal.constant.SecurityConstant.OPTIONS_HTTP_METHOD;
import static com.github.mpacala00.supportportal.constant.SecurityConstant.TOKEN_PREFIX;

/**
 * used to authorize any request
 * the filter will launch once every time there is a request
 */

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private JwtTokenProvider provider;

    @Autowired
    public JwtAuthorizationFilter(JwtTokenProvider provider) {
        this.provider = provider;
    }

    //this method will be called when a request is made
    //check if everything is valid basically
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        //method is POST, GET, DELETE etc.
        //in this case OPTIONS method is allowed; it returns other allowed options from the server like GET etc.
        //https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/OPTIONS
        if(request.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD)) {
            response.setStatus(HttpStatus.OK.value());
        } else {
            //if the request is not OPTIONS:
            //Authorization header means that request contains the credentials to authorize the user
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if(authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            //substring method starts index with 1, so using .length() here is ok
            //used to remove "Bearer " from the header
            String token = authorizationHeader.substring(TOKEN_PREFIX.length());
            String username = provider.getTokenSubject(token);
            if( provider.isTokenValid(username, token) &&
                    SecurityContextHolder.getContext().getAuthentication() == null ) {
                //getAuthentication()
                //Obtains the currently authenticated principal, or an authentication request token

                //get the authorities from token
                List<GrantedAuthority> authorityList = provider.getAuthorities(token);
                Authentication authentication = provider.getAuthentication(username, authorityList, request);

                //setting user authentication
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                //very, very important to clear the context if something with the token is not valid
                //or the context already possess an authentication
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
