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
					String fileId = fileUtils.createFileId(user, objectName);
					files.add(new FileDto(fileId, filename[0], filename[1]));
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
	
	public Map<String, String> getObjectMeta(String bucketName, String objectName) {
		try {
			StatObjectResponse stat = client.statObject(
		            StatObjectArgs.builder()
		                .bucket(bucketName)
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
	public String save(MultipartFile file, String dirName, User user){
		String fileObject = dirName + file.getOriginalFilename();
		Map<String, String> meta = this.createMetadataFor(file, dirName);
		log.info(fileObject);
		try {
			client.putObject(
		        PutObjectArgs.builder()
		        	.bucket(user.getId()+"")
		        	.object(fileObject)
		        	.stream(file.getInputStream(), file.getSize(), -1)
	                .contentType(file.getContentType())
	                .headers(meta)
	                .build()
			);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		} 
		return fileUtils.createFileId(user, fileObject);
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
	
	private Map<String, String> createMetadataFor(MultipartFile file, String dirName) {
		Map<String, String> meta = new HashMap<>();
		
		String path = dirName.isEmpty() ? "My Drive" : dirName;
		
		String[] filename = file.getOriginalFilename().split("\\.");
		
		meta.put("x-amz-meta-filename", filename[0]);
		meta.put("x-amz-meta-path", path);
		meta.put("x-amz-meta-type", filename[1]);
		meta.put("x-amz-meta-size", this.getFileSize(file));
		meta.put("x-amz-meta-uploaded", this.getCurrentDate());
		
		return meta;
	}
	
	private String getFileSize(MultipartFile file) {
		long size = file.getSize();
		
		if (size > 1024)
			size /= 1024;
		else
			return size + " B";
		
		if (size > 1024)
			return (size / 1024) + " MB";
		return size + " KB";
	}
	
	private String getCurrentDate() {
		LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return currentDate.format(formatter);
	}
}
