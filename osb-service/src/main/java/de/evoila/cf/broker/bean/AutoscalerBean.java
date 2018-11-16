package de.evoila.cf.broker.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * Created by reneschollmeyer, evoila on 29.11.17.
 */
@Service
@ConfigurationProperties(prefix = "autoscaler")
public class AutoscalerBean {

    private String platform;

    private String scalerId;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getScalerId() {
        return scalerId;
    }

    public void setScalerId(String scalerId) {
        this.scalerId = scalerId;
    }
}
