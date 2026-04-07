package com.beacon.backend.dto.message;

import java.time.LocalDate;

public record MessageResponse(
	String messageContent,
	String displayName,
	String profileUUID,
	String sender,
	String receiver,
	LocalDate dateSent
) {}