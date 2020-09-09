package com.github.mpacala00.supportportal.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * creating a custom HttpResponse to avoid leaking app information during errors
 * gives more control over the api
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor //should be removed for production
public class HttpResponse {

    private int httpStatusCode;
    private HttpStatus httpStatus;
    private String reason; //reasonPhrase, such as CONTINUE (in case of status code 100)
    private String message; //message to show from developer
}
