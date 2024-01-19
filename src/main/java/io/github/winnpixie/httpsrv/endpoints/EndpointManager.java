package io.github.winnpixie.httpsrv.endpoints;

import io.github.winnpixie.httpsrv.endpoints.impl.DefaultEndpoint;

import java.util.ArrayList;
import java.util.List;

public class EndpointManager {
    private final List<Endpoint> endpoints = new ArrayList<>();
    private final DefaultEndpoint defaultEndpoint = new DefaultEndpoint();

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public boolean register(Endpoint endpoint) {
        return endpoints.add(endpoint);
    }

    public boolean unregister(Endpoint endpoint) {
        return endpoints.remove(endpoint);
    }

    public Endpoint getEndpoint(String path) {
        for (Endpoint endpoint : endpoints) {
            if (!path.startsWith(endpoint.getPath())) continue;

            return endpoint;
        }

        return defaultEndpoint;
    }
}