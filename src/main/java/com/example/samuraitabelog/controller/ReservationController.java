package com.example.samuraitabelog.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitabelog.entity.Reservation;
import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.ReservationInputForm;
import com.example.samuraitabelog.form.ReservationRegisterForm;
import com.example.samuraitabelog.repository.ReservationRepository;
import com.example.samuraitabelog.repository.RestaurantRepository;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.ReservationService;

@Controller
public class ReservationController {
	private final ReservationRepository reservationRepository;
	private final RestaurantRepository restaurantRepository;
	private final ReservationService reservationService;
	
	public ReservationController(ReservationRepository reservationRepository, RestaurantRepository restaurantRepository, ReservationService reservationService) {        
        this.reservationRepository = reservationRepository;     
        this.restaurantRepository = restaurantRepository;
        this.reservationService = reservationService;
    }
	
	// 予約一覧ページを表示するためのindex()メソッド
	@GetMapping("/reservations")
	// @AuthenticationPrincipalで現在ログイン中のユーザー情報をuserDetailsImplオブジェクトとして取得
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, 
    					@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, 
    					Model model) {
        User user = userDetailsImpl.getUser();
        Page<Reservation> reservationPage = reservationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        model.addAttribute("reservationPage", reservationPage);         
        
        return "reservations/index";
    }
	
	// 予約フォームの送信先（/restaurants/{id}/reservations/input）を担当するinput()メソッド
	@GetMapping("/restaurants/{id}/reservations/input")
	public String input(@PathVariable(name = "id") Integer id,
            			@ModelAttribute @Validated ReservationInputForm reservationInputForm,BindingResult bindingResult,RedirectAttributes redirectAttributes,
            			Model model) {
		
		// どの飲食店か識別する。予約内容に不備があったときに、レストラン詳細ページに戻すために使う。
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		
		if (bindingResult.hasErrors()) {            
            model.addAttribute("restaurant", restaurant);            
            model.addAttribute("errorMessage", "予約内容に不備があります。"); 
            return "restaurants/show";
        }
		
		redirectAttributes.addFlashAttribute("reservationInputForm", reservationInputForm);
		return "redirect:/restaurants/{id}/reservations/confirm";
	}
	
	// 予約内容の確認ページを表示する。input()メソッドのリダイレクト先であるconfirm()メソッド
	@GetMapping("/restaurants/{id}/reservations/confirm")
	public String confirm(@PathVariable(name = "id") Integer id,
            				@ModelAttribute ReservationInputForm reservationInputForm,
            				@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,                          
            				Model model) {
		// 飲食店の情報を取得
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		// ログインしているユーザー情報を取得
		User user = userDetailsImpl.getUser();
		// 予約日時を取得
		LocalDateTime reservedDatetime = reservationInputForm.getReservationDateTime();
		// 人数を取得
		Integer guestCount = reservationInputForm.getGuestCount();
		
		// 予約に必要な情報を取得して、インスタンス化し、ビューに渡す準備をする
		ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm(restaurant.getId(), user.getId(), reservedDatetime, guestCount);
		
		// 予約する飲食店の情報をビューに渡す
		model.addAttribute("restaurant", restaurant);
		// 格納されている予約情報をビューに渡す(ユーザーが確認できるようになる)
		model.addAttribute("reservationRegisterForm", reservationRegisterForm); 
		
		return "reservations/confirm";
	}
	
	// 予約情報を登録するcreate()メソッド
	@PostMapping("/restaurants/{id}/reservations/create")
    public String create(@ModelAttribute ReservationRegisterForm reservationRegisterForm) {                
		reservationService.create(reservationRegisterForm);        
        
        return "redirect:/reservations?reserved";
	}
}
