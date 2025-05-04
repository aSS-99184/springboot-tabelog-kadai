package com.example.samuraitabelog.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reviews")
@Data
public class Review {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id")
	private Integer id;
	
	// 複数のレビュー、一つの飲食店
	@ManyToOne
	   @JoinColumn(name = "restaurant_id")
	   private Restaurant restaurant; 
	
	// 複数のレビュー、一人のユーザー
	@ManyToOne
	   @JoinColumn(name = "user_id")
	   private User user;
	
	@Column(name = "content")
	   private String content;    
	   
	@Column(name = "score")
	private Integer score;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	   private Timestamp createdAt;
	   
	   @Column(name = "updated_at", insertable = false, updatable = false)
	   private Timestamp updatedAt;

}
