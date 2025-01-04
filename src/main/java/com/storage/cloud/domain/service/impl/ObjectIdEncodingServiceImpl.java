package com.storage.cloud.domain.service.impl;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.springframework.stereotype.Service;

import com.storage.cloud.domain.model.ObjectId;
import com.storage.cloud.domain.service.ObjectIdEncodingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ObjectIdEncodingServiceImpl implements ObjectIdEncodingService {

	private static Encoder encoder = Base64.getEncoder();
	private static Decoder decoder = Base64.getDecoder();

	@Override
	public String encode(String bucket, String objectName) {
		String fileId = bucket + ':' + objectName;
		return encoder.encodeToString(fileId.getBytes());
	}

	@Override
	public ObjectId decode(String encodedFileId) {
		String decodedFileId = new String(decoder.decode(encodedFileId));
		String[] tmp = decodedFileId.split(":");
		return new ObjectId(tmp[0], tmp[1]);
	}
	
	

}
