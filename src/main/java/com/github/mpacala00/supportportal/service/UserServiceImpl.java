package com.github.mpacala00.supportportal.service;

import com.github.mpacala00.supportportal.domain.User;
import com.github.mpacala00.supportportal.domain.UserPrincipal;
import com.github.mpacala00.supportportal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;

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

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username);
        if(user == null) {
            LOGGER.error("User not found by username: "+username);
            throw new UsernameNotFoundException("User not found by username: "+username);
        } else {
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(LocalDate.now());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOGGER.info("Returning found user: "+username);

            return userPrincipal;
        }
    }
}
