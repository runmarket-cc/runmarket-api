package com.runmarket.pacer.web.controller;

import com.runmarket.pacer.domain.port.in.auth.AuthToken;
import com.runmarket.pacer.domain.port.in.socket.IssueSocketTokenCommand;
import com.runmarket.pacer.domain.port.in.socket.IssueSocketTokenUseCase;
import com.runmarket.pacer.web.dto.SocketTokenRequest;
import com.runmarket.pacer.web.dto.TokenResponse;
import com.runmarket.pacer.web.mapper.TokenResponseMapper;
import com.runmarket.pacer.web.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/socket-token")
@RequiredArgsConstructor
public class SocketTokenController {

    private final IssueSocketTokenUseCase issueSocketTokenUseCase;
    private final TokenResponseMapper tokenResponseMapper;

    @PostMapping
    public ResponseEntity<TokenResponse> issue(@Valid @RequestBody SocketTokenRequest request) {
        AuthToken token = issueSocketTokenUseCase.issue(new IssueSocketTokenCommand(
                SecurityUtils.currentUserEmail(),
                request.role(),
                request.groupId(),
                request.runnerId()
        ));
        return ResponseEntity.ok(tokenResponseMapper.toResponse(token));
    }
}
