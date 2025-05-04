package com.example.samuraitabelog.form;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationInputForm {
	@NotNull(message = "予約したい日時を選択してください。")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") 
	private LocalDateTime reservationDateTime;   
	
    
    @NotNull(message = "人数を入力してください。")
    @Min(value = 1, message = "人数は1人以上に設定してください。")
    private Integer guestCount; 

}
