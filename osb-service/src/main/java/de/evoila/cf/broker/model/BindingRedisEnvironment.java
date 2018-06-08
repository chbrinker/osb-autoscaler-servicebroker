package de.evoila.cf.broker.model;

public class BindingRedisEnvironment {

    private String applicationName;
    private String applicationId;
    private String space;
    private String organization;

    public BindingRedisEnvironment(String applicationName, String applicationId, String space, String organization) {
        this.applicationName = applicationName;
        this.applicationId = applicationId;
        this.space = space;
        this.organization = organization;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
