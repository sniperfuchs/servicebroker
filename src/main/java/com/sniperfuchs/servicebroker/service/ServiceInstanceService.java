package com.sniperfuchs.servicebroker.service;

import com.sniperfuchs.servicebroker.controller.ProvisionResponse;
import com.sniperfuchs.servicebroker.exception.ExistingServiceInstanceAttributeMismatchException;
import com.sniperfuchs.servicebroker.exception.InvalidIdentifierException;
import com.sniperfuchs.servicebroker.exception.ServiceInstanceNotFoundException;
import com.sniperfuchs.servicebroker.model.ServiceInstance;
import com.sniperfuchs.servicebroker.repository.ServiceInstanceRepository;
import com.sniperfuchs.servicebroker.util.IdentifierValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServiceInstanceService
{

    private ServiceInstanceRepository serviceInstanceRepository;

    public ServiceInstanceService(ServiceInstanceRepository serviceInstanceRepository)
    {
        this.serviceInstanceRepository = serviceInstanceRepository;
    }

    public ServiceInstance fetchInstanceById(String instance_id) throws ServiceInstanceNotFoundException
    {
        if(serviceInstanceRepository.findById(instance_id).isEmpty())
        {
            throw new ServiceInstanceNotFoundException("Service instance with id " + instance_id + " does not exist.");
        }
        return serviceInstanceRepository.findById(instance_id).get();
    }

    public ResponseEntity<ProvisionResponse> createInstance(String instance_id,
                                                            String service_id,
                                                            String plan_id,
                                                            String organization_guid,
                                                            String space_guid,
                                                            Object parameters)
    {
        if(!IdentifierValidator.validate(instance_id))
        {
            throw new InvalidIdentifierException("Identifier instance_id " + instance_id + " is null or contains reserved characters (RFC3986).");
        }

        if(!IdentifierValidator.validate(service_id))
        {
            throw new InvalidIdentifierException("Identifier service_id " + service_id + " is null or contains reserved characters (RFC3986).");
        }

        if(!IdentifierValidator.validate(plan_id))
        {
            throw new InvalidIdentifierException("Identifier plan_id " + plan_id + " is null or contains reserved characters (RFC3986).");
        }

        if(!IdentifierValidator.validate(organization_guid))
        {
            throw new InvalidIdentifierException("Identifier organization_guid " + organization_guid + " is null or contains reserved characters (RFC3986).");
        }

        if(!IdentifierValidator.validate(space_guid))
        {
            throw new InvalidIdentifierException("Identifier space_guid " + space_guid + " is null or contains reserved characters (RFC3986).");
        }




        ServiceInstance serviceInstance = ServiceInstance.builder()
                .id(instance_id)
                .service_id(service_id)
                .plan_id(plan_id)
                .organization_guid(organization_guid)
                .space_guid(space_guid)
                .parameters(parameters)
                .build();

        ProvisionResponse provisionResponse = new ProvisionResponse(null, null, null);

        Optional<ServiceInstance> optionalServiceInstance = serviceInstanceRepository.findById(service_id);

        if(optionalServiceInstance.isPresent())
        {
            ServiceInstance existingServiceInstance = optionalServiceInstance.get();
            if(existingServiceInstance.hasSameAttributes(serviceInstance))
            {
                return new ResponseEntity<>(provisionResponse, HttpStatus.OK);
            }
            else
            {
                throw new ExistingServiceInstanceAttributeMismatchException("A service instance with this instance_id but different attributes already exists.");
            }
        }



        //TODO Populate instance with parameters
        //TODO Deploy service on kubernetes cluster with HELM, maybe in different service


        serviceInstanceRepository.save(serviceInstance);



        return new ResponseEntity<>(provisionResponse, HttpStatus.CREATED);
    }

    public ServiceInstance updateInstanceById(String instance_id)
    {
        if(serviceInstanceRepository.findById(instance_id).isEmpty())
        {
            throw new ServiceInstanceNotFoundException("Service instance with id " + instance_id + " does not exist.");
        }
        return serviceInstanceRepository.findById(instance_id).get();
    }

    public boolean instanceAlreadyExistsWithSameAttributes(String service_id,
                                         String plan_id,
                                         Object context,
                                         String organization_guid,
                                         String space_guid,
                                         Object parameters)
    {

        Optional<ServiceInstance> optionalServiceInstance = serviceInstanceRepository.findById(service_id);

        if(optionalServiceInstance.isPresent())
        {
            return optionalServiceInstance.get().hasSameAttributes(ServiceInstance.builder()
                    .service_id(service_id)
                    .plan_id(plan_id)
                    .organization_guid(organization_guid)
                    .space_guid(space_guid)
                    //.parameters(parameters) TODO:: Include parameters and context
                    .build());

        }
        return false;
    }
}
