package com.mansereok.server.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "user_info")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalInfo {

	// 사용자 정보
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Enumerated(EnumType.STRING)
	private Gender gender;

	// 생년월일시, 양력, 음력 정보
	private String birthDate; // YYYY/MM/DD
	private String birthTime; // HH:mm
	private boolean isTimeUnknown; // 시간 모름 여부
	private CalendarType calendarType;

	// 출생지 정보 (태어난 도시 모를 경우 기본값 서울 특별시)
	private String city;
	private String locationId; // 기본값: 1835847 (서울 특별시)

	// 자정 보정 여부
	private boolean midnightAdjust; // 자정 보정 여부

	// 생성 시간
	private LocalDateTime createdAt;

	public static PersonalInfo create(String name, Gender gender, String birthDate,
		String birthTime, boolean isTimeUnknown, String city) {
		PersonalInfo personalInfo = new PersonalInfo();
		personalInfo.name = name;
		personalInfo.gender = gender;
		personalInfo.birthDate = birthDate;
		personalInfo.birthTime = birthTime;
		personalInfo.isTimeUnknown = isTimeUnknown;
		personalInfo.city = city;
		personalInfo.createdAt = LocalDateTime.now();
		return personalInfo;
	}
}
