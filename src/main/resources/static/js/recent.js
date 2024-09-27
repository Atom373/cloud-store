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
	
	getRecentlyViewedFilesInfoFromServer();
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

function getRecentlyViewedFilesInfoFromServer() {
	$.ajax({
        url: '/api/file/recent', 
        type: 'GET',
        success: function(files) {
			if (files.length === 0) {
				$('#noFilesLabel').removeClass("d-none");
			}
			
			for (const file of files) {
				var fileItem = $('#fileItem').clone();
				
				fileItem.removeAttr('id');
				fileItem.removeClass("d-none");
				fileItem.find('div.spinner-border').remove();
				fileItem.find('div.dropdown').removeClass("d-none");
				
				setUpFileCallbacks(fileItem, file.encodedId);
				
				fileItem.find('a.open-link').text(file.name);
				
				setUpFileIcon(fileItem, file.extension);
				
				$('#files').prepend(fileItem);
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
		if ($(this).data('is-starred') === 'false') {
			sendAddToStarredRequest(fileId, $(this));
			$('#addedToStarredMsg').toast('show');
		} else {
			sendRemoveFromStarredRequest(fileId, $(this));
			$('#removedFromStarredMsg').toast('show');
		} 
	});
	
	fileItem.find('a.trash-link').off('click').on('click',function() {
		fileItem.remove();
		sendAddToTrashRequest(encodedFileId);
	});
}

function sendRenameFileRequest(fileId, newFilename, fileItem) {
	$.ajax({
        url: '/api/rename/file/' + fileId, 
        type: 'PATCH',
		data: { newFilename: newFilename },
		success: function(newFileId) {
			setUpFileCallbacks(fileItem, newFileId);
		},
		error: function(response) {
			console.log(response);
		}
    });
}

function sendAddToStarredRequest(fileId, starredLink) {
	$.ajax({
        url: '/api/starred/add/' + fileId, 
        type: 'POST',
		success: function() {
			starredLink.html('<i class="bx bxs-star" style="font-size: 20px;"></i> Remove from starred');
			starredLink.data('is-starred', 'true');
		},
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