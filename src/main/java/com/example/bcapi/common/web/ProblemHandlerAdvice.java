package com.example.bcapi.common.web;

import com.example.bcapi.manufacturer.domain.InvalidCountryCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ProblemHandlerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidCountryCodeException.class)
    ProblemDetail handleInvalidCountryCode(InvalidCountryCodeException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle("Invalid Country Code");
        return problem;
    }
}
