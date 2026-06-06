package com.homecentral.parcel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecentral.parcel.api.dto.ApiAccountRequest;
import com.homecentral.parcel.api.vo.ApiAccountVO;
import com.homecentral.parcel.entity.ApiAccount;
import com.homecentral.parcel.mapper.ApiAccountMapper;
import com.homecentral.parcel.service.IApiAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ApiAccountServiceImpl implements IApiAccountService {

    private static final Logger log = LoggerFactory.getLogger(ApiAccountServiceImpl.class);

    private final ApiAccountMapper apiAccountMapper;

    private final String globalAliyunAppcode;

    public ApiAccountServiceImpl(ApiAccountMapper apiAccountMapper,
                                 @Value("${app.aliyun.express.appcode:}") String globalAliyunAppcode) {
        this.apiAccountMapper = apiAccountMapper;
        this.globalAliyunAppcode = globalAliyunAppcode;
    }

    @Override
    public ApiAccountVO get(Long userId, String provider) {
        ApiAccount entity = apiAccountMapper.selectOne(
                new LambdaQueryWrapper<ApiAccount>()
                        .eq(ApiAccount::getUserId, userId)
                        .eq(ApiAccount::getProvider, provider));
        return entity == null ? null : toVO(entity);
    }

    @Override
    @Transactional
    public ApiAccountVO save(Long userId, String provider, ApiAccountRequest request) {
        ApiAccount entity = apiAccountMapper.selectOne(
                new LambdaQueryWrapper<ApiAccount>()
                        .eq(ApiAccount::getUserId, userId)
                        .eq(ApiAccount::getProvider, provider));
        boolean isNew = entity == null;
        if (isNew) {
            entity = new ApiAccount();
            entity.setUserId(userId);
            entity.setProvider(provider);
            entity.setCreatedAt(OffsetDateTime.now());
        }
        if (request.getApiKey() != null) {
            String trimmed = request.getApiKey().trim();
            if (trimmed.isEmpty()) {
                throw new RuntimeException("AppCode 不能为空");
            }
            entity.setApiKey(trimmed);
        }
        if (request.getCustomer() != null) {
            entity.setCustomer(request.getCustomer().trim().isEmpty() ? null : request.getCustomer().trim());
        }
        if (request.getEnabled() != null) {
            entity.setEnabled(request.getEnabled());
        } else if (isNew) {
            entity.setEnabled(true);
        }
        entity.setUpdatedAt(OffsetDateTime.now());
        if (isNew) {
            apiAccountMapper.insert(entity);
        } else {
            apiAccountMapper.updateById(entity);
        }
        log.info("ApiAccount saved: user={} provider={} enabled={}", userId, provider, entity.getEnabled());
        return toVO(entity);
    }

    @Override
    @Transactional
    public void delete(Long userId, String provider) {
        apiAccountMapper.delete(
                new LambdaQueryWrapper<ApiAccount>()
                        .eq(ApiAccount::getUserId, userId)
                        .eq(ApiAccount::getProvider, provider));
    }

    @Override
    public String resolveAppcode(Long userId, String provider) {
        if (userId != null) {
            try {
                ApiAccount entity = apiAccountMapper.selectOne(
                        new LambdaQueryWrapper<ApiAccount>()
                                .eq(ApiAccount::getUserId, userId)
                                .eq(ApiAccount::getProvider, provider)
                                .eq(ApiAccount::getEnabled, true));
                if (entity != null && entity.getApiKey() != null && !entity.getApiKey().isBlank()) {
                    return entity.getApiKey();
                }
            } catch (Exception e) {
                log.warn("[api-account] resolveAppcode failed for user={} provider={}: {}",
                        userId, provider, e.getMessage());
            }
        }
        if (globalAliyunAppcode == null || globalAliyunAppcode.isBlank()) {
            return null;
        }
        return globalAliyunAppcode;
    }

    private ApiAccountVO toVO(ApiAccount entity) {
        ApiAccountVO vo = new ApiAccountVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setProvider(entity.getProvider());
        vo.setApiKeyMasked(maskApiKey(entity.getApiKey()));
        vo.setCustomer(entity.getCustomer());
        vo.setEnabled(entity.getEnabled());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    static String maskApiKey(String key) {
        if (key == null || key.isEmpty()) return null;
        if (key.length() <= 4) return "****";
        return "****" + key.substring(key.length() - 4);
    }
}
