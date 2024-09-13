package com.storage.cloud.domain.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.FileDto;
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
	public List<FileDto> getAllFilesFrom(String dirName, User user) {
		List<FileDto> files = new ArrayList<>();
		Iterable<Result<Item>> results = client.listObjects(
			    ListObjectsArgs.builder()
			    		.bucket(user.getId()+"")
			    		.prefix(dirName)
			    		.build()
		);
		results.forEach(result -> {
			try {
				String[] filename = result.get().objectName().split("\\.");
				files.add(new FileDto(filename[0], filename[1]));
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			} 
		});
		files.sort(Comparator.comparing(FileDto::getName));
		
		return files;
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
	public void save(MultipartFile file, String dirName, User user){
		String fullFilename = dirName + file.getOriginalFilename();
		log.info(fullFilename);
		try {
			client.putObject(
			        PutObjectArgs.builder()
			        	.bucket(user.getId()+"")
			        	.object(fullFilename).stream(
			        			file.getInputStream(), file.getSize(), -1
			        		)
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
	public void makeDirectory(String dirName, User user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(String filename, User user) {
		// TODO Auto-generated method stub
		
	}
}
