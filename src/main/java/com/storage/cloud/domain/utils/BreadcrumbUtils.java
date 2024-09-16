package com.storage.cloud.domain.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.storage.cloud.domain.dto.Breadcrumb;

@Component
public class BreadcrumbUtils {

	public List<Breadcrumb> getBreadcrumbsFor(String dirName) {
		List<Breadcrumb> result = new ArrayList<>();
		
		if (dirName.isEmpty()) {
			return result;
		}
		
		String current = "";
		
		for (String foldername : dirName.split("/")) {
			current += foldername + "/";
			result.add(new Breadcrumb(current, foldername));
		}
		System.out.println(result);
		return result;
	}
}
