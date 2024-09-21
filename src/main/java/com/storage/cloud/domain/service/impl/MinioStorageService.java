package com.storage.cloud.domain.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.FileDto;
import com.storage.cloud.domain.dto.FolderDto;
import com.storage.cloud.domain.dto.ObjectsDto;
import com.storage.cloud.domain.service.StorageService;
import com.storage.cloud.domain.utils.FileUtils;
import com.storage.cloud.security.model.User;
import com.storage.cloud.security.service.UserService;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService implements StorageService {

	private final MinioClient client;
	private final FileUtils fileUtils;
	private final UserService userService;
	
	@Override
	public ObjectsDto getAllObjectsFrom(String directory, User user) {
		List<FileDto> files = new ArrayList<>();
		Set<FolderDto> folders = new HashSet<>();
		
		Iterable<Result<Item>> results = client.listObjects(
			    ListObjectsArgs.builder()
			    		.bucket(user.getId().toString())
			    		.prefix(directory)
			    		.build()
		);
		results.forEach(result -> {
			try {
				String objectName = result.get().objectName();
				String relativeName = objectName.substring(directory.length(), objectName.length());
				
				if (relativeName.contains("/")) { // if contains '/' its a folder
					String foldername = relativeName.split("/")[0];
					String linkToFolder = "/main?path=" + directory + foldername + "/";
					folders.add(new FolderDto(foldername, linkToFolder));
				} else if (!relativeName.isEmpty()) {
					String filename = fileUtils.getFilename(objectName);
					String extension = fileUtils.getFileExtension(objectName);
					String fileId = fileUtils.createObjectId(user, objectName);
					files.add(new FileDto(fileId, filename, extension));
				}
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			} 
		});
		files.sort(Comparator.comparing(FileDto::getName));
		
		return new ObjectsDto(files, folders);
	}

	@Override
	public Resource getFileResource(String bucket, String objectName) {
		try {
			InputStream inputStream = client.getObject(
	                GetObjectArgs.builder()
	                        .bucket(bucket)
	                        .object(objectName)
	                        .build()
			);
			return new InputStreamResource(inputStream);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			throw new RuntimeException(e);
		} 
	}
	
	public Map<String, String> getObjectMeta(String bucket, String objectName) {
		try {
			StatObjectResponse stat = client.statObject(
		            StatObjectArgs.builder()
		                .bucket(bucket)
		                .object(objectName)
		                .build()
		    );
			return stat.userMetadata();
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void createBucketFor(User user) {
		try {
			client.makeBucket(
				MakeBucketArgs.builder()
					.bucket(user.getId().toString())
					.build()
			);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		}
	}
	
	@Override
	public String createFolder(String directory, String foldername, User user) {
        String folderObject = directory + foldername + "/";

        try {
			client.putObject(
			    PutObjectArgs.builder()
			        .bucket(user.getId().toString())
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
	public String save(MultipartFile file, String directory, User user){
		String fileObject = directory + file.getOriginalFilename();
		Map<String, String> meta = this.createMetadataFor(file, directory);
		log.info(fileObject);
		try {
			client.putObject(
		        PutObjectArgs.builder()
		        	.bucket(user.getId().toString())
		        	.object(fileObject)
		        	.stream(file.getInputStream(), file.getSize(), -1)
	                .contentType(file.getContentType())
	                .headers(meta)
	                .build()
			);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		} 
		userService.increaseUsedDiskSpace(user, file.getSize());
		return fileUtils.createObjectId(user, fileObject);
	}

	@Override
	public String rename(String bucket, String objectName, String newFilename) {
		String dir = fileUtils.getDir(objectName);
		String extension = fileUtils.getFileExtension(objectName);
		
		String newObjectName = dir + newFilename + "." + extension;
		System.out.println("newObjectName="+newObjectName);
		try {
			client.copyObject(
	            CopyObjectArgs.builder()
	                .bucket(bucket)
	                .object(newObjectName)
	                .source(CopySource.builder()
	                			.bucket(bucket)
	                			.object(objectName)
	                			.build()
	                )
	                .build()
	        );
		
	        client.removeObject(
	            RemoveObjectArgs.builder()
	                .bucket(bucket)
	                .object(objectName)
	                .build()
	        );
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		} 
		return newObjectName;
	}

	@Override
	public void delete(String filename, User user) {
		// TODO Auto-generated method stub
		
	}
	
	public void updateLastViewedDate(String bucket, String objectName) {
		Map<String, String> meta = this.getObjectMeta(bucket, objectName);
		
		Map<String, String> updatedMeta = this.getMetaWithStandardKeyNames(meta);
		
		String viewedDate = meta.get("viewed");
		String newViewedDate = this.getCurrentDate();
		
		if (viewedDate != null && viewedDate.equals(newViewedDate))
			return; 
		
		updatedMeta.put("x-amz-meta-viewed", newViewedDate); 
		
		System.out.println("new meta is: " + updatedMeta);
		
		this.updateMeta(bucket, objectName, updatedMeta);
	}
	
	public void addToStarred(String bucket, String objectName) {
		this.changeIsStarredStatus(bucket, objectName, true);
	}
	
	public void removeFromStarred(String bucket, String objectName) {
		this.changeIsStarredStatus(bucket, objectName, false);
	}
	
	private void changeIsStarredStatus(String bucket, String objectName, Boolean isStarred) {
		Map<String, String> meta = this.getObjectMeta(bucket, objectName);
		
		Map<String, String> updatedMeta = this.getMetaWithStandardKeyNames(meta);
		
		updatedMeta.put("x-amz-meta-is-starred", isStarred.toString()); 
		
		System.out.println("new meta is: " + updatedMeta);
		
		this.updateMeta(bucket, objectName, updatedMeta);
	}
	
	private Map<String, String> getMetaWithStandardKeyNames(Map<String, String> meta) {
		Map<String, String> updatedMeta = new HashMap<>();
		
		meta.forEach((key, value) -> {
			String standardKey = "x-amz-meta-" + key;
			updatedMeta.put(standardKey, value);
		});
		return updatedMeta;
	}
	
	private void updateMeta(String bucket, String objectName, Map<String, String> updatedMeta) {
		updatedMeta.put("x-amz-metadata-directive", "REPLACE");
		try {
			client.copyObject(
	            CopyObjectArgs.builder()
	                .bucket(bucket)
	                .object(objectName)
	                .headers(updatedMeta) 
	                .source(CopySource.builder()
	                    .bucket(bucket)
	                    .object(objectName)
	                    .build()
	                )
	                .build()
	        );
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		} 
	}
	
	private Map<String, String> createMetadataFor(MultipartFile file, String directory) {
		Map<String, String> meta = new HashMap<>();
		
		String path = directory.isEmpty() ? "My Drive" : directory;
		
		String extension = file.getOriginalFilename().split("\\.")[1];
		
		meta.put("x-amz-meta-path", path);
		meta.put("x-amz-meta-type", extension.toUpperCase());
		meta.put("x-amz-meta-size", fileUtils.formatSize(file.getSize()));
		meta.put("x-amz-meta-uploaded", this.getCurrentDate());
		meta.put("x-amz-meta-viewed", "-");
		meta.put("x-amz-meta-is-starred", Boolean.FALSE.toString());
		meta.put("x-amz-meta-is-trash", Boolean.FALSE.toString());

		return meta;
	}
	
	private String getCurrentDate() {
		LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return currentDate.format(formatter);
	}
}
