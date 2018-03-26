package de.evoila.cf.broker.bean;

/**
 * Created by reneschollmeyer, evoila on 29.11.17.
 */
public interface AutoscalerBean {

    String getSecret();
    String getPlatform();
    String getScalerId();
    String getUrl();
    String getScheme();
}
