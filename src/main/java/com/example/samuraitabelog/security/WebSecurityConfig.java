
package com.example.samuraitabelog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((requests) -> requests
				// すべてのユーザーにアクセスを許可するURL
				.requestMatchers("/css/**", "/images/**", "/js/**", "/storage/**", "/", "/signup/**", "/password/password_reset_request", "/password/password_reset_request/**", "/password/password_reset","/password/password_reset/**",   "/password/password_reset_message", "/restaurants", "/restaurants/{id}", "/stripe/webhook", "/restaurants/{id}/reviews", "/stripe/webhook", "/subscription/premium").permitAll() 
				// 管理者にアクセスを許可するURL
				.requestMatchers("/admin/**").hasRole("ADMIN")
				// 無料会員にのみアクセスを許可するURL
				.requestMatchers("/subscription/premium", "/create-checkout-session").hasRole("GENERAL")
				// 有料会員にのみアクセスを許可するURL
				.requestMatchers("/subscription/payment", "/subscription/update-card-session", "/subscription/cancel", "/subscription/cancel-subscription").hasRole("PREMIUM")
						
				// 上記以外はログインが必要
				.anyRequest().authenticated()
			)

			.formLogin((form) -> form
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.defaultSuccessUrl("/?loggedIn")
				.failureUrl("/login?error")
				.permitAll()
			)
			.logout((logout) -> logout
				.logoutSuccessUrl("/?loggedOut")
				.permitAll()
				)
				.csrf().ignoringRequestMatchers("/stripe/webhook");
		return http.build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
