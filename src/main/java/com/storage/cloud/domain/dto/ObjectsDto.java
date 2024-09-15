package com.storage.cloud.domain.dto;

import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class ObjectsDto {

	private final List<FileDto> files;
	private final Set<FolderDto> folders;
}
