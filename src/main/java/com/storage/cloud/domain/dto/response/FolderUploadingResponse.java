package com.storage.cloud.domain.dto.response;

import lombok.Data;

@Data
public class FolderUploadingResponse {

	private final String encodedId;
	private final String linkToFolder;
	private final String percentOfUsedSpace;
	private final String formattedUsedSpace;
}
