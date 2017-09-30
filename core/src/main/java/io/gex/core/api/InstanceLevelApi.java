package io.gex.core.api;

import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.rest.InstanceRest;
import org.apache.commons.lang3.StringUtils;

public class InstanceLevelApi {

    private final static LogWrapper logger = LogWrapper.create(InstanceLevelApi.class);

    public static void instanceRegister(String instanceID, String awsInstanceID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if(StringUtils.isBlank(instanceID)){
            throw logger.logAndReturnException(CoreMessages.EMPTY_INSTANCE_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        InstanceRest.instanceRegister(instanceID, awsInstanceID);
    }
}
