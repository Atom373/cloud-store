package com.storage.cloud.security.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.storage.cloud.security.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/username/is-unique")
@RequiredArgsConstructor
public class UsernameValidationController {

	private final UserService userService;
	
	@PostMapping
	public boolean isUnique(@RequestParam String username) {
		return userService.isUnique(username);
	}
	
}
