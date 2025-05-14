package com.example.samuraitabelog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitabelog.entity.Favorite;
import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer>{
	public Page<Favorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
	public Favorite findByRestaurantAndUser(Restaurant restaurant, User user);
	public void deleteByUserId(Integer userId);

}
