package com.storage.cloud.domain.service.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.FileDto;
import com.storage.cloud.domain.dto.FolderDto;
import com.storage.cloud.domain.dto.ObjectsDto;
import com.storage.cloud.domain.service.StorageService;
import com.storage.cloud.security.model.User;

import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService implements StorageService {

	private final MinioClient client;  
	
	@Override
	public ObjectsDto getAllObjectsFrom(String dirName, User user) {
		List<FileDto> files = new ArrayList<>();
		Set<FolderDto> folders = new HashSet<>();
		
		Iterable<Result<Item>> results = client.listObjects(
			    ListObjectsArgs.builder()
			    		.bucket(user.getId()+"")
			    		.prefix(dirName)
			    		.build()
		);
		results.forEach(result -> {
			try {
				String objectName = result.get().objectName();
				String relativeName = objectName.substring(dirName.length(), objectName.length());
				
				if (relativeName.contains("/")) { // if contains '/' its a folder
					String foldername = relativeName.split("/")[0];
					String linkToFolder = "/main?path=" + dirName + foldername + "/";
					folders.add(new FolderDto(foldername, linkToFolder));
				} else if (!relativeName.isEmpty()) {
					String[] filename = relativeName.split("\\.");
					files.add(new FileDto(filename[0], filename[1]));
				}
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			} 
		});
		files.sort(Comparator.comparing(FileDto::getName));
		
		return new ObjectsDto(files, folders);
	}

	
	@Override
	public void createBucketFor(User user) {
		try {
			client.makeBucket(
				MakeBucketArgs.builder()
					.bucket(user.getId()+"")
					.build()
			);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		}
	}
	
	@Override
	public String createFolder(String dirName, String foldername, User user) {
        String folderObject = dirName + foldername + "/";

        try {
			client.putObject(
			    PutObjectArgs.builder()
			        .bucket(user.getId()+"")
			        .object(folderObject)
			        .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
			        .build()
			);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		} 
        return folderObject;
	}
	
	@Override
	public void save(MultipartFile file, String dirName, User user){
		String fileObject = dirName + file.getOriginalFilename();
		log.info(fileObject);
		try {
			client.putObject(
		        PutObjectArgs.builder()
		        	.bucket(user.getId()+"")
		        	.object(fileObject)
		        	.stream(file.getInputStream(), file.getSize(), -1)
	                .contentType(file.getContentType())
	                .build()
			);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		} 
	}

	@Override
	public void rename(String oldFilename, String newFilename, User user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String filename, User user) {
		// TODO Auto-generated method stub
		
	}
}
