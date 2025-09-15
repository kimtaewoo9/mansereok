package com.mansereok.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CalendarType {
	SOLAR_CALENDAR("양력"),
	LUNAR_CALENDAR("음력");

	private final String description;
}
