package com.example.samuraitabelog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.entity.Review;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.ReviewEditForm;
import com.example.samuraitabelog.form.ReviewRegisterForm;
import com.example.samuraitabelog.repository.ReviewRepository;

@Service
public class ReviewService {
		// ReviewRepositoryを、コンストラクタで受け取って使う準備
		private final ReviewRepository reviewRepository;
		
		public ReviewService(ReviewRepository reviewRepository) { 
			this.reviewRepository = reviewRepository;
		}
	
	   // レビューを作るメソッド
	   @Transactional
	   public void create(Restaurant restaurant, User user, ReviewRegisterForm reviewRegisterForm) {
	       
		   Review review = new Review();        
	       
	       review.setRestaurant(restaurant);                
	       review.setUser(user);
	       review.setScore(reviewRegisterForm.getScore());
	       review.setContent(reviewRegisterForm.getContent());
	                   
	       reviewRepository.save(review);
	   }     
	   
	   // レビューを編集するメソッド
	   @Transactional
	   public void update(ReviewEditForm reviewEditForm) {
		   // 編集したいレビューIDを取得
	       Review review = reviewRepository.getReferenceById(reviewEditForm.getId());        
	       
	       review.setScore(reviewEditForm.getScore());                
	       review.setContent(reviewEditForm.getContent());
	                   
	       reviewRepository.save(review);
	   }    
	   
	   // すでに特定にユーザーがレビューしているかチェック
	   public boolean hasUserAlreadyReviewed(Restaurant restaurant, User user) {
	       return reviewRepository.findByRestaurantAndUser(restaurant, user) != null;
	   }

}
