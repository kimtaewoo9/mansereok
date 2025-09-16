package com.mansereok.server.service.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatCreateResponse {

	private int status;
	private BasicChartData data;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BasicChartData {

		@JsonProperty("_기본명식")
		private SajuChart sajuChart;

		@JsonProperty("_신살")
		private SinsalInfo sinsal;

		private ProfileInfo profile;

		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		public static class SajuChart {

			@JsonProperty("_세차")
			private Pillar yearPillar;

			@JsonProperty("_월건")
			private Pillar monthPillar;

			@JsonProperty("_일진")
			private Pillar dayPillar;

			@JsonProperty("_시진")
			private Pillar timePillar;
		}

		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		public static class Pillar {

			@JsonProperty("_천간")
			private Element cheongan;

			@JsonProperty("_지지")
			private Element jiji;
		}

		// 신살 정보
		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		public static class SinsalInfo {

			@JsonProperty("_세차")
			private SinsalElement yearSinsal;

			@JsonProperty("_월건")
			private SinsalElement monthSinsal;

			@JsonProperty("_일진")
			private SinsalElement daySinsal;

			@JsonProperty("_시진")
			private SinsalElement timeSinsal;
		}

		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		public static class SinsalElement {

			private int id;
			private String name;
			private String chinese;
		}

		// 프로필 정보
		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		public static class ProfileInfo {

			private int index;
			private String avatar;
			private String sexagenaryCycle;
			private String sunBirth;
			private String lunBirth;
			private String adjustedBirth;
			private String location;
			private String adjusted;
		}
	}
}
