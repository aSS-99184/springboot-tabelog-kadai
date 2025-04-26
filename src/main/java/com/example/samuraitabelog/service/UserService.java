package com.example.samuraitabelog.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.Role;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.SignupForm;
import com.example.samuraitabelog.repository.RoleRepository;
import com.example.samuraitabelog.repository.UserRepository;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	
	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Transactional
	public User create(SignupForm signupForm) {
		
		// 登録するユーザーの箱
		User user = new User();		
		// 会員登録で作るユーザーは無料会員
		Role role = roleRepository.findByName("ROLE_GENERAL");
		
		user.setName(signupForm.getName());
		user.setFurigana(signupForm.getFurigana());
		user.setPostalCode(signupForm.getPostalCode());
		user.setAddress(signupForm.getAddress());
		user.setPhoneNumber(signupForm.getPhoneNumber());
		user.setEmail(signupForm.getEmail());
		// 暗号化
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
		user.setRole(role);
		user.setEnabled(true);		
		
		return userRepository.save(user);
	}
	
	// メールアドレスでユーザーを検索して登録済かどうかチェック
	public boolean isEmailRegistered(String email) {
		User user = userRepository.findByEmail(email);
		return user != null;
	}
	
	// パスワードチェック
	public boolean isSamePassword(String password, String passwordConfirmation) {
		return password.equals(passwordConfirmation);
	}
	

}
