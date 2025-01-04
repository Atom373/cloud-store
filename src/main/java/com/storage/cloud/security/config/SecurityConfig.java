package com.storage.cloud.security.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.storage.cloud.security.model.User;
import com.storage.cloud.security.repository.UserRepo;
import com.storage.cloud.security.service.OAuth2UserService;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("remember-me.key")
	private String key;
	
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
	public SecurityFilterChain securityFilterChain( HttpSecurity http, 
													OAuth2UserService userService,
													UserDetailsService userDetailsService,
													PersistentTokenRepository tokenRepository) throws Exception {
		http
			.csrf( csrf -> csrf
				.disable() 
			)
			.authorizeHttpRequests( requests -> requests
				.requestMatchers("/register", "/js/**", "/css/**", 
								 "/api/download/**", "/api/username/is-unique").permitAll()
				.anyRequest().authenticated()
			)
			.formLogin( form -> form
				.loginPage("/login")
				.defaultSuccessUrl("/main", true)
				.permitAll()
			)
			.oauth2Login( oauth -> oauth
				.loginPage("/login")
				.defaultSuccessUrl("/main", true)
				.permitAll()
				.userInfoEndpoint(userInfo -> userInfo
					.userService(userService)
				)
			)
			.logout( logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login")
				.invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
			)
			.rememberMe( rememberMe -> rememberMe
				.key(key)
				.tokenValiditySeconds(30 * 24 * 60 * 60) // 30 days
				.userDetailsService(userDetailsService)
				.alwaysRemember(true)
				.tokenRepository(tokenRepository)
			);
			
			return http.build();
	}
	
	@Bean
	public PersistentTokenRepository tokenRepository(DataSource dataSource) {
	    JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
	    tokenRepository.setDataSource(dataSource);
	    return tokenRepository;
	}

}
