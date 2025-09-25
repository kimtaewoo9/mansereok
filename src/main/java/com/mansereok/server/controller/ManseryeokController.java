package com.mansereok.server.controller;

import com.mansereok.server.service.ManseryeokCalculationService;
import com.mansereok.server.service.request.ManseryeokCalculationRequest;
import com.mansereok.server.service.response.ManseryeokCalculationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "자체 구현 만세력 계산 API", description = "회원가입 없이 생년월일시로 만세력을 계산하는 API")
public class ManseryeokController {

	private final ManseryeokCalculationService manseryeokCalculationService;

	@Operation(
		summary = "만세력 계산",
		description = "생년월일시, 성별, 음력여부 정보를 입력받아 완전한 만세력 정보를 반환합니다"
	)
	@PostMapping("/api/v1/manseryeok/calculate")
	public ResponseEntity<ManseryeokCalculationResponse> calculateManseryeok(
		@Valid @RequestBody ManseryeokCalculationRequest request
	) {
		log.info("만세력 계산 요청: solarDate={}, gender={}, isLunar={}",
			request.getSolarDate(), request.getGender(), request.getIsLunar());
		ManseryeokCalculationResponse response = manseryeokCalculationService.calculate(request);
		log.info("만세력 계산 완료: daySky={}", response.getSaju().getDaySky().getChinese());

		return ResponseEntity.ok(response);
	}
}
