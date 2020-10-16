package com.github.mpacala00.supportportal.controller;

import com.github.mpacala00.supportportal.domain.User;
import com.github.mpacala00.supportportal.exception.domain.EmailNotFoundException;
import com.github.mpacala00.supportportal.exception.domain.ExceptionHandling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = {"/", "/user"}) //home mapping "/" to handle error response that is mapped to /error
//this extend will provide ExceptionHandlers
public class UserController extends ExceptionHandling {

    //this endpoint is not included in PUBLIC_URLS, so only authenticated user should be able to access this
    @GetMapping("/home")
    public String showUser() {
        return "application works";
    }

    //test exceptions
    //replace public urls to bypass auth
    @GetMapping("/exception")
    public String testException() throws EmailNotFoundException {
        throw new EmailNotFoundException("Not found");
    }

    @GetMapping
    public User showEmptyUser() {
        return new User().builder().id(1L).email("test@test.com").build();
    }
}
