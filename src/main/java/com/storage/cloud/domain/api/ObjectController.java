package com.storage.cloud.domain.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.FileUploadingResponse;
import com.storage.cloud.domain.dto.ObjectsDto;
import com.storage.cloud.domain.service.FileIdEncodingService;
import com.storage.cloud.domain.service.StorageService;
import com.storage.cloud.domain.utils.FileUtils;
import com.storage.cloud.domain.utils.UserDataUtils;
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
	private final UserDataUtils userDataUtils;
	
	@PostMapping("/upload/file")
	@ResponseStatus(HttpStatus.CREATED)
	public FileUploadingResponse uploadFile(MultipartFile file, HttpSession session,
						   					@AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		
		String fileId = storageService.save(file, currentDir, user);
		
		String percentOfUsedSpace = userDataUtils.convertToPercents(user.getUsedDiskSpace());
		String formattedUsedSpace = fileUtils.formatSize(user.getUsedDiskSpace());
		
		return new FileUploadingResponse(fileId, percentOfUsedSpace, formattedUsedSpace);
	}
	
	@GetMapping("/download/file/{encodedFileId}") // file Id consists of bucket name and objectName
    public ResponseEntity<Resource> downloadFile(@PathVariable String encodedFileId) {
		String[] fileId = encodingService.decode(encodedFileId);
		
		String filename = fileUtils.getFilenameFromFileId(fileId);
		
        Resource resource = storageService.getFileResource(fileId[0], fileId[1]);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachement; filename=\"" + filename + "\"")
                .body(resource);
    }
	
	@GetMapping("/open/file/{encodedFileId}") // file Id consists of bucket name and objectName
    public ResponseEntity<Resource> openFile(@PathVariable String encodedFileId) throws IOException {
		System.out.println("in open file method: " + encodedFileId);
		String[] fileId = encodingService.decode(encodedFileId);
		
		String filename = fileUtils.getFilenameFromFileId(fileId);
		String contentType = Files.probeContentType(Paths.get(filename));
		
		storageService.updateLastViewedDate(fileId[0], fileId[1]);
		
        Resource resource = storageService.getFileResource(fileId[0], fileId[1]);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
	
	@PatchMapping("/rename/file/{encodedFileId}")
	public String renameFile(@PathVariable String encodedFileId,
							 @RequestParam String newFilename) {
		String[] fileId = encodingService.decode(encodedFileId);
		
		String newObjectName = storageService.rename(fileId[0], fileId[1], newFilename);
		
		return encodingService.encode(fileId[0], newObjectName);
	}
	
	@GetMapping("/object/all")
	public ObjectsDto getAllUsersObjects(HttpSession session,
			   							 @AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		return storageService.getAllObjectsFrom(currentDir, user);
	}
	
	@GetMapping("/object/meta/{encodedObjectId}")
	public Map<String, String> getObjectMetadata(@PathVariable String encodedObjectId) {
		String[] fileId = encodingService.decode(encodedObjectId);
		return storageService.getObjectMeta(fileId[0], fileId[1]);
	}
	
	@PostMapping("/folder")
	@ResponseStatus(HttpStatus.CREATED)
	public String createFolder(@RequestParam String foldername, HttpSession session,
							   @AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		return storageService.createFolder(currentDir, foldername, user);
	}
}
