package io.gex.core.log;

import com.google.gson.JsonObject;
import io.gex.core.PropertiesHelper;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;

import java.util.HashMap;
import java.util.Map;

public class SlackLogger {

    private static String API_WEBHOOK = "https://hooks.slack.com/services/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";
    private static String PROD_API_WEBHOOK = "https://hooks.slack.com/services/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";
    private static Map<String, Long> errors = new HashMap<>();
    private SlackApi api;

    public SlackLogger(boolean isProd) {
        api = new SlackApi(isProd ? PROD_API_WEBHOOK : API_WEBHOOK);
    }

    //todo attachments not null
    public void send(String message, JsonObject attachments) {
        try {
            if (!errors.containsKey(message) ||
                    (errors.containsKey(message) && (System.currentTimeMillis() - errors.get(message)) > 3600000)) {
                attachments.addProperty("version", PropertiesHelper.VERSION);
                api.call(new SlackMessage("#client_errors", "java", message).setIcon(":rocket:").
                        addAttachments(new SlackAttachment().setText(attachments.toString()).setColor("#b92638").setFallback("Client error")));
                errors.put(message, System.currentTimeMillis());
            }
        } catch (Throwable e) {
            //do nothing
        }
    }

    public static void flush() {
        errors.clear();
    }
}
