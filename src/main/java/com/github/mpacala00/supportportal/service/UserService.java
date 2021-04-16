package com.github.mpacala00.supportportal.service;

import com.github.mpacala00.supportportal.domain.User;
import com.github.mpacala00.supportportal.exception.domain.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {

    User findByUsername(String username);
    User findByEmail(String email);

    User register(String firstName, String lastName, String username, String email) throws UsernameExistsException, EmailExistsException, UserNotFoundException;

    List<User> findAll();

    void deleteUser(String username) throws IOException;

    //for adding user as a super admin, for example
    User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNotLocked,
                    boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, WrongFileTypeException;

    //we need to know existing username to update its account, could also be done with user's id or just with
    //one method in interface and 2 implementations of it in the actual service
    User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail,
                    String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, WrongFileTypeException;

    void resetPassword(String email) throws EmailNotFoundException, MessagingException;

    User updateProfileImage(String username, MultipartFile newProfileImage) throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException, WrongFileTypeException;
}
