package com.beacon.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.beacon.backend.dto.message.MessageRequest;
import com.beacon.backend.model.AccountUser;
import com.beacon.backend.model.Message;
import com.beacon.backend.repository.AccountUserRepository;
import com.beacon.backend.repository.MessageRepository;
import com.beacon.backend.utils.exception.*;

@Service
public class MessageService {
	private final MessageRepository messageRepository;
	private final ProfileService profileService;
	private final AccountUserRepository accountUserRepository;
	
	public MessageService(MessageRepository messageRepository, ProfileService profileService, AccountUserRepository accountUserRepository) {
		this.messageRepository = messageRepository;
		this.profileService = profileService;
		this.accountUserRepository = accountUserRepository;
	}
	
	// checks to ensure user exists
	private AccountUser resolveAuthenticatedUser(String username) {
		if (username == null || username.isBlank()) {
			throw new InvalidUsernameException(username);
		}
		
		String normalizedUsername = username.trim();
		
		return accountUserRepository.findByUsername(normalizedUsername)
				.orElseThrow(() -> new InvalidUsernameException(normalizedUsername));
	}
	
	// ensures user is a valid user
	// then returns the list of messages associated with their account
	public List<Message> getAllMessages(String username) {
		AccountUser currentUser = resolveAuthenticatedUser(username);
		return messageRepository.findAllByAccountUserOwnerId(currentUser.getId());
	}
	
	// creates a message based on MessageRequest passed in
	// validates that all parts of the message are not invalid
	// then creates and saves a new message object
	public Message createMessage(MessageRequest request) {
		validateRequest(request);
		
		AccountUser foundUser = profileService.findProfileOwnerByUUID(request.profileUUID());
		
		Message message = new Message();
		message.setDisplayName(request.displayName());
		message.setMessageContents(request.messageContent());
		message.setDateSent(request.dateSent());
		message.setProfileUUID(request.profileUUID());
		message.setReceiver(request.receiver());
		message.setSender(request.sender());
		message.setAccountUserOwner(foundUser);
		
		return messageRepository.save(message);
	}
	
	// ensures user is a valid user
	// finds a message by the ID of the message and the owner
	// then removes the message from the repository
	public void removeMessageById(int id, String username) {
		AccountUser currentUser = resolveAuthenticatedUser(username);
		Message foundMessage = messageRepository.findByMessageIdAndAccountUserOwner(id, currentUser).orElseThrow(MessageNotFoundException::new);
		int messageId = foundMessage.getMessageId();
		messageRepository.deleteById(messageId);
	}
	
	public void validateRequest(MessageRequest request) {
		String messageContent = request.messageContent();
		String displayName = request.displayName();
		String profileUUID = request.profileUUID();
		String sender = request.sender();
		String receiver = request.receiver();
		LocalDate dateSent = request.dateSent();
		
		if(messageContent == null || messageContent.isBlank() || 
				displayName == null || displayName.isBlank() || 
				profileUUID == null || profileUUID.isBlank() || 
				sender == null || sender.isBlank() || 
				receiver == null || receiver.isBlank() || 
				dateSent == null) {
			throw new InvalidMessageAttributeException();
		}
	}
}