package com.example.samuraitabelog.controller;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitabelog.entity.Category;
import com.example.samuraitabelog.entity.Restaurant;
import com.example.samuraitabelog.form.RestaurantEditForm;
import com.example.samuraitabelog.form.RestaurantRegisterForm;
import com.example.samuraitabelog.repository.CategoryRepository;
import com.example.samuraitabelog.repository.RestaurantRepository;
import com.example.samuraitabelog.service.CategoryService;
import com.example.samuraitabelog.service.RestaurantService;

@Controller
@RequestMapping("/admin/restaurants")
public class AdminRestaurantController {
	private final RestaurantRepository restaurantRepository;
	private final RestaurantService restaurantService;
	private final CategoryRepository categoryRepository;
	private final CategoryService categoryService;
	
	public AdminRestaurantController(RestaurantRepository restaurantRepository, RestaurantService restaurantService, CategoryRepository categoryRepository, CategoryService categoryService) {
		this.restaurantRepository = restaurantRepository;
		this.restaurantService = restaurantService;
		this.categoryRepository = categoryRepository;
		this.categoryService = categoryService;
	}
	
	@GetMapping
	public String index(Model model, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, @RequestParam(name = "keyword", required = false)String keyword) {
		Page<Restaurant> restaurantPage;
		
		if(keyword != null && !keyword.isEmpty()) {
			restaurantPage = restaurantRepository.findByNameLike("%" + keyword + "%", pageable);
		} else {
			restaurantPage = restaurantRepository.findAll(pageable);
		}
		
		model.addAttribute("restaurantPage",restaurantPage);
		model.addAttribute("keyword", keyword);
		
		return"admin/restaurants/index";
	}
	
	// 詳細ページ
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, Model model) {
		Restaurant restaurant= restaurantRepository.getReferenceById(id);
		// そのレストランに紐づくカテゴリだけ取得
		List<Category> categories = categoryRepository.findByRestaurant_Id(id);
		model.addAttribute("categories", categories);
		model.addAttribute("restaurant", restaurant);
		
		return "admin/restaurants/show";
	}
	
	// 登録ボタンから登録ページを表示
	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("restaurantRegisterForm", new RestaurantRegisterForm());
		 // チェックボックス用カテゴリ一覧
		model.addAttribute("allCategories", categoryRepository.findAll());
		return "admin/restaurants/register";
	}
	
	// 登録フォームの送信ボタンからベースに内容を保存
	@PostMapping("/create")
	public String create(@ModelAttribute @Validated RestaurantRegisterForm restaurantRegisterForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
		if (bindingResult.hasErrors()){
			// バリデーションエラー時にもカテゴリ一覧を渡す
			model.addAttribute("allCategories", categoryRepository.findAll());
			return "admin/restaurants/register";
		}
		
		restaurantService.create(restaurantRegisterForm);	
		redirectAttributes.addFlashAttribute("successMessage", "飲食店を登録しました。");
		
		return "redirect:/admin/restaurants";
	}
	
	// 編集フォームの表示
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id, Model model) {
		Restaurant restaurant = restaurantRepository.getReferenceById(id);
		// 今登録されている画像をテンプレートに渡すために一時的に保存
		String image = restaurant.getImage();
	
		// ローカル変数も categoryIds にする
		List<Integer> categoryIds = restaurant.getCategory().stream().map(Category::getId).toList();
		
		RestaurantEditForm restaurantEditForm = new RestaurantEditForm(restaurant.getId(), restaurant.getName(), null, restaurant.getDescription(), restaurant.getPriceRange(), restaurant.getLunchOpeningTime(), restaurant.getLunchClosingTime(), restaurant.getDinnerOpeningTime(), restaurant.getDinnerClosingTime(), restaurant.getPostalCode(), restaurant.getAddress(), restaurant.getPhoneNumber(), restaurant.getCloseDays(),categoryIds);
		model.addAttribute("image", image);
		model.addAttribute("restaurantEditForm", restaurantEditForm);
		model.addAttribute("allCategories", categoryRepository.findAll());
		
		return "admin/restaurants/edit";
		 
	}
	
	// 編集フォームから送られてきたデータを受け取って、飲食店情報を更新
	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated RestaurantEditForm restaurantEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes,Model model) {
		if(bindingResult.hasErrors()) {
		model.addAttribute("allCategories", categoryService.findAll());
		return"admin/restaurants/edit";
		
		}
		
		restaurantService.update(restaurantEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "飲食店情報を編集しました。");
		
		return"redirect:/admin/restaurants";
	}
	
	
	// 削除
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
		restaurantRepository.deleteById(id);
		
		redirectAttributes.addFlashAttribute("successMessage", "飲食店を削除しました。");
		
		return "redirect:/admin/restaurants";
	}
}
