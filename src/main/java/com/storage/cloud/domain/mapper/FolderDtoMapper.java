package com.storage.cloud.domain.mapper;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.storage.cloud.domain.dto.FolderDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FolderDtoMapper {

	public FolderDto map(String relativeName, String directory, Map<String, String> meta) {
		String foldername = relativeName.split("/")[0];
		String linkToFolder = "/main?path=" + directory + foldername + "/";
		boolean isStarred = Boolean.parseBoolean(meta.get("is-starred"));
		
		return new FolderDto(foldername, linkToFolder, isStarred);
	}
	
	public FolderDto map(String fullPath, Map<String, String> meta) {
		String[] tmp = fullPath.split("/");
		String foldername = tmp[tmp.length - 1];
		String linkToFolder = "/main?path=" + fullPath;
		boolean isStarred = Boolean.parseBoolean(meta.get("is-starred"));
		
		return new FolderDto(foldername, linkToFolder, isStarred);
	}
}
