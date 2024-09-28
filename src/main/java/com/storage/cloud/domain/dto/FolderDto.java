package com.storage.cloud.domain.dto;

import lombok.Data;

@Data
public class FolderDto {

	private final String encodedId;
	private final String name;
	private final String link;
	private final boolean isStarred;
	private final String dateOfDeletion;
}
