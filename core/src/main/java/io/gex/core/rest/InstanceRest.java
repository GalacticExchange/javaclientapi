package io.gex.core.rest;

import io.gex.core.HardwareHelper;
import io.gex.core.PropertiesHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class InstanceRest {

    private final static LogWrapper logger = LogWrapper.create(InstanceRest.class);
    private final static String INSTANCE = "/applicationRegistrations";

    public static void instanceRegister(String instanceID, String awsInstanceID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        MultivaluedMap<String, String> body = new MultivaluedHashMap<>();
        body.add("instanceID", instanceID);
        body.add("version", PropertiesHelper.VERSION);
        body.add("systemInfo", HardwareHelper.getNodeInfo().toString());
        if (StringUtils.isNoneBlank(awsInstanceID)) {
            body.add("awsInstanceID", awsInstanceID);
        }
        Rest.sendRequest(HttpMethod.POST, INSTANCE, LogType.INSTANCE_REGISTER_ERROR, null, body, null);
    }
}
