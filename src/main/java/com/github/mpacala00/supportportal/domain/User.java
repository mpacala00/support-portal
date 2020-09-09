package com.github.mpacala00.supportportal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data //getters & setters, toString, equalsAndHashCode, requiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
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
    private boolean isNotLocked; //for blocking accounts functionality
}
