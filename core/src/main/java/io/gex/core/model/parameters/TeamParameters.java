package io.gex.core.model.parameters;

import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Team;
import org.apache.commons.lang3.StringUtils;

public class TeamParameters {

    private final static LogWrapper logger = LogWrapper.create(TeamParameters.class);

    private final static String ABOUT_PARAMETER = "--about=";

    public static Team parseUpdate(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Team team = new Team();
        try {
            for (String argument : arguments) {
                if (StringUtils.containsIgnoreCase(argument, ABOUT_PARAMETER)) {
                    team.setAbout(BaseHelper.trimAndRemoveSubstring(argument, ABOUT_PARAMETER));
                    if (StringUtils.isBlank(team.getAbout())) {
                        throw new IllegalArgumentException(argument);
                    }
                } else {
                    throw new IllegalArgumentException(argument);
                }
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.INVALID_PARAMETER + e.getMessage(), LogType.PARSE_ERROR);
        }
        return team;
    }

}
