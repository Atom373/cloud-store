package com.storage.cloud.domain.model;

public record ObjectId(
		String bucket,
		String name
	) {}