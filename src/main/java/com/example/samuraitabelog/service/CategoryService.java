package com.example.samuraitabelog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.Category;
import com.example.samuraitabelog.form.CategoryEditForm;
import com.example.samuraitabelog.form.CategoryRegisterForm;
import com.example.samuraitabelog.repository.CategoryRepository;

@Service
public class CategoryService {
	private final CategoryRepository categoryRepository;  
	
	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}
	
	// 登録機能
	@Transactional
	public void create(CategoryRegisterForm categoryRegisterForm) {
		Category category = new Category();
		category.setName(categoryRegisterForm.getName());
		
		categoryRepository.save(category);
	}
	
	@Transactional
	 public void update(CategoryEditForm categoryEditForm) {
		Category category = categoryRepository.getReferenceById(categoryEditForm.getId());
		category.setName(categoryEditForm.getName());
		
		categoryRepository.save(category);
	}
	
	
	
	

}
