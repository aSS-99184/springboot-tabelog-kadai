package com.example.samuraitabelog.form;

import java.time.LocalTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RestaurantEditForm {
	
	@NotNull
	private Integer id; 
	
	@NotBlank(message = "飲食店名を入力してください。")
	private String name;
	
	// 新しく画像をアップロードするためのファイル情報を受け取るフィールド
	private MultipartFile imageFile;
	
	@NotBlank(message = "説明を入力してください。")
	private String description;
	
	@NotBlank(message = "価格帯を入力してください(例：1,000円、2,000～3,000円など)。")
	private String priceRange;
	
	private LocalTime lunchOpeningTime;
	private LocalTime lunchClosingTime;
	
	private LocalTime dinnerOpeningTime;
	private LocalTime dinnerClosingTime;
	
	@NotBlank(message = "郵便番号を入力してください")
	private String postalCode;
	
	@NotBlank(message = "住所を入力してください。")
	private String address;
	
	@NotBlank(message = "電話番号を入力してください。")
	private String phoneNumber;
	
	@NotBlank(message = "定休日を入力してください(例：不定休、日曜日など)。")
	private String closeDays;
	
	@NotEmpty(message = "カテゴリをチェックしてください。複数選択できます。")
    private List<Integer> categoryIds;

	

}
