package com.example.samuraitabelog.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reservations")
@Data
public class Reservation {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Integer id;
	
	// 予約は1つの飲食店をもつ、飲食店は複数の予約をもつ
	@ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
	
	// 予約は1つのユーザーをもつ、ユーザーは複数の予約をもつ
	@ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
	
	@Column(name = "reserved_datetime")
	private LocalDateTime reservedDatetime;
	
	@Column(name = "guest_count")
	private Integer guestCount;
	
	@Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;
	
	@Column(name = "updated_at", insertable = false, updatable = false)
    private Timestamp updatedAt; 

}
