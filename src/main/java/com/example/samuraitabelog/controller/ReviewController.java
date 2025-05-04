package com.example.samuraitabelog.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.entity.Review;
import com.example.samuraitabelog.form.ReviewRegisterForm;
import com.example.samuraitabelog.repository.RestaurantRepository;
import com.example.samuraitabelog.repository.ReviewRepository;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.ReviewService;

@Controller
@RequestMapping("/restaurants/{estaurantId}/reviews")
public class ReviewController {
	
	private final ReviewRepository reviewRepository;
	private final RestaurantRepository restaurantRepository;
	private final ReviewService reviewService;
	
	public ReviewController(ReviewRepository reviewRepository, RestaurantRepository restaurantRepository, ReviewService reviewService) {
		this.reviewRepository = reviewRepository;
		this.restaurantRepository = restaurantRepository;
		this.reviewService = reviewService;
	}
	
	@GetMapping
	public String index(@PathVariable(name = "restaurantId") Integer restaurantId, 
						@PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable, Model model) {
		Restaurant resutaurant = restaurantRepository.getReferenceById(restaurantId);
		Page<Review> reviewPage = reviewRepository.findByRestaurantOrderByCreatedAtDesc(resutaurant, pageable); 
		
		model.addAttribute("resutaurant", resutaurant);
		model.addAttribute("reviewPage", reviewPage);
		
		return "reviews/index";
	}
	
	@GetMapping("/register")
	public String register(@PathVariable(name = "restaurantId") Integer restaurantId, Model model) {
		Restaurant resutaurant = restaurantRepository.getReferenceById(restaurantId);
		
		model.addAttribute("resutaurant", resutaurant);
		model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());
		
		return "reviews/register";
	}
	
	@PostMapping("/create")
	public String create(@PathVariable(name = "restaurantId") Integer restaurantId,
            			@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            			@ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm,BindingResult bindingResult,RedirectAttributes redirectAttributes,
            			Model model) {
		Restaurant resutaurant = restaurantRepository.getReferenceById(restaurantId);
		User user = userDetailsImpl.getUser();
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("resutaurant", resutaurant);
	        return "reviews/register";
	    }
		
		reviewService.create(resutaurant, user, reviewRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを投稿しました。");
		
		return "redirect:/resutaurants/{restaurantId}";
	}
	
	@GetMapping("/{reviewId}/edit")
	public String edit(@PathVariable(name = "restaurantId") Integer restaurantId, @PathVariable(name = "reviewId") Integer reviewId, Model model) {
		
	}
	
	@PostMapping("/{reviewId}/update")
	public String update() {
		
	}
	
	@PostMapping("/{reviewId}/delete")
	public String delete() {
		
	}
}
