package com.storage.cloud.security.service;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import com.storage.cloud.domain.service.StorageService;
import com.storage.cloud.security.model.User;
import com.storage.cloud.security.repository.UserRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
	
	private final StorageService storageService;
	private final UserRepo userRepo;
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest request) {
		OAuth2User auth2User = super.loadUser(request);
		
		String sub = auth2User.getAttribute("sub");
		
		Optional<User> user = userRepo.findBySub(sub);
		
		if (user.isPresent())
			return user.get();
		
		String username = auth2User.getAttribute("name");
		User newUser = new User(username, null);
		newUser.setSub(sub);
		newUser.setAttributes(auth2User.getAttributes());
		
		userRepo.save(newUser);
		
		storageService.createBucketFor(newUser);
		
		return newUser;
	}
	
}
