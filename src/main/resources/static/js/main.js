const fileIcons = {
	'doc': {class: 'bxs-file-doc', color: 'blue'},
	'docx': {class: 'bxs-file-doc', color: 'blue'},
	'pdf': {class: 'bxs-file-pdf', color: 'darkred'},
	'png': {class: 'bxs-file-png', color: 'white'},
	'jpg': {class: 'bxs-file-jpg', color: 'white'},
	'html': {class: 'bxs-file-html', color: 'chocolate'}
}

$(document).ready(function () {
	
	getFilesInfoFromServer();
	
	$('#fileUploadBtn').on('click', function() {
		$('#fileInput').click();
	});
	
	$('#fileInput').on('change', function(event) {
		const file = event.target.files[0];
		
		addNewFileItem(file);
	});
});

function addNewFileItem(file) {
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
	
	function onUploadingSuccess() {
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
	
	uploadToServer(file, onUploadingSuccess, onUploadingError);
	
	$('#files').prepend(fileItem);
}

function getFilesInfoFromServer() {
	$.ajax({
        url: '/api/file/all', 
        type: 'GET',
        success: function(filesInfo) {
			for (const file of filesInfo) {
				var fileItem = $('#fileItem').clone();
				
				fileItem.removeAttr('id');
				fileItem.removeClass("d-none");
				fileItem.find('div.spinner-border').remove();
				fileItem.find('div.dropdown').removeClass("d-none");
				
				fileItem.find('span').text(file.name);
				
				setUpFileIcon(fileItem, file.extension);
				
				$('#files').prepend(fileItem);
			}
		},
      	error: function() {
			console.log("Error while retrieving the files from server");
		}
    });
}

function uploadToServer(file, onUploadingSuccess, onUploadingError) {
	const formData = new FormData();
    formData.append('file', file);
    
    $.ajax({
        url: '/api/file/upload', 
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