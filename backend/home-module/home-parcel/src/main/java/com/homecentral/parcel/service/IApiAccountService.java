package com.homecentral.parcel.service;

import com.homecentral.parcel.api.dto.ApiAccountRequest;
import com.homecentral.parcel.api.vo.ApiAccountVO;

public interface IApiAccountService {

    ApiAccountVO get(Long userId, String provider);

    ApiAccountVO save(Long userId, String provider, ApiAccountRequest request);

    void delete(Long userId, String provider);

    String resolveAppcode(Long userId, String provider);
}
