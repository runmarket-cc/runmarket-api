package com.runmarket.pacer.web.dto.content;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** 리스트 항목. {@code subItems} 가 있으면 중첩(하위) 목록으로 렌더링한다. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LegalListItem(
        String text,
        List<LegalListItem> subItems
) {
    public static LegalListItem item(String text) {
        return new LegalListItem(text, null);
    }

    public static LegalListItem item(String text, LegalListItem... subItems) {
        return new LegalListItem(text, List.of(subItems));
    }
}
