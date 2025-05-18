package com.example.samuraitabelog.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.samuraitabelog.entity.User;
import com.example.samuraitabelog.repository.RoleRepository;
import com.example.samuraitabelog.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class SubscriptionService {
	private final UserRepository userRepository ;
	private final UserService userService;
	private final RoleRepository roleRepository;
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
	public SubscriptionService (UserRepository userRepository, UserService userService, RoleRepository roleRepository, HttpServletRequest httpServletRequest) {
		this.userRepository = userRepository;
		this.userService = userService;
		this.roleRepository = roleRepository;
		this.httpServletRequest = httpServletRequest;
	}
	
	
	// Stripeの顧客を作るメソッド
		 public Customer createCustomer(User user) throws StripeException {
		        CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
		            .setName(user.getName())
		            .setEmail(user.getEmail())
		            .build();

		        return Customer.create(customerCreateParams);
		    }
	
	// Checkout完了の通知を受けたときに、その顧客をアプリ側で「プレミアム会員」にする
	public ResponseEntity<String>processSessionCompleted(Event event) {
		
		System.out.println("Webhook受信: checkout.session.completed");
		try {
			Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();

			if (!optionalStripeObject.isPresent()) {
	            return ResponseEntity.status(400).body("Stripeデータがありません");
	        }
			
		Session session = (Session) optionalStripeObject.get();
	    System.out.println("セッション取得成功");
	    
	    
	    PaymentIntent paymentIntent = (PaymentIntent) session.getPaymentIntentObject();
	    if (paymentIntent == null) {
            return ResponseEntity.status(400).body("PaymentIntentが取得できません");
        }
		
	    String paymentMethodId = paymentIntent.getPaymentMethod(); 
	    if (paymentMethodId != null && !paymentMethodId.isEmpty()) {
            try {
                PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
                paymentMethod.attach(PaymentMethodAttachParams.builder()
                    .setCustomer(session.getCustomer())
                    .build());
                System.out.println("支払い方法を顧客にアタッチしました");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("支払い方法のアタッチに失敗: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(400).body("paymentMethodId が null または空です");
        }
	    
	 // メタデータから user_id を取得
        Map<String, String> metadata = session.getMetadata();
        String userId = metadata.get("user_id");
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(400).body("user_id が metadata に存在しません");
        }

        // ユーザー取得＆更新
        User user = userRepository.findById(Integer.valueOf(userId)).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("ユーザーが見つかりません: " + userId);
        }

        String customerId = session.getCustomer();
        user.setStripeCustomerId(customerId);
        userService.updateUserRoleToPremium(user);
        System.out.println("ユーザーのロールをプレミアムに更新しました: " + user.getRole().getName());

        return ResponseEntity.ok("ロールを変更しました");

    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("処理中にエラーが発生しました: " + e.getMessage());
        return ResponseEntity.status(500).body("処理中にエラーが発生しました");
    }
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
		// 顧客IDがなかったら新しく作る
		if (stripeCustomerId == null || stripeCustomerId.isEmpty()) {
			
			try {
				Customer customer = Customer.create(
						CustomerCreateParams.builder()
						.setEmail(user.getEmail())
						.build());
				stripeCustomerId = customer.getId();
				user.setStripeCustomerId(stripeCustomerId);
				userRepository.save(user);
			} catch (StripeException e) {
				e.printStackTrace();
				throw new IllegalStateException("Stripeの顧客IDが登録されていません");
			}
		}
		
		String requestUrl = httpServletRequest.getRequestURL().toString();
		String baseUrl = requestUrl.replaceAll("/update-card-session", "");
		
			SessionCreateParams params = SessionCreateParams.builder()
					.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
					.setMode(SessionCreateParams.Mode.SETUP)
					.setCustomer(stripeCustomerId)
					// ユーザーがカード登録を完了したときに戻る場所
					.setSuccessUrl(baseUrl + "/subscription/payment?status=done")
					// ユーザーが処理を途中でキャンセルしたときに戻る場所
					.setCancelUrl(baseUrl + "/subscription/payment?status=failed")
					.build();
			
			try {
				Session session = Session.create(params);
				return session.getId();

			} catch (Exception e) {
				e.printStackTrace();
				return null; 
		}
			
	}
}
