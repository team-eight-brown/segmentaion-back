package com.vk.itmo.segmentation.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FilterDistributeType {
    EMAIL_REGEXP("EmailRegexp"),
    IP_REGEXP("IpRegexp"),
    LOGIN_REGEXP("LoginRegexp");
    private final String code;

    @JsonCreator
    public static FilterDistributeType fromCode(String code) {
        for (FilterDistributeType type : FilterDistributeType.values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}
