package com.mansereok.server.service.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

@Data
@Schema(description = "만세력 계산 요청")
public class ManseryeokCalculationRequest {

	@NotNull(message = "생년월일은 필수입니다")
	@JsonFormat(pattern = "yyyy-MM-dd")
	@Schema(description = "생년월일 (양력)", example = "1987-02-13", required = true)
	@JsonProperty("solar_date")
	private LocalDate solarDate;

	@JsonFormat(pattern = "HH:mm")
	@Schema(description = "출생시간 (HH:mm 형식)", example = "14:30")
	@JsonProperty("solar_time")
	private LocalTime solarTime;

	@NotNull(message = "성별은 필수입니다")
	@Pattern(regexp = "^(MALE|FEMALE)$", message = "성별은 MALE 또는 FEMALE이어야 합니다")
	@Schema(description = "성별", example = "MALE", allowableValues = {"MALE",
		"FEMALE"}, required = true)
	private String gender;

	@NotNull(message = "음력 여부는 필수입니다")
	@Schema(description = "음력 여부", example = "false", required = true)
	@JsonProperty("is_lunar")
	private Boolean isLunar;

	// 시간이 null인 경우 12:00으로 기본값 설정
	public LocalTime getSolarTime() {
		return solarTime != null ? solarTime : LocalTime.of(12, 0);
	}
}
