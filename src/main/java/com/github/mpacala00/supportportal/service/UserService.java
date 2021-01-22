package com.github.mpacala00.supportportal.service;

import com.github.mpacala00.supportportal.domain.User;
import com.github.mpacala00.supportportal.exception.domain.EmailExistsException;
import com.github.mpacala00.supportportal.exception.domain.UserNotFoundException;
import com.github.mpacala00.supportportal.exception.domain.UsernameExistsException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    User findByUsername(String username);
    User findByEmail(String email);

    User register(String firstName, String lastName, String username, String email) throws UsernameExistsException, EmailExistsException, UserNotFoundException;

    List<User> getUsers();

    //for adding user as a super admin, for example
    User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNotLocked,
                    boolean isActive, MultipartFile profileImage);

    //we need to know existing username to update its account, could also be done with user's id or just with
    //one method in interface and 2 implementations of it in the actual service
    User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail,
                    String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage);

    void resetPassword(String email);

    User updateProfileImage(String username, MultipartFile newProfileImage);
}
