package com.storage.cloud.domain.service;


import java.io.InputStream;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.ObjectsDto;
import com.storage.cloud.security.model.User;

public interface StorageService {

	ObjectsDto getObjectsFrom(String directory, User user);
	
	InputStream getFile(String bucketName, String objectName);
	
	Map<String, String> getObjectMeta(String bucketName, String objectName);
	
	void createBucketFor(User user);
	
	String createFolder(String directory, String foldername, User user);
	
	String save(MultipartFile file, String directory, User user);
	
	String rename(String bucket, String objectName, String newFilename);
	
	void updateLastViewedDate(String bucket, String objectName);
	
	void delete(String bucket, String objectName, User user);
}
