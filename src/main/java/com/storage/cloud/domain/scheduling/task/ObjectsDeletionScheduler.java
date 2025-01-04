package com.storage.cloud.domain.scheduling.task;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.storage.cloud.domain.service.impl.MinioStorageService;
import com.storage.cloud.security.model.User;
import com.storage.cloud.security.repository.UserRepo;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ObjectsDeletionScheduler {

	private final UserRepo userRepo;
	private final MinioClient client;
	private final MinioStorageService storageService;
	
	@Scheduled(cron = "0 0 23 * * ?")
	//@Scheduled(fixedDelayString = "PT1M")
	public void deleteOldTrashedObjects() {
		String currentDate = LocalDate.now().toString();
		
		Iterable<User> users = userRepo.findAll();
		
		for (User user : users)  {
			String bucket = user.getBucketName();
			
			List<String> objectsToDelete = new LinkedList<>();
			
			Iterable<Result<Item>> results = client.listObjects(
				    ListObjectsArgs.builder()
				    		.bucket(bucket)
				    		.recursive(true)
				    		.build()
			);
			for (Result<Item> result : results) {
				try {
					String objectName = result.get().objectName();
					
					Map<String, String> meta = storageService.getObjectMeta(bucket, objectName);
					
					if (!Boolean.parseBoolean(meta.get("is-trash"))) {
						continue;
					}
					log.trace("object: %s will be deleted at: %s".formatted(objectName, meta.get("date-of-deletion")));
					if (meta.get("date-of-deletion").equals(currentDate)) {
						objectsToDelete.add(objectName);
						log.trace("Obj to delete: " + objectName);
					}
				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
					throw new RuntimeException(e);
				} 
			}
			objectsToDelete.forEach(objectName -> storageService.delete(bucket, objectName, user));
		}
	}
}
