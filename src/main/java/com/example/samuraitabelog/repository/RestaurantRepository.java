package com.example.samuraitabelog.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.Restaurant;

public interface RestaurantRepository extends  JpaRepository<Restaurant, Integer>{
	// 店名キーワード検索
	public Page<Restaurant> findByNameLike(String keyword, Pageable pageable);
	
	// 店名 or カテゴリ名検索
	public Page<Restaurant> findByNameLikeOrCategoryNameLike(String nameKeyword, String categoryKeyword, Pageable pageable);

	// カテゴリ名検索
	public Page<Restaurant> findByCategoryNameLike(String catrgoryName, Pageable pageable);
	
	// 新着順10件
	public List<Restaurant> findTop10ByOrderByCreatedAtDesc();
	
	
	
}
