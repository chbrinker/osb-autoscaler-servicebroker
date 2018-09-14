package de.evoila.cf.broker.custom.autoscaler;

import de.evoila.cf.broker.exception.PlatformException;
import de.evoila.cf.broker.model.Plan;
import de.evoila.cf.broker.model.Platform;
import de.evoila.cf.broker.model.ServiceInstance;
import de.evoila.cf.broker.repository.PlatformRepository;
import de.evoila.cf.broker.service.PlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by reneschollmeyer, evoila on 22.11.17.
 */
@Service
@EnableConfigurationProperties
public class AutoscalerPlatformService implements PlatformService {

    private final Logger log = LoggerFactory.getLogger(AutoscalerPlatformService.class);

    private PlatformRepository platformRepository;

    public AutoscalerPlatformService(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    @Override
    @PostConstruct
    public void registerCustomPlatformService() {
        if(platformRepository != null) {
            platformRepository.addPlatform(Platform.EXISTING_SERVICE, this);
        }
    }

    @Override
    public boolean isSyncPossibleOnCreate(Plan plan) {
        return false;
    }

    @Override
    public boolean isSyncPossibleOnDelete(ServiceInstance instance) {
        return false;
    }

    @Override
    public boolean isSyncPossibleOnUpdate(ServiceInstance instance, Plan plan) {
        return false;
    }


    @Override
    public ServiceInstance preCreateInstance(ServiceInstance serviceInstance, Plan plan) {
        return serviceInstance;
    }

    @Override
    public ServiceInstance createInstance(ServiceInstance serviceInstance,Plan plan,Map<String, Object> customParameters) throws PlatformException {
        serviceInstance = new ServiceInstance(serviceInstance, serviceInstance.getDashboardUrl(), serviceInstance.getId());

        return serviceInstance;

    }

    @Override
    public ServiceInstance getCreateInstancePromise(ServiceInstance serviceInstance, Plan plan) {
        return serviceInstance;
    }

    @Override
    public ServiceInstance postCreateInstance(ServiceInstance serviceInstance, Plan plan) {
        return serviceInstance;
    }

    @Override
    public void preDeleteInstance(ServiceInstance serviceInstance) { }

    @Override
    public void deleteInstance(ServiceInstance serviceInstance, Plan plan) { }

    @Override
    public void postDeleteInstance(ServiceInstance serviceInstance) { }

    @Override
    public ServiceInstance updateInstance(ServiceInstance serviceInstance,Plan plan,Map<String, Object> customParameters) throws PlatformException {
        return serviceInstance;
    }

	@Override
	public ServiceInstance preUpdateInstance(ServiceInstance serviceInstance, Plan plan) throws PlatformException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServiceInstance postUpdateInstance(ServiceInstance serviceInstance, Plan plan) throws PlatformException {
		throw new UnsupportedOperationException();
	}

}
