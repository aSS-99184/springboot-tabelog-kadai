package com.example.samuraitabelog.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "categories")
@Data
public class Category {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
	private Integer id;
	
	@Column(name = "name")
    private String name;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at" )
	private LocalDateTime updatedAt;
	
	// Restaurantとは多対多の関係
	// Restaurant側のcategoriesフィールドにマッピング
	@ManyToMany(mappedBy = "category")
	// 自動生成から除外する
	@ToString.Exclude
	// カテゴリの中にある「レストランの情報」はスキップ
	@JsonIgnore
    private Set<Restaurant> restaurant = new HashSet<>();
	
	@PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
	
	@PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
	
	
	

}
