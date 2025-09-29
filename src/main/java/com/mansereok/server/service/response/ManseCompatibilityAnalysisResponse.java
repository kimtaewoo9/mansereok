package com.mansereok.server.service.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManseCompatibilityAnalysisResponse {

	private String person1Name;
	private String person2Name;
	private String analysisResult;
}
