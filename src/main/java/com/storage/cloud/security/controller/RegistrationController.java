package com.storage.cloud.security.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.storage.cloud.domain.service.StorageService;
import com.storage.cloud.security.controller.payload.RegistrationForm;
import com.storage.cloud.security.model.User;
import com.storage.cloud.security.service.RegistrationServiceFacade;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {
	
	private final RegistrationServiceFacade registrationService;
	private final StorageService storageService;
	
	@GetMapping
	public String registrationPage(Model model) {
		model.addAttribute("registrationForm", new RegistrationForm("", ""));
		return "registration";
	}
	
	@PostMapping
	public String processRegistration(@ModelAttribute RegistrationForm form,
									  HttpServletRequest request) {
		User user = registrationService.register(form);
		
		request.getSession().setAttribute(
					HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
					SecurityContextHolder.getContext()
		);
		storageService.createBucketFor(user);
		return "redirect:/main";
	}
}
