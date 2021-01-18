package com.github.mpacala00.supportportal.controller;

import com.github.mpacala00.supportportal.domain.User;
import com.github.mpacala00.supportportal.domain.UserPrincipal;
import com.github.mpacala00.supportportal.exception.domain.*;
import com.github.mpacala00.supportportal.service.UserService;
import com.github.mpacala00.supportportal.utility.JwtTokenProvider;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.github.mpacala00.supportportal.constant.SecurityConstant.JWT_TOKEN_HEADER;

@RestController
@RequestMapping(path = {"/", "/user"}) //home mapping "/" to handle error response that is mapped to /error
//this extend will provide ExceptionHandlers
public class UserController extends ExceptionHandling {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @NotNull User user)
            throws UsernameExistsException, UserNotFoundException, EmailExistsException {
        User registeredUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(),
                user.getEmail());

        return new ResponseEntity<User>(registeredUser, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody @NotNull User user) {

        authenticate(user.getUsername(), user.getPassword());
        User loginUser = userService.findByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);

        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        //args are type of resEntity, headers, status
        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
    }

    //test exceptions
    //replace public urls in SecurityConstant to bypass auth
    @GetMapping("/exception")
    public String testException() throws EmailNotFoundException {
        throw new EmailNotFoundException("Not found");
    }

    private void authenticate(String username, String password) {
        Authentication auth = new UsernamePasswordAuthenticationToken(username, password);
        authenticationManager.authenticate(auth);
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, tokenProvider.generateJwtToken(userPrincipal));
        return headers;
    }
}
