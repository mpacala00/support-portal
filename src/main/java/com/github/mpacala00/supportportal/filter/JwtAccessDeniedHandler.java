package com.github.mpacala00.supportportal.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mpacala00.supportportal.constant.SecurityConstant;
import com.github.mpacala00.supportportal.domain.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                       AccessDeniedException exception) throws IOException, ServletException {

        HttpResponse httpResponse =
                new HttpResponse(HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED.getReasonPhrase().toUpperCase(),
                        SecurityConstant.ACCESS_DENIED_MESSAGE );

        httpServletResponse.setContentType(APPLICATION_JSON_VALUE); //import from MediaType
        httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());

        OutputStream outputStream = httpServletResponse.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputStream, httpResponse);
        outputStream.flush();
    }
}
