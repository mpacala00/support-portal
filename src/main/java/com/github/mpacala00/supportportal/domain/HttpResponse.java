package com.github.mpacala00.supportportal.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * creating a custom HttpResponse to avoid leaking app information during errors
 * gives more control over the api
 */

@Getter
@Setter
@NoArgsConstructor //should be removed for production
public class HttpResponse {

    private String timeStamp;
    private int httpStatusCode;
    private HttpStatus httpStatus;
    private String reason; //reasonPhrase, such as CONTINUE (in case of status code 100)
    private String message; //message to show from developer

    public HttpResponse(int httpStatusCode, HttpStatus httpStatus, String reason, String message) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss");
        timeStamp = formatter.format(now);

        this.httpStatusCode = httpStatusCode;
        this.httpStatus = httpStatus;
        this.reason = reason;
        this.message = message;
    }
}
