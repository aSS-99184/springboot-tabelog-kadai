package com.example.samuraitabelog.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.form.RestaurantEditForm;
import com.example.samuraitabelog.form.RestaurantRegisterForm;
import com.example.samuraitabelog.repository.RestaurantRepository;

@Service
public class RestaurantService {
	private final RestaurantRepository restaurantRepository;
	
	public RestaurantService(RestaurantRepository restaurantRepository) {
		this.restaurantRepository = restaurantRepository;
	}
	
	// 登録機能
	@Transactional
	public void create(RestaurantRegisterForm restaurantRegisterForm) {
		Restaurant restaurant = new Restaurant();
		MultipartFile imageFile = restaurantRegisterForm.getImageFile();
		// 画像ファイルがアップロードされたらエンティティimageカラムに保存
		if (!imageFile.isEmpty()) {
			String image = imageFile.getOriginalFilename();
			String hashedImage = generateNewFileName(image);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImage);
			copyImageFile(imageFile, filePath);
			restaurant.setImage(hashedImage);
		}
		
		restaurant.setName(restaurantRegisterForm.getName());
		restaurant.setDescription(restaurantRegisterForm.getDescription());
		restaurant.setPriceRange(restaurantRegisterForm.getPriceRange());
		restaurant.setLunchOpeningTime(restaurantRegisterForm.getLunchOpeningTime());
		restaurant.setLunchClosingTime(restaurantRegisterForm.getLunchClosingTime());
		restaurant.setDinnerOpeningTime(restaurantRegisterForm.getDinnerOpeningTime());
		restaurant.setDinnerClosingTime(restaurantRegisterForm.getDinnerClosingTime());
		restaurant.setAddress(restaurantRegisterForm.getAddress());
		restaurant.setPhoneNumber(restaurantRegisterForm.getPhoneNumber());
		restaurant.setCloseDays(restaurantRegisterForm.getCloseDays());
		restaurant.setPostalCode(restaurantRegisterForm.getPostalCode());

		restaurantRepository.save(restaurant);
	}
	
	@Transactional
	public void update(RestaurantEditForm restaurantEditForm) {
		Restaurant restaurant = restaurantRepository.getReferenceById(restaurantEditForm.getId());
		MultipartFile imageFile = restaurantEditForm.getImageFile();
		
		if(!imageFile.isEmpty()) {
			String image = imageFile.getOriginalFilename();
			String hashedImage = generateNewFileName(image);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImage);
			copyImageFile(imageFile, filePath);
			restaurant.setImage(hashedImage);
		}
		
		restaurant.setName(restaurantEditForm.getName());
		restaurant.setDescription(restaurantEditForm.getDescription());
		restaurant.setPriceRange(restaurantEditForm.getPriceRange());
		restaurant.setLunchOpeningTime(restaurantEditForm.getLunchOpeningTime());
		restaurant.setLunchClosingTime(restaurantEditForm.getLunchClosingTime());
		restaurant.setDinnerOpeningTime(restaurantEditForm.getDinnerOpeningTime());
		restaurant.setDinnerClosingTime(restaurantEditForm.getDinnerClosingTime());
		restaurant.setAddress(restaurantEditForm.getAddress());
		restaurant.setPhoneNumber(restaurantEditForm.getPhoneNumber());
		restaurant.setCloseDays(restaurantEditForm.getCloseDays());
		restaurant.setPostalCode(restaurantEditForm.getPostalCode());

		restaurantRepository.save(restaurant);
	}
	
	
	
	// 上書き防止。画像ファイル名をランダムに変更して保存
	public String generateNewFileName(String fileName) {
		String[] fileNames = fileName.split("\\.");
		for (int i = 0; i < fileNames.length - 1; i++) {
			fileNames[i] = UUID.randomUUID().toString();
		}
		String hashedFileName = String.join(".", fileNames);
		return hashedFileName;
	}
	
	// アップロードされた画像ファイルをサーバーのフォルダに保存
	public void copyImageFile(MultipartFile imageFile, Path filePath) {
		try {
			Files.copy(imageFile.getInputStream(), filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
