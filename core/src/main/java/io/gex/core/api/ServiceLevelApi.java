package io.gex.core.api;

import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Service;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.ServiceLevelRest;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ServiceLevelApi {

    private final static LogWrapper logger = LogWrapper.create(ServiceLevelApi.class);

    public static List<Service> services(String applicationID) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(applicationID)) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_APP_ID, LogType.EMPTY_PROPERTY_ERROR);
        }
        return ServiceLevelRest.services(applicationID, BasePropertiesHelper.getValidToken());
    }

}
