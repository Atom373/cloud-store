package com.storage.cloud.domain.dto;

import lombok.Data;

@Data
public class FileDto {

	private final String id; // file Id consists of bucket name and objectName
	private final String name;
	private final String extension;
	private final boolean isStarred;
}
