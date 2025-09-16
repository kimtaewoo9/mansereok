package com.mansereok.server.service.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OhaengCreateResponse {

	private int status;
	private AnalysisData data;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AnalysisData {

		@JsonProperty("_오행")
		private List<ElementInfo> ohaeng;

		@JsonProperty("_십성")
		private List<ElementInfo> sipseong;

		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		public static class ElementInfo {

			private Element element;
			private double point;
			private double percent;
			private String description;
		}
	}
}
