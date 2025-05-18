package com.example.samuraitabelog.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
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
import com.stripe.param.checkout.SessionRetrieveParams;

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
	public void processSessionCompleted(Event event) {
		
		System.out.println("Webhook受信: checkout.session.completed");
		Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();

	    
	    System.out.println("セッション取得成功");
		
		if (optionalStripeObject.isPresent()) {
		    System.out.println("中身ある");
		} else {
		    System.out.println("optionalStripeObject は空");
		}
		
		optionalStripeObject.ifPresentOrElse(stripeObject -> {
            Session session = (Session)stripeObject;
            System.out.println("セッション取得成功");

            try {
            	// SessionRetrieveParams を使って、Stripe セッションの詳細を取得する
            	SessionRetrieveParams params = SessionRetrieveParams.builder().addExpand("payment_intent").build();
                session = Session.retrieve(session.getId(), params, null);
                System.out.println("セッション詳細取得成功");
                
                // Stripe のメタデータはPaymentIntent に紐づいてる。
                PaymentIntent paymentIntent = (PaymentIntent) session.getPaymentIntentObject();
                if (paymentIntent != null) {
                    String paymentMethodId = paymentIntent.getPaymentMethod();
                   
                } else {
                    System.out.println("PaymentIntentがnullです。");
                }
                
                
                // 支払い情報から paymentMethodId を取得
                String paymentMethodId = paymentIntent.getPaymentMethod();
                System.out.println("取得した paymentMethodId: " + paymentMethodId);
                
                // 支払い方法が null でないかチェック
                if (paymentMethodId != null && !paymentMethodId.isEmpty()) {
                	try {
                        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
                        paymentMethod.attach(PaymentMethodAttachParams.builder().setCustomer(session.getCustomer()).build());
                        System.out.println("支払い方法を顧客にアタッチしました");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("支払い方法のアタッチに失敗しました: " + e.getMessage());
                    }
                } else {
                    System.out.println("paymentMethodId が null または空です。");
                    return;
                }
                
                // 支払い情報から user_id を取得
                Map<String, String> metadata = session.getMetadata();
                String userId = metadata.get("user_id");
                System.out.println("user_id取得: " + userId);
                
                if(userId == null || userId.isEmpty()) {
                	System.out.println("user_idがmetadataにありません。処理を中断します。");
                	return;
                }

                // user_id をもとに User をDBから取得
                User user = userRepository.findById(Integer.valueOf(userId)).orElse(null);
                if (user == null) {	
                	System.out.println("指定された user_id は存在しません: " + userId);
                	return;
                }

                // セッションから顧客IDを取得
                String customerId = session.getCustomer();
        		user.setStripeCustomerId(customerId);       
        
                // ユーザーのロールを更新
        		User updatedUser = userService.updateUserRoleToPremium(user);       
                System.out.println("ユーザーのロールをプレミアムに更新しました" + user.getRole().getName());
                
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("エラー発生: " + e.getMessage());
            }
            System.out.println("Stripeの登録処理が成功しました。");
            
        },() -> {
            System.out.println("Stripeの登録処理が失敗しました。");
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
