package com.debox.reward.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app.deposit")
public class DepositAddressesProperties {

    /**
     * 展示给用户的链上收款地址（key 如 ERC20_USDT，value 为地址）
     */
    private Map<String, String> addresses = new LinkedHashMap<>();
}
