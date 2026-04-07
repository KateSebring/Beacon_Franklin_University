package com.beacon.backend.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Configuration
public class SecurityConfig {
	
	 @Autowired
	 private JwtFilter jwtFilter;
	
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    // allows frontend requests to access backend services
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
    	CorsConfiguration config = new CorsConfiguration();
    	config.setAllowedOrigins(List.of("http://localhost:3000"));
    	config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    	config.setAllowedHeaders(List.of("*"));
    	config.setExposedHeaders(List.of("Authorization"));
    	config.setAllowCredentials(true);
    	
    	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    	source.registerCorsConfiguration("/**", config);
    	
    	return source;
    }
    
    // allows H2-Console to load properly via frameOptions
    // and allows unauthenticated users to access registration, login, and H2-console
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	return http
	    		.headers(headers -> headers
	    				.frameOptions(frame -> frame.sameOrigin()))
	    		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
	    		.csrf(csrf -> csrf.disable())
	    		.formLogin(form -> form.disable())
	    	    .httpBasic(basic -> basic.disable())
	    		.authorizeHttpRequests(request -> request
	    				.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
	    				.requestMatchers("/api/auth/**", "/h2-console/**","/api/public/profiles/**", "/api/contact/**").permitAll()
	    				.requestMatchers("/api/users/**", "/api/profile/**", "/api/messages/**").hasRole("USER")
	    				.requestMatchers("/api/admin/**").hasRole("ADMIN")
	    				.anyRequest().authenticated())
	    		.sessionManagement(session -> 
	    			session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	    		 .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
	    		.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AccountUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    	DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
    	authenticationProvider.setPasswordEncoder(passwordEncoder);
    	return new ProviderManager(authenticationProvider);
    }
}
