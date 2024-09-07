package com.storage.cloud.security.repository;

import org.springframework.data.repository.CrudRepository;

import com.storage.cloud.security.model.User;


public interface UserRepo extends CrudRepository<User, Long>{
	
	User findByUsername(String username);
}
