package me.ifmo.backend.services.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class WebSocketEvent {
    private final String entity;
    private final String action;
    private final Object data;
}
