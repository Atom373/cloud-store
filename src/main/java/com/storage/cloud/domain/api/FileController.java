package com.storage.cloud.domain.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.FileDto;
import com.storage.cloud.domain.service.StorageService;
import com.storage.cloud.security.model.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

	private final StorageService storageService;
	
	@PostMapping("/upload")
	@ResponseStatus(HttpStatus.CREATED)
	public void uploadFile(MultipartFile file, HttpSession session,
						   @AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		System.out.println("To save: " + currentDir + file.getOriginalFilename());
		storageService.save(file, currentDir, user);
	}
	
	@GetMapping("/all")
	public List<FileDto> getAllUsersFiles(HttpSession session,
			   							  @AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		return storageService.getAllFilesFrom(currentDir, user);
	}
}
