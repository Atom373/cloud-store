package com.storage.cloud.domain.service;


import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.ObjectsDto;
import com.storage.cloud.security.model.User;

public interface StorageService {

	ObjectsDto getAllObjectsFrom(String dirName, User user);
	
	void createBucketFor(User user);
	
	String createFolder(String dirName, String foldername, User user);
	
	void save(MultipartFile file, String directory, User user);
	
	void rename(String oldFilename, String newFilename, User user);
	
	void delete(String filename, User user);
}
