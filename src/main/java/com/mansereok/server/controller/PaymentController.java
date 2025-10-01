package com.mansereok.server.controller;

import com.mansereok.server.entity.Payment;
import com.mansereok.server.service.PaymentService;
import com.mansereok.server.service.request.PaymentCompleteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping("/api/payment/complete")
	public ResponseEntity<?> completePayment(@RequestBody PaymentCompleteRequest request) {
		try {
			Payment payment = paymentService.completePayment(request);
			return ResponseEntity.ok(payment);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
