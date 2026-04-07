package com.beacon.backend.utils.adapter;

import com.beacon.backend.dto.user.UserResponse;
import com.beacon.backend.model.AccountUser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccountUserMapper {
    public UserResponse toUserResponse(AccountUser user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getDOB(),
                user.getEmail()
        );
    }

    public List<UserResponse> allToUserResponse(List<AccountUser> users) {
        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }
}
