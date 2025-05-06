package com.example.samuraitabelog.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.Category;

public interface CategoryRepository extends JpaRepository<Category,Integer>{
	public Page<Category> findByNameLike(String keyword, Pageable pageable);
	public Page<Category> findAll (Pageable pageable);
	List<Category> findByRestaurant_Id(Integer restaurantId);
	Page<Category> findByRestaurant_Id(Integer restaurantId, Pageable pageable);
}
