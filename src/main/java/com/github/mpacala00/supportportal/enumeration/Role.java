package com.github.mpacala00.supportportal.enumeration;

import com.github.mpacala00.supportportal.constant.Authority;

public enum Role {

    ROLE_USER(Authority.USER_AUTHORITIES),
    ROLE_HR(Authority.USER_AUTHORITIES),
    ROLE_MANAGER(Authority.USER_AUTHORITIES),
    ROLE_ADMIN(Authority.USER_AUTHORITIES),
    ROLE_SUPER_ADMIN(Authority.USER_AUTHORITIES);

    String[] authorities;

    Role(String... authorities) { //Object... <- can be either null, one object or an array of objects
        this.authorities = authorities;
    }

    public String[] getAuthorities() {
        return authorities;
    }
}