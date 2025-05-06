package com.example.samuraitabelog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.VerificationToken;

@Transactional
public interface VerificationTokenRepository extends JpaRepository< VerificationToken, Integer> {
    public VerificationToken findByToken(String token);
	public void deleteByUserId(Integer userId);
}
