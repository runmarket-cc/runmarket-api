package com.runmarket.pacer.web.dto.content;

import java.util.List;

/** 약관/방침 등 법적 고지 문서. 프론트(www.runmarket.cc)가 호출하여 직접 렌더링한다. */
public record LegalDocumentContent(
        String title,
        String effectiveDate,
        List<LegalSection> sections
) {}
