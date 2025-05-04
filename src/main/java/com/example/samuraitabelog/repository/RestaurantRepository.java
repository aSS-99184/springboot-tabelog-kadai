package com.example.samuraitabelog.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.Restaurant;

public interface RestaurantRepository extends  JpaRepository<Restaurant, Integer>{
	public Page<Restaurant> findByNameLike(String keyword, Pageable pageable);
	
	public List<Restaurant> findTop10ByOrderByCreatedAtDesc();
	
	

}
