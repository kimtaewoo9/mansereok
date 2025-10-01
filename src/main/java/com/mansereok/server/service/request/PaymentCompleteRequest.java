package com.mansereok.server.service.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentCompleteRequest {

	private String paymentId; // 포트원 결제 ID
	private String orderId;   // 가맹점 주문 ID
}
