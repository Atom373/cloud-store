package com.storage.cloud.domain.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.storage.cloud.domain.dto.ObjectsDto;
import com.storage.cloud.domain.dto.response.FileUploadingResponse;
import com.storage.cloud.domain.dto.response.FolderRenamingResponse;
import com.storage.cloud.domain.dto.response.FolderUploadingResponse;
import com.storage.cloud.domain.dto.response.ObjectDeletingResponse;
import com.storage.cloud.domain.model.ObjectId;
import com.storage.cloud.domain.service.ObjectIdEncodingService;
import com.storage.cloud.domain.service.impl.MinioStorageService;
import com.storage.cloud.domain.utils.FileUtils;
import com.storage.cloud.domain.utils.UserDataUtils;
import com.storage.cloud.security.model.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		
		String filename = fileUtils.getFullFilename(objectId.name());
		
		InputStream inputStream = storageService.getFile(objectId.bucket(), objectId.name());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachement; filename=\"" + filename + "\"")
                .body(new InputStreamResource(inputStream));
    }
	
	@GetMapping("/open/file/{encodedObjectId}") // object Id consists of bucket name and object name
    public ResponseEntity<Resource> openFile(@PathVariable String encodedObjectId) throws IOException {
		log.debug("in open file method: " + encodedObjectId);
		ObjectId objectId = encodingService.decode(encodedObjectId);
		
		String filename = fileUtils.getFullFilename(objectId.name());
		String contentType = Files.probeContentType(Paths.get(filename));
		
		if (contentType == null) 
			contentType = "text/plain";
		
		storageService.updateLastViewedDate(objectId.bucket(), objectId.name());
		
        InputStream inputStream = storageService.getFile(objectId.bucket(), objectId.name());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(new InputStreamResource(inputStream));
    }
	
	@PatchMapping("/rename/file/{encodedObjectId}")
	public String renameFile(@PathVariable String encodedObjectId,
							 @RequestParam String newFilename) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		
		String newObjectName = storageService.renameFile(objectId.bucket(), objectId.name(), newFilename);
		
		return encodingService.encode(objectId.bucket(), newObjectName);
	}
	
	@GetMapping("/object/all")
	public ObjectsDto getUsersObjectsFromCurrentDir(HttpSession session,
			   							 			@AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		
		if (!currentDir.isEmpty())
			storageService.updateLastViewedDate(user.getBucketName(), currentDir);
		
		return storageService.getObjectsFrom(currentDir, user);
	}
	
	@GetMapping("/object/search")
	public ObjectsDto searchObjectsByName(@RequestParam String partOfName, 
										  @AuthenticationPrincipal User user) {
		return storageService.getObjectsByNameContains(partOfName, user);
	}
	
	@GetMapping("/object/starred")
	public ObjectsDto getUsersStarredObjects(@AuthenticationPrincipal User user) {
		return storageService.getStarredObjects(user);
	}
	
	@GetMapping("/object/trashed")
	public ObjectsDto getUsersTrashedObjects(@AuthenticationPrincipal User user) {
		return storageService.getTrashedObjects(user);
	}
	
	@GetMapping("/object/meta/{encodedObjectId}")
	public Map<String, String> getObjectMetadata(@PathVariable String encodedObjectId) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		return storageService.getObjectMeta(objectId.bucket(), objectId.name());
	}
	
	@DeleteMapping("/object/{encodedObjectId}")
	public ObjectDeletingResponse deleteObject(@PathVariable String encodedObjectId, 
							 				   @AuthenticationPrincipal User user) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		
		storageService.delete(objectId.bucket(), objectId.name(), user);
		
		String percentOfUsedSpace = userDataUtils.convertToPercents(user.getUsedDiskSpace());
		String formattedUsedSpace = fileUtils.formatSize(user.getUsedDiskSpace());
		
		return new ObjectDeletingResponse(percentOfUsedSpace, formattedUsedSpace);
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
	
	@PostMapping("/trash/add/{encodedObjectId}")
	public void addToTrash(@PathVariable String encodedObjectId) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		storageService.addToTrash(objectId.bucket(), objectId.name());
	}
	
	@PostMapping("/trash/remove/{encodedObjectId}")
	public void removeFromTrash(@PathVariable String encodedObjectId) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		storageService.removeFromTrash(objectId.bucket(), objectId.name());
	}
	
	@PostMapping("/folder")
	@ResponseStatus(HttpStatus.CREATED)
	public String createFolder(@RequestParam String foldername, HttpSession session,
							   @AuthenticationPrincipal User user) {
		String currentDir = (String) session.getAttribute("currentDir");
		return storageService.createFolder(currentDir, foldername, user);
	}
	
	@PostMapping("/upload/folder")
	public FolderUploadingResponse uploadFolder(@RequestParam MultipartFile[] files, HttpSession session,
			   									@AuthenticationPrincipal User user) throws Exception {
		String currentDir = (String) session.getAttribute("currentDir");
		String objectId = storageService.saveAll(files, currentDir, user);
		String linkToFolder = "/main?path=" + currentDir + fileUtils.getBaseDir(files[0]);
		
		String percentOfUsedSpace = userDataUtils.convertToPercents(user.getUsedDiskSpace());
		String formattedUsedSpace = fileUtils.formatSize(user.getUsedDiskSpace());
		
		return new FolderUploadingResponse(objectId, linkToFolder, percentOfUsedSpace, formattedUsedSpace);
	}
	
	@GetMapping("/download/folder/{encodedObjectId}")
	public ResponseEntity<Resource> downloadFolder(@PathVariable String encodedObjectId) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		
		String foldername = fileUtils.getFoldername(objectId.name());
		
		ByteArrayInputStream inputStream = storageService.getCompressedFolder(objectId.bucket(), objectId.name());
		return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachement; filename=\"" + foldername + ".zip\"")
                .body(new InputStreamResource(inputStream));
	}
	
	@PatchMapping("/rename/folder/{encodedObjectId}")
	public FolderRenamingResponse renameFolder(@PathVariable String encodedObjectId,
							   				   @RequestParam String newFoldername) {
		ObjectId objectId = encodingService.decode(encodedObjectId);
		
		String newDirectory = storageService.renameFolder(objectId.bucket(), objectId.name(), newFoldername);
		String linkToFolder = "/main?path=" + newDirectory;
		String enocdedId = fileUtils.createEncodedObjectId(objectId.bucket(), newDirectory);
		log.debug("New link is : " + linkToFolder);
		return new FolderRenamingResponse(enocdedId, linkToFolder);
	}
}