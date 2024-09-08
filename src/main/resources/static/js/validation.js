const serverUrl = 'http://localhost:8080/api'

var usernameIsValid = false;
var passwordIsValid = false;
var passwordsMatch = false;

$(document).ready(function () {

    $('.invalid-feedback').hide();
    $('.valid-feedback').hide();
	
	if ($('#usernameInput').val().length !== 0) {
		checkUniqueness($('#usernameInput').val());
	}

    $('#usernameInput').on('input', function() {
        var username = $('#usernameInput').val();
		
        if (username.length < 3) {
			$('#tooShortUsernameErrorMsg').show();
			$('#usernameIsValidMsg').hide();
			$('#usernameAlreadyTakenErrorMsg').hide();
			
			$('#usernameInput').addClass('is-invalid');
			$('#usernameInput').removeClass('is-valid');
			
			usernameIsValid = false;
		} else {
			checkUniqueness(username);
		}
			
    });

    $('#passwordInput').on('input', function() {
        var password = $('#passwordInput').val();
		
        if (password.length < 4) {
			$('#tooShortPasswordErrorMsg').show();
			$('#strongPasswordMsg').hide();
			
			$('#passwordInput').addClass('is-invalid');
			$('#passwordInput').removeClass('is-valid');
			
			passwordIsValid = false;
		} else {
            $('#strongPasswordMsg').show();
			$('#tooShortPasswordErrorMsg').hide();
			
			$('#passwordInput').addClass('is-valid');
			$('#passwordInput').removeClass('is-invalid');
			
			passwordIsValid = true;
		}
		
		checkPasswordsMatching();
    });

    $('#repeatPasswordInput').on('input', checkPasswordsMatching);
	
	$('#registrationForm').on('submit', function(event){
		//console.log(usernameIsValid + "-" + passwordIsValid + "-" + passwordsMatch);
		if (!(usernameIsValid && passwordIsValid && passwordsMatch))
			event.preventDefault();
	});
})

function checkUniqueness(username) {
    $.ajax({
        url: serverUrl + '/username/is-unique', 
        type: 'POST',
		data: { username: username },
        success: function(isUnique) {
			console.log("is unique = " + isUnique);
			if (isUnique) {
				$('#usernameIsValidMsg').show();
				$('#tooShortUsernameErrorMsg').hide();
				$('#usernameAlreadyTakenErrorMsg').hide();
				
				$('#usernameInput').addClass('is-valid');
				$('#usernameInput').removeClass('is-invalid');
				
				usernameIsValid = true;
			} else {
	            $('#usernameAlreadyTakenErrorMsg').show();
				$('#tooShortUsernameErrorMsg').hide();
				$('#usernameIsValidMsg').hide();
				
				$('#usernameInput').addClass('is-invalid');
				$('#usernameInput').removeClass('is-valid');
				
				usernameIsValid = false;
			}
        },
        error: function(xhr, status, error) {
            console.log("Username check caused an error");
        },
    });
}

function checkPasswordsMatching() {
    var password = $('#passwordInput').val();
    var repeatedPassword = $('#repeatPasswordInput').val();
	
    if (password !== repeatedPassword) {
        $('#passwordsDontMatchErrorMsg').show();
		$('#passwordsMatchMsg').hide();
		
		$('#repeatPasswordInput').addClass('is-invalid');
		$('#repeatPasswordInput').removeClass('is-valid');
		
		passwordsMatch = false;
    } else {
        $('#passwordsMatchMsg').show();
		$('#passwordsDontMatchErrorMsg').hide();
		
		$('#repeatPasswordInput').addClass('is-valid');
		$('#repeatPasswordInput').removeClass('is-invalid');
		
		passwordsMatch = true;
	}
}