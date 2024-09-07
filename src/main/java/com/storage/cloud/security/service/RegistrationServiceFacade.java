package com.storage.cloud.security.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.storage.cloud.security.controller.payload.RegistrationForm;
import com.storage.cloud.security.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationServiceFacade {

	private final UserService userService;
	private final PasswordEncoder encoder;
	
	public void register(RegistrationForm form) {
		User user = new User(
				form.username(),
				encoder.encode(form.password())
		);
		userService.save(user);
	}
}
