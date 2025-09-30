package com.mansereok.server.controller;

import com.mansereok.server.service.InterpretationService;
import com.mansereok.server.service.request.CompatibilityAnalysisRequest;
import com.mansereok.server.service.request.ManseryeokCreateRequest;
import com.mansereok.server.service.response.CompatibilityAnalysisResponse;
import com.mansereok.server.service.response.ManseryeokInterpretationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InterpretationController {

	private final InterpretationService interpretationService;

	@Operation(
		summary = "사주 해석 생성(Posteller 만세력)",
		description = "생년월일시 정보를 입력받아 AI 기반 사주팔자 해석을 제공합니다"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "해석 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ManseryeokInterpretationResponse.class)
			)
		),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@PostMapping("/api/v1/manseryeok/interpretation")
	public ResponseEntity<ManseryeokInterpretationResponse> getInterpretation(
		@Parameter(
			description = "사주 분석 요청 정보",
			required = true,
			content = @Content(
				examples = @ExampleObject(
					value = """
						{
						  "name": "홍길동",
						  "gender": "M",
						  "calendar": "S",
						  "birthday": "1990/05/15",
						  "birthtime": "14:30",
						  "hmUnsure": false,
						  "day": 15,
						  "hour": 14,
						  "locationId": 1835847,
						  "locationName": "서울특별시, 대한민국",
						  "midnightAdjust": false,
						  "min": 30,
						  "month": 5,
						  "year": 1990
						}
						"""
				)
			)
		)
		@RequestBody ManseryeokCreateRequest request
	) {
		log.info("[ManseryeokController.getInterpretation] name={}]", request.getName());
		ManseryeokInterpretationResponse response = interpretationService.createInterpretation(
			request);
		return ResponseEntity.ok(response);
	}

	@Operation(
		summary = "두 사람의 궁합 분석",
		description = "두 사람의 생년월일시 정보를 입력받아 AI 기반 사주 궁합 분석을 제공합니다"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "궁합 분석 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CompatibilityAnalysisResponse.class)
			)
		),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@PostMapping("/api/v1/manseryeok/compatibility")
	public ResponseEntity<CompatibilityAnalysisResponse> getCompatibilityAnalysis(
		@Parameter(
			description = "두 사람의 궁합 분석 요청 정보",
			required = true
		)
		@RequestBody CompatibilityAnalysisRequest request
	) {
		log.info("[ManseryeokController.getCompatibilityAnalysis] person1={}, person2={}",
			request.getPerson1().getName(), request.getPerson2().getName());
		CompatibilityAnalysisResponse response = interpretationService.createCompatibilityAnalysis(
			request);
		return ResponseEntity.ok(response);
	}
}
