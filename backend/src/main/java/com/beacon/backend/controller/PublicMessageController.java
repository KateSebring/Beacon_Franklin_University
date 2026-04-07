package com.beacon.backend.controller;

import java.util.List;
import com.beacon.backend.dto.message.*;
import com.beacon.backend.model.Message;
import com.beacon.backend.service.MessageService;
import com.beacon.backend.utils.adapter.MessageMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "http://localhost:3000")
public class PublicMessageController {
	private final MessageService messageService;
	private final MessageMapper messageMapper;
	
	public PublicMessageController(MessageService messageService, MessageMapper messageMapper) {
		this.messageService = messageService;
		this.messageMapper = messageMapper;
	}
	
	@PostMapping
	public ResponseEntity<MessageResponse> createMessage(@RequestBody MessageRequest request) {
		Message message = messageService.createMessage(request);
		MessageResponse messageResponse = messageMapper.toMessageResponse(message);
		return ResponseEntity.ok(messageResponse);
	}
}