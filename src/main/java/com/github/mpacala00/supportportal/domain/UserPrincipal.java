package com.github.mpacala00.supportportal.domain;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor //for passing the user to this class
public class UserPrincipal implements UserDetails {

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(this.user.getAuthoritites())
                .map(SimpleGrantedAuthority::new) //map the string to SimpleGrantedAuthority
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; //always active, never expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.user.isNotLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        //logic can be added that will set a certain time for valid credentials
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.user.isActive();
    }
}
