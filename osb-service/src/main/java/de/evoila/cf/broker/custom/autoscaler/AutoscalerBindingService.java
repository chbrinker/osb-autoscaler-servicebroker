package de.evoila.cf.broker.custom.autoscaler;

import de.evoila.cf.autoscaler.api.binding.Binding;
import de.evoila.cf.autoscaler.api.binding.BindingContext;
import de.evoila.cf.broker.bean.AutoscalerBean;
import de.evoila.cf.broker.bean.RedisBean;
import de.evoila.cf.broker.connection.CFClientConnector;
import de.evoila.cf.broker.exception.ServiceBrokerException;
import de.evoila.cf.broker.exception.ServiceBrokerFeatureIsNotSupportedException;
import de.evoila.cf.broker.exception.ServiceInstanceBindingBadRequestException;
import de.evoila.cf.broker.exception.ServiceInstanceBindingException;
import de.evoila.cf.broker.exception.ServiceInstanceBindingExistsException;
import de.evoila.cf.broker.exception.ServiceInstanceDoesNotExistException;
import de.evoila.cf.broker.exception.ServiceInstanceExistsException;
import de.evoila.cf.broker.model.*;
import de.evoila.cf.broker.service.impl.BindingServiceImpl;
import groovy.json.JsonBuilder;
import redis.clients.jedis.Jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
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
    
    @Autowired
    private CFClientConnector cfClient;
    
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
                                                 ServiceInstance serviceInstance, Plan plan) throws ServiceInstanceBindingBadRequestException, ServiceBrokerException {

        ResponseEntity<String> response = post(bindingId, serviceInstanceBindingRequest.getAppGuid(), serviceInstance);
        Jedis jedis = createJedisConnection();

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error(new ServiceInstanceBindingException(serviceInstance.getId(), bindingId, response.getStatusCode(),
                    response.getBody()).getMessage());
            
        	if (response.getStatusCode() == HttpStatus.BAD_REQUEST)  {
        		throw new ServiceInstanceBindingBadRequestException(bindingId, response.getBody());
        	}
        	if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        		throw new ServiceBrokerException("The Broker is not authorized at the service instance. (401 Response)");
        	}
        	if (response.getStatusCode() == HttpStatus.CONFLICT) {
        		throw new ServiceBrokerException("The service instance already holds a binding in conflict with the requested one. Maybe the broker and the service instance are out of sync.");
        	}
        	throw new ServiceBrokerException("The Broker faced an unexpected error while calling the ServiceInstance to create a binding: " + response.getStatusCodeValue() + " - " + response.getBody());	
        } else {
            log.info("Binding resulted in " + response.getStatusCode() + ", serviceInstance = "
                    + serviceInstance.getId() + ", bindingId = " + bindingId);
            
            if (jedis.get(serviceInstanceBindingRequest.getAppGuid()) != null) {
                BindingRedisObject redisObject = new BindingRedisObject(cfClient.getServiceEnvironment(serviceInstanceBindingRequest.getAppGuid()), true);
                String redisString = new JsonBuilder(redisObject).toString();
                jedis.set(serviceInstanceBindingRequest.getAppGuid(), redisString);

                log.info("Successfully updated the subscription status for app = " + serviceInstanceBindingRequest.getAppGuid()
                		+ ". Application is now registered.");
            } else {
            	/*
            	 * This might be the case if no instance of the nozzle is running for the dedicated redis instance, because the Nozzle creates 
            	 * entries for applications which it receives a log or a metric from Cloud Foundry.
            	 * 
            	 * An other possibility is that the app guid is invalid or there is no application with that guid generating metrics or logs for the Nozzle.
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
    protected ServiceInstanceBinding bindServiceKey(String bindingId, ServiceInstanceBindingRequest serviceInstanceBindingRequest,
            ServiceInstance serviceInstance, Plan plan, List<ServerAddress> externalAddresses) throws ServiceBrokerException, ServiceBrokerFeatureIsNotSupportedException {
    	throw new ServiceBrokerFeatureIsNotSupportedException(bindingId, serviceInstance.getId(), "This Broker does not support service key generation. Therefore app_guid must be present.");
    }

    @Override
    protected void deleteBinding(ServiceInstanceBinding binding, ServiceInstance serviceInstance, Plan plan) throws ServiceBrokerException {
        String bindingId = binding.getId();
        ResponseEntity<String> response = delete(bindingId, serviceInstance.getId());
        
        Jedis jedis = createJedisConnection();

        if(!response.getStatusCode().is2xxSuccessful()) {
            log.error(new ServiceInstanceBindingException(serviceInstance.getId(), bindingId, response.getStatusCode(),
                    response.getBody()).getMessage());
            
        	if (response.getStatusCode() == HttpStatus.GONE)  {
        		throw new ServiceBrokerException("ServiceInstance can not find the binding. Maybe the service broker and the ServiceInstance are out of sync. (410 Response)");
        	}
        	if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        		throw new ServiceBrokerException("The Broker is not authorized at the service instance. (401 Response)");
        	}
        	throw new ServiceBrokerException("The Broker faced an unexpected error while calling the ServiceInstance to delete a binding: " + response.getStatusCodeValue() + " - " + response.getBody());
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
        jedis.close();
    }

    private ResponseEntity<String> post(String bindingId, String appGuid, ServiceInstance serviceInstance) throws ServiceInstanceBindingBadRequestException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("secret", autoscalerBean.getSecret());

        BindingContext context = new BindingContext(autoscalerBean.getPlatform(), serviceInstance.getSpaceGuid(), serviceInstance.getOrganizationGuid());

        Binding binding = new Binding(bindingId, appGuid, "unknown", autoscalerBean.getScalerId(),
                serviceInstance.getId(), System.currentTimeMillis(), context);

        HttpEntity<Binding> request = new HttpEntity<>(binding, headers);

        String url = autoscalerBean.getScheme() + "://" + autoscalerBean.getUrl() + BINDING_ENDPOINT;

        ResponseEntity<String> response = new ResponseEntity<>("Could not get a valid response from the autoscaler core.",HttpStatus.INTERNAL_SERVER_ERROR);
        try {
        	response = restTemplate.postForEntity(url, request, String.class);
        } catch (HttpClientErrorException ex) {
        	log.error("Request to the autoscaler core raised an " + ex.getRawStatusCode() + " error.");
        	response = new ResponseEntity<>(ex.getResponseBodyAsString(), HttpStatus.valueOf(ex.getRawStatusCode()));
        }
        return response;
    }

    private ResponseEntity<String> delete(String bindingId, String instanceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("secret", autoscalerBean.getSecret());

        HttpEntity request = new HttpEntity(headers);

        String url = autoscalerBean.getScheme() + "://" + autoscalerBean.getUrl() + BINDING_ENDPOINT + "/" + bindingId;

        ResponseEntity<String> response = new ResponseEntity<>("Could not get a valid response from the autoscaler core.",HttpStatus.INTERNAL_SERVER_ERROR);
        try {
        	response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        } catch (HttpClientErrorException ex) {
        	log.error("Delete Binding Request to the autoscaler core raised an " + ex.getRawStatusCode() + " error.");
        	response = new ResponseEntity<>(ex.getResponseBodyAsString(), HttpStatus.valueOf(ex.getRawStatusCode()));
        }
        return response;
    }

}
