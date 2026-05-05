package com.runmarket.pacer.socket.security;

import com.runmarket.pacer.socket.model.WsSessionAttributes;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Getter
public class WsAuthenticationToken extends AbstractAuthenticationToken {

    private final WsSessionAttributes attributes;

    public WsAuthenticationToken(WsSessionAttributes attributes) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + attributes.role().name())));
        this.attributes = attributes;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getPrincipal() { return attributes; }

}
