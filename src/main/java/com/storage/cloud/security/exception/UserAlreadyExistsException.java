package com.storage.cloud.security.exception;

public class UserAlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 7545402479333221545L;
	
	public UserAlreadyExistsException() {}
	
	public UserAlreadyExistsException(String msg) {
		super(msg);
	}
}
