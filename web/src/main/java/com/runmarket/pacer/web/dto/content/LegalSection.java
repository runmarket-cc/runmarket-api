package com.runmarket.pacer.web.dto.content;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** 문서의 한 조항. heading 이 없는 도입부 문단도 표현할 수 있다. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LegalSection(
        String heading,
        List<LegalBlock> blocks
) {
    public static LegalSection of(String heading, LegalBlock... blocks) {
        return new LegalSection(heading, List.of(blocks));
    }

    /** heading 없는 도입부 섹션 */
    public static LegalSection intro(LegalBlock... blocks) {
        return new LegalSection(null, List.of(blocks));
    }
}
