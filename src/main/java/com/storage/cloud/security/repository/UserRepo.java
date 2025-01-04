package com.storage.cloud.security.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.storage.cloud.security.model.User;


public interface UserRepo extends CrudRepository<User, Long>{
	
	User findByUsername(String username);
	
	Optional<User> findBySub(String sub);
}
