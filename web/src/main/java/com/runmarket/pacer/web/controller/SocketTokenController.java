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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/races")
@RequiredArgsConstructor
public class SocketTokenController {

    private final IssueSocketTokenUseCase issueSocketTokenUseCase;
    private final TokenResponseMapper tokenResponseMapper;

    @PostMapping("/{raceId}/socket-token")
    public ResponseEntity<TokenResponse> issue(
            @PathVariable UUID raceId,
            @Valid @RequestBody SocketTokenRequest request) {
        AuthToken token = issueSocketTokenUseCase.issue(new IssueSocketTokenCommand(
                SecurityUtils.currentUserEmail(),
                raceId,
                request.role(),
                request.groupId(),
                request.runnerId(),
                request.groupIds()
        ));
        return ResponseEntity.ok(tokenResponseMapper.toResponse(token));
    }
}
