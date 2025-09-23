package com.mansereok.server.service;

import com.mansereok.server.entity.PersonalInfo;
import com.mansereok.server.repository.PersonalInfoRepository;
import com.mansereok.server.service.request.ManseryeokCreateRequest;
import com.mansereok.server.service.response.ChartCreateResponse;
import com.mansereok.server.service.response.DaeunCreateResponse;
import com.mansereok.server.service.response.ManseryeokInterpretationResponse;
import com.mansereok.server.service.response.OhaengCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostellerService {

	private final PersonalInfoRepository personalInfoRepository;

	private final PostellerApiClient postellerApiClient;

	private final GeminiApiClient geminiApiClient;
	private final GptApiClient gptApiClient;

	public DaeunCreateResponse getDaeun(ManseryeokCreateRequest request) {
		log.info("🚀 포스텔러 대운 API 호출, name={}", request.getName());
		return postellerApiClient.getDaeun(request);
	}

	public ChartCreateResponse getChart(ManseryeokCreateRequest request) {
		log.info("🚀 포스텔러 사주 기본 차트(일간/사주팔자) API 호출, name={}", request.getName());
		return postellerApiClient.getChart(request);
	}

	public OhaengCreateResponse getOhaeng(ManseryeokCreateRequest request) {
		log.info("🚀 포스텔러 (오행/십성) API 호출, name={}", request.getName());
		return postellerApiClient.getOhaeng(request);
	}

	public ManseryeokInterpretationResponse createInterpretation(ManseryeokCreateRequest request) {
		PersonalInfo savedPersonalInfo = save(request);

		DaeunCreateResponse daeunResponse = getDaeun(request);// 대운 API 호출
		ChartCreateResponse chartResponse = getChart(request);// 사주 기본 차트 API 호출
		OhaengCreateResponse ohaengResponse = getOhaeng(request);// 오행/십성 API 호출

//		log.info("🚀GEMINI 해석 시작");
//		String interpretation = geminiApiClient.interpret(
//			daeunResponse,
//			chartResponse,
//			ohaengResponse,
//			request);

		log.info("🚀 GPT 해석 시작");
		String interpretation = gptApiClient.interpret(
			daeunResponse,
			chartResponse,
			ohaengResponse,
			request);

		return ManseryeokInterpretationResponse.builder()
			.personalInfo(savedPersonalInfo)
			.interpretation(interpretation)
			.build();
	}

	private PersonalInfo save(ManseryeokCreateRequest request) {
		PersonalInfo personalInfo = PersonalInfo.from(request);
		return personalInfoRepository.save(personalInfo);
	}
}
