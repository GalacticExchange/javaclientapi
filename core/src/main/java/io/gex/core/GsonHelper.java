package io.gex.core;


import com.google.gson.*;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.gex.core.CoreMessages.EMPTY_OBJECT;

public class GsonHelper {

    private final static LogWrapper logger = LogWrapper.create(GsonHelper.class);

    private static final Gson gson = createGson();

    public static <T> T parse(JsonObject object, Class<T> entityObjClass, String parseErrorMessage) throws GexException {
        if (object == null) {
            throw logger.logAndReturnException(parseErrorMessage + " " + EMPTY_OBJECT, LogType.PARSE_ERROR);
        }
        try {
            return gson.fromJson(object, entityObjClass);
        } catch (Exception e) {
            throw logger.logAndReturnException(parseErrorMessage, e, LogType.PARSE_ERROR);
        }
    }

    public static <T> List<T> parse(JsonArray jsonArray, Class<T> entityObjClass, String parseErrorMessage) throws GexException {
        if (jsonArray == null) {
            throw logger.logAndReturnException(parseErrorMessage + " " + EMPTY_OBJECT, LogType.PARSE_ERROR);
        }
        try {
            if (jsonArray.size() > 0) {
                return gson.fromJson(jsonArray, new ListOfJson<>(entityObjClass));
            }
            return new ArrayList<>();
        } catch (Exception e) {
            throw logger.logAndReturnException(parseErrorMessage, e,  LogType.PARSE_ERROR);
        }
    }

    public static <T> T parse(String json, Class<T> entityObjClass, String parseErrorMessage) throws GexException {
        if (StringUtils.isEmpty(json)) {
            throw logger.logAndReturnException(parseErrorMessage + " " + EMPTY_OBJECT, LogType.PARSE_ERROR);
        }
        try {
            return gson.fromJson(json, entityObjClass);
        } catch (Exception e) {
            throw logger.logAndReturnException(parseErrorMessage, e, LogType.PARSE_ERROR);
        }
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static JsonElement toJsonTree(Object obj) {
        return gson.toJsonTree(obj);
    }

    public static JsonObject hidePasswordFields(JsonObject obj) { //todo implement deep found for password
        JsonObject res = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("password")) {
                res.addProperty(entry.getKey(), "******");
            } else {
                res.add(entry.getKey(), entry.getValue());
            }
        }
        return res;
    }

    private static Gson createGson() {
        JsonSerializer<LocalDateTime> dateTimeSerializer = new JsonSerializer<LocalDateTime>() {
            private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DateConverter.DATE_TIME_FORMAT)
                    .withZone(ZoneOffset.UTC);

            @Override
            public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                String dateString = DateConverter.localDateTimeToString(src, dateTimeFormatter);
                return dateString == null ? null : new JsonPrimitive(dateString);
            }
        };

        JsonDeserializer<LocalDateTime> dateTimeDeserializer = new JsonDeserializer<LocalDateTime>() {
            private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DateConverter.DATE_TIME_FORMAT)
                    .withZone(ZoneOffset.UTC);

            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                if (json == null || json.isJsonNull()) {
                    return null;
                } else if (json.getAsJsonPrimitive().isNumber()) {
                    return DateConverter.utcSecondsToLocalDateTime(json.getAsBigInteger().longValue());
                } else {
                    try {
                        return DateConverter.stringToLocalDateTime(json.getAsString(), dateTimeFormatter);
                    } catch (GexException e) {
                        throw new JsonParseException(e);
                    }
                }
            }
        };

        return new GsonBuilder().registerTypeAdapter(LocalDateTime.class, dateTimeSerializer)
                .registerTypeAdapter(LocalDateTime.class, dateTimeDeserializer).create();
    }


    private static class ListOfJson<T> implements ParameterizedType {
        private Class<?> wrapped;

        public ListOfJson(Class<T> wrapper) {
            this.wrapped = wrapper;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{wrapped};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
