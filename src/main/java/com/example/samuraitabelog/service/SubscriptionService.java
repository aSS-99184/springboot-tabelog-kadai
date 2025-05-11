package com.example.samuraitabelog.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.samuraitabelog.entity.Role;
import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.repository.RoleRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class SubscriptionService {
	@Autowired
	private final UserRepository userRepository ;
	@Autowired
	private RoleRepository roleRepository;
	private final HttpServletRequest httpServletRequest;
	
	// application.properties から読み込む
	@Value("${stripe.api-key}")
    private String stripeApiKey;
	
	// サービスクラスが最初に使われたときに一回だけ自動でAPIキーの登録する
	@PostConstruct
	public void init() {
		Stripe.apiKey = stripeApiKey;
	}
	
	// コンストラクタ
	public SubscriptionService (UserRepository userRepository,RoleRepository roleRepository, HttpServletRequest httpServletRequest) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.httpServletRequest = httpServletRequest;
	}
	
	// Checkout セッションを作成
	public String createStripeSession(User user) {
		String customerId =user.getStripeCustomerId();
		String requestUrl = httpServletRequest.getRequestURL().toString();
		
		// 顧客IDが存在しない場合
		if (customerId == null || customerId.isEmpty()) {
				try {
				// 新しいStripe顧客を作成
				Customer customer = Customer.create(
						CustomerCreateParams.builder()
						.setEmail(user.getEmail())
						.build());

				// 新しく作成された顧客IDを取得
				customerId = customer.getId();
				// 顧客IDをユーザー情報に保存する処理
				user.setStripeCustomerId(customerId);
				userRepository.save(user);
				} catch (StripeException e) {
					// エラー処理
					e.printStackTrace();
					return "";
				}
		}
		
							
		
		SessionCreateParams params = SessionCreateParams.builder()
				// モードをサブスクリプションを選択
				.setMode(SessionCreateParams.Mode.SUBSCRIPTION)
				// ユーザーがサブスクリプションに成功したときに表示するページ
				.setSuccessUrl("http://localhost:8080/subscription/premium?status=success")
				// ユーザーが購入をキャンセルした時に表示するページ
				.setCancelUrl("http://localhost:8080/subscription/premium?status=failed")
				.setCustomer(customerId)
				.putMetadata("user_id", String.valueOf(user.getId()))
				// 商品定義
				.addLineItem(
						SessionCreateParams.LineItem.builder()
						.setQuantity(1L)
						.setPrice("price_1RN5JjIc44qIhXiA05JsVeO7")
						.build()
				)
				.build();
		
		try {
			
					
			// Stripeにセッション作成リクエストして、作成されたセッションのIDを返す
			Session session = Session.create(params);
			return session.getId();
			// エラー時にエラー内容をコンソール表示
		} catch (StripeException e) {
			// 画面にはエラー内容を見えないように
			e.printStackTrace();
			return "";
		}
	}
	
	// Checkout完了の通知を受けたときに、その顧客をアプリ側で「プレミアム会員」にする
	public void processSessionCompleted(Event event) {
		System.out.println("Webhook受信: checkout.session.completed");
		Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
        optionalStripeObject.ifPresentOrElse(stripeObject -> {
            Session session = (Session)stripeObject;
            System.out.println("セッション取得成功");

            try {
            	// SessionRetrieveParams を使って、Stripe セッションの詳細を取得する
            	SessionRetrieveParams params = SessionRetrieveParams.builder().addExpand("payment_intent").build();
                session = Session.retrieve(session.getId(), params, null);
                System.out.println("セッション詳細取得成功");
                
                // 支払い情報から user_id を取得
                Map<String, String> paymentIntentObject = session.getPaymentIntentObject().getMetadata();
                String userId = paymentIntentObject.get("user_id");
                
                System.out.println("user_id取得: " + userId);
                
                // user_id をもとに User をDBから取得
                User user = userRepository.findById(Integer.valueOf(userId)).orElseThrow(() -> new RuntimeException("User not found"));
                
                
                // そのユーザーのロールを "ROLE_PREMIUM" に変えて save()する。プレミアムロールを取得。
                Role premiumRole = roleRepository.findByName("ROLE_PREMIUM").orElseThrow(() -> new RuntimeException("Role 'ROLE_PREMIUM' not found"));
                		
                // ユーザーのロールを更新
                user.setRole(premiumRole);
                userRepository.save(user);
                System.out.println("ユーザーのロールをプレミアムに更新しました");
                httpServletRequest.getSession().setAttribute("user", user);
                
            } catch (StripeException e) {
                e.printStackTrace();
                System.out.println("エラー発生: " + e.getMessage());
            }
            System.out.println("予約一覧ページの登録処理が成功しました。");
            
        },() -> {
            System.out.println("予約一覧ページの登録処理が失敗しました。");
        });
	}
	
	// 有料会員解約
	public void cancelStripe(Integer userId) throws StripeException {
		// ユーザーを取得
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));

		String customerId = user.getStripeCustomerId();
		SubscriptionListParams params = SubscriptionListParams.builder()
				.setCustomer(customerId)
				.build();
		List<Subscription> subscription = Subscription.list(params).getData();
		for (Subscription sub : subscription) {
			sub.cancel();
		}
		System.out.println("サブスクリプションを解約しました");
	}
	
	// クレジットカード情報編集
	public String updateCardSession(User user) {
		Stripe.apiKey = stripeApiKey;
		String stripeCustomerId = user.getStripeCustomerId();
		if (stripeCustomerId == null || stripeCustomerId.isEmpty()) {
			throw new IllegalStateException("Stripeの顧客IDが登録されていません");
		}
		String requestUrl = httpServletRequest.getRequestURL().toString();

			SessionCreateParams params = SessionCreateParams.builder()
					.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
					.setMode(SessionCreateParams.Mode.SETUP)
					.setCustomer(stripeCustomerId)
					// ユーザーがカード登録を完了したときに戻る場所
					.setSuccessUrl("http://localhost:8080/subscription/payment?status=done")
					// ユーザーが処理を途中でキャンセルしたときに戻る場所
					.setCancelUrl("http://localhost:8080/subscription/payment?status=failed")
					.build();
			try {
				Session session = Session.create(params);
				return session.getId();
			} catch (StripeException e) {
				e.printStackTrace();
				return null; 
		}
			
	}
}
