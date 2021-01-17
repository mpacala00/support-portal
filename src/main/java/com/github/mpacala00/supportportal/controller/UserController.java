package com.github.mpacala00.supportportal.controller;

import com.github.mpacala00.supportportal.domain.User;
import com.github.mpacala00.supportportal.exception.domain.*;
import com.github.mpacala00.supportportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = {"/", "/user"}) //home mapping "/" to handle error response that is mapped to /error
//this extend will provide ExceptionHandlers
public class UserController extends ExceptionHandling {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user)
            throws UsernameExistsException, UserNotFoundException, EmailExistsException {
        User registeredUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(),
                user.getEmail());

        return new ResponseEntity<User>(registeredUser, HttpStatus.OK);
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
