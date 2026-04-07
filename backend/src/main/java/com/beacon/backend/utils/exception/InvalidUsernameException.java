package com.beacon.backend.utils.exception;

public class InvalidUsernameException extends RuntimeException {
	public InvalidUsernameException(String username) {
        super("Invalid username. Please try again.");
    }
}
