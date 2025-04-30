package com.example.samuraitabelog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.PasswordResetToken;
import com.example.samuraitabelog.entity.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer>{
	public PasswordResetToken findByToken(String token);
	// 最新のトークンを1件取得
	public PasswordResetToken findTopByUserOrderByCreatedAtDesc(User user);

}
