package de.evoila.cf.broker.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * Created by reneschollmeyer, evoila on 29.11.17.
 */
@Service
@ConfigurationProperties(prefix = "autoscaler")
public class AutoscalerBean {

    private String scheme;

    private int port;

    private String url;

    private String platform;

    private String scalerId;

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

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
