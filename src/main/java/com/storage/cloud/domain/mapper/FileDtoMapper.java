package com.storage.cloud.domain.mapper;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.storage.cloud.domain.dto.FileDto;
import com.storage.cloud.domain.utils.FileUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDtoMapper {

	private final FileUtils fileUtils;
	
	public FileDto map(String bucket, String objectName, Map<String, String> meta) {
		String filename = fileUtils.getFilename(objectName);
		String extension = fileUtils.getFileExtension(objectName);
		String fileId = fileUtils.createEncodedObjectId(bucket, objectName);
		boolean isStarred = Boolean.parseBoolean(meta.get("is-starred"));
		String dateOfDeletion = meta.get("date-of-deletion");
		
		log.debug("In mapper:filaname=%s, fileId=%s, is starred = %s".formatted(filename, fileId, isStarred));
		
		return new FileDto(fileId, filename, extension, isStarred, dateOfDeletion);
	}
	
}
