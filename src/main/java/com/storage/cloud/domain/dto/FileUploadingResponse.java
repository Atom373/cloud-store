package com.storage.cloud.domain.dto;

import lombok.Data;

@Data
public class FileUploadingResponse {

	private final String encodedId;
	private final String percentOfUsedSpace;
	private final String formattedUsedSpace;
}
