package com.sniperfuchs.servicebroker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class ServiceBinding {
    @Id
    private String id;
    private BindingMetadata metadata;
    private Map<String, Object> credentials;
    private String syslog_drain_url;
    private String route_service_url;
    //TODO volume mounts
    private Map<String, Object> parameters;
    //TODO endpoints

    private String app_guid;
    private BindResource bind_resource;

    public boolean hasSameAttributes(ServiceBinding other)
    {
        if(!id.equals(other.id))
        {
            return false;
        }

        if(parameters != null && !parameters.equals(other.parameters))
        {
            return false;
        }

        if(app_guid != null && !app_guid.equals(other.app_guid))
        {
            return false;
        }
        return true;
    }
}
