package com.example.samuraitabelog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.samuraitabelog.service.SubscriptionService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

@Controller
public class StripeWebhookController {
    private final SubscriptionService subscriptionService;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public StripeWebhookController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
    
    // Stripe からの Webhook リクエストを受け取るエンドポイント
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
    	System.out.println("Webhook受信: checkout.session.completed");
    	System.out.println("Webhook 受信: " + payload);
    	
    	// Stripe.apiKey = stripeApiKey;
        Event event = null;

        try {
        	// イベントの検証
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // イベントが checkout.session.completed タイプ
        //  Stripe の checkout.session.completed イベントを受け取って、subscriptionService.processSessionCompleted(event); に処理を任せる。
        // ユーザーが支払いを完了したときに Stripe が送信する
        if ("checkout.session.completed".equals(event.getType())) {
        	System.out.println("Webhook受信: checkout.session.completed");
        	// subscriptionService.processSessionCompleted(event);が呼ばれて支払い後の処理ユーザーのロール変更
        	subscriptionService.processSessionCompleted(event);
        }

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

}
