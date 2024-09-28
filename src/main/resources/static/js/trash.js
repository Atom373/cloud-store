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
	
	getTrashedObjectsInfoFromServer();
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

function getTrashedObjectsInfoFromServer() {
	$.ajax({
        url: '/api/object/trashed', 
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
				fileItem.find('div.spinner-border').remove();
				fileItem.find('div.dropdown').removeClass("d-none");
				
				setUpFileCallbacks(fileItem, file.encodedId);
				
				fileItem.find('span').text(file.name);
				
				var dateOfDeletion = new Date(file.dateOfDeletion);
				var differenceInMilliseconds = dateOfDeletion - new Date();
				var daysTillDeletion = Math.ceil(differenceInMilliseconds / (1000 * 60 * 60 * 24));
				
				if (daysTillDeletion % 10 === 1)
					fileItem.find('span.badge').text(daysTillDeletion + ' day');
				else
					fileItem.find('span.badge').text(daysTillDeletion + ' days');
				
				setUpFileIcon(fileItem, file.extension);
				
				$('#files').prepend(fileItem);
			}
			for (const folder of response.folders) {
				var folderItem = $('#folderItem').clone();
					
				folderItem.removeAttr('id');
				folderItem.removeClass("d-none");

				setUpFolderCallbacks(folderItem, folder.encodedId);
				
				var foldername = folderItem.find('span').first();
				foldername.text(folder.name);
				
				var dateOfDeletion = new Date(folder.dateOfDeletion);
				var differenceInMilliseconds = dateOfDeletion - new Date();
				var daysTillDeletion = Math.ceil(differenceInMilliseconds / (1000 * 60 * 60 * 24));
				
				if (daysTillDeletion % 10 === 1)
					folderItem.find('span.badge').text(daysTillDeletion + ' day');
				else
					folderItem.find('span.badge').text(daysTillDeletion + ' days');
				
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
	
	fileItem.find('a.restore-link').on('click', function() {
		fileItem.remove();
		if ($('#files').children().length - 1 === 0) {
			$('#noFilesLabel').removeClass("d-none");
		}
		sendRemoveFromTrashRequest(fileId);
	});
	
	fileItem.find('a.delete-link').on('click', function() {
		fileItem.remove();
		if ($('#files').children().length - 1 === 0) {
			$('#noFilesLabel').removeClass("d-none");
		}
		sendDeleteObjectRequest(fileId);
	});
}

function sendDeleteObjectRequest(objectId) {
	$.ajax({
        url: '/api/object/' + objectId, 
        type: 'DELETE',
		success: function(objectDeletingResponse) {
			$('#fileWasDeletedMsg').toast('show');
			changeProgressBar(objectDeletingResponse);
		},
		error: function(response) {
			console.log(response);
		}
    });
}

function sendRemoveFromTrashRequest(encodedFileId) {
	$.ajax({
        url: '/api/trash/remove/' + encodedFileId, 
        type: 'POST',
		success: function() {
			$('#fileWasRestoredMsg').toast('show');
		},
		error: function(response) {
			console.log(response);
		}
    });
}

function changeProgressBar(objectDeletingResponse) {
	$('div.progress-bar').css('width', objectDeletingResponse.percentOfUsedSpace);
	$('div.used-space-label').text(objectDeletingResponse.formattedUsedSpace + " of 5 GB used");
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

function setUpFolderCallbacks(folderItem, folderId) {
	folderItem.find('a.restore-link').on('click', function() {
		folderItem.remove();
		if ($('#folders').children().length - 1 === 0) {
			$('#noFoldersLabel').removeClass("d-none");
		}
		sendRemoveFromTrashRequest(folderId);
	});
	
	folderItem.find('a.delete-link').on('click', function() {
		folderItem.remove();
		if ($('#folders').children().length - 1 === 0) {
			$('#noFoldersLabel').removeClass("d-none");
		}
		sendDeleteObjectRequest(folderId);
	});
}