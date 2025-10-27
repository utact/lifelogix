package com.lifelogix.user.api.dto.request;

import lombok.Data;

@Data
public class OAuthTokenRequest {
    private String code;
}
