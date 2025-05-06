package com.example.samuraitabelog.form;

import jakarta.validation.constraints.NotBlank;

public class paymentEditForm {
	@NotBlank(message = "カード番号を入力してください。")
	private String cardNumber;
	
	@NotBlank(message = "有効期限を入力してください。")
	private String expirationDate;
	
	@NotBlank(message = "セキュリティコードを入力してください。")
	private String cvc;
	

}
