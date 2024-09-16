package com.storage.cloud.domain.api;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.ObjectsDto;
import com.storage.cloud.domain.service.FileIdEncodingService;
import com.storage.cloud.domain.service.StorageService;
import com.storage.cloud.domain.utils.FileUtils;
import com.storage.cloud.security.model.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ObjectController {

	private final StorageService storageService;
	private final FileIdEncodingService encodingService;
	private final FileUtils fileUtils;
	
	@PostMapping("/file/upload")
	@ResponseStatus(HttpStatus.CREATED)
	public void uploadFile(MultipartFile file, HttpSession session,
						   @AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		storageService.save(file, currentDir, user);
	}
	
	@GetMapping("/download/file/{encodedFileId}") // file Id consists of bucket name and objectName
    public ResponseEntity<Resource> downloadFile(@PathVariable String encodedFileId) {
		String[] fileId = encodingService.decode(encodedFileId);
		
		String filename = fileUtils.getFilenameFromFileId(fileId);
		
        Resource resource = storageService.getFileResource(fileId[0], fileId[1]);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
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
