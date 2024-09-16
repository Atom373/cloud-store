package com.storage.cloud.domain.service;

public interface FileIdEncodingService {

	String encode(String bucket, String objectName);
	
	String[] decode(String encodedFileId);
}
