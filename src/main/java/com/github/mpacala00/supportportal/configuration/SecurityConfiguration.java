package com.github.mpacala00.supportportal.configuration;

import com.github.mpacala00.supportportal.constant.SecurityConstant;
import com.github.mpacala00.supportportal.filter.JwtAccessDeniedHandler;
import com.github.mpacala00.supportportal.filter.JwtAuthenticationEntryPoint;
import com.github.mpacala00.supportportal.filter.JwtAuthorizationFilter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) //security at a method
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    JwtAuthorizationFilter filter;
    JwtAccessDeniedHandler accessDeniedHandler;
    JwtAuthenticationEntryPoint authenticationEntryPoint;
    UserDetailsService userDetailsService;
    BCryptPasswordEncoder encoder;

    @Autowired
    public SecurityConfiguration(JwtAuthorizationFilter filter, JwtAccessDeniedHandler accessDeniedHandler,
                                 JwtAuthenticationEntryPoint authenticationEntryPoint,
                                 @Qualifier("userDetailsService") UserDetailsService userDetailsService,
                                 BCryptPasswordEncoder encoder) {
        this.filter = filter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.userDetailsService = userDetailsService;
        this.encoder = encoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //telling Spring to use the overridden UserDetailsService
        auth.userDetailsService(userDetailsService).passwordEncoder(encoder);
    }

    //this method configures access to certain endpoints
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() //disable CrossSiteRequestForgery sec
                .cors() //enable CrossOrigin
                .and()
                //don't keep track of the session, in case of JWT we need to verify its validity once
                //the token expires after certain amount of time
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests().antMatchers(SecurityConstant.PUBLIC_URLS).permitAll()
                .anyRequest().authenticated()
                .and()
                //changing accessDeniedHandler to the custom implementation
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManagerBean();
    }
}
