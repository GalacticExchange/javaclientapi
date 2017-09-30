package io.gex.agent.webServer;

import io.gex.agent.GexdMessages;
import io.gex.agent.GexdStatus;
import io.gex.agent.GexdStatusHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class GexdServerWindows {

    private final static LogWrapper logger = LogWrapper.create(GexdServerWindows.class);

    private final static String WINDOWS_FIREWALL_RULE = "gexd_daemon_";

    static void deleteWindowsFirewallRules(Integer port) {
        logger.trace("Entered " + LogHelper.getMethodName());
        GexdStatusHelper.sendGexdStatus(GexdStatus.FINDING_WINDOWS_RULES);
        List<String> cmd = Commands.cmd("netsh advfirewall firewall show rule name=all | find \"gexd_daemon_\"");
        try {
            Process p = ShellExecutor.getExecutionProcess(ShellParameters.newBuilder(cmd).build());
            InputStream in = p.getInputStream();
            List<String> lines = new ArrayList<>();
            String line;
            if (in != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                while ((line = bufferedReader.readLine()) != null) {
                    if (StringUtils.isNotBlank(line)) {
                        logger.logInfo(line, LogType.FIREWALL);
                        String[] parts = line.split("\\s+");
                        lines.add(parts[parts.length - 1]);
                    }
                }
            }
            if (p.waitFor() != 0) {
                return;
            }
            if (CollectionUtils.isNotEmpty(lines)) {
                GexdStatusHelper.sendGexdStatus(GexdStatus.DELETE_WINDOWS_RULES);
                lines.remove(WINDOWS_FIREWALL_RULE + port);
                for (String l : lines) {
                    cmd = Commands.cmd("netsh advfirewall firewall delete rule name=\"" + l + "\"");
                    logger.logInfo(GexdMessages.DELETING_WINDOWS_RULE + l, LogType.FIREWALL);
                    try {
                        ShellExecutor.executeCommand(ShellParameters.newBuilder(cmd).build());
                    } catch (GexException e1) {
                        // not raising exception
                        logger.logError(e1, LogType.FIREWALL_ERROR);
                    }
                }
            }
        } catch (Exception e) {
            logger.logError(e, LogType.FIREWALL_ERROR);
        }
    }

    static void checkWindowsFirewall(Integer port) {
        logger.trace("Entered " + LogHelper.getMethodName());
        GexdStatusHelper.sendGexdStatus(GexdStatus.CHECKING_WINDOWS_RULES);
        List<String> cmd = Commands.cmd("netsh advfirewall firewall show rule name=\"" + WINDOWS_FIREWALL_RULE +
                port + "\"");
        try {
            ShellExecutor.executeCommand(ShellParameters.newBuilder(cmd).build());
        } catch (GexException e) {
            logger.logWarn(GexdMessages.WINDOWS_FIREWALL_RULE_NOT_FOUND, LogType.FIREWALL_ERROR);
            GexdStatusHelper.sendGexdStatus(GexdStatus.ADD_WINDOWS_RULES);
            cmd = Commands.cmd("netsh advfirewall firewall add rule name=\"" + WINDOWS_FIREWALL_RULE
                    + port + "\" dir=in action=allow protocol=TCP localport=" + port);
            try {
                ShellExecutor.executeCommand(ShellParameters.newBuilder(cmd).build());
            } catch (GexException e1) {
                logger.logError(e1, LogType.FIREWALL_ERROR);
            }
        }
    }
}
