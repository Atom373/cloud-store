package com.storage.cloud.domain.service;

import com.storage.cloud.domain.model.ObjectId;

public interface ObjectIdEncodingService {

	String encode(String bucket, String objectName);
	
	ObjectId decode(String encodedFileId);
}
