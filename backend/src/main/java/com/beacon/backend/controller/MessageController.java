package com.beacon.backend.controller;

import java.util.List;
import com.beacon.backend.dto.message.*;
import com.beacon.backend.model.Message;
import com.beacon.backend.service.MessageService;
import com.beacon.backend.utils.adapter.MessageMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:3000")
public class MessageController {
	private final MessageService messageService;
	private final MessageMapper messageMapper;
	
	public MessageController(MessageService messageService, MessageMapper messageMapper) {
		this.messageService = messageService;
		this.messageMapper = messageMapper;
	}
	
	@GetMapping
	public ResponseEntity<List<MessageResponse>> getAllMessages(Authentication authentication) {
		String currentUsername = authentication.getName();
		List<Message> userMessages = messageService.getAllMessages(currentUsername);
		return ResponseEntity.ok(messageMapper.allToMessageResponse(userMessages));
	}
	
	// delete message based on its ID
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMessage(@PathVariable int id, Authentication authentication) {
		String currentUsername = authentication.getName();
		messageService.removeMessageById(id, currentUsername);
		return ResponseEntity.noContent().build();
	}
}