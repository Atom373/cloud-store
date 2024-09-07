package com.storage.cloud.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.storage.cloud.security.controller.payload.RegistrationForm;
import com.storage.cloud.security.service.RegistrationServiceFacade;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {
	
	private final RegistrationServiceFacade registrationService; 

	@GetMapping
	public String registrationPage(Model model) {
		model.addAttribute("registrationForm", new RegistrationForm("", ""));
		return "registration";
	}
	
	@PostMapping
	public String processRegistration(@ModelAttribute RegistrationForm form) {
		registrationService.register(form);
		return "redirect:/main";
	}
}
