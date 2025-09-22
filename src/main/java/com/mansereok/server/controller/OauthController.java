package com.mansereok.server.controller;

import com.mansereok.server.dto.AccessTokenDto;
import com.mansereok.server.dto.GoogleProfileDto;
import com.mansereok.server.dto.KakaoProfileDto;
import com.mansereok.server.dto.RedirectDto;
import com.mansereok.server.entity.RefreshToken;
import com.mansereok.server.entity.SocialType;
import com.mansereok.server.entity.User;
import com.mansereok.server.service.GoogleService;
import com.mansereok.server.service.KakaoService;
import com.mansereok.server.service.RefreshTokenService;
import com.mansereok.server.service.UserService;
import com.mansereok.server.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OauthController {

	private final UserService userService;
	// Oauth
	private final GoogleService googleService;
	private final KakaoService kakaoService;

	// Access Token, Refresh Token
	private final JwtUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;

	@PostMapping("/member/google/doLogin")
	public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto,
		HttpServletResponse response) {

		// access token 발급 .
		AccessTokenDto accessTokenDto = googleService.getAccessToken(redirectDto.getCode());
		// 사용자 정보 얻기 .
		GoogleProfileDto googleProfileDto =
			googleService.getGoogleProfile(accessTokenDto.getAccess_token());

		// 회원가입이 되어 있지 않다면, 회원가입 해야함.
		User user = userService.getUserBySocialId(googleProfileDto.getSub());
		if (user == null) {
			user = userService.registerWithOauth(
				googleProfileDto.getEmail().split("@")[0],
				googleProfileDto.getEmail(),
				googleProfileDto.getName(),
				googleProfileDto.getSub(),
				SocialType.GOOGLE
			);
		}

		// 회원가입이 되어있는 회원이라면, JWT 토큰 발급 + refresh token 발급
		Map<String, Object> claims = Map.of(
			"role", user.getRole().name(),
			"email", user.getEmail(),
			"userId", user.getId()
		);

		String accessToken = jwtUtil.generateAccessToken(user.getUsername(), claims);

		RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user);

		// refresh 토큰을 쿠키에 저장
		Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken.getToken());
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(false);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
		response.addCookie(refreshCookie); // response 에 담아서 전송 .

		// 최종 응답 생성 .
		Map<String, Object> responseBody = Map.of(
			"accessToken", accessToken,
			"type", "Bearer",
			"user", Map.of(
				"username", user.getUsername(),
				"email", user.getEmail(),
				"role", user.getRole().name()
			)
		);

		// access token 이랑 refresh token 반환 .
		// 프론트엔드에서는 이제 token을 꺼내서 localStorage에 저장하면 됨 .
		return ResponseEntity.ok(responseBody);
	}

	@PostMapping("/member/kakao/doLogin")
	public ResponseEntity<?> kakaoLogin(RedirectDto redirectDto,
		HttpServletResponse response) {

		// 인가코드 받아서 access token 받아옴
		AccessTokenDto accessTokenDto = kakaoService.getAccessTokenDto(redirectDto.getCode());

		// access token 으로 사용자 정보 얻어오기.
		KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfileDto(
			accessTokenDto.getAccess_token());

		// 회원가입 안되어 있으면 회원가입 ..
		User user = userService.getUserBySocialId(kakaoProfileDto.getId());
		if (user == null) {
			user = userService.registerWithOauth(
				kakaoProfileDto.getKakao_account().getEmail().split("@")[0],
				kakaoProfileDto.getKakao_account().getEmail(),
				null, // 카카오는 본명 정보를 주지 않음.
				kakaoProfileDto.getId(),
				SocialType.KAKAO
			);
		}
		// 회원가입 되어 있으면 access token + refresh token 발급 .
		// access token
		Map<String, Object> claims = Map.of(
			"role", user.getRole().name(),
			"email", user.getEmail(),
			"userId", user.getId()
		);
		String accessToken = jwtUtil.generateAccessToken(user.getUsername(), claims);

		// refresh token
		RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user);
		Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken.getToken());
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(false);
		refreshCookie.setPath("/");
		refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
		response.addCookie(refreshCookie); // response 에 담아서 전송 .

		Map<String, Object> responseBody = Map.of(
			"accessToken", accessToken,
			"type", "Bearer",
			"user", Map.of(
				"username", user.getUsername(),
				"email", user.getEmail(),
				"role", user.getRole().name()
			)
		);

		// access token 이랑 refresh token 반환 .
		// 프론트엔드에서는 이제 token을 꺼내서 localStorage에 저장하면 됨 .
		return ResponseEntity.ok(responseBody);
	}
}
