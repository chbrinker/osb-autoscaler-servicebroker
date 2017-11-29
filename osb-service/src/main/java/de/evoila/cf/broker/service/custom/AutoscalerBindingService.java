package de.evoila.cf.broker.service.custom;

import de.cf.autoscaler.api.binding.Binding;
import de.cf.autoscaler.api.binding.BindingContext;
import de.evoila.cf.broker.bean.AutoscalerBean;
import de.evoila.cf.broker.exception.*;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.service.impl.BindingServiceImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String BINDING_ENDPOINT = "/bindings";

    private RestTemplate restTemplate;

    @Autowired
    private AutoscalerBean autoscalerBean;

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

        ResponseEntity<String> response = post(bindingId, serviceInstance);

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

    @Override
    protected ServiceInstanceBinding bindService(String bindingId, ServiceInstance serviceInstance, Plan plan)
            throws ServiceBrokerException {

        log.debug("bind service");

        return new ServiceInstanceBinding(bindingId, serviceInstance.getId(), null, null);
    }

    private ResponseEntity<String> post(String bindingId, ServiceInstance serviceInstance) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("secret", autoscalerBean.getSecret());

        BindingContext context = new BindingContext(autoscalerBean.getPlatform(), serviceInstance.getSpaceGuid(), serviceInstance.getOrganizationGuid());

        Binding binding = new Binding(bindingId, serviceInstance.getId(), autoscalerBean.getScalerId(), System.currentTimeMillis(), context);

        HttpEntity<Binding> request = new HttpEntity<>(binding, headers);

        String url = "http://" + autoscalerBean.getUrl() + BINDING_ENDPOINT;

        return restTemplate.postForEntity(url, request, String.class);
    }

    private ResponseEntity<String> delete(String bindingId, String instanceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("secret", autoscalerBean.getSecret());

        HttpEntity request = new HttpEntity(headers);

        String url = "http://" + autoscalerBean.getUrl() + BINDING_ENDPOINT + "/" + bindingId;

        return restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
    }

    public String nextSessionId() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }
}
