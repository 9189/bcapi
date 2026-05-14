package com.example.bcapi.common.web;

import com.example.bcapi.beer.domain.BeerNotFoundException;
import com.example.bcapi.beer.domain.InvalidBeerTypeException;
import com.example.bcapi.manufacturer.domain.ManufacturerNotFoundException;
import com.example.bcapi.manufacturer.domain.InvalidCountryCodeException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ProblemHandlerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Access Denied");
        return problem;
    }

    @ExceptionHandler(InvalidCountryCodeException.class)
    ProblemDetail handleInvalidCountryCode(InvalidCountryCodeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle("Invalid Country Code");
        return problem;
    }

    @ExceptionHandler(ManufacturerNotFoundException.class)
    ProblemDetail handleManufacturerNotFound(ManufacturerNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Manufacturer Not Found");
        return problem;
    }

    @ExceptionHandler(BeerNotFoundException.class)
    ProblemDetail handleBeerNotFound(BeerNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Beer Not Found");
        return problem;
    }

    @ExceptionHandler(InvalidBeerTypeException.class)
    ProblemDetail handleInvalidBeerType(InvalidBeerTypeException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
        problem.setTitle("Invalid Beer Type");
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        String detail = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(java.util.stream.Collectors.joining(", "));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Invalid Parameter");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Parameter");
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrityViolation() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "A record with the same unique fields already exists.");
        problem.setTitle("Conflict");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        problem.setTitle("Internal Server Error");
        return problem;
    }
}
