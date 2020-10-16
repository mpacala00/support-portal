package com.github.mpacala00.supportportal.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mpacala00.supportportal.constant.SecurityConstant;
import com.github.mpacala00.supportportal.domain.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * This class replaces the default implementation of Spring Security 403 Forbidden
 * It gets triggered when user does not have the required authentication
 */

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException exception)
            throws IOException {

        HttpResponse httpResponse =
                new HttpResponse(HttpStatus.FORBIDDEN.value(),
                        HttpStatus.FORBIDDEN,
                        HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase(),
                        SecurityConstant.FORBIDDEN_MESSAGE);

        response.setContentType(APPLICATION_JSON_VALUE); //import from MediaType
        response.setStatus(HttpStatus.FORBIDDEN.value());

        OutputStream outputStream = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputStream, httpResponse);
        outputStream.flush();

        //response.sendError(403, "Access Denied");
    }
}
