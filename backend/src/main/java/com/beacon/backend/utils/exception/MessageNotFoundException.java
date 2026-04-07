package com.beacon.backend.utils.exception;

public class MessageNotFoundException extends RuntimeException {
	public MessageNotFoundException() {
		super("Message not found.");
	}
}
