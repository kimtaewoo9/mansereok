package com.mansereok.server.controller;

import com.mansereok.server.service.ManseryeokService;
import com.mansereok.server.service.request.ManseryeokCreateRequest;
import com.mansereok.server.service.response.ManseryeokCreateResponse;
import com.mansereok.server.service.response.ManseryeokInterpretationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ManseryeokController {

	private final ManseryeokService manseryeokService;

	@PostMapping("/api/v1/manseryeok/interpretation")
	public ResponseEntity<ManseryeokInterpretationResponse> getInterpretation(
		@RequestBody ManseryeokCreateRequest request
	) {
		log.info("[ManseryeokController.getInterpretation] name={}]", request.getName());
		System.out.println("Request:" + request);
		ManseryeokInterpretationResponse response = manseryeokService.createInterpretation(request);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/api/v1/manseryeok/saju")
	public ResponseEntity<ManseryeokCreateResponse> getManseryeok(
		@RequestBody ManseryeokCreateRequest request
	) {
		ManseryeokCreateResponse response = manseryeokService.getManseryeok(request);

		return ResponseEntity.ok(response);
	}


}
