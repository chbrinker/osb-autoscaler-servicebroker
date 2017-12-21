package de.evoila.cf.cpi.custom.props;

import de.evoila.cf.broker.exception.PlatformException;
import de.evoila.cf.broker.model.Plan;
import de.evoila.cf.broker.model.Platform;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.repository.PlatformRepository;
import de.evoila.cf.broker.service.PlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.ws.rs.NotSupportedException;
import java.util.Map;

/**
 * Created by reneschollmeyer, evoila on 22.11.17.
 */
@Service
@EnableConfigurationProperties
public class AutoscalerPlatformService implements PlatformService {

    private final Logger log = LoggerFactory.getLogger(AutoscalerPlatformService.class);

    @Autowired(required = false)
    private PlatformRepository platformRepository;

    @Override
    @PostConstruct
    public void registerCustomPlatformService() {
        if(platformRepository != null) {
            platformRepository.addPlatform(Platform.EXISTING_SERVICE, this);
        }
    }

    @Override
    public boolean isSyncPossibleOnCreate(Plan plan) {
        return true;
    }

    @Override
    public boolean isSyncPossibleOnDelete(ServiceInstance instance) {
        return true;
    }

    @Override
    public boolean isSyncPossibleOnUpdate(ServiceInstance instance, Plan plan) {
        return true;
    }

    @Override
    public ServiceInstance postProvisioning(ServiceInstance serviceInstance, Plan plan) throws PlatformException {
        return serviceInstance;
    }

    @Override
    public void preDeprovisionServiceInstance(ServiceInstance serviceInstance) {

    }

    @Override
    public ServiceInstance createInstance(ServiceInstance serviceInstance, Plan plan, Map<String, String> customParameters) throws PlatformException {

        serviceInstance = new ServiceInstance(serviceInstance, serviceInstance.getDashboardUrl(), serviceInstance.getId());

        return serviceInstance;
    }

    @Override
    public ServiceInstance getCreateInstancePromise(ServiceInstance instance, Plan plan) {
        return new ServiceInstance(instance, null, null);
    }

    @Override
    public void deleteServiceInstance(ServiceInstance serviceInstance) throws PlatformException {

    }

    @Override
    public ServiceInstance updateInstance(ServiceInstance instance, Plan plan) throws NotSupportedException {
        throw new NotSupportedException("Updating Service Instances is currently not supported");
    }
}
