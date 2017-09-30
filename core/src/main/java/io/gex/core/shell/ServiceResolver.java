package io.gex.core.shell;

import io.gex.core.model.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

import static io.gex.core.PropertiesHelper.*;

public class ServiceResolver {

    public static String constructNativeSshHost(Service service, String username) {
        return username + "@" + (service.isMasterContainer() ? readProperty(PROXY_PROP_NAME) : service.getPublicIp());
    }

    public static Integer getPort(Service service) {
        return service != null ? service.getPort() : null;
    }

    public static String getHost(Service service) {
        if (service == null || StringUtils.isEmpty(service.getPublicIp())) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        if (Pattern.matches("^https?$", service.getProtocol())) {
            builder.append(service.getProtocol());
            builder.append("://");
            builder.append(service.isMasterContainer() ? readProperty(WEBPROXY_PROP_NAME) : service.getPublicIp());
        } else {
            builder.append(service.isMasterContainer() ? readProperty(PROXY_PROP_NAME) : service.getPublicIp());
        }

        return builder.toString();
    }
}

