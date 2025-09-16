package com.mansereok.server.service;

import com.mansereok.server.entity.PersonalInfo;
import com.mansereok.server.repository.PersonalInfoRepository;
import com.mansereok.server.service.request.ManseryeokCreateRequest;
import com.mansereok.server.service.response.ManseryeokCreateResponse;
import com.mansereok.server.service.response.ManseryeokInterpretationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManseryeokService {

	private final PersonalInfoRepository personalInfoRepository;

	private final PostellerApiClient postellerApiClient;
	private final GptApiClient gptApiClient;

	public ManseryeokCreateResponse getManseryeok(ManseryeokCreateRequest request) {
		save(request);

		log.info("🚀 포스텔러 API 호출, name={}", request.getName());
		return postellerApiClient.get(request);
	}

	public ManseryeokInterpretationResponse createInterpretation(ManseryeokCreateRequest request) {
		log.info("🚀 포스텔러 API 호출, name={}", request.getName());
		ManseryeokCreateResponse manseryeokCreateResponse = postellerApiClient.get(request);

		log.info("🚀GPT 해석 시작");
		String interpretation = gptApiClient.interpret(manseryeokCreateResponse, request);
		
		return ManseryeokInterpretationResponse.builder()
			.personalInfo(PersonalInfo.from(request))
			.interpretation(interpretation)
			.build();
	}

	private PersonalInfo save(ManseryeokCreateRequest request) {
		PersonalInfo personalInfo = PersonalInfo.from(request);
		return personalInfoRepository.save(personalInfo);
	}
}
