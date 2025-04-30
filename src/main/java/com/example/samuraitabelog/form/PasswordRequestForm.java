package com.example.samuraitabelog.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
	public class PasswordRequestForm {
		@NotBlank(message = "登録してあるメールアドレスを入力してください。")
		@Email(message = "メールアドレスは正しい形式で入力してください。")
		private String email;
		
}
