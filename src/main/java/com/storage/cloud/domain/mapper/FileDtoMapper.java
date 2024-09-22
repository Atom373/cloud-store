package com.storage.cloud.domain.mapper;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.storage.cloud.domain.dto.FileDto;
import com.storage.cloud.domain.utils.FileUtils;
import com.storage.cloud.security.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileDtoMapper {

	private final FileUtils fileUtils;
	
	public FileDto map(String objectName, User user, Map<String, String> meta) {
		String filename = fileUtils.getFilename(objectName);
		String extension = fileUtils.getFileExtension(objectName);
		String fileId = fileUtils.createEncodedObjectId(user, objectName);
		boolean isStarred = Boolean.parseBoolean(meta.get("is-starred"));
		
		System.out.println("In mapper:filaname=%s, fileId=%s".formatted(filename, fileId));
		
		return new FileDto(fileId, filename, extension, isStarred);
	}
	
}
