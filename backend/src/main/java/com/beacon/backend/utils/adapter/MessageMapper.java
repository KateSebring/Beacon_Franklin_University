package com.beacon.backend.utils.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import com.beacon.backend.dto.message.MessageResponse;
import com.beacon.backend.model.Message;

@Component
public class MessageMapper {
	public MessageResponse toMessageResponse(Message message) {
		if(message == null) {
			return null;
		}
		
		return new MessageResponse(
				message.getMessageContents(),
				message.getDisplayName(),
				message.getProfileUUID(),
				message.getSender(),
				message.getReceiver(),
				message.getDateSent());
	}
	
	public List<MessageResponse> allToMessageResponse(List<Message> messages) {
		return messages.stream()
				.map(this::toMessageResponse)
				.collect(Collectors.toList());
	}
}
