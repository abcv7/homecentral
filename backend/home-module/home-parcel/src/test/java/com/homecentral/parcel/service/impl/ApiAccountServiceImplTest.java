package com.homecentral.parcel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.parcel.api.dto.ApiAccountRequest;
import com.homecentral.parcel.api.vo.ApiAccountVO;
import com.homecentral.parcel.entity.ApiAccount;
import com.homecentral.parcel.mapper.ApiAccountMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiAccountServiceImplTest {

    @Mock
    private ApiAccountMapper apiAccountMapper;

    @InjectMocks
    private ApiAccountServiceImpl service;

    @Test
    void shouldReturnNullWhenNoAccountExists() {
        when(apiAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        ApiAccountVO vo = service.get(1L, "ALIYUN_EXPRESS");

        assertNull(vo);
    }

    @Test
    void shouldMaskApiKeyWithLast4Chars() {
        ApiAccount entity = new ApiAccount();
        entity.setId(10L);
        entity.setUserId(1L);
        entity.setProvider("ALIYUN_EXPRESS");
        entity.setApiKey("1ca63e2e1e4c4ffa9fc8a361052e49a5");
        entity.setEnabled(true);
        when(apiAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(entity);

        ApiAccountVO vo = service.get(1L, "ALIYUN_EXPRESS");

        assertNotNull(vo);
        assertEquals("****49a5", vo.getApiKeyMasked());
        assertEquals("ALIYUN_EXPRESS", vo.getProvider());
        assertTrue(vo.getEnabled());
    }

    @Test
    void shouldMaskShortApiKey() {
        ApiAccount entity = new ApiAccount();
        entity.setId(10L);
        entity.setApiKey("abc");
        when(apiAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(entity);

        ApiAccountVO vo = service.get(1L, "ALIYUN_EXPRESS");

        assertEquals("****", vo.getApiKeyMasked());
    }

    @Test
    void shouldInsertNewAccountWhenSavingFirstTime() {
        when(apiAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(apiAccountMapper.insert(any(ApiAccount.class))).thenAnswer(inv -> 1);

        ApiAccountRequest req = new ApiAccountRequest();
        req.setApiKey("new-key-1234");
        req.setCustomer("test-customer");
        req.setEnabled(true);

        ApiAccountVO vo = service.save(1L, "ALIYUN_EXPRESS", req);

        assertNotNull(vo);
        assertEquals("****1234", vo.getApiKeyMasked());
        assertEquals("test-customer", vo.getCustomer());
        assertTrue(vo.getEnabled());

        ArgumentCaptor<ApiAccount> captor = ArgumentCaptor.forClass(ApiAccount.class);
        verify(apiAccountMapper).insert(captor.capture());
        ApiAccount saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals("ALIYUN_EXPRESS", saved.getProvider());
        assertEquals("new-key-1234", saved.getApiKey());
    }

    @Test
    void shouldUpdateExistingAccount() {
        ApiAccount existing = new ApiAccount();
        existing.setId(10L);
        existing.setUserId(1L);
        existing.setProvider("ALIYUN_EXPRESS");
        existing.setApiKey("old-key");
        existing.setEnabled(false);
        when(apiAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        ApiAccountRequest req = new ApiAccountRequest();
        req.setApiKey("new-key-9999");
        req.setEnabled(true);

        service.save(1L, "ALIYUN_EXPRESS", req);

        verify(apiAccountMapper).updateById(existing);
        assertEquals("new-key-9999", existing.getApiKey());
        assertTrue(existing.getEnabled());
    }

    @Test
    void shouldThrowWhenApiKeyIsBlank() {
        ApiAccountRequest req = new ApiAccountRequest();
        req.setApiKey("   ");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.save(1L, "ALIYUN_EXPRESS", req));
        assertEquals("AppCode 不能为空", ex.getMessage());
        verify(apiAccountMapper, never()).insert(any(ApiAccount.class));
        verify(apiAccountMapper, never()).updateById(any(ApiAccount.class));
    }

    @Test
    void shouldDeleteAccount() {
        service.delete(1L, "ALIYUN_EXPRESS");

        verify(apiAccountMapper).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    void shouldResolveUserAppcodeWhenEnabled() {
        ApiAccount entity = new ApiAccount();
        entity.setUserId(1L);
        entity.setProvider("ALIYUN_EXPRESS");
        entity.setApiKey("user-key-1234");
        entity.setEnabled(true);
        when(apiAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(entity);

        String appcode = service.resolveAppcode(1L, "ALIYUN_EXPRESS");

        assertEquals("user-key-1234", appcode);
    }

    @Test
    void shouldFallbackToGlobalWhenUserAccountDisabled() {
        // The mapper's actual query filters enabled=true; here we simulate that
        // by having selectOne return null when the user has only a disabled record.
        when(apiAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        String appcode = service.resolveAppcode(1L, "ALIYUN_EXPRESS");

        assertNull(appcode);
    }

    @Test
    void shouldFallbackToGlobalWhenUserHasNoAccount() {
        when(apiAccountMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        String appcode = service.resolveAppcode(1L, "ALIYUN_EXPRESS");

        assertNull(appcode);
    }

    @Test
    void shouldFallbackToGlobalWhenUserIdIsNull() {
        String appcode = service.resolveAppcode(null, "ALIYUN_EXPRESS");

        assertNull(appcode);
        verify(apiAccountMapper, never()).selectOne(any(LambdaQueryWrapper.class));
    }
}
