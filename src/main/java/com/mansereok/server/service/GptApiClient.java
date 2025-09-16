package com.mansereok.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mansereok.server.dto.GptRequest;
import com.mansereok.server.dto.Message;
import com.mansereok.server.service.request.ManseryeokCreateRequest;
import com.mansereok.server.service.response.ManseryeokCreateResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class GptApiClient {

	private final RestClient restClient;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String apiKey;

	public GptApiClient(@Value("${openai.api.key}") String apiKey,
		@Value("${openai.api.base-url:https://api.openai.com}") String baseUrl) {
		this.apiKey = apiKey;
		this.restClient = RestClient.builder()
			.baseUrl(baseUrl + "/v1")
			.defaultHeader("Authorization", "Bearer " + apiKey)
			.defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	public String interpret(ManseryeokCreateResponse response, ManseryeokCreateRequest request) {
		log.info("GPT 사주 해석 요청: name={}", request.getName());
		try {
			String prompt = createPrompt(response, request);

			GptRequest gptRequest = new GptRequest(
				"gpt-4o",
				List.of(
					new Message("system",
						"당신은 30년 경력의 전문 사주명리학자입니다. 주어진 사주팔자 정보를 바탕으로 정확하고 건설적인 해석을 제공해주세요. 부정적인 내용도 포함하되 극복 방안을 함께 제시하고, 운명론적이기보다는 개인의 노력과 선택의 중요성을 강조해주세요."),
					new Message("user", prompt)
				),
				2500,
				0.7
			);

			String requestBody = objectMapper.writeValueAsString(gptRequest);

			String gptResponse = restClient.post()
				.uri("/chat/completions")
				.body(requestBody)
				.retrieve()
				.body(String.class);

			return extractContentFromResponse(gptResponse);

		} catch (JsonProcessingException e) {
			log.error("JSON 처리 오류: {}", e.getMessage());
			return "해석 생성 중 데이터 처리 오류가 발생했습니다. 나중에 다시 시도해주세요.";
		} catch (Exception e) {
			log.error("GPT API 요청 중 오류: {}", e.getMessage());
			return "해석 생성 중 API 요청 오류가 발생했습니다. 나중에 다시 시도해주세요.";
		}
	}

	private String createPrompt(ManseryeokCreateResponse response,
		ManseryeokCreateRequest request) {
		var data = response.getData();
		var currentDaeun = ManseryeokCreateResponse.SajuDataUtils.getCurrentDaeun(data);
		var currentYear = ManseryeokCreateResponse.SajuDataUtils.getCurrentYearFortune(data);
		var currentMonth = ManseryeokCreateResponse.SajuDataUtils.getCurrentMonthFortune(data);
		var ohaengAnalysis = ManseryeokCreateResponse.SajuDataUtils.analyzeOhaeng(data);

		StringBuilder prompt = new StringBuilder();

		prompt.append("다음은 사주팔자 정보입니다.\n\n");
		prompt.append(String.format("이름: %s, 성별: %s\n", request.getName(), request.getGender()));

		prompt.append("【사주팔자】\n");
		prompt.append("\n");
		
		prompt.append("【현재 대운 정보】\n");
		prompt.append("- 대운 간지: ").append(data.getDaeunGanji()).append("\n");
		prompt.append("- 대운 순서: ").append(data.getDaeunNumber()).append("번째\n");
		if (currentDaeun != null) {
			prompt.append("- 천간: ").append(currentDaeun.getGanji().getCheongan().getName())
				.append(" (").append(currentDaeun.getGanji().getCheongan().getChinese())
				.append(")\n");
			prompt.append("- 지지: ").append(currentDaeun.getGanji().getJiji().getName())
				.append(" (").append(currentDaeun.getGanji().getJiji().getChinese()).append(")\n");
			prompt.append("- 십성: ")
				.append(currentDaeun.getGanji().getCheongan().getSipseong().getName()).append("\n");
			prompt.append("- 운성: ").append(currentDaeun.getGanji().getUnseong().getName())
				.append("\n");
			prompt.append("- 오행: ")
				.append(currentDaeun.getGanji().getCheongan().getOhaeng().getName()).append("\n");
		}
		prompt.append("\n");

		if (currentYear != null) {
			prompt.append("【현재 연운 (").append(currentYear.getYear()).append("년)】\n");
			prompt.append("- 간지: ").append(
					ManseryeokCreateResponse.SajuDataUtils.getGanjiString(currentYear.getGanji()))
				.append("\n");
			prompt.append("- 십성: ")
				.append(currentYear.getGanji().getCheongan().getSipseong().getName()).append("\n");
			prompt.append("- 운성: ").append(currentYear.getGanji().getUnseong().getName())
				.append("\n\n");
		}

		if (currentMonth != null) {
			prompt.append("【현재 월운 (").append(currentMonth.getMonth()).append("월)】\n");
			prompt.append("- 간지: ").append(
					ManseryeokCreateResponse.SajuDataUtils.getGanjiString(currentMonth.getGanji()))
				.append("\n");
			prompt.append("- 십성: ")
				.append(currentMonth.getGanji().getCheongan().getSipseong().getName()).append("\n");
			prompt.append("- 운성: ").append(currentMonth.getGanji().getUnseong().getName())
				.append("\n\n");
		}

		prompt.append("【오행 분석】\n");
		prompt.append("- 목: ").append(ohaengAnalysis.getWood()).append("개\n");
		prompt.append("- 화: ").append(ohaengAnalysis.getFire()).append("개\n");
		prompt.append("- 토: ").append(ohaengAnalysis.getEarth()).append("개\n");
		prompt.append("- 금: ").append(ohaengAnalysis.getMetal()).append("개\n");
		prompt.append("- 수: ").append(ohaengAnalysis.getWater()).append("개\n");
		prompt.append("- 가장 강한 오행: ").append(ohaengAnalysis.getDominantElement()).append("\n");
		prompt.append("- 가장 약한 오행: ").append(ohaengAnalysis.getWeakElement()).append("\n\n");

		prompt.append("【주요 대운 흐름】\n");
		data.getDaeunList().stream().limit(5).forEach(daeun -> {
			prompt.append("- ").append(daeun.getAge()).append("세: ")
				.append(ManseryeokCreateResponse.SajuDataUtils.getGanjiString(daeun.getGanji()))
				.append(" (").append(daeun.getGanji().getUnseong().getName()).append(")\n");
		});
		prompt.append("\n");

		prompt.append("위 정보를 바탕으로 다음 5개 항목에 대해 상세히 해석해주세요:\n\n");
		prompt.append("## 1. 성격 및 기본 성향\n");
		prompt.append("## 2. 현재 대운의 특징과 기회\n");
		prompt.append("## 3. 현재 시기 운세 (올해/이번달)\n");
		prompt.append("## 4. 인생 전반적 흐름과 전망\n");
		prompt.append("## 5. 실용적 조언 및 개선 방안\n\n");
		prompt.append("각 항목을 명확히 구분하여 작성하고, 구체적이고 실용적인 조언을 포함해주세요.");

		return prompt.toString();
	}

	private String extractContentFromResponse(String jsonResponse) throws JsonProcessingException {
		JsonNode root = objectMapper.readTree(jsonResponse);
		JsonNode choicesNode = root.path("choices");
		if (choicesNode.isArray() && choicesNode.size() > 0) {
			JsonNode contentNode = choicesNode.get(0).path("message").path("content");
			if (contentNode != null) {
				return contentNode.asText();
			}
		}
		log.error("GPT 응답에서 'content' 필드를 찾을 수 없습니다. 응답: {}", jsonResponse);
		throw new IllegalArgumentException("GPT 응답 형식이 올바르지 않습니다.");
	}
}
