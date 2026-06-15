package com.runmarket.pacer.web.dto.content;

import java.util.List;

/** 표 블록. {@code rows} 의 각 행은 {@code headers} 와 같은 개수의 셀을 가진다. */
public record LegalTable(
        List<String> headers,
        List<List<String>> rows
) {}
