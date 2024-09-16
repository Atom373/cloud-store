package com.storage.cloud.domain.utils;

import org.springframework.stereotype.Component;

@Component
public class FileUtils {

	public String getFilenameFromFileId(String[] fileId) {
		String objectName = fileId[1];
		String[] path = objectName.split("/");
		return path[path.length - 1];
	}
}
