package com.example.samuraitabelog.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitabelog.entity.Reservation;
import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.ReservationRegisterForm;
import com.example.samuraitabelog.repository.ReservationRepository;
import com.example.samuraitabelog.repository.RestaurantRepository;
import com.example.samuraitabelog.repository.UserRepository;

@Service
public class ReservationService {
	private final ReservationRepository reservationRepository;  
    private final RestaurantRepository restaurantRepository;  
    private final UserRepository userRepository;
    
    public ReservationService(ReservationRepository reservationRepository, RestaurantRepository restaurantRepository, UserRepository userRepository) {
    	this.reservationRepository = reservationRepository;
    	this.restaurantRepository = restaurantRepository;
    	this.userRepository = userRepository;
    }
    
    @Transactional
    public void create(ReservationRegisterForm reservationRegisterForm) {
    	Reservation reservation = new Reservation();
    	Restaurant restaurant = restaurantRepository.getReferenceById(reservationRegisterForm.getRestaurantId());
    	User user = userRepository.getReferenceById(reservationRegisterForm.getUserId());
    	LocalDateTime reservedDatetime = reservationRegisterForm.getReservedDatetime();
    	Integer guestCount = reservationRegisterForm.getGuestCount();
    	
    	reservation.setRestaurant(restaurant);
    	reservation.setUser(user);
    	reservation.setReservedDatetime(reservedDatetime);
    	reservation.setGuestCount(guestCount);
    	
    	reservationRepository.save(reservation);    	
    }
}
