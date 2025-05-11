package com.example.samuraitabelog.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentEditForm {
	@NotBlank(message = "カード番号を入力してください。")
	private String cardNumber;
	
	@NotBlank(message = "有効期限を入力してください。")
	private String expirationDate;
	
	@NotBlank(message = "セキュリティコードを入力してください。")
	private String cvc;
	

}
