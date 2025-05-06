package com.example.samuraitabelog.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.PasswordResetToken;
import com.example.samuraitabelog.entity.Role;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.PasswordEditForm;
import com.example.samuraitabelog.form.SignupForm;
import com.example.samuraitabelog.form.UserEditForm;
import com.example.samuraitabelog.repository.PasswordResetTokenRepository;
import com.example.samuraitabelog.repository.RoleRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.example.samuraitabelog.repository.VerificationTokenRepository;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final VerificationTokenRepository verificationTokenRepository;
	
	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository, VerificationTokenRepository verificationTokenRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.verificationTokenRepository = verificationTokenRepository;
	}
	
	@Transactional
	public User create(SignupForm signupForm) {
		
		// 登録するユーザーの箱
		User user = new User();		
		// 会員登録で作るユーザーは無料会員
		Role role = roleRepository.findByName("ROLE_GENERAL");
		
		// 登録のためのデータ準備
		user.setName(signupForm.getName());
		user.setFurigana(signupForm.getFurigana());
		user.setPostalCode(signupForm.getPostalCode());
		user.setAddress(signupForm.getAddress());
		user.setPhoneNumber(signupForm.getPhoneNumber());
		user.setEmail(signupForm.getEmail());
		// 暗号化
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
		
		user.setRole(role);
		user.setEnabled(false);		
		
		return userRepository.save(user);
	}
	
	@Transactional
    public void update(UserEditForm userEditForm) {
        User user = userRepository.getReferenceById(userEditForm.getId());
        
        user.setName(userEditForm.getName());
        user.setFurigana(userEditForm.getFurigana());
        user.setPostalCode(userEditForm.getPostalCode());
        user.setAddress(userEditForm.getAddress());
        user.setPhoneNumber(userEditForm.getPhoneNumber());
        user.setEmail(userEditForm.getEmail());      
        
        userRepository.save(user);
    }
	
	@Transactional
	// トークンからユーザーを取得する
	public User findUserByPasswordResetToken (String token) {
		PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
		if (passwordResetToken == null) {
			return null;
		}
		return passwordResetToken.getUser();
	}
	
	@Transactional
	// // ユーザーリポジトリを使ってメールアドレスでユーザーを取得
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	
	// パスワード再設定
	@Transactional
	public void newpassword(PasswordEditForm passwordEditForm, User user) {
		String encodedPassword = passwordEncoder.encode(passwordEditForm.getPassword());
		user.setPassword(passwordEncoder.encode(passwordEditForm.getPassword()));
		userRepository.save(user);
	}
	
	
	// メールアドレスでユーザーを検索して登録済かどうかチェック
	public boolean isEmailRegistered(String email) {
		User user = userRepository.findByEmail(email);
		return user != null;
	}
	
	// パスワードとパスワード(確認用)が一致するかチェック
	public boolean isSamePassword(String password, String passwordConfirmation) {
		return password.equals(passwordConfirmation);
	}
	
	// メール認証用のページで認証成功したら実行する
	@Transactional
    public void enableUser(User user) {
        user.setEnabled(true); 
        userRepository.save(user);
    } 
	
	// メールアドレスが変更されたかどうかをチェックする
	public boolean isEmailChanged(UserEditForm userEditForm) {
        User currentUser = userRepository.getReferenceById(userEditForm.getId());
        return !userEditForm.getEmail().equals(currentUser.getEmail());      
    }
	
	// 会員情報関連データ削除
	@Transactional
	public void deleteAccount(Integer userId) {
		// ユーザーに関連するトークンを削除
		verificationTokenRepository.deleteByUserId(userId);
		// ユーザーを削除
		userRepository.deleteById(userId);
	}
}
