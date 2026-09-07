package com.runmarket.pacer.web.filter;

import com.runmarket.pacer.domain.port.in.auth.AuthToken;
import com.runmarket.pacer.domain.port.out.auth.TokenProvider;
import com.runmarket.pacer.web.security.UserDetailsServiceAdapter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private UserDetailsServiceAdapter userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private static final long REFRESH_THRESHOLD_MS = 1800000L; // 30 mins

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService, REFRESH_THRESHOLD_MS);
        SecurityContextHolder.clearContext();
    }

    private UserDetails sampleUserDetails(String email) {
        return User.builder()
                .username(email)
                .password("encoded")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    @DisplayName("토큰 만료가 30분 이하로 남았을 때 응답 Authorization 헤더에 갱신된 토큰이 설정된다")
    void doFilterInternal_renewsTokenWhenExpiringWithinThreshold() throws ServletException, IOException {
        String token = "valid-expiring-token";
        String refreshedTokenString = "refreshed-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(tokenProvider.validateToken(token)).willReturn(true);
        given(tokenProvider.getSubject(token)).willReturn("user@example.com");
        given(userDetailsService.loadUserByUsername("user@example.com")).willReturn(sampleUserDetails("user@example.com"));
        given(tokenProvider.isExpiringWithin(eq(token), eq(REFRESH_THRESHOLD_MS))).willReturn(true);
        given(tokenProvider.refreshToken(token)).willReturn(new AuthToken(refreshedTokenString, LocalDateTime.now().plusDays(30)));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + refreshedTokenString);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@example.com");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰 만료가 30분을 초과하여 남았을 때는 Authorization 갱신 헤더가 설정되지 않는다")
    void doFilterInternal_doesNotRenewTokenWhenPlentyOfTimeLeft() throws ServletException, IOException {
        String token = "valid-fresh-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(tokenProvider.validateToken(token)).willReturn(true);
        given(tokenProvider.getSubject(token)).willReturn("user@example.com");
        given(userDetailsService.loadUserByUsername("user@example.com")).willReturn(sampleUserDetails("user@example.com"));
        given(tokenProvider.isExpiringWithin(eq(token), eq(REFRESH_THRESHOLD_MS))).willReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader(HttpHeaders.AUTHORIZATION)).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(tokenProvider, never()).refreshToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 및 토큰 갱신 없이 체인을 계속 진행한다")
    void doFilterInternal_passesThroughWhenNoAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader(HttpHeaders.AUTHORIZATION)).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).validateToken(any());
        verify(filterChain).doFilter(request, response);
    }
}
