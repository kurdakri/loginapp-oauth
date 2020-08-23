package com.example.loginapp.oauth.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.loginapp.libraryentities.datasourceusers.Users;

@FeignClient(name = "loginapp-datasourceusers")
public interface UsersFeignClient {

	@GetMapping("/users/search/{username}")
	public Users searchUser(@PathVariable String username);
	
	@PutMapping("/users/modify")
	public Users updateUser(@RequestBody Users user);
	
}
