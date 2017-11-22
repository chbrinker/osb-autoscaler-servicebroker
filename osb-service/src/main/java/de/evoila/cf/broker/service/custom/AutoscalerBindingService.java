package de.evoila.cf.broker.service.custom;

import de.evoila.cf.broker.exception.*;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.service.impl.BindingServiceImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by reneschollmeyer, evoila on 22.11.17.
 */
@Service
public class AutoscalerBindingService extends BindingServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(AutoscalerBindingService.class);

    private RestTemplate restTemplate;

    @PostConstruct
    private void initialize() {
        restTemplate = new RestTemplate();
    }

    @Override
    public ServiceInstanceBindingResponse createServiceInstanceBinding(String bindingId, String instanceId, String serviceId,
                                                                       String planId, boolean generateServiceKey, String route)
            throws ServiceInstanceBindingExistsException, ServiceBrokerException,
            ServiceInstanceDoesNotExistException, ServiceDefinitionDoesNotExistException, ServiceInstanceBindingException {

        validateBindingNotExists(bindingId, instanceId);

        ServiceInstance serviceInstance = serviceInstanceRepository.getServiceInstance(instanceId);

        if(serviceInstance == null) {
            throw new ServiceInstanceDoesNotExistException(instanceId);
        }

        Plan plan = serviceDefinitionRepository.getPlan(planId);

        ResponseEntity<String> response = post(bindingId, instanceId);

        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new ServiceInstanceBindingException(instanceId, bindingId, response.getStatusCode(), response.getBody());
        } else {
            log.info("Binding resulted in " + response.getStatusCode() + ", serviceInstance = " + instanceId
            + ", bindingId = " + bindingId);
        }

        ServiceInstanceBinding binding = bindService(bindingId, serviceInstance, plan);

        ServiceInstanceBindingResponse bindingResponse = new ServiceInstanceBindingResponse(binding);
        bindingRepository.addInternalBinding(binding);

        return bindingResponse;
    }

    @Override
    public void deleteServiceInstanceBinding(String bindingId)
            throws ServiceBrokerException, ServerviceInstanceBindingDoesNotExistsException, ServiceInstanceBindingException {
        ServiceInstance serviceInstance = getBinding(bindingId);

        String instanceId = serviceInstance.getId();

        ResponseEntity<String> response = delete(bindingId, serviceInstance.getId());

        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new ServiceInstanceBindingException(instanceId, bindingId, response.getStatusCode(), response.getBody());
        } else {
            log.info("Unbinding resulted in " + response.getStatusCode() + ", serviceInstance = " + instanceId
                    + ", bindingId = " + bindingId);
        }

        deleteBinding(bindingId, serviceInstance);
    }

    @Override
    protected void deleteBinding(String bindingId, ServiceInstance serviceInstance) throws ServiceBrokerException {
        bindingRepository.deleteBinding(bindingId);
    }

    @Override
    protected RouteBinding bindRoute(ServiceInstance serviceInstance, String route) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Map<String, Object> createCredentials(String bindingId, ServiceInstance serviceInstance, ServerAddress host) throws ServiceBrokerException {
        log.info("Binding the Autoscaler Service...");

        String dbURL = String.format("autoscaler://%s:%s@%s:%d/%s", this.nextSessionId(),
                this.nextSessionId(), host.getIp(), host.getPort(),
                serviceInstance.getId());

        Map<String, Object> credentials = new HashMap<String, Object>();
        credentials.put("uri", dbURL);

        return credentials;
    }

    @Override
    public ServiceInstanceBinding getServiceInstanceBinding(String id) {
        return null;
    }

    private ResponseEntity<String> post(String bindingId, String instanceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("secret", "jbkneo38858fjvone92");

        Map<String, String> context = new HashMap<>();
        context.put("platform", "cloudfoundry");
        context.put("organization_guid", "evoila");
        context.put("space_guid", "default");

        JSONObject json = new JSONObject();
        try {
            json.put("platform", "cloudfoundry");
            json.put("organization_guid", "evoila");
            json.put("space_guid", "default");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("id", "testAppBinding");
        body.add("resourceId", instanceId);
        body.add("scalerId", "0");
        body.add("context", json.toString());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        String url = "http://autoscaler.cf.eu-de-netde.msh.host/bindings";

        return restTemplate.postForEntity(url, request, String.class);
    }

    private ResponseEntity<String> delete(String bindingId, String instanceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("secret", "jbkneo38858fjvone92");

        HttpEntity request = new HttpEntity(headers);

        String url = "http://autoscaler.cf.eu-de-netde.msh.host/bindings/" + bindingId;

        return restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
    }

    public String nextSessionId() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }
}
