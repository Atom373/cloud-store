package com.storage.cloud.domain.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.storage.cloud.domain.utils.BreadcrumbUtils;
import com.storage.cloud.domain.utils.FileUtils;
import com.storage.cloud.domain.utils.UserDataUtils;
import com.storage.cloud.security.model.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainController {
	
	private final BreadcrumbUtils breadcrumbUtils;
	private final FileUtils fileUtils; 
	private final UserDataUtils userDataUtils;
	
	@GetMapping
	public String mainPage(@RequestParam(defaultValue = "") String path,
						   Model model, HttpSession session, 
						   @AuthenticationPrincipal User user) {
		System.out.println(path);
		session.setAttribute("currentDir", path);
		
		long usedSpace = user.getUsedDiskSpace();
		System.out.println(user);
		System.out.println(userDataUtils.convertToPercents(usedSpace));
		
		model.addAttribute("breadcrumbs", breadcrumbUtils.getBreadcrumbsFor(path));
		model.addAttribute("percentOfUsedSpace", userDataUtils.convertToPercents(usedSpace));
		model.addAttribute("usedDiskSpace", fileUtils.formatSize(usedSpace)); 
		
		return "main";
	}

}
