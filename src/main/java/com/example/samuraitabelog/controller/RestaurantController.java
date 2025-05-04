package com.example.samuraitabelog.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitabelog.entity.Favorite;
import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.entity.Review;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.form.ReservationInputForm;
import com.example.samuraitabelog.repository.FavoriteRepository;
import com.example.samuraitabelog.repository.RestaurantRepository;
import com.example.samuraitabelog.repository.ReviewRepository;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.FavoriteService;
import com.example.samuraitabelog.service.ReviewService;

@Controller
@RequestMapping("/restaurants")
public class RestaurantController {
	private final RestaurantRepository restaurantRepository;
	private final ReviewRepository reviewRepository;
	private final ReviewService reviewService;
	private final FavoriteRepository favoriteRepository; 
    private final FavoriteService favoriteService;
	
	public RestaurantController(RestaurantRepository restaurantRepository, ReviewRepository reviewRepository, ReviewService reviewService, FavoriteRepository favoriteRepository,FavoriteService favoriteService) {
		this.restaurantRepository = restaurantRepository;
		this.reviewRepository = reviewRepository;
		this.reviewService = reviewService;
		this.favoriteRepository = favoriteRepository;
        this.favoriteService = favoriteService;
	}
	
	// 店舗名で検索
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model)
	{
		Page<Restaurant> restaurantPage;
		
		if (keyword != null && !keyword.isEmpty()) {
			restaurantPage = restaurantRepository.findByNameLike("%" + keyword + "%", pageable);
		} else {
			restaurantPage = restaurantRepository.findAll(pageable);
		}
		
		model.addAttribute("restaurantPage", restaurantPage);
		model.addAttribute("keyword", keyword);
		
		return "restaurants/index";
	}
	
	// 特定の店舗の詳細をURLでリクエストして、店舗のshowを表示する
	 @GetMapping("/{id}")
	 	// @PathVariable アノテーションでURLのパスから id を取得して、メソッドの引数に渡す。
	    public String show(@PathVariable(name = "id") Integer id, Model model, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		 Restaurant restaurant = restaurantRepository.getReferenceById(id);
		// お気に入り登録がまだない状態でもエラーが出ないようにする
		 Favorite favorite = null;
	    // デフォルトが「ユーザーはまだその飲食店にレビューしていない」
		boolean hasUserAlreadyReviewed = false;
		//デフォルトが「まだお気に入り登録されていない状態」
		boolean isFavorite = false;
		
		// ユーザーがログインしていれば
		if (userDetailsImpl != null) {
			// ログイン中のユーザー情報を取り出す
            User user = userDetailsImpl.getUser();
            // そのユーザーがすでにこの店舗にレビューしたかどうかをチェック
            hasUserAlreadyReviewed = reviewService.hasUserAlreadyReviewed(restaurant, user); 
            
            // そのユーザーがすでにその店舗をお気に入り登録しているなら情報を取り出して、favoriteに保存する。
            isFavorite = favoriteService.isFavorite(restaurant, user);
            if (isFavorite) {
                favorite = favoriteRepository.findByRestaurantAndUser(restaurant, user);
            } 
        }
        
        List<Review> newReviews = reviewRepository.findTop6ByRestaurantOrderByCreatedAtDesc(restaurant);        
        long totalReviewCount = reviewRepository.countByRestaurant(restaurant);
		
	        model.addAttribute("restaurant", restaurant);
	        model.addAttribute("reservationInputForm", new ReservationInputForm());
	        model.addAttribute("hasUserAlreadyReviewed", hasUserAlreadyReviewed);
	        model.addAttribute("newReviews", newReviews);        
	        model.addAttribute("totalReviewCount", totalReviewCount);
	        model.addAttribute("favorite", favorite);
	        model.addAttribute("isFavorite", isFavorite); 
	        
	        return "restaurants/show";
	    }

}
