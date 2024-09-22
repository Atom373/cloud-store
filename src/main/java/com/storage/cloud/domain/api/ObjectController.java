package com.storage.cloud.domain.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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

import com.storage.cloud.domain.dto.FileDto;
import com.storage.cloud.domain.dto.FileUploadingResponse;
import com.storage.cloud.domain.dto.ObjectsDto;
import com.storage.cloud.domain.model.ObjectId;
import com.storage.cloud.domain.service.ObjectIdEncodingService;
import com.storage.cloud.domain.service.impl.MinioStorageService;
import com.storage.cloud.domain.utils.FileUtils;
import com.storage.cloud.domain.utils.UserDataUtils;
import com.storage.cloud.security.model.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ObjectController {

	private final MinioStorageService storageService;
	private final ObjectIdEncodingService encodingService;
	private final FileUtils fileUtils;
	private final UserDataUtils userDataUtils;
	
	@PostMapping("/upload/file")
	@ResponseStatus(HttpStatus.CREATED)
	public FileUploadingResponse uploadFile(MultipartFile file, HttpSession session,
						   					@AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		
		String objectId = storageService.save(file, currentDir, user);
		
		String percentOfUsedSpace = userDataUtils.convertToPercents(user.getUsedDiskSpace());
		String formattedUsedSpace = fileUtils.formatSize(user.getUsedDiskSpace());
		
		return new FileUploadingResponse(objectId, percentOfUsedSpace, formattedUsedSpace);
	}
	
	@GetMapping("/file/recent")
	public List<FileDto> getUsersRecentlyViewedFiles(@AuthenticationPrincipal User user) {
		return storageService.getRecentlyViewedFiles(user);
	}
	
	@GetMapping("/download/file/{encodedObjectId}") // object Id consists of bucket name and object name
    public ResponseEntity<Resource> downloadFile(@PathVariable String encodedObjectId) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		
		String filename = fileUtils.getFullFilename(objectId);
		
        Resource resource = storageService.getFileResource(objectId.bucket(), objectId.name());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachement; filename=\"" + filename + "\"")
                .body(resource);
    }
	
	@GetMapping("/open/file/{encodedObjectId}") // object Id consists of bucket name and object name
    public ResponseEntity<Resource> openFile(@PathVariable String encodedObjectId) throws IOException {
		System.out.println("in open file method: " + encodedObjectId);
		ObjectId objectId = encodingService.decode(encodedObjectId);
		
		String filename = fileUtils.getFullFilename(objectId);
		String contentType = Files.probeContentType(Paths.get(filename));
		
		storageService.updateLastViewedDate(objectId.bucket(), objectId.name());
		
        Resource resource = storageService.getFileResource(objectId.bucket(), objectId.name());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
	
	@PatchMapping("/rename/file/{encodedObjectId}")
	public String renameFile(@PathVariable String encodedObjectId,
							 @RequestParam String newFilename) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		
		String newObjectName = storageService.rename(objectId.bucket(), objectId.name(), newFilename);
		
		return encodingService.encode(objectId.bucket(), newObjectName);
	}
	
	@GetMapping("/object/all")
	public ObjectsDto getUsersObjectsFromCurrentDir(HttpSession session,
			   							 @AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		return storageService.getObjectsFrom(currentDir, user);
	}
	
	@GetMapping("/object/starred")
	public ObjectsDto getUsersStarredObjects(@AuthenticationPrincipal User user) {
		return storageService.getStarredObjects(user);
	}
	
	@GetMapping("/object/meta/{encodedObjectId}")
	public Map<String, String> getObjectMetadata(@PathVariable String encodedObjectId) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		return storageService.getObjectMeta(objectId.bucket(), objectId.name());
	}
	
	@PostMapping("/starred/add/{encodedObjectId}")
	public void addToStarred(@PathVariable String encodedObjectId) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		storageService.addToStarred(objectId.bucket(), objectId.name());
	}
	
	@PostMapping("/starred/remove/{encodedObjectId}")
	public void removeFromStarred(@PathVariable String encodedObjectId) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		storageService.removeFromStarred(objectId.bucket(), objectId.name());
	}
	
	@PostMapping("/folder")
	@ResponseStatus(HttpStatus.CREATED)
	public String createFolder(@RequestParam String foldername, HttpSession session,
							   @AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		return storageService.createFolder(currentDir, foldername, user);
	}
}
