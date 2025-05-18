package com.example.samuraitabelog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.repository.RoleRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.example.samuraitabelog.security.UserDetailsImpl;
import com.example.samuraitabelog.service.SubscriptionService;
import com.example.samuraitabelog.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequestMapping("/subscription")
@Controller
public class SubscriptionController {

	@Autowired
	private SubscriptionService subscriptionService;
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Value("${stripe.public.key}")
	private String stripePublicKey;
	
	
	
	public SubscriptionController(SubscriptionService subscriptionService, UserService userService, RoleRepository roleRepository,UserRepository userRepository) { 
		this.subscriptionService = subscriptionService;
		this.userService = userService;
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
	}

	// 有料会員登録ページを表示
	@GetMapping("/premium")
	public String showPremiumPage(@RequestParam(required = false) String status) {
		return "subscription/premium";
	}
	
	// 有料会員登録 Stripe セッションを作成して、Stripeの支払い画面へリダイレクト
	@PostMapping("/create-checkout-session")
	public String createCheckoutSession(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, 
										RedirectAttributes redirectAttributes,
										Model model) {
		

		// 現在ログインしているユーザーを取得
		User user = userDetailsImpl.getUser(); 
				
		// ログインしていない場合はログインページへリダイレクト
		if (user == null) {
			System.out.println("ユーザーはログインしていません"); 
			return "redirect:/login";
		} 
			try {
				String customerId = user.getStripeCustomerId();
				// Stripeの顧客IDをつくるstripeCustomerId
				if (user.getStripeCustomerId() == null || user.getStripeCustomerId().isEmpty()) {

					Customer customer = subscriptionService.createCustomer(user);
					// 作った顧客IDをDBに保存
					userService.saveStripeCustomerId(user, customer.getId());
					customerId = customer.getId(); 
				}
				
				String baseUrl = "http://localhost:8080/subscription/premium";
				String successUrl = baseUrl + "?status=success";
				String cancelUrl = baseUrl + "?status=failed";
				

			// Checkout セッションのパラメータ作成
			SessionCreateParams params = SessionCreateParams.builder()
					// モードをサブスクリプションを選択
					.setMode(SessionCreateParams.Mode.SUBSCRIPTION)
					// ユーザーがサブスクリプションに成功したときに表示するページ
					.setSuccessUrl(successUrl)
					// ユーザーが購入をキャンセルした時に表示するページ
					.setCancelUrl(cancelUrl)
					.setCustomer(customerId)
					.putMetadata("user_id", String.valueOf(user.getId()))
					// 商品定義
					.addLineItem(
							SessionCreateParams.LineItem.builder()
							.setQuantity(1L)
							.setPrice("price_1RPUhbRSOota6fUsLrSYQ1Pu")
							.build()
					)
					.build();
			
			// セッション作成
	        Session session = Session.create(params);
	        model.addAttribute("sessionId", session.getId());

	        return "subscription/premium";
			
			} catch (StripeException e) {
				e.printStackTrace();
				redirectAttributes.addFlashAttribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。");
				return "subscription/premium";
			}
	}

		
	// クレジットカード情報変更ページを表示
	@GetMapping("/payment")
	public String showPaymentPage(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model, HttpServletRequest httpServletRequest) {
		User user = userDetailsImpl.getUser(); 
		if (user == null) {
		return "subscription/payment";
	}
		
		// カード情報更新用のセッションIDを取得
		String sessionId = subscriptionService.updateCardSession(user);
		if (sessionId == null) {
			model.addAttribute("status", "failed");
			return "subscription/payment";
		}
		// JavaScriptに渡す
		model.addAttribute("sessionId", sessionId);
		return "subscription/payment";
	}
	
	// クレジットカード情報編集のセッション
	@PostMapping("/update-card-session")
	public String updateCardSession(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, HttpServletRequest httpServletRequest) {
		User user = userDetailsImpl.getUser();
		if (user == null) {
			return "redirect:/login";
		}
		// セッションIDをつくる
		String sessionId = subscriptionService.updateCardSession(user);
		
		if (sessionId == null) {
			return "redirect:/subscription/payment?status=failed";
		}
		return "redirect:/subscription/payment?status=done"; 
	}
	
	// 有料会員解約ページを表示
	@GetMapping("/cancel")
	public String showCancelPage(@RequestParam(required = false) String status, Model model) {
		model.addAttribute("status", status);
		return "subscription/cancel";
	}
	
	// 有料会員解約処理
	//どこかで失敗したらまとめて
	@Transactional
	@PostMapping("/cancel-subscription")
	public String cancelSubscription(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
									HttpServletRequest request, HttpServletResponse response,
									RedirectAttributes redirectAttributes) {
		
		User user = userDetailsImpl.getUser();

		try {
			// Stripeでのサブスクリプションをキャンセル
			subscriptionService.cancelStripe(user.getId());
			// UserServiceを使ってユーザーのロールを戻す
			User updatedUser = userService.updateUserRoleToGeneral(user);
			
			// 認証情報を更新
			UserDetailsImpl newDetails = userDetailsImpl.updateUserRole(updatedUser);
			Authentication newAuth = new UsernamePasswordAuthenticationToken( newDetails, null, newDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(newAuth);
			// ログアウト
			new SecurityContextLogoutHandler().logout(request, response, newAuth);
			
			redirectAttributes.addFlashAttribute("successMessage", "有料会員の解約が完了しました。");
			return "redirect:/subscription/cancel?status=done";
	
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/subscription/cancel?status=failed";
		}
	}
	
}
