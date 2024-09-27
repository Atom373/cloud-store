const fileIcons = {
	'doc': {class: 'bxs-file-doc', color: 'blue'},
	'docx': {class: 'bxs-file-doc', color: 'blue'},
	'pdf': {class: 'bxs-file-pdf', color: 'darkred'},
	'png': {class: 'bxs-file-png', color: 'white'},
	'jpg': {class: 'bxs-file-jpg', color: 'white'},
	'html': {class: 'bxs-file-html', color: 'chocolate'},
	'js': {class: 'bxs-file-js', color: 'yellow'},
	'css': {class: 'bxs-file-css', color: 'lightblue'},
	'zip': {class: 'bxs-file-archive', color: 'lightyellow'}
};

$(document).ready(function () { 
	
	$('#searchTrigger').on('click', searchObjects);
	
	getStarredObjectsInfoFromServer();
})

function searchObjects() {
	$("#searchModal").modal('show');
	
	$('#search').focus();
	
	$('#search').on('input', function() {
		var partOfName = $('#search').val().trim(); 
		
		if (partOfName.length < 2)
			return;

		$.ajax({
	        url: '/api/object/search',
	        method: 'GET',
	        data: { partOfName: partOfName },
	        success: function(response) {
				$('#searchResults').empty(); 
				console.log(JSON.stringify(response, null, 2));
	           	for (const file of response.files) {
					fileItem = $('#searchItem').clone();
					
					fileItem.removeAttr('id');
					fileItem.removeClass("d-none");
					
					const openLink = '/api/open/file/' + file.encodedId;
					
					fileItem.find('a.open-link').attr('href', openLink);
					fileItem.find('a.open-link').attr('target', '_blank');
					fileItem.find('a.open-link').text(file.name);
					
					setUpFileIcon(fileItem, file.extension);
					
					var starredLink = fileItem.find('a.starred-link');
					var starredLinkIcon = starredLink.find('i');
					
					if (file.starred) {
						starredLinkIcon.addClass('bxs-star');
						fileItem.data('is-starred', 'true');
					} else {
						starredLinkIcon.addClass('bx-star');
						fileItem.data('is-starred', 'false');
					}
					
					starredLink.on('click', function() {
						if (fileItem.data('is-starred') === 'false') {
							sendAddToStarredRequest(file.encodedId);
							starredLinkIcon.removeClass('bx-star');
							starredLinkIcon.addClass('bxs-star');
						} else {
							sendRemoveFromStarredRequest(file.encodedId);
							starredLinkIcon.removeClass('bxs-star');
							starredLinkIcon.addClass('bx-star');
						}
					});
					
					fileItem.find('a.info-link').on('click', function() {
						$.ajax({
					        url: '/api/object/meta/' + file.encodedId, 
					        type: 'GET',
							cache: false,
					        success: function(meta) {
								console.log(JSON.stringify(meta, null, 2));
								const filename = fileItem.find('a.open-link').text();
								$('#filenameInfo').text(filename);
								$('#pathInfo').text(meta.path);
								$('#typeInfo').text(meta.type);
								$('#sizeInfo').text(meta.size);
								$('#uploadedInfo').text(meta.uploaded);
								$('#viewedInfo').text(meta.viewed);
							}
						});
						$('#fileInfoModal').modal('show');
					});
					
					$('#searchResults').prepend(fileItem);
				}
				for (const folder of response.folders) {
					folderItem = $('#searchItem').clone();
								
					folderItem.removeAttr('id');
					folderItem.removeClass("d-none");
							
					folderItem.find('a.open-link').attr('href', folder.link);
					folderItem.find('a.open-link').text(folder.name);
					
					var starredLink = folderItem.find('a.starred-link');
					var starredLinkIcon = starredLink.find('i');
					
					if (folder.starred) {
						starredLinkIcon.addClass('bxs-star');
						folderItem.data('is-starred', 'true');
					} else {
						starredLinkIcon.addClass('bx-star');
						folderItem.data('is-starred', 'false');
					}
					
					starredLink.on('click', function() {
						if (folderItem.data('is-starred') === 'false') {
							sendAddToStarredRequest(folder.encodedId);
							starredLinkIcon.removeClass('bx-star');
							starredLinkIcon.addClass('bxs-star');
						} else {
							sendRemoveFromStarredRequest(folder.encodedId);
							starredLinkIcon.removeClass('bxs-star');
							starredLinkIcon.addClass('bx-star');
						}
					});
										
					folderItem.find('a.info-link').off('click').on('click',function() {
						$.ajax({
					        url: '/api/object/meta/' + folder.encodedId, 
					        type: 'GET',
							cache: false,
					        success: function(meta) {
								console.log(JSON.stringify(meta, null, 2));
								const foldername = folderItem.find('a.open-link').text();
								$('#foldernameInfo').text(foldername);
								$('#folderPathInfo').text(meta.path);
								$('#folderCreatedInfo').text(meta.created);
								$('#folderViewedInfo').text(meta.viewed);
							}
						});
						$('#folderInfoModal').modal('show');
					});
					
					folderItem.find('i').first().addClass('bx-folder');
					
					$('#searchResults').prepend(folderItem);
				}
	        }
	    });
	});
}

function getStarredObjectsInfoFromServer() {
	$.ajax({
        url: '/api/object/starred', 
        type: 'GET',
        success: function(response) {
			if (response.folders.length ===  0) {
				$('#noFoldersLabel').removeClass("d-none");
			}
			
			if (response.files.length === 0) {
				$('#noFilesLabel').removeClass("d-none");
			}
			
			for (const file of response.files) {
				var fileItem = $('#fileItem').clone();
				
				fileItem.removeAttr('id');
				fileItem.removeClass("d-none");
				
				setUpFileCallbacks(fileItem, file.encodedId);
				
				fileItem.find('a.open-link').text(file.name);
				
				setUpFileIcon(fileItem, file.extension);
				
				$('#files').prepend(fileItem);
			}
			for (const folder of response.folders) {
				var folderItem = $('#folderItem').clone();
					
				folderItem.removeAttr('id');
				folderItem.removeClass("d-none");
				
				var link = folderItem.find('a').first();
				link.text(folder.name);
				link.attr('href', folder.link);
				
				setUpFolderCallbacks(folderItem, folder.encodedId);
				
				$('#folders').prepend(folderItem);
			}
		},
      	error: function() {
			console.log("Error while retrieving the files from server");
		}
    });
}

function setUpFileCallbacks(fileItem, fileId) {
	console.log(fileId);
	const link = '/api/download/file/' + fileId;
	//console.log(link);
	
	const openLink = fileItem.find('a.open-link');
	openLink.attr('href', '/api/open/file/' + fileId);
	openLink.attr('target', '_blank');
	
	fileItem.find('a.download-link').attr('href', link);
	
	fileItem.find('a.share-link').off('click').on('click',function() {
		navigator.clipboard.writeText(window.location.origin + link);
		$('#linkWasCopiedMsg').toast('show');
	});
	
	fileItem.find('a.info-link').off('click').on('click',function() {
		$.ajax({
	        url: '/api/object/meta/' + fileId, 
	        type: 'GET',
			cache: false,
	        success: function(meta) {
				console.log(JSON.stringify(meta, null, 2));
				const filename = openLink.text();
				$('#filenameInfo').text(filename);
				$('#pathInfo').text(meta.path);
				$('#typeInfo').text(meta.type);
				$('#sizeInfo').text(meta.size);
				$('#uploadedInfo').text(meta.uploaded);
				$('#viewedInfo').text(meta.viewed);
			}
		});
		$('#fileInfoModal').modal('show');
	});
	
	fileItem.find('a.rename-link').off('click').on('click',function() {
		$('#renameFileModal').modal('show');
		
		$('#renameFileBtn').click(function() {
			var newFilename = $('#newFilenameInput').val().trim();
			
			if (newFilename.length === 0 || newFilename.includes('/') || newFilename.includes('.'))
				return;
			
			openLink.text(newFilename);
			
			sendRenameFileRequest(fileId, newFilename, fileItem);
			
			$('#renameFileModal').modal('hide');
		});
	});
	
	fileItem.find('a.starred-link').off('click').on('click',function() {
		fileItem.remove();
		if ($('#files').children().length - 1 === 0) {
			$('#noFilesLabel').removeClass("d-none");
		}
		sendRemoveFromStarredRequest(fileId, $(this));
		$('#removedFromStarredMsg').toast('show');
	});
	
	fileItem.find('a.trash-link').off('click').on('click',function() {
		fileItem.remove();
		sendAddToTrashRequest(encodedFileId);
	});
}

function sendRemoveFromStarredRequest(objectId, starredLink) {
	$.ajax({
        url: '/api/starred/remove/' + objectId, 
        type: 'POST',
		error: function(response) {
			console.log(response);
		}
    });
}

function sendAddToTrashRequest(encodedFileId) {
	$.ajax({
        url: '/api/trash/add/' + encodedFileId, 
        type: 'POST',
		success: function() {
			$('#movedToTrashMsg').toast('show');
		},
		error: function(response) {
			console.log(response);
		}
    });
}

function setUpFileIcon(fileItem, fileExtension) {
	// Default values
	var iconClass = 'bx-file';
	var iconColor = 'white';
	
	if (fileIcons[fileExtension]) {
		iconClass = fileIcons[fileExtension].class;
		iconColor = fileIcons[fileExtension].color;
	}
	
	var icon = fileItem.find('i').first();
	icon.addClass(iconClass);
	icon.css('color', iconColor);
}

function setUpFolderCallbacks(folderItem, encodedFolderId, linkToFolder) {
	const openLink = folderItem.find('a.open-link');
	openLink.attr('href', linkToFolder);
	
	const downloadLink = '/api/download/folder/' + encodedFolderId;
	
	folderItem.find('a.download-link').attr('href', downloadLink);
	
	folderItem.find('a.rename-link').off('click').on('click',function() {
		$('#renameFolderModal').modal('show');
		
		$(this).attr('href', '#');
		
		$('#renameFolderBtn').click(function() {
			var newFoldername = $('#newFoldernameInput').val().trim();
			
			if (newFoldername.length === 0 || newFoldername.includes('/') || newFoldername.includes('.'))
				return;
			
			openLink.text(newFoldername);
			
			sendRenameFolderRequest(encodedFolderId, newFoldername, folderItem);
			
			$('#renameFolderModal').modal('hide');
		});
	});
	
	folderItem.find('a.share-link').off('click').on('click',function() {
		navigator.clipboard.writeText(window.location.origin + downloadLink);
		$('#linkWasCopiedMsg').toast('show');
	});
	
	folderItem.find('a.info-link').off('click').on('click',function() {
		$.ajax({
	        url: '/api/object/meta/' + encodedFolderId, 
	        type: 'GET',
			cache: false,
	        success: function(meta) {
				console.log(JSON.stringify(meta, null, 2));
				const foldername = openLink.text();
				$('#foldernameInfo').text(foldername);
				$('#folderPathInfo').text(meta.path);
				$('#folderCreatedInfo').text(meta.created);
				$('#folderViewedInfo').text(meta.viewed);
			}
		});
		$('#folderInfoModal').modal('show');
	});
	
	folderItem.find('a.starred-link').off('click').on('click',function() {
		folderItem.remove();
		if ($('#folders').children().length - 1 === 0) {
			$('#noFoldersLabel').removeClass("d-none");
		}
		sendRemoveFromStarredRequest(encodedFolderId, $(this));
		$('#removedFromStarredMsg').toast('show');	
	});
		
	folderItem.find('a.trash-link').off('click').on('click',function() {
		folderItem.remove();
		if ($('#folders').children().length - 1 === 0) {
			$('#noFoldersLabel').removeClass("d-none");
		}
		sendAddToTrashRequest(encodedFolderId);
	});
}