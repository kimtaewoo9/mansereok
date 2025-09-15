package com.mansereok.server.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TimeType {

	EXACT("시간 모름"),
	UNKNOWN("야자시/조자시");

	private final String description;
}
