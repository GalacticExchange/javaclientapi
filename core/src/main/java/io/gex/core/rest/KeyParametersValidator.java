package io.gex.core.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeyParametersValidator {

    private final static LogWrapper logger = LogWrapper.create(KeyParametersValidator.class);

    private MultivaluedMap<String, Class<?>> keyParameters;
    private List<String> invalidKeyParameters;

    public KeyParametersValidator() {
        keyParameters = new MultivaluedHashMap<>();
        invalidKeyParameters = new ArrayList<>();
    }

    public <T> KeyParametersValidator add(String key, Class<T> type) {
        logger.trace("Entered " + LogHelper.getMethodName());
        this.keyParameters.add(key, type);
        return this;
    }

    public void clear() {
        keyParameters.clear();
        invalidKeyParameters.clear();
    }

    public void check(JsonObject obj) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (MapUtils.isEmpty(keyParameters) || obj == null) {
            return;
        }
        Iterator<String> iterator = keyParameters.keySet().iterator();
        String key = null;
        while (iterator.hasNext()) {
            try {
                key = iterator.next();
                if (!obj.has(key)) {
                    invalidKeyParameters.add(key);
                    continue;
                }
                Class<?> type = keyParameters.getFirst(key);
                if (type.equals(String.class) && StringUtils.isEmpty(obj.get(key).getAsString())) {
                    throw new IllegalStateException();
                }
                if (type.equals(Long.class)) {
                    obj.get(key).getAsLong();
                    continue;
                }
                if (type.equals(Integer.class)) {
                    obj.get(key).getAsInt();
                    continue;
                }
                if (type.equals(Double.class)) {
                    obj.get(key).getAsDouble();
                    continue;
                }
                if (type.equals(JsonObject.class)) {
                    obj.getAsJsonObject(key);
                    continue;
                }
                if (type.equals(JsonArray.class)) {
                    obj.getAsJsonArray(key);
                    continue;
                }
                if (type.equals(Boolean.class)) {
                    obj.get(key).getAsBoolean();
                    continue;
                }

            } catch (NullPointerException | ClassCastException | IllegalStateException e) {
                invalidKeyParameters.add(key);
            }
        }
        if (!CollectionUtils.isEmpty(invalidKeyParameters)) {
            String msg = CoreMessages.INVALID_KEY_PARAMETERS;
            for (String com : invalidKeyParameters) {
                msg += com + "; ";
            }
            throw logger.logAndReturnException(CoreMessages.SERVER_RESPONSE_ERROR + " " + msg, LogType.RESPONSE_VALIDATION_ERROR);
        }
    }
}
