package com.beacon.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beacon.backend.model.AccountUser;
import com.beacon.backend.model.Message;

public interface MessageRepository 
	extends JpaRepository<Message, Integer> {
	List<Message> findAllByAccountUserOwnerId(int accountUserOwnerId);
	Optional<Message> findByMessageIdAndAccountUserOwner(int messageId, AccountUser accountUserOwner);
}