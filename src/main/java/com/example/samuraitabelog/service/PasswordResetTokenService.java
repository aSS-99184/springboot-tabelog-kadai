package com.example.samuraitabelog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.PasswordResetToken;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.repository.PasswordResetTokenRepository;

@Service
public class PasswordResetTokenService {
	
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	
	public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
	}
        
    @Transactional
    // 新しくトークンを作って保存
    public void create(User user, String token) {
    	
    	PasswordResetToken passwordResetToken = new PasswordResetToken();
    	
    	passwordResetToken.setUser(user);
    	passwordResetToken.setToken(token);
    	passwordResetTokenRepository.save(passwordResetToken);
    	PasswordResetToken 	newpasswordResetToken = new PasswordResetToken();
    	
    }
    
    // トークンを検索して取得
    public PasswordResetToken getPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }
    
    // パスワード再設定をリクエストしたときに最新だけ有効
    public PasswordResetToken findLatestTokenByUser(User user) {
        return passwordResetTokenRepository.findTopByUserOrderByCreatedAtDesc(user);
    }
    
    // トークンが最新かどうかチェック
    private boolean isLatest(PasswordResetToken token) {
        PasswordResetToken latest = findLatestTokenByUser(token.getUser());
        return latest != null && token.getId().equals(latest.getId());
    }
    
    //　有効なトークンだけ返す
    public PasswordResetToken getValidToken(String tokenString) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(tokenString);
        if (token == null || !isLatest(token)) {
            return null;
        }
        return token;
    }
    
    public void delete(PasswordResetToken passwordResetToken) {
    	passwordResetTokenRepository.delete(passwordResetToken);
    }
    
   
}
