package com.pixcel.app.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.pixcel.app.user.security.CustomUserDetails;

import jakarta.servlet.http.Cookie;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login").usernameParameter("loginId")
						.passwordParameter("password").successHandler((request, response, authentication) -> {

							CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

							Cookie userIdCookie = new Cookie("userId", user.getUserId());
							Cookie loginIdCookie = new Cookie("loginId", user.getLoginId());
							Cookie userNameCookie = new Cookie("userName",
									URLEncoder.encode(user.getUserName(), StandardCharsets.UTF_8));
							Cookie authYnCookie = new Cookie("authYn", user.getAuthYn());
							Cookie subscribeYnCookie = new Cookie("subscribeYn", user.getSubscribeYn());

							userIdCookie.setPath("/");
							loginIdCookie.setPath("/");
							userNameCookie.setPath("/");
							authYnCookie.setPath("/");
							subscribeYnCookie.setPath("/");

							userIdCookie.setMaxAge(60 * 60);
							loginIdCookie.setMaxAge(60 * 60);
							userNameCookie.setMaxAge(60 * 60);
							authYnCookie.setMaxAge(60 * 60);
							subscribeYnCookie.setMaxAge(60 * 60);

							response.addCookie(userIdCookie);
							response.addCookie(loginIdCookie);
							response.addCookie(userNameCookie);
							response.addCookie(authYnCookie);
							response.addCookie(subscribeYnCookie);

							response.sendRedirect("/myproject/list");
						})

						.failureUrl("/login?error=true").permitAll())
				.logout(logout -> logout.logoutUrl("/logout").addLogoutHandler((request, response, authentication) -> {

					String[] cookieNames = { "JSESSIONID", "authYn", "loginId", "subscribeYn", "userId", "userName" };

					for (String cookieName : cookieNames) {
						Cookie cookie = new Cookie(cookieName, null);
						cookie.setPath("/");
						cookie.setMaxAge(0);
						response.addCookie(cookie);
					}
				}).invalidateHttpSession(true).clearAuthentication(true).logoutSuccessUrl("/login")
						.permitAll())
				.httpBasic(basic -> basic.disable());

		return http.build();
	}

	// 비밀번호 암호화
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
