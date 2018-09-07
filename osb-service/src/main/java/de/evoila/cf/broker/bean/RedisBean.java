package de.evoila.cf.broker.bean;

import java.util.List;

public interface RedisBean {

    public List<String> getHosts();
    public void setHosts(List<String> hosts);
    public int getPort();
    public void setPort(int port);
    public String getPassword();
    public void setPassword(String password);
}
