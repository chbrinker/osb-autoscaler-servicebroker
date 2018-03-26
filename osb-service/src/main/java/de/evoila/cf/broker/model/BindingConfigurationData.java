package de.evoila.cf.broker.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by reneschollmeyer, evoila on 15.01.18.
 */
public class BindingConfigurationData {

    @JsonProperty("scaling")
    private Map<String, Object> scaling;

    @JsonProperty("cpu")
    private Map<String, Object> cpu;

    @JsonProperty("ram")
    private Map<String, Object> ram;

    @JsonProperty("latency")
    private Map<String, Object> latency;

    @JsonProperty("requests")
    private Map<String, Object> requests;

    @JsonProperty("learning")
    private Map<String, Object> learning;

    public Map<String, Object> getScaling() {
        return scaling;
    }

    public void setScaling(Map<String, Object> scaling) {
        this.scaling = scaling;
    }

    public Map<String, Object> getCpu() {
        return cpu;
    }

    public void setCpu(Map<String, Object> cpu) {
        this.cpu = cpu;
    }

    public Map<String, Object> getRam() {
        return ram;
    }

    public void setRam(Map<String, Object> ram) {
        this.ram = ram;
    }

    public Map<String, Object> getLatency() {
        return latency;
    }

    public void setLatency(Map<String, Object> latency) {
        this.latency = latency;
    }

    public Map<String, Object> getRequests() {
        return requests;
    }

    public void setRequests(Map<String, Object> requests) {
        this.requests = requests;
    }

    public Map<String, Object> getLearning() {
        return learning;
    }

    public void setLearning(Map<String, Object> learning) {
        this.learning = learning;
    }
}
