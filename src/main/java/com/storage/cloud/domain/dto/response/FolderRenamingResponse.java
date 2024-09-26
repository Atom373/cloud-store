package com.storage.cloud.domain.dto.response;

import lombok.Data;

@Data
public class FolderRenamingResponse {

	private final String encodedId;
	private final String linkToFolder;
}
