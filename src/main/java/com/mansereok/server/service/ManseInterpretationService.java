package com.mansereok.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mansereok.server.service.request.Gpt5Request;
import com.mansereok.server.service.response.ManseCompatibilityAnalysisResponse;
import com.mansereok.server.service.response.ManseInterpretationResponse;
import com.mansereok.server.service.response.ManseryeokCalculationResponse;
import com.mansereok.server.service.response.ManseryeokCalculationResponse.JijangganElement;
import com.mansereok.server.service.response.ManseryeokCalculationResponse.PillarElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class ManseInterpretationService {

	private final RestClient restClient;
	private final ObjectMapper objectMapper = new ObjectMapper();

	// 60갑자 순서 정의 (대운 계산용)
	private static final List<String> HEAVENLY_STEMS = Arrays.asList("甲", "乙", "丙", "丁", "戊",
		"己", "庚", "辛", "壬", "癸");
	private static final List<String> EARTHLY_BRANCHES = Arrays.asList("子", "丑", "寅", "卯", "辰",
		"巳", "午", "未", "申", "酉", "戌", "亥");
	private static final List<String> GAPJA_CYCLE = new ArrayList<>();

	static {
		for (int i = 0; i < 60; i++) {
			GAPJA_CYCLE.add(HEAVENLY_STEMS.get(i % 10) + EARTHLY_BRANCHES.get(i % 12));
		}
	}

	private static final String GPT5_SYSTEM_INSTRUCTION =
		"--- SYSTEM INSTRUCTION ---\n" +
			"당신은 30년 경력의 전문 사주명리학자입니다. 주어진 사주팔자 정보를 바탕으로 정확하고 건설적인 해석을 제공해주세요. " +
			"부정적인 내용도 포함하되 극복 방안을 함께 제시하고, 운명론적이기보다는 개인의 노력과 선택의 중요성을 강조해주세요. " +
			"'해요'체를 사용하여 부드럽고 친근한 말투를 사용해주세요.\n\n" +
			"--- USER QUERY ---\n";


	public ManseInterpretationService(@Value("${openai.api.key}") String apiKey,
		@Value("${openai.api.base-url:https://api.openai.com}") String baseUrl) {
		this.restClient = RestClient.builder()
			.baseUrl(baseUrl + "/v1")
			.defaultHeader("Authorization", "Bearer " + apiKey)
			.defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	/**
	 * 만세력 계산 결과를 바탕으로 GPT-5에게 사주 해석을 요청하고, 구조화된 응답 객체(ManseInterpretationResponse)를 반환합니다.
	 *
	 * @param name     분석 대상자의 이름
	 * @param response 만세력 계산 결과
	 * @return GPT-5 해석과 추가 정보가 담긴 응답 DTO
	 */
	public ManseInterpretationResponse interpret(String name,
		ManseryeokCalculationResponse response) {
		log.info("✅ 사주 해석 요청 시작, 요청자: {}", name);

		// 사주의 핵심인 '일간' 정보를 미리 추출합니다.
		String ilgan = "정보 없음";
		if (response != null && response.getSaju() != null
			&& response.getSaju().getDaySky() != null) {
			PillarElement daySky = response.getSaju().getDaySky();
			ilgan = daySky.getKorean() + daySky.getFiveCircle(); // 예: "임" + "수" -> "임수"
		}

		try {
			String userPrompt = createAnalysisPrompt(name, response);
			String input = GPT5_SYSTEM_INSTRUCTION + userPrompt;

			Gpt5Request gpt5Request = new Gpt5Request(
				"gpt-5",
				input,
				8000,
				"medium",
				"medium"
			);

			String requestBody = objectMapper.writeValueAsString(gpt5Request);

			log.info("GPT-5 요청 데이터 생성 완료. API 호출 시작...");
			String gptResponse = restClient.post()
				.uri("/responses")
				.body(requestBody)
				.retrieve()
				.body(String.class);

			log.info("GPT-5 응답 수신 완료.");
			String interpretationText = extractContentFromResponseGpt5(gptResponse);

			// 성공 시, 모든 정보를 담아 DTO를 빌드하여 반환합니다.
			return new ManseInterpretationResponse(
				name,
				ilgan,
				interpretationText
			);

		} catch (Exception e) {
			log.error("GPT API 요청 중 오류 발생: {}", e.getMessage(), e);

			return new ManseInterpretationResponse(
				name,
				ilgan,
				"해석 생성 중 API 요청 오류가 발생했습니다. 서버 로그를 확인해주세요."
			);
		}
	}

	/**
	 * ManseryeokCalculationResponse 객체를 바탕으로 GPT에게 전달할 프롬프트를 생성
	 */
	private String createAnalysisPrompt(String name, ManseryeokCalculationResponse response) {
		StringBuilder prompt = new StringBuilder();
		ManseryeokCalculationResponse.SajuInfo saju = response.getSaju();
		ManseryeokCalculationResponse.InputInfo input = response.getInput();

		// ===== 시스템 역할 정의 (기존과 동일) =====
		prompt.append("### 시스템 역할 정의 ###\n");
		prompt.append("당신은 30년 이상의 경력을 가진 최고 수준의 사주명리학 전문가입니다.\n");
		prompt.append("다음 원칙을 반드시 준수하여 분석을 제공하세요:\n");
		prompt.append("1. 정확성: 주어진 데이터를 기반으로 전통 사주명리학 이론에 충실하게 해석\n");
		prompt.append("2. 구체성: 추상적 표현을 피하고 실제 생활에 적용 가능한 구체적 조언 제공\n");
		prompt.append("3. 균형성: 장점과 단점을 균형있게 제시하되, 극복 방안도 함께 제공\n");
		prompt.append("4. 실용성: 학술적 용어는 쉽게 풀어서 설명하고 실생활에 도움되는 조언 포함\n");
		prompt.append("5. 개인화: 이 사람만의 고유한 특징을 찾아 맞춤형 해석 제공\n\n");

		// ===== 분석 대상 정보 =====
		prompt.append("### 분석 대상 기본 정보 ###\n");
		prompt.append(String.format("이름: %s\n", name));
		prompt.append(
			String.format("성별: %s\n", "MALE".equalsIgnoreCase(input.getGender()) ? "남자" : "여자"));
		prompt.append(
			String.format("생년월일시(양력): %s %s\n", input.getSolarDate(), input.getSolarTime()));
		String sajuPalja = String.format("%s%s %s%s %s%s %s%s",
			saju.getYearSky().getKorean(), saju.getYearGround().getKorean(),
			saju.getMonthSky().getKorean(), saju.getMonthGround().getKorean(),
			saju.getDaySky().getKorean(), saju.getDayGround().getKorean(),
			saju.getTimeSky() != null ? saju.getTimeSky().getKorean() : "?",
			saju.getTimeGround() != null ? saju.getTimeGround().getKorean() : "?"
		);
		prompt.append(String.format("사주명식: %s\n\n", sajuPalja));

		// ===== 사주 원국 분석 데이터 =====
		prompt.append("### 사주 원국(原局) 데이터 ###\n\n");
		appendPillarInfo(prompt, "년주(年柱)", "조상/어린시절/사회적기반", saju.getYearSky(),
			saju.getYearGround());
		appendPillarInfo(prompt, "월주(月柱)", "부모/청년기/직업운", saju.getMonthSky(),
			saju.getMonthGround());
		appendPillarInfo(prompt, "일주(日柱)", "나자신/배우자/중년기", saju.getDaySky(), saju.getDayGround());
		if (saju.getTimeSky() != null && saju.getTimeGround() != null) {
			appendPillarInfo(prompt, "시주(時柱)", "자녀/노년기/결실", saju.getTimeSky(),
				saju.getTimeGround());
		}
		prompt.append("\n");

		// ===== 오행 및 십성 분포 분석 =====
		Map<String, Integer> ohaengCounts = new HashMap<>();
		Map<String, Integer> sipseongCounts = new HashMap<>();
		calculateDistribution(saju, ohaengCounts, sipseongCounts);

		prompt.append("【오행(五行) 세력 분석】\n");
		ohaengCounts.forEach((key, value) -> prompt.append(String.format("%s: %d개%s\n", key, value,
			saju.getDaySky().getFiveCircle().equals(key) ? " (일간)" : "")));
		prompt.append("\n");

		prompt.append("【십성(十星) 분포 분석】\n");
		sipseongCounts.forEach(
			(key, value) -> prompt.append(String.format("%s: %d개\n", key, value)));
		prompt.append("\n");

		// ===== 대운 흐름 데이터 =====
		prompt.append("### 운세 흐름 데이터 (대운) ###\n");
		prompt.append(String.format("대운수: %d\n", saju.getBigFortuneNumber()));
		appendDaewoonFlow(prompt, saju, input.getGender());
		prompt.append("\n");

		// ===== 분석 요청사항 (기존과 동일) =====
		prompt.append("### 종합 분석 요청 ###\n\n");
		prompt.append("위 데이터를 종합하여 아래 11개 항목을 분석해주세요.\n");
		prompt.append("각 항목당 최소 3-5문장 이상 구체적으로 작성하고,\n");
		prompt.append("해요체를 통해 친절하게 설명해주세요.\n\n");
		prompt.append("【분석 시작】\n");
		prompt.append(
			String.format("먼저 \"%s님은 %s %s에 태어나셨습니다.\"로 시작하세요.\n\n", name, input.getSolarDate(),
				input.getSolarTime()));
		prompt.append("## 1. 타고난 성격과 기질 (일간 중심 심층 분석)\n");
		prompt.append("## 2. 현재 대운이 가져온 변화와 기회\n");
		prompt.append("## 3. 올해와 이달의 운세 흐름\n");
		prompt.append("## 4. 인생 전체 흐름과 전환점\n");
		prompt.append("## 5. 성공을 위한 맞춤형 전략\n");
		prompt.append("## 6. 이상형과 연애 스타일\n");
		prompt.append("## 7. 연애운과 결혼 시기\n");
		prompt.append("## 8. 주의해야 할 함정과 위험\n");
		prompt.append("## 9. 천직과 적성 (구체적 직업 제시)\n");
		prompt.append("## 10. 인간관계 패턴과 처세술\n");
		prompt.append("## 11. 핵심 장단점과 개선 포인트\n\n");
		prompt.append("【주의사항】\n");
		prompt.append("- 각 항목은 명확히 구분하여 작성\n");
		prompt.append("- 전문용어는 괄호 안에 쉬운 설명 추가\n");
		prompt.append("- 부정적 내용도 숨기지 말고 정직하게 전달\n");
		prompt.append("- 모든 해석은 데이터에 근거하여 논리적으로 설명\n");
		prompt.append("- 실생활에 즉시 적용 가능한 조언 포함");

		return prompt.toString();
	}

	// 사주 기둥 정보 추가
	private void appendPillarInfo(StringBuilder prompt, String pillarName, String meaning,
		PillarElement sky, PillarElement ground) {
		prompt.append(String.format("▶ %s - %s:\n", pillarName, meaning));
		if (sky != null) {
			prompt.append(String.format("  천간: %s(%s) | 오행:%s | 음양:%s | 십성:%s\n",
				sky.getKorean(), sky.getChinese(), sky.getFiveCircle(), sky.getMinusPlus(),
				sky.getTenStar()));
		}
		if (ground != null) {
			prompt.append(String.format("  지지: %s(%s) | 오행:%s | 음양:%s | 십성:%s\n",
				ground.getKorean(), ground.getChinese(), ground.getFiveCircle(),
				ground.getMinusPlus(), ground.getTenStar()));
			if (ground.getJijanggan() != null) {
				List<JijangganElement> jijangganList = new ArrayList<>();
				if (ground.getJijanggan().getFirst() != null) {
					jijangganList.add(ground.getJijanggan().getFirst());
				}
				if (ground.getJijanggan().getSecond() != null) {
					jijangganList.add(ground.getJijanggan().getSecond());
				}
				if (ground.getJijanggan().getThird() != null) {
					jijangganList.add(ground.getJijanggan().getThird());
				}

				String jijangganStr = jijangganList.stream()
					.map(j -> String.format("%s(%s, %s)", j.getKorean(), j.getFiveCircle(),
						j.getMinusPlus()))
					.collect(Collectors.joining(", "));
				prompt.append(String.format("  지장간: [ %s ]\n", jijangganStr));
			}
		}
	}

	private void calculateDistribution(ManseryeokCalculationResponse.SajuInfo saju,
		Map<String, Integer> ohaengCounts, Map<String, Integer> sipseongCounts) {
		List<PillarElement> pillars = Arrays.asList(
			saju.getYearSky(), saju.getYearGround(), saju.getMonthSky(), saju.getMonthGround(),
			saju.getDaySky(), saju.getDayGround(), saju.getTimeSky(), saju.getTimeGround()
		);

		for (PillarElement p : pillars) {
			if (p != null) {
				ohaengCounts.merge(p.getFiveCircle(), 1, Integer::sum);
				sipseongCounts.merge(p.getTenStar(), 1, Integer::sum);
				// 지장간의 오행도 카운트
				if (p.getJijanggan() != null) {
					if (p.getJijanggan().getFirst() != null) {
						ohaengCounts.merge(p.getJijanggan().getFirst().getFiveCircle(), 1,
							Integer::sum);
					}
					if (p.getJijanggan().getSecond() != null) {
						ohaengCounts.merge(p.getJijanggan().getSecond().getFiveCircle(), 1,
							Integer::sum);
					}
					if (p.getJijanggan().getThird() != null) {
						ohaengCounts.merge(p.getJijanggan().getThird().getFiveCircle(), 1,
							Integer::sum);
					}
				}
			}
		}
	}

	// 대운 흐름 생성 및 추가
	private void appendDaewoonFlow(StringBuilder prompt,
		ManseryeokCalculationResponse.SajuInfo saju, String gender) {
		prompt.append("【인생 대운 로드맵】\n");

		// 순행/역행 판단 (isRightDirection 로직)
		boolean isForward =
			("MALE".equalsIgnoreCase(gender) && "양".equals(saju.getYearSky().getMinusPlus())) ||
				("FEMALE".equalsIgnoreCase(gender) && "음".equals(saju.getYearSky().getMinusPlus()));

		String monthPillar = saju.getMonthSky().getChinese() + saju.getMonthGround().getChinese();
		int startIndex = GAPJA_CYCLE.indexOf(monthPillar);

		if (startIndex == -1) {
			prompt.append("대운 정보를 생성할 수 없습니다.\n");
			return;
		}

		for (int i = 0; i < 10; i++) { // 100년간의 대운 (10개)
			int age = saju.getBigFortuneNumber() + (i * 10);

			int daewoonIndex;
			if (isForward) {
				daewoonIndex = (startIndex + i + 1) % 60;
			} else {
				daewoonIndex = (startIndex - i - 1 + 60) % 60;
			}

			String daewoonGanji = GAPJA_CYCLE.get(daewoonIndex);
			prompt.append(String.format("%d세 ~ %d세: %s 대운\n", age, age + 9, daewoonGanji));
		}
	}

	private String extractContentFromResponseGpt5(String jsonResponse)
		throws JsonProcessingException {
		if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
			throw new IllegalArgumentException("GPT 응답이 비어있습니다.");
		}
		try {
			JsonNode root = objectMapper.readTree(jsonResponse);
			if (root.path("error").isObject()) {
				JsonNode errorNode = root.get("error");
				String errorMessage = errorNode.path("message").asText("알 수 없는 API 오류");
				log.error("GPT API 에러: {}", errorMessage);
				throw new IllegalArgumentException("GPT API 에러: " + errorMessage);
			}
			JsonNode outputNode = root.path("output");
			if (!outputNode.isArray()) {
				log.error("응답에 'output' 배열이 없습니다.");
				throw new IllegalArgumentException("GPT 응답 형식이 올바르지 않습니다. ('output' 배열 누락)");
			}
			for (JsonNode outputItem : outputNode) {
				if ("message".equals(outputItem.path("type").asText())) {
					JsonNode contentArray = outputItem.path("content");
					if (contentArray.isArray() && contentArray.size() > 0) {
						String content = contentArray.get(0).path("text").asText();
						log.info("✅ GPT 응답 성공 - 길이: {} 문자", content.length());
						return content;
					}
				}
			}
			log.error("GPT 응답에서 최종 'text' 필드를 찾을 수 없습니다. JSON 구조 확인 필요.");
			throw new IllegalArgumentException("GPT 응답에서 내용 추출 실패.");
		} catch (JsonProcessingException e) {
			log.error("JSON 파싱 실패: {}", e.getMessage());
			throw e;
		}
	}

	/**
	 * 두 사람의 만세력 데이터를 바탕으로 GPT-5에게 궁합 분석을 요청합니다.
	 *
	 * @param person1Name 첫 번째 사람의 이름
	 * @param person1Saju 첫 번째 사람의 만세력 데이터
	 * @param person2Name 두 번째 사람의 이름
	 * @param person2Saju 두 번째 사람의 만세력 데이터
	 * @return AI가 분석한 궁합 결과가 담긴 응답 DTO
	 */
	public ManseCompatibilityAnalysisResponse analyzeCompatibility(
		String person1Name, ManseryeokCalculationResponse person1Saju,
		String person2Name, ManseryeokCalculationResponse person2Saju) {

		log.info("✅ 궁합 분석 요청 시작: {} & {}", person1Name, person2Name);

		try {
			// 궁합 분석을 위한 전용 프롬프트 생성
			String userPrompt = createCompatibilityPrompt(person1Name, person1Saju, person2Name,
				person2Saju);
			String input = GPT5_SYSTEM_INSTRUCTION + userPrompt;

			Gpt5Request gpt5Request = new Gpt5Request(
				"gpt-5",
				input,
				10000,
				"high",
				"medium"
			);

			String requestBody = objectMapper.writeValueAsString(gpt5Request);

			log.info("GPT-5 궁합 분석 요청 데이터 생성 완료. API 호출 시작...");
			String gptResponse = restClient.post()
				.uri("/responses")
				.body(requestBody)
				.retrieve()
				.body(String.class);

			log.info("GPT-5 궁합 분석 응답 수신 완료.");
			String analysisText = extractContentFromResponseGpt5(gptResponse);

			return new ManseCompatibilityAnalysisResponse(
				person1Name,
				person2Name,
				analysisText
			);

		} catch (Exception e) {
			log.error("GPT API 궁합 분석 요청 중 오류 발생: {}", e.getMessage(), e);
			return new ManseCompatibilityAnalysisResponse(
				person1Name,
				person2Name,
				"궁합 분석 중 API 요청 오류가 발생했습니다. 서버 로그를 확인해주세요."
			);
		}
	}

	/**
	 * 두 사람의 사주 정보를 바탕으로 궁합 분석용 프롬프트를 생성합니다.
	 */
	private String createCompatibilityPrompt(String person1Name,
		ManseryeokCalculationResponse person1Saju,
		String person2Name, ManseryeokCalculationResponse person2Saju) {
		StringBuilder prompt = new StringBuilder();

		// --- 시스템 역할 및 분석 요청 정의 ---
		prompt.append("### 시스템 역할 정의 ###\n");
		prompt.append("당신은 관계 심리학과 사주명리학에 모두 정통한 30년 경력의 궁합 전문 상담가입니다.\n");
		prompt.append(
			"두 사람의 사주 데이터를 기반으로, 단순 길흉 판단을 넘어 서로를 깊이 이해하고 관계를 발전시키는 데 도움이 되는 건설적인 조언을 제공해야 합니다.\n");
		prompt.append("'해요'체를 사용하여 친절하고 따뜻하게 설명해주세요.\n\n");

		// --- 분석 대상자 1 정보 ---
		prompt.append("### 첫 번째 사람 정보 ###\n");
		appendPersonInfoToPrompt(prompt, person1Name, person1Saju);

		// --- 분석 대상자 2 정보 ---
		prompt.append("### 두 번째 사람 정보 ###\n");
		appendPersonInfoToPrompt(prompt, person2Name, person2Saju);

		// --- 종합 분석 요청 항목 ---
		prompt.append("### 종합 궁합 분석 요청 ###\n\n");
		prompt.append("위 두 사람의 데이터를 종합하여 아래 5개 항목을 심층적으로 분석해주세요.\n");
		prompt.append("각 항목은 명확히 구분하고, 사주명리학적 근거를 제시하되 누구나 쉽게 이해할 수 있도록 설명해주세요.\n\n");
		prompt.append("【분석 시작】\n");
		prompt.append(
			String.format("먼저 \"%s님과 %s님의 궁합을 분석해 드릴게요.\"로 시작하세요.\n\n", person1Name, person2Name));

		prompt.append("## 1. 서로에게 첫눈에 끌리는 부분 (일간 관계 중심)\n");
		prompt.append(
			"- 두 사람의 일간(日干) 오행 관계(상생/상극)를 분석하고, 이것이 서로의 첫인상과 성격적 끌림에 어떻게 작용하는지 설명해주세요.\n\n");

		prompt.append("## 2. 함께할 때의 에너지와 안정감 (오행 보완 관계)\n");
		prompt.append(
			"- 각자에게 부족한 오행과 넘치는 오행을 분석하고, 두 사람이 함께 있을 때 서로의 오행 기운을 어떻게 보완해주거나 혹은 충돌하는지 설명해주세요.\n\n");

		prompt.append("## 3. 현실적인 관계에서의 역할과 갈등 요소 (십성 관계 중심)\n");
		prompt.append("- 각자의 십성(十星) 분포를 통해 두 사람이 연인/부부 관계에서 어떤 역할을 맡게 될 가능성이 높은지 예측해주세요.\n");
		prompt.append("- 어떤 부분(예: 표현 방식, 가치관 등)에서 갈등이 발생할 수 있고, 이를 어떻게 지혜롭게 해결할 수 있을지 조언해주세요.\n\n");

		prompt.append("## 4. 관계 발전을 위한 조언\n");
		prompt.append("- 서로의 장점을 더욱 살리고 단점을 보완해주기 위한 구체적인 행동 지침을 2~3가지 제안해주세요.\n\n");

		prompt.append("## 5. 총평\n");
		prompt.append("- 두 사람의 관계를 한 문장으로 요약하고, 행복한 관계를 위한 핵심 포인트를 강조하며 긍정적으로 마무리해주세요.\n");

		return prompt.toString();
	}

	/**
	 * 궁합 프롬프트에 한 사람의 사주 정보를 추가하는 헬퍼 메서드입니다.
	 */
	private void appendPersonInfoToPrompt(StringBuilder prompt, String name,
		ManseryeokCalculationResponse manseResponse) {
		ManseryeokCalculationResponse.SajuInfo saju = manseResponse.getSaju();

		prompt.append(String.format("이름: %s\n", name));
		String sajuPalja = String.format("%s%s %s%s %s%s %s%s",
			saju.getYearSky().getKorean(), saju.getYearGround().getKorean(),
			saju.getMonthSky().getKorean(), saju.getMonthGround().getKorean(),
			saju.getDaySky().getKorean(), saju.getDayGround().getKorean(),
			saju.getTimeSky() != null ? saju.getTimeSky().getKorean() : "?",
			saju.getTimeGround() != null ? saju.getTimeGround().getKorean() : "?"
		);
		prompt.append(String.format("사주명식: %s\n", sajuPalja));
		prompt.append(String.format("일간: %s%s\n", saju.getDaySky().getKorean(),
			saju.getDaySky().getFiveCircle()));

		// 오행 분포
		Map<String, Integer> ohaengCounts = new HashMap<>();
		Map<String, Integer> sipseongCounts = new HashMap<>(); // sipseong is not used here but calculated for consistency
		calculateDistribution(saju, ohaengCounts, sipseongCounts);
		prompt.append("오행 분포: ");
		ohaengCounts.forEach((key, value) -> prompt.append(String.format("%s %d개, ", key, value)));
		prompt.delete(prompt.length() - 2, prompt.length()); // 마지막 ", " 제거
		prompt.append("\n\n");
	}
}
