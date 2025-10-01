package com.mansereok.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;


@Entity
@Table(name = "payments")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String paymentId; // 포트원 결제 ID

	@Column(nullable = false)
	private String orderId; // 우리 시스템의 주문 ID

	@Column(nullable = false)
	private Long amount; // 검증을 위해 필수

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status; // 결제 상태

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public Payment(String paymentId, String orderId, Long amount, PaymentStatus status) {
		this.paymentId = paymentId;
		this.orderId = orderId;
		this.amount = amount;
		this.status = status;
		this.createdAt = LocalDateTime.now();
	}
}
