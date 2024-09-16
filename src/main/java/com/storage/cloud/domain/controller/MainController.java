package com.storage.cloud.domain.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.storage.cloud.domain.utils.BreadcrumbUtils;
import com.storage.cloud.security.model.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainController {
	
	private final BreadcrumbUtils breadcrumbUtils;
	
	@GetMapping
	public String mainPage(@RequestParam(defaultValue = "") String path,
						   Model model, HttpSession session, 
						   @AuthenticationPrincipal User user) {
		System.out.println(path);
		session.setAttribute("currentDir", path);
		
		model.addAttribute("breadcrumbs", breadcrumbUtils.getBreadcrumbsFor(path));
		
		return "main";
	}

}
