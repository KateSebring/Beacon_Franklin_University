package com.beacon.backend.utils.exception;

public class InvalidMessageAttributeException extends RuntimeException {
	public InvalidMessageAttributeException() {
		super("A message attribute is invalid");
	}
}
