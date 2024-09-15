package com.storage.cloud.domain.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.ObjectsDto;
import com.storage.cloud.domain.service.StorageService;
import com.storage.cloud.security.model.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ObjectController {

	private final StorageService storageService;
	
	@PostMapping("/file/upload")
	@ResponseStatus(HttpStatus.CREATED)
	public void uploadFile(MultipartFile file, HttpSession session,
						   @AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		storageService.save(file, currentDir, user);
	}
	
	@GetMapping("/object/all")
	public ObjectsDto getAllUsersObjects(HttpSession session,
			   								@AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		return storageService.getAllObjectsFrom(currentDir, user);
	}
	
	@PostMapping("/folder")
	@ResponseStatus(HttpStatus.CREATED)
	public String createFolder(@RequestParam String foldername, HttpSession session,
							   @AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		return storageService.createFolder(currentDir, foldername, user);
	}
}
