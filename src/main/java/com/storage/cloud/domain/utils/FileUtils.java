package com.storage.cloud.domain.utils;

import org.springframework.stereotype.Component;

import com.storage.cloud.domain.service.FileIdEncodingService;
import com.storage.cloud.security.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileUtils {

	private final FileIdEncodingService encodingService;
	
	public String getFilenameFromFileId(String[] fileId) {
		String objectName = fileId[1];
		String[] path = objectName.split("/");
		return path[path.length - 1];
	}
	
	public String createFileId(User user, String objectName) {
		return encodingService.encode(user.getId()+"", objectName);
	}
	
	public String getDir(String objectName) {
		if (!objectName.contains("/"))
			return "";
		int lastBackslashIndex = objectName.lastIndexOf("/");
		return objectName.substring(0, lastBackslashIndex) + '/';
	}
	
	public String getFileExtension(String objectName) {
		return objectName.split("\\.")[1];
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
