package com.storage.cloud.domain.service.impl;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.dto.FileDto;
import com.storage.cloud.domain.dto.FolderDto;
import com.storage.cloud.domain.dto.ObjectsDto;
import com.storage.cloud.domain.mapper.FileDtoMapper;
import com.storage.cloud.domain.mapper.FolderDtoMapper;
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
	private final FileDtoMapper fileDtoMapper;
	private final FolderDtoMapper folderDtoMapper;
	
	
	private record SortableByViewedDto<T>(
			T dto,
			String lastViewed
	) {}
	
	@Override
	public ObjectsDto getObjectsFrom(String directory, User user) {
		List<FileDto> files = new ArrayList<>();
		Set<FolderDto> folders = new HashSet<>();
		
		String bucket = user.getId().toString();
		
		Iterable<Result<Item>> results = client.listObjects(
			    ListObjectsArgs.builder()
			    		.bucket(bucket)
			    		.prefix(directory)
			    		.build()
		);
		
		for (Result<Item> result : results) {
			try {
				String objectName = result.get().objectName();
				String relativeName = objectName.substring(directory.length(), objectName.length());
				
				Map<String, String> meta = this.getObjectMeta(bucket, objectName);
				
				if (Boolean.parseBoolean(meta.get("is-trash"))) {
					continue;
				}
				
				if (relativeName.contains("/") || relativeName.startsWith(".")) {
					FolderDto dto = folderDtoMapper.map(bucket, objectName, meta);
					folders.add(dto);
				} else if (!relativeName.isEmpty()) {
					FileDto dto = fileDtoMapper.map(bucket, objectName, meta);
					files.add(dto);
				}
			} catch (Exception e) {
				log.error("In get all: " + e.getLocalizedMessage());
			} 
		}
		files.sort(Comparator.comparing(FileDto::getName));
		
		return new ObjectsDto(files, folders);
	}

	public ObjectsDto getStarredObjects(User user) {
		List<FileDto> files = new ArrayList<>();
		Set<FolderDto> folders = new HashSet<>();
		
		String bucket = user.getId().toString();
		
		Iterable<Result<Item>> results = client.listObjects(
			    ListObjectsArgs.builder()
			    		.bucket(bucket)
			    		.recursive(true)
			    		.build()
		);
		
		for (Result<Item> result : results) {
			try {
				String objectName = result.get().objectName();
				
				Map<String, String> meta = this.getObjectMeta(bucket, objectName);
				
				if (Boolean.parseBoolean(meta.get("is-trash"))) {
					continue;
				}
				
				if (!Boolean.parseBoolean(meta.get("is-starred"))) {
					continue;
				}
				
				if (objectName.endsWith("/")) { // if ends with '/' its a folder
					FolderDto dto = folderDtoMapper.map(bucket, objectName, meta);
					folders.add(dto);
				} else {
					FileDto dto = fileDtoMapper.map(bucket, objectName, meta);
					files.add(dto);
				}
			} catch (Exception e) {
				log.error("Get starred: " + e.getLocalizedMessage());
				throw new RuntimeException(e);
			} 
		}
		files.sort(Comparator.comparing(FileDto::getName));
		
		return new ObjectsDto(files, folders);
	}
	
	public List<FileDto> getRecentlyViewedFiles(User user) {
		List<SortableByViewedDto<FileDto>> files = new ArrayList<>();
		
		String bucket = user.getId().toString();
		
		Iterable<Result<Item>> results = client.listObjects(
			    ListObjectsArgs.builder()
			    		.bucket(bucket)
			    		.recursive(true)
			    		.build()
		);
		
		for (Result<Item> result : results) {
			try {
				String objectName = result.get().objectName();
				System.out.println("Recent: obj name = " + objectName);
				Map<String, String> meta = this.getObjectMeta(bucket, objectName);
				
				String lastViewed = meta.get("viewed");
				
				if (Boolean.parseBoolean(meta.get("is-trash"))) {
					continue;
				}
				
				if (lastViewed == null || lastViewed.equals("-"))
					continue; 
				
				System.out.println("Recent: viewed = " + lastViewed);
				if (!objectName.endsWith("/")) { // if doesn't end with '/' its a file
					FileDto dto = fileDtoMapper.map(bucket, objectName, meta);
					files.add(new SortableByViewedDto<FileDto>(dto, lastViewed));
				}
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
				throw new RuntimeException(e);
			} 
		}
		files.sort(Comparator.comparing(SortableByViewedDto::lastViewed));
		
		files = files.subList(0, Math.min(12, files.size()));
		
		return files.stream()
				.map(sortable -> sortable.dto())
				.toList();
	}
	
	public ObjectsDto getTrashedObjects(User user) {
		List<FileDto> files = new ArrayList<>();
		Set<FolderDto> folders = new HashSet<>();
		
		String bucket = user.getId().toString();
		
		Iterable<Result<Item>> results = client.listObjects(
			    ListObjectsArgs.builder()
			    		.bucket(bucket)
			    		.recursive(true)
			    		.build()
		);
		
		for (Result<Item> result : results) {
			try {
				String objectName = result.get().objectName();
				
				Map<String, String> meta = this.getObjectMeta(bucket, objectName);
				
				if (!Boolean.parseBoolean(meta.get("is-trash"))) {
					continue;
				}
				
				if (objectName.endsWith("/")) { // if ends with '/' its a folder
					FolderDto dto = folderDtoMapper.map(bucket, objectName, meta);
					folders.add(dto);
				} else {
					FileDto dto = fileDtoMapper.map(bucket, objectName, meta);
					files.add(dto);
				}
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
				throw new RuntimeException(e);
			} 
		}
		files.sort(Comparator.comparing(FileDto::getName));
		
		return new ObjectsDto(files, folders);
	}
	
	@Override
	public InputStream getFile(String bucket, String objectName) {
		try {
			InputStream inputStream = client.getObject(
	                GetObjectArgs.builder()
	                        .bucket(bucket)
	                        .object(objectName)
	                        .build()
			);
			return inputStream;
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			throw new RuntimeException(e);
		} 
	}

	public ByteArrayInputStream getCompressedFolder(String bucket, String objectName) {
		try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

            Iterable<Result<Item>> results = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(objectName)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir()) {
                	InputStream inputStream = this.getFile(bucket, item.objectName());

                    ZipEntry zipEntry = new ZipEntry(item.objectName().substring(objectName.length())); 
                    zipOutputStream.putNextEntry(zipEntry);

                    inputStream.transferTo(zipOutputStream);
                    inputStream.close();
                    zipOutputStream.closeEntry();
                }
            }
            zipOutputStream.close();

            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
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
        String folderObject = directory + foldername;
        
        if (!folderObject.endsWith("/"))
        	folderObject += "/";
        
        Map<String, String> meta = this.createMetadataFor(foldername, directory);
        try {
			client.putObject(
			    PutObjectArgs.builder()
			        .bucket(user.getId().toString())
			        .object(folderObject)
			        .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
			        .headers(meta)
			        .build()
			);
		} catch (Exception e) {
			log.error("In create folder: " + e.getLocalizedMessage());
		} 
        return folderObject;
	}
	
	@Override
	public String save(MultipartFile file, String directory, User user){
		String fileObject = directory + file.getOriginalFilename();
		String bucket = user.getId().toString();
		Map<String, String> meta = this.createMetadataFor(file, directory);
		try {
			client.putObject(
		        PutObjectArgs.builder()
		        	.bucket(bucket)
		        	.object(fileObject)
		        	.stream(file.getInputStream(), file.getSize(), -1)
	                .contentType(file.getContentType())
	                .headers(meta)
	                .build()
			);
		} catch (Exception e) {
			log.error("In save : " + e.getLocalizedMessage());
		} 
		userService.increaseUsedDiskSpace(user, file.getSize());
		return fileUtils.createEncodedObjectId(bucket, fileObject);
	}
	
	public String saveAll(MultipartFile[] files, String directory, User user) {
		Set<String> foldersToCreate = this.collectAllFolders(files);
		List<String> paths = new ArrayList<>();
		
		String bucket = user.getId().toString();
		String folderObject = directory + fileUtils.getBaseDir(files[0]);
		
		long overallSize = 0;
		
		for (String foldername : foldersToCreate) {
			this.createFolder(directory, foldername, user);
		}
		
		for (MultipartFile file : files) {
			paths.add(file.getOriginalFilename());
			overallSize += file.getSize();
			this.save(file, directory, user);
        }
		userService.increaseUsedDiskSpace(user, overallSize);
		return fileUtils.createEncodedObjectId(bucket, folderObject);
	}

	@Override
	public String renameFile(String bucket, String objectName, String newFilename) {
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

	public String renameFolder(String bucket, String directory, String newFoldername) {

		String newDirectory = fileUtils.getPathToLastFolder(directory) + newFoldername + "/";
		System.out.println("Current dir is : " + directory);
		System.out.println("New dir is : " + newDirectory);
		
		Iterable<Result<Item>> results = client.listObjects(
			    ListObjectsArgs.builder()
			    		.bucket(bucket)
			    		.prefix(directory)
			    		.recursive(true)
			    		.build()
		);
		
		for (Result<Item> result : results) {
			try {
				String objectName = result.get().objectName();
				String newObjectName = newDirectory + objectName.substring(directory.length(), objectName.length());
				
				System.out.println("Old obj name = " + objectName);
				System.out.println("New obj name = " + newObjectName);
				
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
		}
		return newDirectory;
	}
	
	@Override
	public void delete(String bucket, String objectName, User user) {
		long objectSize = 0;
		try {
			Iterable<Result<Item>> results = client.listObjects(
				    ListObjectsArgs.builder()
				    		.bucket(bucket)
				    		.prefix(objectName)
				    		.recursive(true)
				    		.build()
			);
			for (Result<Item> result : results) {
				objectSize += result.get().size();
				System.out.println("In get obj size: name = " + result.get().objectName() + ", size = " + result.get().size());
				client.removeObject(
	                    RemoveObjectArgs.builder()
	                            .bucket(bucket)  
	                            .object(result.get().objectName())
	                            .build()
	            );
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		} 
		userService.decreaseUsedDiskSpace(user, objectSize);
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
	
	public void addToTrash(String bucket,  String objectName) {
		this.changeIsTrashStatus(bucket, objectName, true);
	}
	
	public void removeFromTrash(String bucket,  String objectName) {
		this.changeIsTrashStatus(bucket, objectName, false);
	}
	
	private void changeIsStarredStatus(String bucket, String objectName, Boolean isStarred) {
		Map<String, String> meta = this.getObjectMeta(bucket, objectName);
		
		Map<String, String> updatedMeta = this.getMetaWithStandardKeyNames(meta);
		
		updatedMeta.put("x-amz-meta-is-starred", isStarred.toString()); 
		
		System.out.println("new meta is: " + updatedMeta);
		
		this.updateMeta(bucket, objectName, updatedMeta);
	}
	
	private void changeIsTrashStatus(String bucket, String objectName, Boolean isTrash) {
		Map<String, String> meta = this.getObjectMeta(bucket, objectName);
		
		Map<String, String> updatedMeta = this.getMetaWithStandardKeyNames(meta);
		
		updatedMeta.put("x-amz-meta-is-trash", isTrash.toString()); 
		updatedMeta.put("x-amz-meta-added-to-trash-date", this.getCurrentDate()); 
		
		System.out.println("Trash status: new meta is: " + updatedMeta);
		
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
		String fullFilename = file.getOriginalFilename();
		
		if (fullFilename.contains("/"))
			path += fileUtils.getDir(fullFilename);
		
		String extension = fileUtils.getFileExtension(fullFilename);
		
		meta.put("x-amz-meta-path", path);
		meta.put("x-amz-meta-type", extension.toUpperCase());
		meta.put("x-amz-meta-size", fileUtils.formatSize(file.getSize()));
		meta.put("x-amz-meta-uploaded", this.getCurrentDate());
		meta.put("x-amz-meta-viewed", "-");
		meta.put("x-amz-meta-is-starred", Boolean.FALSE.toString());
		meta.put("x-amz-meta-is-trash", Boolean.FALSE.toString());

		return meta;
	}
	
	private Map<String, String> createMetadataFor(String foldername, String directory) {
		Map<String, String> meta = new HashMap<>();
		
		String path = directory.isEmpty() ? "My Drive" : directory;
		
		meta.put("x-amz-meta-path", path);
		meta.put("x-amz-meta-type", "Folder");
		meta.put("x-amz-meta-created", this.getCurrentDate());
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

	private Set<String> collectAllFolders(MultipartFile[] files) {
		Set<String> directories = new HashSet<>();
		
		for (MultipartFile file : files) {
			int i=0, j=0;
			String path = file.getOriginalFilename();
			while ( (i = path.indexOf('/', j)) != -1) {
				String directory = path.substring(0, i+1);
				j = i + 1;
				directories.add(directory);
			}
		}
		System.out.println("Dirs are : " + directories);
		return directories;
	}
}