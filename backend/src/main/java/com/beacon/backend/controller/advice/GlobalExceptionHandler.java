package com.beacon.backend.controller.advice;

import com.beacon.backend.utils.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("User Not Found");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ProblemDetail handleInvalidEmail(InvalidEmailException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid Email");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Duplicate Email");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(InvalidUsernameException.class)
    public ProblemDetail handleInvalidUsername(InvalidUsernameException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid Username");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ProblemDetail handleDuplicateUsername(DuplicateUsernameException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Duplicate Username");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPassword(InvalidPasswordException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid Password");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Authentication Failed");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(InvalidProfileAttributeException.class)
    public ProblemDetail handleInvalidProfileAttribute(InvalidProfileAttributeException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid Profile Attribute");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(InvalidProfileUuidException.class)
    public ProblemDetail handleInvalidProfileUuid(InvalidProfileUuidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid Profile UUID");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ProblemDetail handleProfileNotFound(ProfileNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Profile Not Found");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(ForbiddenAccountAccessException.class)
    public ProblemDetail handleForbiddenAccountAccess(ForbiddenAccountAccessException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Forbidden Account Access");
        pd.setDetail(ex.getMessage());
        return pd;
    }
    
    @ExceptionHandler(MessageNotFoundException.class)
    public ProblemDetail handleMessageNotFound(MessageNotFoundException ex) {
    	ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    	pd.setTitle("Message Not Found");
    	pd.setDetail(ex.getMessage());
    	return pd;
    }
    
    @ExceptionHandler(InvalidMessageAttributeException.class)
    public ProblemDetail handleInvalidMessageAttributeException(InvalidMessageAttributeException ex) {
    	ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    	pd.setTitle("Invalid Message Attribute");
    	pd.setDetail(ex.getMessage());
    	return pd;
    }
}
