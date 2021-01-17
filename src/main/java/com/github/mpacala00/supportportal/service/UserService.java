package com.github.mpacala00.supportportal.service;

import com.github.mpacala00.supportportal.domain.User;
import com.github.mpacala00.supportportal.exception.domain.EmailExistsException;
import com.github.mpacala00.supportportal.exception.domain.UserNotFoundException;
import com.github.mpacala00.supportportal.exception.domain.UsernameExistsException;

import java.util.List;

public interface UserService {

    User findByUsername(String username);
    User findByEmail(String email);

    User register(String firstName, String lastName, String username, String email) throws UsernameExistsException, EmailExistsException, UserNotFoundException;

    List<User> getUsers();
}
