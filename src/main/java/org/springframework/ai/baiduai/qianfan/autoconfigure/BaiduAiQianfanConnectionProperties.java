package org.springframework.ai.baiduai.qianfan.autoconfigure;

import com.baidubce.qianfan.core.auth.Auth;
import com.baidubce.qianfan.model.RateLimitConfig;
import com.baidubce.qianfan.model.RetryConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(BaiduAiQianfanConnectionProperties.CONFIG_PREFIX)
public class BaiduAiQianfanConnectionProperties {

    public static final String CONFIG_PREFIX = "spring.ai.baiduai.qianfan";

    private AuthType type = AuthType.IAM;
    private String accessKey;
    private String secretKey;

    @NestedConfigurationProperty
    private RetryConfig retry = new RetryConfig();

    @NestedConfigurationProperty
    private RateLimitConfig rateLimit = new RateLimitConfig();

    public AuthType getType() {
        return type;
    }

    public void setType(AuthType type) {
        this.type = type;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public RetryConfig getRetry() {
        return retry;
    }

    public void setRetry(RetryConfig retry) {
        this.retry = retry;
    }

    public RateLimitConfig getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimitConfig rateLimit) {
        this.rateLimit = rateLimit;
    }

    public enum AuthType {
        IAM(Auth.TYPE_IAM),
        OAUTH(Auth.TYPE_OAUTH);

        private String value;
        AuthType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
