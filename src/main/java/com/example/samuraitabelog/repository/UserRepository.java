package com.example.samuraitabelog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	public User findByEmail(String email);
	// 会員一覧ページ　氏名・フリガナ検索
	public Page<User> findByNameLikeOrFuriganaLike(String nameKeyword, String furiganaKeyword, Pageable pageable);
}
