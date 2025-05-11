package com.example.samuraitabelog.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentForm {
	@NotBlank(message = "カード番号を入力してください。")
	@Pattern(regexp = "\\d{16}", message = "カード番号は16桁の数字で入力してください。")
	 private String cardNumber;
	
	@NotBlank(message = "有効期限を入力してください。")
	@Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "有効期限はMM/YY形式で入力してください。")
	private String expirationDate;
	
	@NotBlank(message = "セキュリティコードを入力してください。")
	@Pattern(regexp = "\\d{3,4}", message = "セキュリティコードは3桁または4桁の数字で入力してください。")
	private String cvc;
	
}
