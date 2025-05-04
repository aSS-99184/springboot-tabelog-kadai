package com.example.samuraitabelog.form;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReservationRegisterForm {
	private Integer restaurantId;
    
    private Integer userId;    
        
    private LocalDateTime reservedDatetime;   
    
    private Integer guestCount;
}
