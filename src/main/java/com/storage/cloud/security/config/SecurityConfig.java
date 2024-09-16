package com.storage.cloud.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.storage.cloud.security.model.User;
import com.storage.cloud.security.repository.UserRepo;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public UserDetailsService userDetailsService(UserRepo repo) {
		return username -> {
			User user = repo.findByUsername(username);
			if (user != null)
				return user;
			throw new UsernameNotFoundException("User with username: " + username + " not found");
		};
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf( 
					csrf -> csrf.disable() 
			)
			.authorizeHttpRequests( requests -> requests
				.requestMatchers("/register", "/js/**", "/css/**", "/api/download/**").permitAll()
				.anyRequest().authenticated()
			)
			.formLogin( form -> form
				.loginPage("/login")
				.defaultSuccessUrl("/main", true)
				.permitAll()
			)
			.logout( logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login")
				.invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
			)
			.build();
	}
}
