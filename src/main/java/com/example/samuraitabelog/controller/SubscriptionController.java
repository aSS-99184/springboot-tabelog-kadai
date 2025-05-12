package com.example.samuraitabelog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.repository.RoleRepository;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.SubscriptionService;
import com.example.samuraitabelog.service.UserService;

@RequestMapping("/subscription")
@Controller
public class SubscriptionController {

	@Autowired
	private SubscriptionService subscriptionService;
	@Autowired
	private final UserService userService;
	@Autowired
	private RoleRepository roleRepository;
	@Value("${stripe.public.key}")
	private String stripePublicKey;
	
	public SubscriptionController(SubscriptionService subscriptionService, UserService userService, RoleRepository roleRepository) { 
		this.subscriptionService = subscriptionService;
		this.userService = userService;
		this.roleRepository = roleRepository;
	}

	// 有料会員登録ページを表示
	@GetMapping("/premium")
	public String showPremiumPage(@RequestParam(required = false) String status, Model model) {
		model.addAttribute("status", status);
		return "subscription/premium";
	}
	
	// Stripe セッションを作成して、Stripeの支払い画面へリダイレクト
	@PostMapping("/create-checkout-session")
	public String createCheckoutSession(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userDetailsImpl.getUser(); 
		if (user == null) {
			System.out.println("ユーザーはログインしていません"); 
			return "redirect:/login";
		} else {
	       System.out.println("ログイン中: " + user.getEmail()); 
		}
		
		try {
			 // UserServiceを使ってユーザーを更新
			userService.updateUserRoleToPremium(user);
			// Stripe セッションを作成
			String sessionId = subscriptionService.createStripeSession(user);
			// セッションIDをモデルに追加
			model.addAttribute("sessionId", sessionId);	
			// Stripe 公開鍵をモデルに追加
			model.addAttribute("stripePublicKey", stripePublicKey);
			
		// エラーが発生した場合
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("status", "failed");
			return "subscription/premium";
		}
		// プレミアム会員登録成功メッセージ
		model.addAttribute("status", "success");
		return "subscription/premium";
	}
	
	// クレジットカード情報変更ページを表示
	@PreAuthorize("hasRole('PREMIUM')")
	@GetMapping("/payment")
	public String showPaymentPage(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userDetailsImpl.getUser(); 
		if (user == null) {
		return "subscription/payment";
	}
		String sessionId = subscriptionService.updateCardSession(user);
		if (sessionId == null) {
			model.addAttribute("status", "failed");
			return "subscription/payment";
		}
		// JavaScriptに渡す
		model.addAttribute("sessionId", sessionId);
		return "subscription/payment";
	}
	
	// クレジットカード情報の編集セッション
	@PostMapping("/update-card-session")
	public String updateCardSession(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		User user = userDetailsImpl.getUser();
		if (user == null) {
			return "redirect:/login";
		}
		String sessionId = subscriptionService.updateCardSession(user);
		
		if (sessionId == null) {
			return "redirect:/subscription/payment?status=failed";
		}
		return "redirect:/subscription/payment?status=done"; 
	}
	
	// 有料会員解約ページを表示
	@PreAuthorize("hasRole('PREMIUM')")
	@GetMapping("/cancel")
	public String showCancelPage(@RequestParam(required = false) String status, Model model) {
		model.addAttribute("status", status);
		return "subscription/cancel";
	}
	
	// 解約処理を実行
	@PostMapping("/cancel-subscription")
	public String cancelSubscription(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		User user = userDetailsImpl.getUser();
		
		if (user == null) {
			return "redirect:/login";
		}
		try {
			// Stripeでのサブスクリプションをキャンセル
			subscriptionService.cancelStripe(user.getId());
			// UserServiceを使ってユーザーのロールを戻す
			userService.updateUserRoleToGeneral(user);
			return "redirect:/subscription/cancel?status=done";
	
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/subscription/cancel?status=failed";
		}
	}

}
