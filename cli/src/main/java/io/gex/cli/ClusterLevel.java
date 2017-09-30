package io.gex.cli;

import io.gex.core.api.ClusterLevelApi;
import io.gex.core.api.TeamLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Cluster;
import io.gex.core.model.parameters.ClusterParameters;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

class ClusterLevel {

    private final static LogWrapper logger = LogWrapper.create(ClusterLevel.class);

    private static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                addLine(CliMessages.CLUSTER_START_HELP_SYMBOL + CliMessages.CLUSTER_CREATE_COMMAND,
                        CliMessages.CLUSTER_CREATE_DESCRIPTION).
                addLine(CliMessages.CLUSTER_START_HELP_SYMBOL + CliMessages.CLUSTER_INFO_COMMAND,
                        CliMessages.CLUSTER_INFO_DESCRIPTION).
                addLine(CliMessages.CLUSTER_START_HELP_SYMBOL + CliMessages.CLUSTER_LIST_COMMAND,
                        CliMessages.CLUSTER_LIST_DESCRIPTION);
        columns.print();
    }

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.mainMenuHelpCheck(arguments)) {
            help();
        } else if (arguments[0].toLowerCase().equals(CliMessages.CLUSTER_CREATE_COMMAND)) {
            clusterCreate(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.CLUSTER_LIST_COMMAND)) {
            clusterList(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.CLUSTER_INFO_COMMAND)) {
            clusterInfo(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
            System.exit(1);
        }
    }

    private static void clusterCreate(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.CLUSTER_CREATE_PARAMS);
            return;
        } else if (arguments.length < 2) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.CLUSTER_CREATE_PARAMS);
            System.exit(1);
        }
        Cluster cluster = ClusterLevelApi.clusterCreate(new ClusterParameters().parse(arguments));
        CliMessages.printWelcomeMessage((cluster != null && StringUtils.isNotBlank(cluster.getName())) ? cluster.getName() : null);
    }

    // team clusters alias
    private static void clusterList(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.CLUSTER_LIST_PARAMS);
            return;
        } else if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.CLUSTER_LIST_PARAMS);
            System.exit(1);
        }
        printClusterList(TeamLevelApi.teamClusters());
    }

    private static void clusterInfo(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.CLUSTER_INFO_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.CLUSTER_INFO_PARAMS);
            System.exit(1);
        }
        printCluster(ClusterLevelApi.clusterInfo(arguments[0]));
    }

    static void printClusterList(List<Cluster> clusters) {
        if (CollectionUtils.isEmpty(clusters)) {
            LogHelper.print(CliMessages.CLUSTER_EMPTY);
            return;
        }
        System.out.println(CliMessages.DELIMITER);
        for (Cluster cluster : clusters) {
            printCluster(cluster);
            System.out.println(CliMessages.DELIMITER);
        }
    }

    private static void printCluster(Cluster cluster) {
        if (cluster == null) {
            return;
        }
        Columns columns = new Columns();
        if (StringUtils.isNotBlank(cluster.getId())) {
            columns.addLine(CliMessages.ID, cluster.getId());
        }
        if (StringUtils.isNotBlank(cluster.getName())) {
            columns.addLine(CliMessages.NAME, cluster.getName());
        }
        if (StringUtils.isNotBlank(cluster.getDomainName())) {
            columns.addLine(CliMessages.DOMAIN_NAME, cluster.getDomainName());
        }
        if (cluster.getTeam() != null && StringUtils.isNotBlank(cluster.getTeam().getName())) {
            columns.addLine(CliMessages.TEAM_NAME, cluster.getTeam().getName());
        }
        if (cluster.getStatus() != null) {
            columns.addLine(CliMessages.STATUS, cluster.getStatus().toString().toLowerCase());
        }
        if (StringUtils.isNotBlank(cluster.getHadoopApplicationID())) {
            columns.addLine(CliMessages.HADOOP_APPLICATION_ID, cluster.getHadoopApplicationID());
        }
        if (cluster.getClusterType() != null) {
            columns.addLine(CliMessages.CLUSTER_TYPE, cluster.getClusterType().toString().toLowerCase());
        }
        if (cluster.getClusterSettings() != null) {
            if (StringUtils.isNotBlank(cluster.getClusterSettings().getHadoopType())) {
                columns.addLine(CliMessages.HADOOP_TYPE, cluster.getClusterSettings().getHadoopType());
            }
            // todo print settings and etc if present
        }
        columns.print();
    }
}
