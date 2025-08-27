package com.example.demo.security;


import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.repository.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Autowired
	public MyUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		com.example.demo.entity.User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));

		return new User(
				user.getEmail(),
				user.getPasswordHash(),
				Collections.singletonList(new SimpleGrantedAuthority(user.getRole())));
	}
}
