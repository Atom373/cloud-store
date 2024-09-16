package com.storage.cloud.security.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.storage.cloud.security.exception.UserAlreadyExistsException;
import com.storage.cloud.security.model.User;
import com.storage.cloud.security.repository.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private static final String USERNAMES_KEY = "usernames";
	
	private final StringRedisTemplate redisTemplate;
	private final UserRepo userRepo;
	
	public boolean isUnique(String username) {
		boolean isMember = redisTemplate.opsForSet().isMember(USERNAMES_KEY, username);
		System.out.println(username + " is member = " + isMember);
		return !isMember;
	}

	public void save(User user) {
		if (!this.isUnique(user.getUsername())) {
			throw new UserAlreadyExistsException();
		}
		userRepo.save(user);
		redisTemplate.opsForSet().add(USERNAMES_KEY, user.getUsername());
	}
}