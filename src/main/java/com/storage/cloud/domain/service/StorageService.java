package com.storage.cloud.domain.service;


import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.ObjectsDto;
import com.storage.cloud.security.model.User;

public interface StorageService {

	ObjectsDto getAllObjectsFrom(String dirName, User user);
	
	Resource getFileResource(String bucketName, String objectName);
	
	Map<String, String> getObjectMeta(String bucketName, String objectName);
	
	void createBucketFor(User user);
	
	String createFolder(String dirName, String foldername, User user);
	
	String save(MultipartFile file, String directory, User user);
	
	void rename(String oldFilename, String newFilename, User user);
	
	void delete(String filename, User user);
}
