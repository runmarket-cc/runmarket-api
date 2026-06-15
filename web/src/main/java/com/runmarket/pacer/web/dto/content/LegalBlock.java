package com.runmarket.pacer.web.dto.content;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 문서 본문 블록. {@code type} 에 따라 사용하는 필드가 달라진다.
 * <ul>
 *   <li>{@code "paragraph"} → {@code text}</li>
 *   <li>{@code "orderedList"} / {@code "unorderedList"} → {@code items}</li>
 *   <li>{@code "table"} → {@code table}</li>
 * </ul>
 * 사용하지 않는 필드는 응답에서 생략된다(NON_NULL).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LegalBlock(
        String type,
        String text,
        List<LegalListItem> items,
        LegalTable table
) {
    public static LegalBlock paragraph(String text) {
        return new LegalBlock("paragraph", text, null, null);
    }

    public static LegalBlock orderedList(LegalListItem... items) {
        return new LegalBlock("orderedList", null, List.of(items), null);
    }

    public static LegalBlock unorderedList(LegalListItem... items) {
        return new LegalBlock("unorderedList", null, List.of(items), null);
    }

    public static LegalBlock table(List<String> headers, List<List<String>> rows) {
        return new LegalBlock("table", null, null, new LegalTable(headers, rows));
    }
}
