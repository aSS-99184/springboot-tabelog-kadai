package com.example.samuraitabelog.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "restaurants")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Restaurant {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "restaurant_id")
	@EqualsAndHashCode.Include
	private Integer id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "image")
	private String image;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "price_range")
	private String priceRange;
	
	@Column(name = "lunch_opening_time")
	private LocalTime lunchOpeningTime;
	
	@Column(name = "lunch_closing_time")
	private LocalTime lunchClosingTime;
	
	@Column(name = "dinner_opening_time")
	private LocalTime dinnerOpeningTime;
	
	@Column(name = "dinner_closing_time")
	private LocalTime dinnerClosingTime;
	
	@Column(name = "postal_code")
	private String postalCode;
	
	@Column(name = "address")
	private String address;
	
	@Column(name = "phone_number")
	private String phoneNumber;
	
	@Column(name = "close_days")
	private String closeDays;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@Column(name = "update_at" )
	private LocalDateTime updateAt;
	
	
	// Categoryと多対多の関係
	@ManyToMany
	@JoinTable(
		// 中間テーブル名
		name = "category_restaurants",
		// Restaurantエンティティの外部キー
		joinColumns = @JoinColumn(name = "restaurant_id"),
		// Categoryエンティティの外部キー
		inverseJoinColumns = @JoinColumn(name = "category_id") 
	)
	// Categoryを除外
	 @ToString.Exclude 
	private Set<Category> category  = new HashSet<>();
	
	
	
	// 今回は新着順表示で要る
	// Java側で日時を制御(後でロジック入れれるかも)
	@PrePersist
	private void prepersist() {
		// 登録時の日時
		this.createdAt = LocalDateTime.now();
		// 登録時に最終更新日もいれる
		this.updateAt = LocalDateTime.now();
	}
	
	@PreUpdate
	private void preUpdate() {
		this.updateAt = LocalDateTime.now();
	}
	
}
