package com.example.loginapp.oauth.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.loginapp.libraryentities.datasourceusers.Users;
import com.example.loginapp.oauth.clients.UsersFeignClient;

import feign.FeignException;

@Service
public class UsersService implements UserDetailsService, IUsersService {
	
	private Logger log = LoggerFactory.getLogger(UsersService.class);
	
	@Autowired
	private UsersFeignClient client;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			Users user = client.searchUser(username);
			List<GrantedAuthority> authorities = user.getRoles().stream()
					.map(role -> new SimpleGrantedAuthority(role.getName()))
					.collect(Collectors.toList());
			log.info("Usuario autenticado: {}", username);
			return new User(user.getUsername(), user.getPassword(), user.getEnabled(), true, true, true, authorities);
		}catch(FeignException e) {
			log.error("No se ha recuperado el usuario");
			throw new UsernameNotFoundException("El usuario no existe");
		}
	}

	@Override
	public Users search(String username) {
		return client.searchUser(username);
	}

	@Override
	public Users update(Users user) {
		try {
			Users currentUser = client.searchUser(user.getUsername());
			if(currentUser != null) {
				client.updateUser(user);
				return user;
			} else {
				throw new UsernameNotFoundException("El usuario no existe");
			}
		}catch(FeignException e) {
			log.error("No se ha recuperado el usuario");
			throw new UsernameNotFoundException("El usuario no existe");
		}
	}

}