package com.mansereok.server.service;

import com.mansereok.server.entity.Manse;
import com.mansereok.server.repository.ManseRepository;
import com.mansereok.server.service.request.ManseryeokCalculationRequest;
import com.mansereok.server.service.response.ManseryeokCalculationResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManseCalculationService {

	private final ManseRepository manseRepository;
	private final SajuDataService sajuDataService;

	public ManseryeokCalculationResponse calculate(ManseryeokCalculationRequest request) {
		try {
			log.info("만세력 계산 시작: solarDate={}, gender={}, isLunar={}",
				request.getSolarDate(), request.getGender(), request.getIsLunar());

			// 1. 생년월일을 삼주(양력)로 변환
			SamjuResult samju = convertBirthToSamju(
				request.getIsLunar() ? "LUNAR" : "SOLAR",
				request.getSolarDate(),
				request.getSolarTime()
			);

			// 2. 생년월일시(양력) 생성
			LocalDateTime solarDatetime = LocalDateTime.of(
				samju.getSolarDate(),
				request.getSolarTime()
			);

			// 3. 순행(true), 역행(false) 판단
			boolean direction = isRightDirection(request.getGender(), samju.getYearSky());

			// 4. 절입시간 가져오기
			LocalDateTime seasonTime = getSeasonStartTime(direction, solarDatetime);

			// 5. 대운수 및 대운 시작년 가져오기
			BigFortuneResult bigFortune = getBigFortuneNumber(direction, seasonTime, solarDatetime);

			// 6. 시주 가져오기
			TimePillarResult timePillar = getTimePillar(samju.getDaySky(), request.getSolarTime());

			// 7. 응답 생성
			return ManseryeokCalculationResponse.builder()
				.input(ManseryeokCalculationResponse.InputInfo.builder()
					.solarDate(request.getSolarDate())
					.solarTime(request.getSolarTime())
					.gender(request.getGender())
					.isLunar(request.getIsLunar())
					.build())
				.saju(ManseryeokCalculationResponse.SajuInfo.builder()
					.bigFortuneNumber(bigFortune.getBigFortuneNumber())
					.bigFortuneStartYear(bigFortune.getBigFortuneStart())
					.seasonStartTime(samju.getSeasonStartTime())
					.yearSky(formatChinese(samju.getYearSky(), samju.getDaySky(), false))
					.yearGround(formatChinese(samju.getYearGround(), samju.getDaySky(), true))
					.monthSky(formatChinese(samju.getMonthSky(), samju.getDaySky(), false))
					.monthGround(formatChinese(samju.getMonthGround(), samju.getDaySky(), true))
					.daySky(formatChinese(samju.getDaySky(), samju.getDaySky(), false))
					.dayGround(formatChinese(samju.getDayGround(), samju.getDaySky(), true))
					.timeSky(timePillar.getTimeSky() != null ?
						formatChinese(timePillar.getTimeSky(), samju.getDaySky(), false) : null)
					.timeGround(timePillar.getTimeGround() != null ?
						formatChinese(timePillar.getTimeGround(), samju.getDaySky(), true) : null)
					.build())
				.build();

		} catch (Exception e) {
			log.error("만세력 계산 중 오류 발생", e);
			throw new RuntimeException("만세력 계산 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	/**
	 * 생년월일을 삼주로 변환
	 */
	private SamjuResult convertBirthToSamju(String birthdayType, LocalDate birthday,
		LocalTime time) {
		LocalTime birthtime = time != null ? time : LocalTime.of(12, 0);

		// 23:30 ~ 23:59 자시에 태어난 경우 다음날로 처리
		if (time != null &&
			((time.isAfter(LocalTime.of(23, 30)) || time.equals(LocalTime.of(23, 30))) &&
				time.isBefore(LocalTime.of(23, 59, 59)))) {
			birthday = birthday.plusDays(1);
			log.info("자시 처리: 날짜를 다음날로 변경 -> {}", birthday);
		}

		log.info("만세력 데이터 조회: birthdayType={}, birthday={}", birthdayType, birthday);

		Manse samju = birthdayType.equals("SOLAR") ?
			manseRepository.findBySolarDate(birthday)
				.orElseThrow(() -> new RuntimeException("해당 양력 날짜의 만세력 데이터를 찾을 수 없습니다.")) :
			manseRepository.findByLunarDate(birthday)
				.orElseThrow(() -> new RuntimeException("해당 음력 날짜의 만세력 데이터를 찾을 수 없습니다."));

		// 절입일인 경우 처리
		if (samju.getSeason() != null && !samju.getSeason().isEmpty()) {
			log.info("절입일 처리: season={}, seasonStartTime={}", samju.getSeason(),
				samju.getSeasonStartTime());

			LocalDateTime seasonTime = samju.getSeasonStartTime();
			LocalDateTime solarDatetime = LocalDateTime.of(birthday, birthtime);

			if (solarDatetime.isBefore(seasonTime)) {
				log.info("절입시간 이전 출생: 이전 날짜 만세력 사용");
				Manse previousManse = manseRepository.findBySolarDate(birthday.minusDays(1))
					.orElseThrow(() -> new RuntimeException("이전 날짜의 만세력 데이터를 찾을 수 없습니다"));

				return SamjuResult.builder()
					.solarDate(samju.getSolarDate())
					.yearSky(previousManse.getYearSky())
					.yearGround(previousManse.getYearGround())
					.monthSky(previousManse.getMonthSky())
					.monthGround(previousManse.getMonthGround())
					.daySky(previousManse.getDaySky())
					.dayGround(previousManse.getDayGround())
					.seasonStartTime(samju.getSeasonStartTime() != null ?
						samju.getSeasonStartTime().toString() : null)
					.build();
			}
		}

		return SamjuResult.builder()
			.solarDate(samju.getSolarDate())
			.yearSky(samju.getYearSky())
			.yearGround(samju.getYearGround())
			.monthSky(samju.getMonthSky())
			.monthGround(samju.getMonthGround())
			.daySky(samju.getDaySky())
			.dayGround(samju.getDayGround())
			.seasonStartTime(samju.getSeasonStartTime() != null ?
				samju.getSeasonStartTime().toString() : null)
			.build();
	}

	/**
	 * 순행(true), 역행(false) 판단 (성별, 연간)
	 */
	private boolean isRightDirection(String gender, String yearSky) {
		String minusPlus = sajuDataService.getMinusPlus().get(yearSky);

		if (minusPlus == null) {
			throw new RuntimeException("연간 " + yearSky + "의 음양 정보를 찾을 수 없습니다");
		}

		// 남양여음 순행, 남음여양 역행
		boolean result;
		if (("MALE".equals(gender) && "양".equals(minusPlus)) ||
			("FEMALE".equals(gender) && "음".equals(minusPlus))) {
			result = true; // 순행
		} else {
			result = false; // 역행
		}

		log.info("대운 방향 판단: gender={}, yearSky={}, minusPlus={}, direction={}",
			gender, yearSky, minusPlus, result ? "순행" : "역행");

		return result;
	}

	/**
	 * 절입 시간 가져오기
	 */
	private LocalDateTime getSeasonStartTime(boolean direction, LocalDateTime solarDatetime) {
		Manse manse;

		if (direction) {
			// 순행: 생년월일 뒤에 오는 절입 시간
			manse = manseRepository.findFirstBySeasonStartTimeGreaterThanEqualOrderBySolarDateAsc(
					solarDatetime)
				.orElseThrow(() -> new RuntimeException("순행 절입 시간을 찾을 수 없습니다"));
		} else {
			// 역행: 생년월일 앞에 오는 절입 시간
			manse = manseRepository.findFirstBySeasonStartTimeLessThanEqualOrderBySolarDateDesc(
					solarDatetime)
				.orElseThrow(() -> new RuntimeException("역행 절입 시간을 찾을 수 없습니다"));
		}

		log.info("절입시간 조회 완료: seasonStartTime={}, direction={}",
			manse.getSeasonStartTime(), direction ? "순행" : "역행");

		return manse.getSeasonStartTime();
	}

	/**
	 * 대운수 및 대운 시작 구하기
	 */
	private BigFortuneResult getBigFortuneNumber(boolean direction, LocalDateTime seasonStartTime,
		LocalDateTime solarDatetime) {
		long diffDays;

		if (direction) {
			// 순행
			diffDays = ChronoUnit.DAYS.between(solarDatetime, seasonStartTime);
		} else {
			// 역행
			diffDays = ChronoUnit.DAYS.between(seasonStartTime, solarDatetime);
		}

		int divider = (int) (diffDays / 3);
		int remainder = (int) (diffDays % 3);

		int bigFortuneNumber = divider;
		if (diffDays < 4) {
			bigFortuneNumber = 1;
		}

		if (remainder == 2) {
			bigFortuneNumber += 1;
		}

		int bigFortuneStart = solarDatetime.getYear() + bigFortuneNumber;

		log.info("대운 계산 완료: diffDays={}, bigFortuneNumber={}, bigFortuneStart={}",
			diffDays, bigFortuneNumber, bigFortuneStart);

		return BigFortuneResult.builder()
			.bigFortuneNumber(bigFortuneNumber)
			.bigFortuneStart(bigFortuneStart)
			.build();
	}

	/**
	 * 시주 계산하기
	 */
	private TimePillarResult getTimePillar(String daySky, LocalTime time) {
		if (time == null) {
			log.info("출생시간이 없어 시주 계산 생략");
			return TimePillarResult.builder()
				.timeSky(null)
				.timeGround(null)
				.build();
		}

		String timeKey = getTimeJuIndex(time);
		if (timeKey == null) {
			log.warn("시간 {}에 해당하는 시주 인덱스를 찾을 수 없습니다", time);
			return TimePillarResult.builder()
				.timeSky(null)
				.timeGround(null)
				.build();
		}

		Map<String, Map<String, String[]>> timeJuData2 = sajuDataService.getTimeJuData2();
		Map<String, String[]> dayData = timeJuData2.get(daySky);

		if (dayData == null) {
			throw new RuntimeException("일간 " + daySky + "의 시주 데이터를 찾을 수 없습니다");
		}

		if (dayData.containsKey(timeKey)) {
			String[] timeJu = dayData.get(timeKey);
			log.info("시주 계산 완료: daySky={}, time={}, timeKey={}, timeSky={}, timeGround={}",
				daySky, time, timeKey, timeJu[0], timeJu[1]);

			return TimePillarResult.builder()
				.timeSky(timeJu[0])
				.timeGround(timeJu[1])
				.build();
		}

		throw new RuntimeException("시주 계산 실패: daySky=" + daySky + ", timeKey=" + timeKey);
	}

	private String getTimeJuIndex(LocalTime time) {
		Map<String, LocalTime[]> timeJuData = sajuDataService.getTimeJuData();

		for (Map.Entry<String, LocalTime[]> entry : timeJuData.entrySet()) {
			LocalTime[] timeRange = entry.getValue();
			if (time.compareTo(timeRange[0]) >= 0 && time.compareTo(timeRange[1]) <= 0) {
				return entry.getKey();
			}
		}

		// 자시 특별 처리 (23:30-01:29)
		if ((time.isAfter(LocalTime.of(23, 30)) || time.equals(LocalTime.of(23, 30))) ||
			(time.isBefore(LocalTime.of(1, 30)) && time.isAfter(LocalTime.of(0, 0)))) {
			return "0";
		}

		return null;
	}

	/**
	 * 중국어 간지를 포맷팅된 정보로 변환
	 */
	private ManseryeokCalculationResponse.PillarElement formatChinese(String chinese, String daySky,
		boolean isGround) {
		Map<String, String> koreanData = sajuDataService.convertChineseToKorean();
		Map<String, Map<String, String>> tenStarData = sajuDataService.getTenStar();
		Map<String, String> minusPlusData = sajuDataService.getMinusPlus();

		// daySky 기준으로 십성 데이터 가져오기
		Map<String, String> tenStar = tenStarData.get(daySky);
		if (tenStar == null) {
			throw new RuntimeException("일간 " + daySky + "의 십성 데이터를 찾을 수 없습니다");
		}

		String tenStarInfo = tenStar.get(chinese);
		if (tenStarInfo == null) {
			throw new RuntimeException("간지 " + chinese + "의 십성 정보를 찾을 수 없습니다 (일간: " + daySky + ")");
		}

		String[] tenStarParts = tenStarInfo.split(",");
		if (tenStarParts.length != 2) {
			throw new RuntimeException("십성 정보 형식이 올바르지 않습니다: " + tenStarInfo);
		}

		ManseryeokCalculationResponse.PillarElement.PillarElementBuilder builder =
			ManseryeokCalculationResponse.PillarElement.builder()
				.chinese(chinese)
				.korean(koreanData.get(chinese))
				.fiveCircle(tenStarParts[1])  // 오행
				.fiveCircleColor(getColor(tenStarParts[1]))
				.tenStar(tenStarParts[0])     // 십성
				.minusPlus(minusPlusData.get(chinese));

		if (isGround) {
			builder.jijanggan(getJijangganInfo(chinese));
		}

		return builder.build();
	}

	private String getColor(String value) {
		return switch (value) {
			case "목" -> "#4CAF50";
			case "화" -> "#F44336";
			case "토" -> "#FFD600";
			case "금" -> "#E0E0E0";
			case "수" -> "#039BE5";
			default -> "";
		};
	}

	private ManseryeokCalculationResponse.JijangganInfo getJijangganInfo(String jiji) {
		Map<String, Map<String, Object>> jijangganData = sajuDataService.getJijangan();
		Map<String, Object> jijiData = jijangganData.get(jiji);

		if (jijiData == null) {
			return null;
		}

		return ManseryeokCalculationResponse.JijangganInfo.builder()
			.first(createJijangganElement((Map<String, Object>) jijiData.get("first")))
			.second(createJijangganElement((Map<String, Object>) jijiData.get("second")))
			.third(createJijangganElement((Map<String, Object>) jijiData.get("third")))
			.build();
	}

	@SuppressWarnings("unchecked")
	private ManseryeokCalculationResponse.JijangganElement createJijangganElement(
		Map<String, Object> elementData) {
		if (elementData == null) {
			return null;
		}

		return ManseryeokCalculationResponse.JijangganElement.builder()
			.chinese((String) elementData.get("chinese"))
			.korean((String) elementData.get("korean"))
			.fiveCircle((String) elementData.get("fiveCircle"))
			.fiveCircleColor((String) elementData.get("fiveCircleColor"))
			.minusPlus((String) elementData.get("minusPlus"))
			.rate((Integer) elementData.get("rate"))
			.build();
	}

	// Inner classes for return types
	@lombok.Data
	@lombok.Builder
	private static class SamjuResult {

		private LocalDate solarDate;
		private String yearSky;
		private String yearGround;
		private String monthSky;
		private String monthGround;
		private String daySky;
		private String dayGround;
		private String seasonStartTime;
	}

	@lombok.Data
	@lombok.Builder
	private static class BigFortuneResult {

		private Integer bigFortuneNumber;
		private Integer bigFortuneStart;
	}

	@lombok.Data
	@lombok.Builder
	private static class TimePillarResult {

		private String timeSky;
		private String timeGround;
	}
}
