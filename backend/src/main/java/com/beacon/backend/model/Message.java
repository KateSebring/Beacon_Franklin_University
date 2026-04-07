package com.beacon.backend.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class Message {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int messageId;
	
	@Column(nullable = false)
	private String messageContent;
	
	@Column(nullable = false)
	private String displayName;
	
	@Column(nullable = false)
	private String profileUUID;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "account_user_id", nullable = false)
	private AccountUser accountUserOwner;
	
	@Column(nullable = false)
	private String sender;
	
	@Column(nullable = false)
	private String receiver;
	
	@Column(nullable = false)
	private LocalDate dateSent;
	
	public Message() {}
	
	public Message(String messageContent, String profileUUID, String displayName, AccountUser accountUserOwner, String sender, String receiver, LocalDate dateSent) {
		this.messageContent = messageContent;
		this.profileUUID = profileUUID;
		this.displayName = displayName;
		// this.accountUserOwner = accountUserOwner;
		this.sender = sender;
		this.receiver = receiver;
		this.dateSent = dateSent;
	}
	
	// getters
	public int getMessageId() {
		return messageId;
	}
	
	public String getMessageContents() {
		return messageContent;
	}
	
	public String getProfileUUID() {
		return profileUUID;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public AccountUser getAccountUserOwner() {
		return accountUserOwner;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public LocalDate getDateSent() {
		return dateSent;
	}
	
	// setters
	public void setMessageContents(String newMessage) {
		this.messageContent = newMessage;
	}
	
	public void setProfileUUID(String newProfileUUID) {
		this.profileUUID = newProfileUUID;
	}
	
	public void setDisplayName(String newDisplayName) {
		this.displayName = newDisplayName;
	}
	
	public void setAccountUserOwner(AccountUser newAccountUserOwner) {
		this.accountUserOwner = newAccountUserOwner;
	}
	
	public void setSender(String newSender) {
		this.sender = newSender;
	}
	
	public void setReceiver(String newReceiver) {
		this.receiver = newReceiver;
	}
	
	public void setDateSent(LocalDate newDateSent) {
		this.dateSent = newDateSent;
	}
}
