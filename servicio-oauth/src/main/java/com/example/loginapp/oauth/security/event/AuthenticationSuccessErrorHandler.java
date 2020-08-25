package com.example.loginapp.oauth.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.example.loginapp.libraryentities.datasourceusers.Users;
import com.example.loginapp.oauth.service.IUsersService;

import brave.Tracer;
import feign.FeignException;

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {
	
	private Logger log = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);
	
	@Autowired
	private Tracer tracer;
	
	@Value("${config.security.oauth.client.id}")
	private String clientUser;
	
	@Autowired
	private IUsersService usersService;

	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		UserDetails user = (UserDetails) authentication.getPrincipal();
		log.info("Success login: {}", user.getUsername());
		
		if(user.getUsername().equals(clientUser)) {
			return;
		}
		
		Users userDB = usersService.search(user.getUsername());
		
		if(userDB.getLoggingfails() != null) {
			userDB.setLoggingfails(0);
			usersService.update(userDB);
		}
	}

	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		String msg = String.format("Error en el login: %s", exception.getMessage());
		tracer.currentSpan().tag("error.message", exception.getMessage());
		try {
			StringBuilder errors = new StringBuilder();
			errors.append(msg);
			if(authentication.getName().equals(clientUser)) {
				return;
			}
			Users usersDB = usersService.search(authentication.getName());
			if(usersDB.getLoggingfails() == null) {
				usersDB.setLoggingfails(0);
			}
			usersDB.setLoggingfails(usersDB.getLoggingfails() + 1);
			errors.append("Intentos de login:"+usersDB.getLoggingfails());
			if(usersDB.getLoggingfails() >= 3) {
				errors.append("Error: Superado el máximo de intentos. Se deshabilita el usuario");
				usersDB.setEnabled(false);
			}
			usersService.update(usersDB);
		}catch(FeignException f) {
			log.error("Se ha producido una excepción en la actualizacion del usuario {}", f);
		}
	}

}