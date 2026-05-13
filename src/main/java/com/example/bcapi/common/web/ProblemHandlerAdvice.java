package com.example.bcapi.common.web;

import com.example.bcapi.beer.domain.BeerNotFoundException;
import com.example.bcapi.beer.domain.InvalidBeerTypeException;
import com.example.bcapi.manufacturer.domain.InvalidCountryCodeException;
import com.example.bcapi.manufacturer.domain.ManufacturerNotFoundException;
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

    @ExceptionHandler(ManufacturerNotFoundException.class)
    ProblemDetail handleManufacturerNotFound(ManufacturerNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Manufacturer Not Found");
        return problem;
    }

    @ExceptionHandler(BeerNotFoundException.class)
    ProblemDetail handleBeerNotFound(BeerNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Beer Not Found");
        return problem;
    }

    @ExceptionHandler(InvalidBeerTypeException.class)
    ProblemDetail handleInvalidBeerType(InvalidBeerTypeException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle("Invalid Beer Type");
        return problem;
    }
}
