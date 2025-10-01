package com.mansereok.server.service;

import com.mansereok.server.dto.payment.PaymentCompleteRequest;
import com.mansereok.server.dto.payment.PortOnePaymentResponse;
import com.mansereok.server.entity.Payment;
import com.mansereok.server.entity.PaymentStatus;
import com.mansereok.server.exception.PaymentException;
import com.mansereok.server.repository.PaymentRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final RestClient restClient = RestClient.create();

	@Value("${portone.api.secret}")
	private String portOneApiSecret;

	public Payment completePayment(PaymentCompleteRequest request) {
		// 1. 포트원 결제내역 단건조회 API 호출
		PortOnePaymentResponse paymentResponse = fetchPaymentDataFromPortOne(
			request.getPaymentId());

		// 2. 주문 데이터의 가격과 실제 지불된 금액 비교 (위변조 검증)
		// 실제 운영시에는 DB에서 주문 정보를 조회해야 합니다.
		// Long expectedAmount = orderService.getOrderAmount(request.getOrderId());
		Long expectedAmount = 1000L; // 예시: 실제로는 DB에서 해당 주문(orderId)의 금액을 가져와야 함

		if (!Objects.equals(paymentResponse.getAmount().getTotal(), expectedAmount)) {
			log.error("결제 금액 불일치: paymentId={}, 기대값={}, 실제값={}",
				request.getPaymentId(), expectedAmount, paymentResponse.getAmount().getTotal());
			throw new PaymentException("결제 금액이 일치하지 않아 위변조가 의심됩니다.");
		}

		// 3. 결제 상태에 따른 처리
		PaymentStatus status = PaymentStatus.fromPortOneStatus(paymentResponse.getStatus());
		if (status == PaymentStatus.PAID || status == PaymentStatus.VIRTUAL_ACCOUNT_ISSUED) {
			// 4. 결제 정보를 우리 DB에 저장
			Payment payment = Payment.builder()
				.paymentId(paymentResponse.getId())
				.orderId(paymentResponse.getOrderId())
				.amount(paymentResponse.getAmount().getTotal())
				.status(status)
				.build();
			return paymentRepository.save(payment);
		} else {
			// PAID, VIRTUAL_ACCOUNT_ISSUED 외 다른 상태는 일단 실패로 간주
			throw new PaymentException("결제가 완료되지 않았습니다. 상태: " + paymentResponse.getStatus());
		}
	}

	private PortOnePaymentResponse fetchPaymentDataFromPortOne(String paymentId) {
		try {
			String url = "https://api.portone.io/payments/" + paymentId;
			return restClient.get()
				.uri(url)
				.header(HttpHeaders.AUTHORIZATION, "PortOne " + portOneApiSecret)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(PortOnePaymentResponse.class);
		} catch (Exception e) {
			log.error("포트원 API 호출 실패: paymentId={}", paymentId, e);
			throw new PaymentException("결제 정보를 조회하는 중 오류가 발생했습니다.");
		}
	}
}
