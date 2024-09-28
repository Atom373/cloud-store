package com.storage.cloud.domain.mapper;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.storage.cloud.domain.dto.FolderDto;
import com.storage.cloud.domain.utils.FileUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FolderDtoMapper {

	private final FileUtils fileUtils;
	
	public FolderDto map(String bucket, String objectName, Map<String, String> meta) {
		String encodedId = fileUtils.createEncodedObjectId(bucket, objectName);
		String[] tmp = objectName.split("/");
		String foldername = tmp[tmp.length - 1];
		String linkToFolder = "/main?path=" + objectName;
		boolean isStarred = Boolean.parseBoolean(meta.get("is-starred"));
		String dateOfDeletion = meta.get("date-of-deletion");
		
		return new FolderDto(encodedId, foldername, linkToFolder, isStarred, dateOfDeletion);
	}
}
