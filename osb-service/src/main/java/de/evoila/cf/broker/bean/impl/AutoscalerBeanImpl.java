package de.evoila.cf.broker.bean.impl;

import de.evoila.cf.broker.bean.AutoscalerBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * Created by reneschollmeyer, evoila on 29.11.17.
 */
@Service
@ConfigurationProperties(prefix = "autoscaler")
public class AutoscalerBeanImpl implements AutoscalerBean {

    private String secret;
    private String platform;
    private String scalerId;
    private String url;
    private String scheme;

    public String getSecret() { return secret; }

    public String getPlatform() { return platform; }

    public String getScalerId() { return scalerId; }

    public String getUrl() { return url; }

    public void setSecret(String secret) { this.secret = secret; }

    public void setPlatform(String platform) { this.platform = platform; }

    public void setScalerId(String scalerId) { this.scalerId = scalerId; }

    public void setUrl(String url) { this.url = url; }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
}
