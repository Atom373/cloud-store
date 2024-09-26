const fileIcons = {
	'doc': {class: 'bxs-file-doc', color: 'blue'},
	'docx': {class: 'bxs-file-doc', color: 'blue'},
	'pdf': {class: 'bxs-file-pdf', color: 'darkred'},
	'png': {class: 'bxs-file-png', color: 'white'},
	'jpg': {class: 'bxs-file-jpg', color: 'white'},
	'html': {class: 'bxs-file-html', color: 'chocolate'},
	'js': {class: 'bxs-file-js', color: 'yellow'},
	'css': {class: 'bxs-file-css', color: 'lightblue'}
};

const fileInfoCache = {}

$(document).ready(function () {
	
	getObjectsInfoFromServer();
	
	$("#newFolderBtn").on('click', function() {
		$("#createFolderModal").modal('show');
	});
	
	$('#createNewFolderBtn').on('click', createNewFolderItem);
	
	$('#fileUploadBtn').on('click', function() {
		$('#fileInput').click();
	});
	
	$('#folderUploadBtn').on('click', function() {
		$('#folderInput').click();
	});
	
	$('#fileInput').on('change',addNewFileItem);
	
	$('#folderInput').on('change', addNewFolderItem);
});

function getObjectsInfoFromServer() {
	$.ajax({
        url: '/api/object/all', 
        type: 'GET',
        success: function(response) {
			if (response.folders.length ===  0) {
				$('#noFoldersLabel').removeClass("d-none");
			}
			
			if (response.files.length === 0) {
				$('#noFilesLabel').removeClass("d-none");
			}
			
			for (const file of response.files) {
				console.log(JSON.stringify(file, null, 2));
				var fileItem = $('#fileItem').clone();
				
				fileItem.removeAttr('id');
				fileItem.removeClass("d-none");
				fileItem.find('div.spinner-border').remove();
				fileItem.find('div.dropdown').removeClass("d-none");
				
				var starredLink = fileItem.find('a.starred-link');
				
				if (file.starred) {
					starredLink.html('<i class="bx bxs-star" style="font-size: 20px;"></i> Remove from starred');
					starredLink.data('is-starred', 'true');
				} else {
					starredLink.html('<i class="bx bx-star" style="font-size: 20px;"></i> Add to starred');
					starredLink.data('is-starred', 'false');
				}
				
				setUpFileCallbacks(fileItem, file.encodedId);
				
				fileItem.find('a.open-link').text(file.name);
				
				setUpFileIcon(fileItem, file.extension);
				
				$('#files').prepend(fileItem);
			}
			for (const folder of response.folders) {
				var folderItem = $('#folderItem').clone();
					
				folderItem.removeAttr('id');
				folderItem.removeClass("d-none");
				folderItem.find('div.spinner-border').remove();
				folderItem.find('div.dropdown').removeClass("d-none");

				var starredLink = folderItem.find('a.starred-link');
				
				if (folder.starred) {
					starredLink.html('<i class="bx bxs-star" style="font-size: 20px;"></i> Remove from starred');
					starredLink.data('is-starred', 'true');
				} else {
					starredLink.html('<i class="bx bx-star" style="font-size: 20px;"></i> Add to starred');
					starredLink.data('is-starred', 'false');
				}
				
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

function setUpFileCallbacks(fileItem, encodedFileId) {
	console.log(encodedFileId);
	const link = '/api/download/file/' + encodedFileId;
	//console.log(link);
	
	const openLink = fileItem.find('a.open-link');
	openLink.attr('href', '/api/open/file/' + encodedFileId);
	openLink.attr('target', '_blank');
	
	fileItem.find('a.download-link').attr('href', link);
	
	fileItem.find('a.share-link').off('click').on('click',function() {
		navigator.clipboard.writeText(window.location.origin + link);
		$('#linkWasCopiedMsg').toast('show');
	});
	
	fileItem.find('a.info-link').off('click').on('click',function() {
		$.ajax({
	        url: '/api/object/meta/' + encodedFileId, 
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
			
			sendRenameFileRequest(encodedFileId, newFilename, fileItem);
			
			$('#renameFileModal').modal('hide');
		});
	});
	
	fileItem.find('a.starred-link').off('click').on('click',function() {
		if ($(this).data('is-starred') === 'false') {
			sendAddToStarredRequest(encodedFileId, $(this));
			$('#addedToStarredMsg').toast('show');
		} else {
			sendRemoveFromStarredRequest(encodedFileId, $(this));
			$('#removedFromStarredMsg').toast('show');
		} 	
	});
	
	fileItem.find('a.trash-link').off('click').on('click',function() {
		fileItem.remove();
		if ($('#files').children().length - 1 === 0) {
			$('#noFilesLabel').removeClass("d-none");
		}
		sendAddToTrashRequest(encodedFileId);
	});
}

function sendRenameFileRequest(encodedFileId, newFilename, fileItem) {
	$.ajax({
        url: '/api/rename/file/' + encodedFileId, 
        type: 'PATCH',
		data: { newFilename: newFilename },
		success: function(newencodedFileId) {
			setUpFileCallbacks(fileItem, newencodedFileId);
		},
		error: function(response) {
			console.log(response);
		}
    });
}

function sendAddToStarredRequest(encodedId, starredLink) {
	$.ajax({
        url: '/api/starred/add/' + encodedId, 
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

function sendRemoveFromStarredRequest(encodedId, starredLink) {
	$.ajax({
        url: '/api/starred/remove/' + encodedId, 
        type: 'POST',
		success: function() {
			starredLink.html('<i class="bx bx-star" style="font-size: 20px;"></i> Add to starred');
			starredLink.data('is-starred', 'false');
		},
		error: function(response) {
			console.log(response);
		}
    });
}

function sendAddToTrashRequest(encodedId) {
	$.ajax({
        url: '/api/trash/add/' + encodedId, 
        type: 'POST',
		success: function() {
			$('#movedToTrashMsg').toast('show');
		},
		error: function(response) {
			console.log(response);
		}
    });
}

function addNewFileItem(event) {
	const file = event.target.files[0];
	
	var fileItem = $('#fileItem').clone();
	fileItem.removeAttr('id');
	
	fileItem.removeClass("d-none");
	 
	var filenameWithoutExtension = file.name.substring(0, file.name.lastIndexOf('.'));
	var fileExtension = file.name.split('.').pop();
	fileItem.find('a.open-link').text(filenameWithoutExtension);
	
	setUpFileIcon(fileItem, fileExtension);
	
	var dropdown = fileItem.find('div.dropdown');
	var spiner = fileItem.find('div.spinner-border');
	
	function onUploadingSuccess(fileUploadingResponse) {
		console.log("After uploading: " + JSON.stringify(fileUploadingResponse, null, 2));
		setUpFileCallbacks(fileItem, fileUploadingResponse.encodedId);
		$("#fileWasUploadedMsg").toast('show');
		spiner.remove();
		dropdown.removeClass("d-none");
		changeProgressBar(fileUploadingResponse);
		console.log('Файл успешно отправлен');
	}
	
	function onUploadingError() {
		$("#fileUploadingErrorMsg").toast('show');
		fileItem.remove();
		console.log('Ошибка при отправке файла');
	}
	
	uploadFileToServer(file, onUploadingSuccess, onUploadingError);
	
	$('#noFilesLabel').addClass("d-none");
	
	$('#files').prepend(fileItem);
}

function uploadFileToServer(file, onUploadingSuccess, onUploadingError) {
	const formData = new FormData();
    formData.append('file', file);
    
    $.ajax({
        url: '/api/upload/file', 
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false, 
        success: onUploadingSuccess,
        error: onUploadingError
    });
}
 
function changeProgressBar(objectUploadingResponse) {
	$('div.progress-bar').css('width', objectUploadingResponse.percentOfUsedSpace);
	$('div.used-space-label').text(objectUploadingResponse.formattedUsedSpace + " of 5 GB used");
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

function createNewFolderItem() {
	var foldername = $('#foldernameInput').val().trim();
	if (foldername.length === 0)
		return;
	
	var folderItem = $('#folderItem').clone();
	
	folderItem.removeAttr('id');
	folderItem.removeClass("d-none");

	var folderLink = folderItem.find('a').first();
	folderLink.text(foldername);
	
	sendCreateFolderRequest(foldername, folderLink);
	
	$('#noFoldersLabel').addClass("d-none");
	
	folderItem.find('div.spinner-border').remove();
	folderItem.find('div.dropdown').removeClass("d-none");
	
	$('#folders').prepend(folderItem);
	
	$("#createFolderModal").modal('hide');
}

function sendCreateFolderRequest(foldername, folderLink) {
	$.ajax({
        url: '/api/folder', 
        type: 'POST',
		data: { foldername: foldername },
		success: function(pathToCreatedFolder) {
			folderLink.attr('href', '/main?path=' + pathToCreatedFolder);
			
            console.log('Папка создана');
        }
    });
}

function addNewFolderItem() {
	const folderInput = $('#folderInput')[0];
    const formData = new FormData();

	if (folderInput.files.length === 0)
		return;
	
	const file = folderInput.files[0];
	
	var folderItem = $('#folderItem').clone();
	folderItem.removeAttr('id');
	
	folderItem.removeClass("d-none");
	 
	folderItem.find('a.open-link').text(file.webkitRelativePath.split("/")[0]);
	
	var dropdown = folderItem.find('div.dropdown');
	var spiner = folderItem.find('div.spinner-border');
	
	function onUploadingSuccess(folderUploadingResponse) {
		console.log("After uploading: " + JSON.stringify(folderUploadingResponse, null, 2));
		setUpFolderCallbacks(folderItem, folderUploadingResponse.encodedId, folderUploadingResponse.linkToFolder);
		$("#folderWasUploadedMsg").toast('show');
		spiner.remove();
		dropdown.removeClass("d-none");
		changeProgressBar(folderUploadingResponse);
		console.log('Файл успешно отправлен');
	}
	
	function onUploadingError() {
		$("#folderUploadingErrorMsg").toast('show');
		folderItem.remove();
		console.log('Ошибка при отправке файла');
	}
	
    for (let file of folderInput.files) {
		console.log(file.name);
        formData.append('files', file, file.webkitRelativePath);
    }
	
	$.ajax({
        url: '/api/upload/folder', 
        type: 'POST',
        data: formData,
		contentType: false,
		processData: false,
		success: onUploadingSuccess,
		error: onUploadingError
    });
	$('#noFoldersLabel').addClass('d-none');
	
	$('#folders').prepend(folderItem);
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
				const filename = openLink.text();
				$('#foldernameInfo').text(filename);
				$('#folderPathInfo').text(meta.path);
				$('#folderCreatedInfo').text(meta.created);
				$('#folderViewedInfo').text(meta.viewed);
			}
		});
		$('#folderInfoModal').modal('show');
	});
	
	folderItem.find('a.starred-link').off('click').on('click',function() {
		if ($(this).data('is-starred') === 'false') {
			sendAddToStarredRequest(encodedFolderId, $(this));
			$('#addedToStarredMsg').toast('show');
		} else {
			sendRemoveFromStarredRequest(encodedFolderId, $(this));
			$('#removedFromStarredMsg').toast('show');
		} 	
	});
		
	folderItem.find('a.trash-link').off('click').on('click',function() {
		folderItem.remove();
		if ($('#folders').children().length - 1 === 0) {
			$('#noFoldersLabel').removeClass("d-none");
		}
		sendAddToTrashRequest(encodedFolderId);
	});
}

function sendRenameFolderRequest(encodedFolderId, newFoldername, folderItem) {
	$.ajax({
        url: '/api/rename/folder/' + encodedFolderId, 
        type: 'PATCH',
		data: { newFoldername: newFoldername },
		success: function(folderRenamingResponse) {
			setUpFolderCallbacks(folderItem, folderRenamingResponse.encodedId, folderRenamingResponse.linkToFolder);
		},
		error: function(response) {
			console.log(response);
		}
    });
}
