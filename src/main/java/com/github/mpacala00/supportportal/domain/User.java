package com.github.mpacala00.supportportal.domain;

import java.io.Serializable;
import java.time.LocalDate;

public class User implements Serializable {

    private Long id; //id for database
    private String userId; //id for display

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email; //for signing-up or renewing password
    private String profileImageUrl;

    private LocalDate joinDate;
    private LocalDate lastLoginDate;
    private LocalDate lastLoginDateDisplay;

    //splitting authorization into roles and authorities makes the system more modular
    private String[] roles; //ROLE_USER, ROLE_ADMIN etc
    private String[] authoritites; //delete, read, update etc

    private boolean isActive; //activate acc after email confirmation
    private boolean isLocked; //for blocking accounts functionality
}
