package com.example.loginapp.oauth.service;

import com.example.loginapp.libraryentities.datasourceusers.Users;

public interface IUsersService {

	public Users search(String username);
	
	public Users update(Users user);
	
}
