package de.evoila.cf.broker.bean;

public interface RedisBean {

    public String getHost();
    public void setHost(String host);
    public int getPort();
    public void setPort(int port);
    public String getPassword();
    public void setPassword(String password);
}
