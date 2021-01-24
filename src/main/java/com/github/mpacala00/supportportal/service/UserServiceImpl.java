package com.github.mpacala00.supportportal.service;

import com.github.mpacala00.supportportal.domain.User;
import com.github.mpacala00.supportportal.domain.UserPrincipal;
import com.github.mpacala00.supportportal.enumeration.Role;
import com.github.mpacala00.supportportal.exception.domain.EmailExistsException;
import com.github.mpacala00.supportportal.exception.domain.EmailNotFoundException;
import com.github.mpacala00.supportportal.exception.domain.UserNotFoundException;
import com.github.mpacala00.supportportal.exception.domain.UsernameExistsException;
import com.github.mpacala00.supportportal.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static com.github.mpacala00.supportportal.constant.FileConstant.*;
import static com.github.mpacala00.supportportal.constant.UserImplConstant.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

//@Slf4j can be used instead of the way that was used below
@Service
//allows to manage transaction, used for propagation and to separate transaction management
//(rollback in case of failure) from business logic
//it puts transactions on saving, updating, deleting etc. to db
@Transactional
@Qualifier("userDetailsService") //name of the bean
public class UserServiceImpl implements UserService, UserDetailsService {

    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        User user = userRepository.findByUsername(username);
        if(user == null) {
            LOGGER.error("User not found by username: "+username);
            throw new UsernameNotFoundException(NO_USER_FOUND+username);
        } else {
            //if the account will be locked at this stage returning userPrincipal will not give authorization
            validateLoginAttempt(user);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(LocalDate.now());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOGGER.info("Returning found user: "+username);

            return userPrincipal;
        }
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User addNewUser(String firstName, String lastName, String username, String email, String role,
                           boolean isNotLocked, boolean isActive, MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {

        validateUsernameAndEmail(StringUtils.EMPTY, username, email);

        String password = generatePassword();
        User user = User.builder()
                .userId(generateUserId()) //userId is the one that will be displayed
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .email(email)
                .password(encodePassword(password))
                .role(getRoleEnumName(role).name())
                .authoritites(getRoleEnumName(role).getAuthorities())
                .profileImageUrl(getTempImageUrl(username))
                .isNotLocked(isNotLocked)
                .isActive(isActive)
                .build();
        userRepository.save(user);
        saveProfileImage(user, profileImage);
        return user;
    }

    @Override
    public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername,
                           String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {

        User currentUser = validateUsernameAndEmail(currentUsername, newUsername, newEmail);
        //dont check if currentUser is null, exception will be thrown in such case
        currentUser.setFirstName(newFirstName);
        currentUser.setLastName(newLastName);
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setRole(getRoleEnumName(role).name());
        currentUser.setAuthoritites(getRoleEnumName(role).getAuthorities());
        currentUser.setNotLocked(isNotLocked);
        currentUser.setActive(isActive);

        userRepository.save(currentUser);
        saveProfileImage(currentUser, profileImage);
        return currentUser;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String email) throws EmailNotFoundException, MessagingException {
        User user = findByEmail(email);
        if(user != null) {
            String password = generatePassword();
            user.setPassword(encodePassword(password));
            userRepository.save(user);
            emailService.sentNewPasswordEmail(user.getFirstName(), password, user.getEmail());
        } else {
            throw new EmailNotFoundException(NOT_FOUND_BY_EMAIL + email);
        }
    }

    @Override
    public User updateProfileImage(String username, MultipartFile newProfileImage)
            throws UserNotFoundException, UsernameExistsException, EmailExistsException, IOException {
        User user = validateUsernameAndEmail(username, null, null);
        saveProfileImage(user, newProfileImage);
        return user;
    }

    @Override
    public User register(String firstName, String lastName, String username, String email)
            throws UsernameExistsException, EmailExistsException, UserNotFoundException {
        //first arg empty because it is a registration
        validateUsernameAndEmail(StringUtils.EMPTY, email, email);

        String password = generatePassword();
        User user = User.builder().userId(generateUserId()).firstName(firstName).lastName(lastName).username(username)
                .email(email).joinDate(LocalDate.now()).password(encodePassword(password)).isActive(true)
                .isNotLocked(true).role(Role.ROLE_USER.name()).authoritites(Role.ROLE_USER.getAuthorities())
                .profileImageUrl(getTempImageUrl(username)).build();

        userRepository.save(user);
        try {
            emailService.sentNewPasswordEmail(firstName, password, email);
        } catch (MessagingException e) {
            LOGGER.error("An error occured while sending e-mail containing password:");
            e.printStackTrace();
        }

        LOGGER.info("New user password: " + password);
        return user;
    }

    private void validateLoginAttempt(User user) {
        if(user.isNotLocked()) {
            user.setNotLocked(!loginAttemptService.maxAttemptsReached(user.getUsername())); //lock the acc
        } else {
            //remove from logging cache cuz the account is locked
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    //this method needs to be well optimized, accessing file systems are time consuming
    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
        if(profileImage != null) {
            Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
            if(!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                LOGGER.info(DIR_CREATED + userFolder);
            }
            //delete the old image file
            Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + DOT + JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() + DOT + JPG_EXTENSION),
                    REPLACE_EXISTING); //REPLACE_EXISTING is good enough if you dont want to deleteIfExists()
            user.setProfileImageUrl(getImageUrl(user.getUsername()));
            LOGGER.info(FILE_SAVED + profileImage.getOriginalFilename());
        }
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    private String getTempImageUrl(String username) {
        //whatever the base url is of the site
        //http://localhost:8080 in case of running the app locally
        //adding the username to the path so we can send it to robohash and get a unique profile image
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(TEMP_IMG_PATH + username).toUriString();
    }

    //location of the image of specified user
    private String getImageUrl(String username) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(USER_IMAGE_PATH + username + FORWARD_SLASH + username + DOT + JPG_EXTENSION).toUriString();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10); //nums and letters
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10); //10 random nums
    }

    //3 params because it will be used for registering or updating user information
    //"new" fields are for updating info
    private User validateUsernameAndEmail(String currentUsername,
                                          String newUsername,
                                          String newEmail)
            throws UserNotFoundException, UsernameExistsException, EmailExistsException {

        User userByNewUsername = findByUsername(newUsername);
        User userByNewEmail = findByEmail(newEmail);
        //first if for updating existing user, else after this one is for registration
        if(StringUtils.isNotBlank(currentUsername)) {
            User currentUser = findByUsername(currentUsername);
            if(currentUser == null) {
                throw new UserNotFoundException(NO_USER_FOUND + currentUsername);
            }

            //check if user is not null and it's ID is unique
            if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistsException(USERNAME_ALREADY_TAKEN);
            }

            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistsException(EMAIL_ALREADY_TAKEN);
            }

            return currentUser;
        } else {
            if(userByNewUsername != null) {
                throw new UsernameExistsException(USERNAME_ALREADY_TAKEN);
            }

            if(userByNewEmail != null) {
                throw new EmailExistsException(EMAIL_ALREADY_TAKEN);
            }

            return null;
        }
    }
}
