package io.gex.core;

import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.ApplicationMode;
import io.gex.core.propertiesHelper.GexdPropertiesHelper;
import io.gex.core.propertiesHelper.NodePropertiesHelper;
import io.gex.core.propertiesHelper.UserPropertiesHelper;

public class Init {

    private final static LogWrapper logger = LogWrapper.create(Init.class);

    public static void checkConnection() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        //check API connection
        if (!ConnectionChecker.isAPIReachable()) {
            // check Internet connection
            if (!ConnectionChecker.isInternetReachable()) {
                throw logger.logAndReturnException(CoreMessages.NO_INTERNET_CONNECTION, LogType.CONNECTION_ERROR);
            }
            throw logger.logAndReturnException(CoreMessages.NO_API_CONNECTION, LogType.CONNECTION_ERROR);
        }
    }

    public static void init(ApplicationMode mode) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        PropertiesHelper.mode = mode;
        // read properties from file or load default
        PropertiesHelper.readProperties();
        // all next calls have to be after PropertiesHelper.readProperties();
        PropertiesHelper.gexd = new GexdPropertiesHelper();
        PropertiesHelper.node = new NodePropertiesHelper();
        PropertiesHelper.user = new UserPropertiesHelper();
        PropertiesHelper.setEnvironment();
        checkConnection();
    }

}