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
	
	public FileDto map(String fullFilename, User user, Map<String, String> meta) {
		String filename = fileUtils.getFilename(fullFilename);
		String extension = fileUtils.getFileExtension(fullFilename);
		String fileId = fileUtils.createObjectId(user, fullFilename);
		boolean isStarred = Boolean.parseBoolean(meta.get("is-starred"));
		
		return new FileDto(fileId, filename, extension, isStarred);
	}
	
}
