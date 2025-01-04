package com.storage.cloud.domain.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.storage.cloud.domain.service.ObjectIdEncodingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUtils {

	private final ObjectIdEncodingService encodingService;
	
	public String createEncodedObjectId(String bucket, String objectName) {
		return encodingService.encode(bucket, objectName);
	}
	
	public String getFullFilename(String objectName) {
		int i = objectName.lastIndexOf('/');
		return objectName.substring(i+1, objectName.length());
	}
	
	public String getPathTo(String objectName) {
		if (!objectName.contains("/"))
			return "";
		int lastBackslashIndex = objectName.lastIndexOf("/");
		return objectName.substring(0, lastBackslashIndex + 1);
	}
	
	public String getBaseDir(MultipartFile file) {
		return file.getOriginalFilename().split("/")[0] + "/";
	}
	
	public String getPathToLastFolder(String path) {
		int length = path.length();
		int penultimateBackslashIndex = path.lastIndexOf("/", length-2); // excluding the last backslash
		if (penultimateBackslashIndex == -1)
			return "";
		return path.substring(0, penultimateBackslashIndex+1); // including
	}
	
	public String getFileExtension(String objectName) {
		if (!objectName.contains("."))
			return "";
		String[] tmp = objectName.split("\\.");
		return tmp[tmp.length - 1];
	}
	
	public String getFilename(String objectName) {
		log.debug("in get filename = "+objectName);
		int lastDotIndex = objectName.lastIndexOf('.');
		int lastBackslashIndex = objectName.lastIndexOf('/');
		if (lastDotIndex != -1 && lastDotIndex > lastBackslashIndex)
			return objectName.substring(lastBackslashIndex+1, lastDotIndex);
		return objectName.substring(lastBackslashIndex+1, objectName.length());
	}
	
	public String getFoldername(String objectName) {
		if (!objectName.contains("/"))
			return objectName;
		String[] tmp = objectName.split("/");
		return tmp[tmp.length - 1];
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
