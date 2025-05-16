package com.example.samuraitabelog.form;

import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Component
@Data
public class PasswordEditForm {
	
	@NotBlank(message = "パスワードを入力してください。")
	@Length(message = "パスワードは8文字以上で入力してください。")
	private String password;
	
	@NotBlank(message = "確認用パスワードを入力してください。")
	@Length(message = "パスワードは8文字以上で入力してください。")
	private String passwordConfirm;
	
	private String token;
}
