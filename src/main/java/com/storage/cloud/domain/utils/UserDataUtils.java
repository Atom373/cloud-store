package com.storage.cloud.domain.utils;

import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class UserDataUtils {

	private static final long MAX_SIZE = 5L * 1024 * 1024 * 1024; // 5 GB
	
	public String convertToPercents(long bytes) {
		double percent = ((double)bytes / MAX_SIZE) * 100;
		return String.format(Locale.US, "%.2f", percent) + "%";
	}
}
