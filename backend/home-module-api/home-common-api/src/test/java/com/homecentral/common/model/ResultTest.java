package com.homecentral.common.model;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void shouldReturnSuccessWhenOk() {
        Result<String> result = Result.ok("hello");
        assertTrue(result.isSuccess());
        assertEquals("OK", result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("hello", result.getData());
    }

    @Test
    void shouldReturnSuccessWithNullDataWhenOkNull() {
        Result<String> result = Result.ok(null);
        assertTrue(result.isSuccess());
        assertNull(result.getData());
    }

    @Test
    void shouldReturnErrorWhenBizError() {
        Result<String> result = Result.bizError("AUTH_FAIL", "登录失败");
        assertFalse(result.isSuccess());
        assertEquals("AUTH_FAIL", result.getCode());
        assertEquals("登录失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void shouldReturnPageWhenPage() {
        Page<String> page = new PageImpl<>(List.of("a", "b"));
        Result<Page<String>> result = Result.page(page);
        assertTrue(result.isSuccess());
        assertEquals("OK", result.getCode());
        assertEquals(2, result.getData().getTotalElements());
    }
}
