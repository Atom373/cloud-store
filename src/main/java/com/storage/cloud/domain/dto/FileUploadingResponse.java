package com.storage.cloud.domain.dto;

import lombok.Data;

@Data
public class FileUploadingResponse {

	private final String fileId;
	private final String percentOfUsedSpace;
	private final String formattedUsedSpace;
}
