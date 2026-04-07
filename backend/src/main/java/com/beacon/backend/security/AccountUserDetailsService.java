package com.beacon.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.beacon.backend.model.AccountUser;
import com.beacon.backend.repository.AccountUserRepository;

@Service
public class AccountUserDetailsService implements UserDetailsService {
	
	@Autowired
	private AccountUserRepository repository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AccountUser user = repository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Username not found in repository"));
		return new AccountUserDetails(user);
	}

	public UserDetails loadUserById(int userId) throws UsernameNotFoundException {
		AccountUser user = repository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User id not found in repository"));
		return new AccountUserDetails(user);
	}
}
