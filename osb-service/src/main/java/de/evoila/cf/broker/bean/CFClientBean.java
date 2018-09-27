package de.evoila.cf.broker.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import de.evoila.cf.broker.bean.CFClientBean;

@Service
@ConfigurationProperties(prefix = "cf")
public class CFClientBean  {

    private String apiHost;
    private String username;
    private String password;
    private boolean skipSsl;

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getSkipSsl(){
        return this.skipSsl;
    }

    public void setSkipSsl(boolean skipSsl){
        this.skipSsl=skipSsl;
    }
}
