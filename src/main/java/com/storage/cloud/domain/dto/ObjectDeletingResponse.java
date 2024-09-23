package com.storage.cloud.domain.dto;

import lombok.Data;

@Data
public class ObjectDeletingResponse {
	
	private final String percentOfUsedSpace;
	private final String formattedUsedSpace;
}
