package com.mansereok.server.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CalendarType {
	SOLAR_CALENDAR("S"), // 양력
	LUNAR_CALENDAR("M"); // 음력

	private final String description;
}
