const fileIcons = {
	'doc': {class: 'bxs-file-doc', color: 'blue'},
	'docx': {class: 'bxs-file-doc', color: 'blue'},
	'pdf': {class: 'bxs-file-pdf', color: 'darkred'},
	'png': {class: 'bxs-file-png', color: 'white'},
	'jpg': {class: 'bxs-file-jpg', color: 'white'},
	'html': {class: 'bxs-file-html', color: 'chocolate'}
}

const fileInfoCache = {}

$(document).ready(function () {
	
	getObjectsInfoFromServer();
	
	$("#newFolderBtn").on('click', function() {
		$("#createFolderModal").modal('show');
	});
	
	$('#createNewFolderBtn').on('click', addNewFolderItem);
	
	$('#fileUploadBtn').on('click', function() {
		$('#fileInput').click();
	});
	
	$('#fileInput').on('change',addNewFileItem);
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
				var fileItem = $('#fileItem').clone();
				
				fileItem.removeAttr('id');
				fileItem.removeClass("d-none");
				fileItem.find('div.spinner-border').remove();
				fileItem.find('div.dropdown').removeClass("d-none");
				
				setUpCallbacks(fileItem, file.id);
				
				fileItem.find('span').text(file.name);
				
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
				
				$('#folders').prepend(folderItem);
			}
		},
      	error: function() {
			console.log("Error while retrieving the files from server");
		}
    });
}

function setUpCallbacks(fileItem, fileId) {
	const link = '/api/download/file/' + fileId;
	console.log(link);
	
	fileItem.find('a.download-link').attr('href', link);
	
	fileItem.find('a.share-link').click(function() {
		console.log(window.location.origin);
		navigator.clipboard.writeText(window.location.origin + link);
		$('#linkWasCopiedMsg').toast('show');
	});
	
	fileItem.find('a.info-link').click(function() {
		$.ajax({
	        url: '/api/object/meta/' + fileId, 
	        type: 'GET',
	        success: function(meta) {
				//console.log(JSON.stringify(meta, null, 2));
				$('#filenameInfo').text(meta.filename);
				$('#pathInfo').text(meta.path);
				$('#typeInfo').text(meta.type);
				$('#sizeInfo').text(meta.size);
				$('#uploadedInfo').text(meta.uploaded);
			}
		});
		$('#fileInfoModal').modal('show');
	});
	
	fileItem.find('a.rename-link').click(function() {
		$('#renameFileModal').modal('show');
		
		$('#renameFileBtn').click(function() {
			var newFilename = $('#newFilenameInput').val().trim();
			
			if (newFilename.length === 0 || newFilename.includes('/') || newFilename.includes('.'))
				return;
			
			fileItem.find('span').text(newFilename);
			
			sendRenameFileRequest(fileId, newFilename, fileItem);
			
			$('#renameFileModal').modal('hide');
		});
	});
	
}

function sendRenameFileRequest(fileId, newFilename, fileItem) {
	$.ajax({
        url: '/api/rename/file/' + fileId, 
        type: 'PATCH',
		data: { newFilename: newFilename },
		success: function(newFileId) {
			setUpCallbacks(fileItem, newFileId);
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
	
	var filename = file.name.split('.');
	var filenameWithoutExtension = filename[0];
	var fileExtension = filename[1];
	fileItem.find('span').text(filenameWithoutExtension);
	
	setUpFileIcon(fileItem, fileExtension);
	
	var dropdown = fileItem.find('div.dropdown');
	var spiner = fileItem.find('div.spinner-border');
	
	function onUploadingSuccess(fileId) {
		setUpCallbacks(fileItem, fileId);
		$("#fileWasUploadedMsg").toast('show');
		spiner.remove();
		dropdown.removeClass("d-none");
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

function addNewFolderItem() {
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