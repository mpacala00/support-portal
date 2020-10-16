package com.github.mpacala00.supportportal.constant;

public class SecurityConstant {
    //psf + TAB for public static final
    //psfs + TAB for public static final String

    public static final long EXPIRATION_TIME = 432_000_000;//5 days in ms
    //whoever has the token can be instantly verified, no further checks where it came from
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";

    //issuer of the token, in this case Get Arrays, author of the course will be used
    public static final String GET_ARRAYS = "Get Arrays";
    //audience example of the token
    public static final String GET_ARRAYS_ADMINISTRATION = "User Management Portal";
    //it will hold all of the authorities of the user
    public static final String AUTHORITIES = "authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to log in to access this page";
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
    //options is a request that checks what server will accept or not
    public static final String OPTIONS_HTTP_METHOD = "OPTIONS";

    //URLs to be accessed without any security checks
    // /** means anything after this endpoint is accessible as well
    //not necessary but makes things clearer
    public static final String[] PUBLIC_URLS = { "/user/login", "/user/register", "/user/reset-password/**",
        "/user/image/**"};
    //comment above and uncomment below to remove authentication steps
    //public static final String[] PUBLIC_URLS = { "/**"};
}
