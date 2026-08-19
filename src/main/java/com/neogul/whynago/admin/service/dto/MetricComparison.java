package com.neogul.whynago.admin.service.dto;

// 증감률·화살표 같은 표시 값은 화면이 만든다. 서버는 비교 대상 두 수치만 담는다.
public record MetricComparison(long current, long previous) {
}
