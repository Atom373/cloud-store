package com.storage.cloud.domain.utils;

import org.springframework.stereotype.Component;

import com.storage.cloud.domain.model.ObjectId;
import com.storage.cloud.domain.service.ObjectIdEncodingService;
import com.storage.cloud.security.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileUtils {

	private final ObjectIdEncodingService encodingService;
	
	public String getFullFilename(ObjectId objectId) {
		String objectName = objectId.name();
		String[] path = objectName.split("/");
		System.out.println("Full filename = " + path[path.length - 1]);
		return path[path.length - 1];
	}
	
	public String getFullFilename(String objectName) {
		int i = objectName.lastIndexOf('/');
		return objectName.substring(i+1, objectName.length());
	}
	
	public String createEncodedObjectId(User user, String objectName) {
		return encodingService.encode(user.getId().toString(), objectName);
	}
	
	public String getDir(String objectName) {
		if (!objectName.contains("/"))
			return "";
		int lastBackslashIndex = objectName.lastIndexOf("/");
		return objectName.substring(0, lastBackslashIndex) + '/';
	}
	
	public String getFileExtension(String objectName) {
		String[] tmp = objectName.split("\\.");
		return tmp[tmp.length - 1];
	}
	
	public String getFilename(String objectName) {
		System.out.println("in get filename = "+objectName);
		if (objectName.contains("/"))
			return objectName.substring(objectName.lastIndexOf('/')+1, objectName.lastIndexOf('.'));
		return objectName.substring(0, objectName.lastIndexOf('.'));
	}
	
	public String formatSize(long size) {
		if (size < 1024) return size + " B";
		else size /= 1024;	
		
		if (size < 1024) return size + " KB";
		else size /= 1024;
	
		if (size < 1024) return size + " MB";
		
		return (size / 1024) + " GB";
	}
}
