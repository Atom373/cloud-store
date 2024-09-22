const fileIcons = {
	'doc': {class: 'bxs-file-doc', color: 'blue'},
	'docx': {class: 'bxs-file-doc', color: 'blue'},
	'pdf': {class: 'bxs-file-pdf', color: 'darkred'},
	'png': {class: 'bxs-file-png', color: 'white'},
	'jpg': {class: 'bxs-file-jpg', color: 'white'},
	'html': {class: 'bxs-file-html', color: 'chocolate'}
};

$(document).ready(function () { 
	
	getTrashedObjectsInfoFromServer();
})

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
				
				setUpFileIcon(fileItem, file.extension);
				
				$('#files').prepend(fileItem);
			}
			for (const folder of response.folders) {
				var folderItem = $('#folderItem').clone();
					
				folderItem.removeAttr('id');
				folderItem.removeClass("d-none");

				var foldername = folderItem.find('span');
				foldername.text(folder.name);
				foldername.attr('href', folder.link);
				
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

function sendDeleteObjectRequest(fileId) {
	$.ajax({
        url: '/api//add/' + fileId, 
        type: 'POST',
		success: function() {
			$('#fileWasDeletedMsg').toast('show');
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