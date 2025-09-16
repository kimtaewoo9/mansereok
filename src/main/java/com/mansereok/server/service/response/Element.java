package com.mansereok.server.service.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Element {

	private int id;
	private String name;
	private String chinese;

	@JsonProperty("_음양")
	private YinYang yinYang;

	@JsonProperty("_오행")
	private Ohaeng ohaeng;

	@JsonProperty("_십성")
	private Sipseong sipseong;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class YinYang {

		private int id;
		private String name;
		private String chinese;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Ohaeng {

		private int id;
		private String name;
		private String chinese;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Sipseong {

		private int id;
		private String name;
		private String chinese;
	}
}
