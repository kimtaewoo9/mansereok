package com.mansereok.server.service;

import com.mansereok.server.exception.PostellerApiException;
import com.mansereok.server.service.request.ManseryeokCreateRequest;
import com.mansereok.server.service.response.ManseryeokCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostellerApiClient {

	private final RestClient restClient = RestClient.create();

	@Value("${posteller.api.base-url:https://api.forceteller.com}")
	private String baseUrl;

	public ManseryeokCreateResponse get(ManseryeokCreateRequest request) {
		try {
			// 실제 포스텔러 API 엔드포인트
			String url = baseUrl + "/api/pro/profile/saju/daeun";

			log.debug("API 호출 URL: {}", url);
			log.debug("요청 데이터: name={}, gender={}, birthday={}",
				request.getName(), request.getGender(), request.getBirthday());

			// RestClient로 API 호출
			ManseryeokCreateResponse response = restClient.post()
				.uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Accept", MediaType.APPLICATION_JSON_VALUE)
				.header("User-Agent", "Manseryeok-Client/1.0")
				.header("Referer", "https://pro.forceteller.com")
				.header("Origin", "https://pro.forceteller.com")
				.body(request)
				.retrieve()
				.body(ManseryeokCreateResponse.class);

			log.info("✅ 만세력 API 호출 성공: name={}", request.getName());
			return response;

		} catch (RestClientException e) {
			log.error("만세력 API 호출 오류: name={}, error={}", request.getName(), e.getMessage());
			throw new PostellerApiException("포스텔러 API 요청 오류: " + e.getMessage(), e);

		} catch (Exception e) {
			log.error("만세력 API 호출 중 예상치 못한 오류: name={}", request.getName(), e);
			throw new PostellerApiException("만세력 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}
}
