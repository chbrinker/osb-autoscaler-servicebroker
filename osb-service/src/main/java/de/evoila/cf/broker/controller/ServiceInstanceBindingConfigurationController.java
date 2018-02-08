package de.evoila.cf.broker.controller;

import de.evoila.cf.broker.bean.AutoscalerBean;
import de.evoila.cf.broker.exception.ServiceInstanceBindingException;
import de.evoila.cf.broker.model.BindingConfigurationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * Created by reneschollmeyer, evoila on 15.01.18.
 */
@RestController
@RequestMapping(value = "")
public class ServiceInstanceBindingConfigurationController {
    private final Logger log = LoggerFactory.getLogger(ServiceInstanceBindingConfigurationController.class);

    private static final String CONFIGURATION_BASE_PATH = "/bindings";
    private static final String SPECIFIC_INSTANCE_PATH = "/serviceInstance";

    private RestTemplate restTemplate;

    private HttpHeaders headers;

    @Autowired
    private AutoscalerBean autoscalerBean;

    @PostConstruct
    private void initialize() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate = new RestTemplate(requestFactory);

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("secret", autoscalerBean.getSecret());
    }

    @GetMapping(value = "/bindings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> loadAll() throws ServiceInstanceBindingException {
        log.debug("GET: " + CONFIGURATION_BASE_PATH + ", loadAll()");

        HttpEntity<String> request = new HttpEntity<>("", headers);

        String url = "https://" + autoscalerBean.getUrl() + CONFIGURATION_BASE_PATH;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new ServiceInstanceBindingException(null, response.getStatusCode(), response.getBody());
        }

        return response;
    }

    @GetMapping(value = "/bindings/serviceInstance/{serviceInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> loadAllByServiceInstance(@PathVariable("serviceInstanceId") String serviceInstanceId) throws ServiceInstanceBindingException {
        log.debug("GET: " + CONFIGURATION_BASE_PATH + "/serviceInstance/" + serviceInstanceId
                + ", loadAllByServiceInstance()");

        HttpEntity<String> request = new HttpEntity<>("", headers);

        String url = "https://" + autoscalerBean.getUrl() + CONFIGURATION_BASE_PATH + SPECIFIC_INSTANCE_PATH
                + "/" + serviceInstanceId;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new ServiceInstanceBindingException(null, response.getStatusCode(), response.getBody());
        }

        return response;
    }


    @GetMapping(value = "/configuration/{bindingId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> loadOne(@PathVariable("bindingId") String bindingId) throws ServiceInstanceBindingException {

        log.debug("GET: " + CONFIGURATION_BASE_PATH + "/{bindingId}, " +
                "loadOne(), bindingId = " + bindingId);

        HttpEntity<String> request = new HttpEntity("", headers);

        String url = "http://" + autoscalerBean.getUrl() + CONFIGURATION_BASE_PATH + "/" + bindingId;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new ServiceInstanceBindingException(bindingId, response.getStatusCode(), response.getBody());
        }

        return response;
    }

    @PatchMapping(value = "/configuration/{bindingId}")
    public ResponseEntity<String> saveOne(@PathVariable("bindingId") String bindingId,
                                          @RequestBody BindingConfigurationData data) throws ServiceInstanceBindingException {
        log.debug("PATCH: " + CONFIGURATION_BASE_PATH + "/{bindingId}, " +
                "saveOne(), bindingId = " + bindingId);

        HttpEntity<BindingConfigurationData> request = new HttpEntity<>(data, headers);

        String url = "http://" + autoscalerBean.getUrl() + CONFIGURATION_BASE_PATH + "/" + bindingId;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, request, String.class);

        if(!response.getStatusCode().is2xxSuccessful()) {
            throw new ServiceInstanceBindingException(bindingId, response.getStatusCode(), response.getBody());
        }

        return response;
    }

}
