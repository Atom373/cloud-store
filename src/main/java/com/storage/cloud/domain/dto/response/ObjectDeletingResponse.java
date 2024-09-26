package com.storage.cloud.domain.dto.response;

import lombok.Data;

@Data
public class ObjectDeletingResponse {
	
	private final String percentOfUsedSpace;
	private final String formattedUsedSpace;
}
