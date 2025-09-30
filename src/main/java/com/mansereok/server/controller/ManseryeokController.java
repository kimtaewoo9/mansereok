package com.mansereok.server.controller;

import com.mansereok.server.service.ManseCalculationService;
import com.mansereok.server.service.ManseInterpretationService;
import com.mansereok.server.service.request.ManseCompatibilityAnalysisRequest;
import com.mansereok.server.service.request.ManseInterpretationRequest;
import com.mansereok.server.service.request.ManseryeokCalculationRequest;
import com.mansereok.server.service.response.ManseCompatibilityAnalysisResponse;
import com.mansereok.server.service.response.ManseInterpretationResponse;
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
@Tag(name = "자체 구현 만세력 API", description = "생년월일시로 만세력을 계산하는 API")
public class ManseryeokController {

	private final ManseCalculationService manseCalculationService;
	private final ManseInterpretationService manseInterpretationService;

	@Operation(
		summary = "만세력 계산"
	)
	@PostMapping("/api/v1/manseryeok/calculate")
	public ResponseEntity<ManseryeokCalculationResponse> calculate(
		@Valid @RequestBody ManseryeokCalculationRequest request
	) {
		log.info("만세력 계산 요청: solarDate={}, gender={}, isLunar={}",
			request.getSolarDate(), request.getGender(), request.getIsLunar());
		ManseryeokCalculationResponse response = manseCalculationService.calculate(request);
		log.info("만세력 계산 완료: daySky={}", response.getSaju().getDaySky().getChinese());

		return ResponseEntity.ok(response);
	}

	@Operation(
		summary = "만세력 종합 해석"
	)
	@PostMapping("/api/v1/manseryeok/interpret")
	public ResponseEntity<ManseInterpretationResponse> interpret(
		@Valid @RequestBody ManseInterpretationRequest request
	) {

		// 1. 만세력 데이터 계산
		ManseryeokCalculationResponse manse = manseCalculationService.calculate(
			new ManseryeokCalculationRequest(
				request.getName(),
				request.getSolarDate(),
				request.getSolarTime(),
				request.getGender(),
				request.getIsLunar()
			)
		);

		// 2. 계산된 만세력으로 해석 시작.
		ManseInterpretationResponse response = manseInterpretationService.interpret(
			request.getName(),
			manse
		);

		return ResponseEntity.ok(response);
	}

	@Operation(
		summary = "궁합 분석",
		description = "두 사람의 이름과 생년월일시 정보를 받아 AI를 통해 종합적인 궁합을 분석합니다."
	)
	@PostMapping("/api/v1/manseryeok/interpret/compatibility")
	public ResponseEntity<ManseCompatibilityAnalysisResponse> analyzeCompatibility(
		@Valid @RequestBody ManseCompatibilityAnalysisRequest request) {

		ManseCompatibilityAnalysisRequest.PersonInfo person1 = request.getPerson1();
		ManseCompatibilityAnalysisRequest.PersonInfo person2 = request.getPerson2();

		// 1. 첫 번째 사람의 만세력 데이터 계산
		ManseryeokCalculationResponse person1Response = manseCalculationService.calculate(
			new ManseryeokCalculationRequest(
				person1.getName(),
				person1.getSolarDate(),
				person1.getSolarTime(),
				person1.getGender(),
				person1.getIsLunar()
			)
		);

		// 2. 두 번째 사람의 만세력 데이터 계산
		ManseryeokCalculationResponse person2Response = manseCalculationService.calculate(
			new ManseryeokCalculationRequest(
				person2.getName(),
				person2.getSolarDate(),
				person2.getSolarTime(),
				person2.getGender(),
				person2.getIsLunar()
			)
		);

		// 3. 계산된 두 개의 만세력 데이터로 궁합 분석 서비스 호출
		ManseCompatibilityAnalysisResponse response = manseInterpretationService.analyzeCompatibility(
			person1.getName(), person1Response,
			person2.getName(), person2Response
		);

		return ResponseEntity.ok(response);
	}
}
