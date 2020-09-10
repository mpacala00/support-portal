package com.github.mpacala00.supportportal.controller;

import com.github.mpacala00.supportportal.domain.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @GetMapping
    public User showEmptyUser() {
        return new User().builder().id(1L).email("test@test.com").build();
    }
}
