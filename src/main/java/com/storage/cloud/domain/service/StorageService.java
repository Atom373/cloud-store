package com.storage.cloud.domain.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.FileDto;
import com.storage.cloud.security.model.User;

public interface StorageService {

	List<FileDto> getAllFilesFrom(String dirName, User user);
	
	void createBucketFor(User user);
	
	void save(MultipartFile file, String directory, User user);
	
	void rename(String oldFilename, String newFilename, User user);
	
	void makeDirectory(String dirName, User user);
	
	void delete(String filename, User user);
}
