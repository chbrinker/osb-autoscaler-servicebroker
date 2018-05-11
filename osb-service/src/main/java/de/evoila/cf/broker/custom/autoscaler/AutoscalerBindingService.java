package de.evoila.cf.broker.custom.autoscaler;

import de.evoila.cf.autoscaler.api.binding.Binding;
import de.evoila.cf.autoscaler.api.binding.BindingContext;
import de.evoila.cf.broker.bean.AutoscalerBean;
import de.evoila.cf.broker.bean.RedisBean;
import de.evoila.cf.broker.exception.ServiceInstanceBindingException;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.service.impl.BindingServiceImpl;
import redis.clients.jedis.Jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by reneschollmeyer, evoila on 22.11.17.
 */
@Service
public class AutoscalerBindingService extends BindingServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(AutoscalerBindingService.class);

    private static final String BINDING_ENDPOINT = "/bindings";
    
    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private AutoscalerBean autoscalerBean;
    
    @Autowired
    private RedisBean redisBean;
    
    private Jedis createJedisConnection() {
    	Jedis jedis = new Jedis(redisBean.getHost(), redisBean.getPort());
        jedis.connect();
        jedis.auth(redisBean.getPassword());
        
        return jedis;
    }

    @Override
    protected RouteBinding bindRoute(ServiceInstance serviceInstance, String route) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServiceInstanceBinding getServiceInstanceBinding(String id) {
        return null;
    }

    @Override
    protected ServiceInstanceBinding bindService(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest,
                                                 ServiceInstance serviceInstance, Plan plan) {

        ResponseEntity<String> response = post(bindingId, serviceInstanceBindingRequest.getAppGuid(), serviceInstance);
        Jedis jedis = createJedisConnection();

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error(new ServiceInstanceBindingException(serviceInstance.getId(), bindingId, response.getStatusCode(),
                    response.getBody()).getMessage());
        } else {
            log.info("Binding resulted in " + response.getStatusCode() + ", serviceInstance = "
                    + serviceInstance.getId() + ", bindingId = " + bindingId);
            
            if (jedis.get(serviceInstanceBindingRequest.getAppGuid()) != null) {
                jedis.set(serviceInstanceBindingRequest.getAppGuid(), "true");

                log.info("Successfully updated the subscription status for app = " + serviceInstanceBindingRequest.getAppGuid()
                		+ ". Application is now registered.");
            } else {
            	/*
            	 * This might be the case if no instance of the nozzle is running for the dedicated redis instance, because the Nozzle creates 
            	 * entries for applications which it receives a log or a metric from Cloud Foundry.
            	 */
                log.error("Error updating the subscription status for app = " + serviceInstanceBindingRequest.getAppGuid()
                        + ". Application is not registered.");
            }
        }
        
        jedis.close();

        ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding(bindingId, serviceInstance.getId(), null, null);
        serviceInstanceBinding.setAppGuid(serviceInstanceBindingRequest.getAppGuid());
        return serviceInstanceBinding;
    }

    @Override
    protected Map<String, Object> createCredentials(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest,
                                                    ServiceInstance serviceInstance, Plan plan, ServerAddress serverAddress) {
        Map<String, Object> credentials = new HashMap<>();

        return credentials;
    }

    @Override
    protected void deleteBinding(ServiceInstanceBinding binding, ServiceInstance serviceInstance, Plan plan) {
        String bindingId = binding.getId();
        ResponseEntity<String> response = delete(bindingId, serviceInstance.getId());
        Jedis jedis = createJedisConnection();

        if(!response.getStatusCode().is2xxSuccessful()) {
            log.error(new ServiceInstanceBindingException(serviceInstance.getId(), bindingId, response.getStatusCode(),
                    response.getBody()).getMessage());
        } else {
            log.info("Unbinding resulted in " + response.getStatusCode() + ", serviceInstance = "
                    + serviceInstance.getId() + ", bindingId = " + bindingId);
            
            if (jedis.get(binding.getAppGuid()) != null) {
            	// A running nozzle will automatically create a new unregistered entry in Redis for this app the next time a metric or a log is received
                jedis.del(binding.getAppGuid());

                log.info("Successfully updated the subscription status for app = " + serviceInstance.getId()
        		+ ". Application is now not registered.");
            } else {
                log.error("Error updating the subscription status for app = " + binding.getAppGuid()
                        + ". Application might still be registered.");
            }
        }
    }

    private ResponseEntity<String> post(String bindingId, String appGuid, ServiceInstance serviceInstance) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("secret", autoscalerBean.getSecret());

        BindingContext context = new BindingContext(autoscalerBean.getPlatform(), serviceInstance.getSpaceGuid(), serviceInstance.getOrganizationGuid());

        Binding binding = new Binding(bindingId, appGuid, "unknown", autoscalerBean.getScalerId(),
                serviceInstance.getId(), System.currentTimeMillis(), context);

        HttpEntity<Binding> request = new HttpEntity<>(binding, headers);

        String url = autoscalerBean.getScheme() + "://" + autoscalerBean.getUrl() + BINDING_ENDPOINT;

        return restTemplate.postForEntity(url, request, String.class);
    }

    private ResponseEntity<String> delete(String bindingId, String instanceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("secret", autoscalerBean.getSecret());

        HttpEntity request = new HttpEntity(headers);

        String url = autoscalerBean.getScheme() + "://" + autoscalerBean.getUrl() + BINDING_ENDPOINT + "/" + bindingId;

        return restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
    }

}
