package io.gex.core.rest;

import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static io.gex.core.CoreMessages.replaceTemplate;

public class EmptinessValidator {
    private final static LogWrapper logger = LogWrapper.create(EmptinessValidator.class);

    private List<Pair<Object, String>> params;
    private List<Pair<Object, String>> invalidParameters;

    public EmptinessValidator() {
        params = new ArrayList<>();
        invalidParameters = new ArrayList<>();
    }

    public EmptinessValidator add(Object value, String parameterName) {
        logger.trace("Entered " + LogHelper.getMethodName());
        params.add(new ImmutablePair<>(value, parameterName));
        return this;
    }

    public void clear() {
        params.clear();
        invalidParameters.clear();
    }

    public void check(Class entity) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CollectionUtils.isEmpty(params)) {
            return;
        }
        params.forEach(parameter -> {
            if (parameter.getLeft() instanceof String
                    && StringUtils.isEmpty((String) parameter.getLeft())) {
                invalidParameters.add(parameter);
            } else if (parameter.getLeft() == null) {
                invalidParameters.add(parameter);
            }
        });
        if (!CollectionUtils.isEmpty(invalidParameters)) {
            StringJoiner stringJoiner = new StringJoiner(", ", replaceTemplate(CoreMessages.EMPTY_PARAMETERS_FOR_ENTITY,
                    entity.getSimpleName()), StringUtils.EMPTY);
            for (Pair<Object, String> parameter : invalidParameters) {
                stringJoiner.add(parameter.getRight());
            }
            throw logger.logAndReturnException(CoreMessages.SERVER_RESPONSE_ERROR + " Empty parameters: " + stringJoiner.toString(),
                    LogType.RESPONSE_VALIDATION_ERROR);
        }
    }
}
