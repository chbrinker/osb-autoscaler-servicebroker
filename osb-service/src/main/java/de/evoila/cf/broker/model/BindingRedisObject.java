package de.evoila.cf.broker.model;

public class BindingRedisObject {

	private String applicationName;
	private String space;
	private String organization;
	private boolean subscribed;

	public BindingRedisObject(String applicationName, String space, String organization, boolean subscribed) {
		this.applicationName = applicationName;
		this.space = space;
		this.organization = organization;
		this.subscribed = subscribed;
	}
	
    public BindingRedisObject(BindingRedisEnvironment environment, boolean subscribed) {
        this.applicationName = environment.getApplicationName();
        this.space = environment.getSpace();
        this.organization = environment.getOrganization();
        this.subscribed = subscribed;
    }

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
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

	public boolean isSubscribed() {
		return subscribed;
	}

	public void setSubscribed(boolean subscribed) {
		this.subscribed = subscribed;
	}

}
