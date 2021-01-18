package com.github.mpacala00.supportportal.service;

import com.github.mpacala00.supportportal.domain.User;
import com.github.mpacala00.supportportal.domain.UserPrincipal;
import com.github.mpacala00.supportportal.enumeration.Role;
import com.github.mpacala00.supportportal.exception.domain.EmailExistsException;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

import static com.github.mpacala00.supportportal.constant.UserImplConstant.*;

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

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username);
        if(user == null) {
            LOGGER.error("User not found by username: "+username);
            throw new UsernameNotFoundException(NO_USER_FOUND+username);
        } else {
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
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User register(String firstName,
                         String lastName,
                         String username,
                         String email) throws UsernameExistsException, EmailExistsException, UserNotFoundException {
        //first arg empty because it is a registration
        validateUsernameAndEmail(StringUtils.EMPTY, email, email);

        String password = generatePassword();
        String encodedPassword = encodePassword(password);
        User user = User.builder().firstName(firstName).lastName(lastName).username(username)
                .email(email).joinDate(LocalDate.now()).password(encodedPassword).isActive(true)
                .isNotLocked(true).role(Role.ROLE_USER.name()).authoritites(Role.ROLE_USER.getAuthorities())
                .profileImageUrl(getTempImageUrl()).build();

        userRepository.save(user);
        LOGGER.info("New user password: " + password);
        return user;
    }

    private String getTempImageUrl() {
        //whatever the base url is of the site
        //http://localhost:8080 in case of running the app locally
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(TEMP_IMG_PATH).toUriString();
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
