package io.gex.cli;

import io.gex.core.api.*;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Cluster;
import io.gex.core.model.Service;
import io.gex.core.model.SocksProxy;
import io.gex.core.model.parameters.SshHostParameters;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//todo do something with ssh
class SshLevel {
    private final static LogWrapper logger = LogWrapper.create(SshLevel.class);
    private static Boolean isNative = false;

    private static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                addLine(CliMessages.SSH_START_HELP_SYMBOL + " " + CliMessages.OPTIONS_PARAMETER, CliMessages.SSH_BLANK_DESCRIPTION).
                addLine(CliMessages.SSH_START_HELP_SYMBOL + CliMessages.SSH_MASTER_COMMAND + " " + CliMessages.OPTIONS_PARAMETER,
                        CliMessages.SSH_MASTER_DESCRIPTION).
                addLine(CliMessages.SSH_START_HELP_SYMBOL + CliMessages.SSH_SHARED_COMMAND + " " + CliMessages.OPTIONS_PARAMETER,
                        CliMessages.SSH_SHARED_DESCRIPTION).
                addLine(CliMessages.SSH_START_HELP_SYMBOL + CliMessages.SSH_HOST_COMMAND, CliMessages.SSH_HOST_DESCRIPTION);
        columns.print();
        System.out.println();
        columns = new Columns().addLine(CliMessages.OPTIONS).
                addLine(CliMessages.SSH_NATIVE_COMMAND, CliMessages.SSH_NATIVE_DESCRIPTION);
        columns.print();
    }

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (arguments.length > 0 && arguments[0].toLowerCase().equals(CliMessages.SSH_HOST_COMMAND)) {
            sshHost(Arrays.copyOfRange(arguments, 1, arguments.length));
        }
        /*if (arguments.length == 1 && (arguments[0].toLowerCase().equals(CliMessages.HELP_SHORT) ||
                arguments[0].toLowerCase().equals(CliMessages.HELP_LONG))) {
            help();
            return;
        }
        if (arguments.length == 0) {
            ssh();
        } else if (arguments[0].toLowerCase().equals(CliMessages.SSH_HOST_COMMAND)) {
            sshHost(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            int index;
            if ((index = ArrayUtils.indexOf(arguments, CliMessages.SSH_NATIVE_COMMAND)) != -1) {
                isNative = true;
                arguments = ArrayUtils.remove(arguments, index);
            }
            if (arguments[0].toLowerCase().equals(CliMessages.SSH_MASTER_COMMAND)) {
                sshMaster(Arrays.copyOfRange(arguments, 1, arguments.length));
            } else if (arguments[0].toLowerCase().equals(CliMessages.SSH_SHARED_COMMAND)) {
                sshShared(Arrays.copyOfRange(arguments, 1, arguments.length));
            } */
        else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
        }
        //}
    }

    private static void ssh() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        List<Cluster> clusters = TeamLevelApi.teamClusters();
        if (CollectionUtils.isEmpty(clusters)) {
            LogHelper.print(CliMessages.NO_CLUSTERS);
            return;
        }
        // TODO :: change it if we have more than 1 cluster
        connectSshByCluster(clusters.get(0));
    }

    private static void sshMaster(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.SSH_MASTER_PARAMS);
            return;
        }
        if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.SSH_MASTER_PARAMS);
            System.exit(1);
        }
        List<Cluster> clusters = TeamLevelApi.teamClusters();
        if (CollectionUtils.isEmpty(clusters)) {
            LogHelper.print(CliMessages.NO_CLUSTERS);
            return;
        }
        // TODO :: change it if we have more than 1 cluster
        Cluster cluster = clusters.get(0);
        Service sshService = ServiceLevelApi.services(cluster.getHadoopApplicationID()).stream()
                .filter(service -> "ssh".equals(service.getProtocol()) && service.isMasterContainer()).findFirst()
                .orElseThrow(() -> logger.logAndReturnException("Master ssh service not found", LogType.SSH_ERROR));
        String username = UserLevelApi.userInfo().getUsername();
        String password = BasePropertiesHelper.getValidToken();
        connectSsh(sshService, username, password);
    }


    private static void sshShared(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String quitSymbol = "q";
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.SSH_SHARED_PARAMS);
            return;
        }
        if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.SSH_SHARED_PARAMS);
            System.exit(1);
        }
        List<Cluster> sharedClusters = ShareLevelApi.shareClusterList();
        if (CollectionUtils.isEmpty(sharedClusters)) {
            LogHelper.print(CliMessages.NO_SHARED_CLUSTERS);
            return;
        }
        if (sharedClusters.size() == 1) {
            connectSshByCluster(sharedClusters.get(0));
            return;
        }
        System.out.println();
        Columns columns = new Columns().addLine(CliMessages.NUMBER_SIGN, CliMessages.NAME_CAPS, CliMessages.TEAM_CAPS,
                CliMessages.STATUS_CAPS);
        int selectedClusterNumber = -1, tmp;
        for (int i = 0; i < sharedClusters.size(); i++) {
            Cluster cluster = sharedClusters.get(i);
            String teamName = cluster.getTeam() != null ? cluster.getTeam().getName() : StringUtils.EMPTY;
            String status = cluster.getStatus() != null ? cluster.getStatus().toString() : StringUtils.EMPTY;
            columns.addLine(String.valueOf(i + 1), cluster.getName(), teamName, status);
        }
        columns.print();
        System.out.println();
        LogHelper.print(CliMessages.SELECT_CLUSTER);
        while (selectedClusterNumber == -1) {
            String input = System.console().readLine();
            if (input.trim().toLowerCase().equals(quitSymbol))
                return;
            try {
                tmp = Integer.parseInt(input);
                if (tmp < 1 || tmp > sharedClusters.size())
                    throw new NumberFormatException();
                selectedClusterNumber = tmp - 1;
            } catch (NumberFormatException e) {
                System.out.println();
                LogHelper.print(CliMessages.INVALID_INPUT_TRY);
            }
        }
        System.out.println();
        connectSshByCluster(sharedClusters.get(selectedClusterNumber));
    }

    private static void connectSshByCluster(Cluster cluster) throws GexException {
        List<Service> sshServices = ServiceLevelApi.services(cluster.getHadoopApplicationID()).stream()
                .filter(service -> "ssh".equals(service.getProtocol())).collect(Collectors.toList());
        String username = UserLevelApi.userInfo().getUsername();
        String password = BasePropertiesHelper.getValidToken();
        if (sshServices.size() == 1) {
            connectSsh(sshServices.get(0), username, password);
            return;
        }
        Columns columns = new Columns().addLine(CliMessages.NUMBER_SIGN, "Container", "MASTER");
        for (int i = 0; i < sshServices.size(); i++) {
            columns.addLine(String.valueOf(i + 1), sshServices.get(i).getContainerName(),
                    String.valueOf(sshServices.get(i).isMasterContainer()));
        }
        columns.print();
        System.out.println();
        LogHelper.print(CliMessages.SELECT_SERVICE);
        int selectedServiceNumber = -1, tmp;
        String quitSymbol = "q";
        while (selectedServiceNumber == -1) {
            String input = System.console().readLine();
            if (input.trim().toLowerCase().equals(quitSymbol)) {
                return;
            }
            try {
                tmp = Integer.parseInt(input);
                if (tmp < 1 || tmp > sshServices.size())
                    throw new NumberFormatException();
                selectedServiceNumber = tmp - 1;
            } catch (NumberFormatException e) {
                System.out.println();
                LogHelper.print(CliMessages.INVALID_INPUT_TRY);
            }
        }
        System.out.println();
        connectSsh(sshServices.get(selectedServiceNumber), username, password);
    }

    private static void connectSsh(Service service, String username, String password) throws GexException {
        if (isNative) {
            SshLevelApi.connectNative(service, username, password);
        } else {
            SshLevelApi.connect(service, username, password);
        }
    }

    private static void sshHost(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.SSH_HOST_PARAMS);
            return;
        }
        if (arguments.length != 5 && arguments.length != 4 && arguments.length != 7) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.SSH_HOST_PARAMS);
            return;
        }
        SshHostParameters properties = new SshHostParameters(arguments);
        SocksProxy socksProxy = null;
        if (StringUtils.isNotEmpty(properties.getProxy())) {
            socksProxy = new SocksProxy();
            socksProxy.setHost(properties.getProxy());
            socksProxy.setUser(properties.getProxyUsername());
            socksProxy.setPassword(properties.getProxyPassword());
        }
        SshLevelApi.connectNative(properties.getHost(), properties.getPort(), properties.getUsername(),
                properties.getPassword(), socksProxy);
    }
}
