package io.gex.cli;

import com.google.gson.JsonObject;
import io.gex.core.*;
import io.gex.core.api.NodeLevelApi;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.*;
import io.gex.core.model.parameters.NodeInstallParameters;
import io.gex.core.model.properties.NodeProperties;
import io.gex.core.propertiesHelper.BasePropertiesHelper;
import io.gex.core.rest.Rest;
import io.gex.core.vagrantHelper.VagrantHelper;
import io.gex.core.virutalBoxHelper.VirtualBoxHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.core.util.FileUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.Console;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static io.gex.core.DateConverter.DATE_TIME_FORMAT;
import static io.gex.core.DateConverter.localDateTimeToString;

class NodeLevel {

    private final static LogWrapper logger = LogWrapper.create(NodeLevel.class);

    private static void help() {
        logger.trace("Entered " + LogHelper.getMethodName());
        Columns columns = new Columns().
                addLine(CliMessages.NODE_START_HELP_SYMBOL + CliMessages.NODE_INSTALL_COMMAND,
                        CliMessages.NODE_INSTALL_DESCRIPTION).
                addLine(CliMessages.NODE_START_HELP_SYMBOL + CliMessages.NODE_REINSTALL_COMMAND,
                        CliMessages.NODE_REINSTALL_DESCRIPTION).
                addLine(CliMessages.NODE_START_HELP_SYMBOL + CliMessages.NODE_UNINSTALL_COMMAND,
                        CliMessages.NODE_UNINSTALL_DESCRIPTION).
                addLine(CliMessages.NODE_START_HELP_SYMBOL + CliMessages.NODE_REMOVE_COMMAND,
                        CliMessages.NODE_REMOVE_DESCRIPTION).
                addLine(CliMessages.NODE_START_HELP_SYMBOL + CliMessages.NODE_INFO_COMMAND,
                        CliMessages.NODE_INFO_DESCRIPTION).
                addLine(CliMessages.NODE_START_HELP_SYMBOL + CliMessages.NODE_LIST_COMMAND,
                        CliMessages.NODE_LIST_DESCRIPTION).
                addLine(CliMessages.NODE_START_HELP_SYMBOL + CliMessages.NODE_STOP,
                        CliMessages.NODE_STOP_DESCRIPTION).
                addLine(CliMessages.NODE_START_HELP_SYMBOL + CliMessages.NODE_START,
                        CliMessages.NODE_START_DESCRIPTION).
                addLine(CliMessages.NODE_START_HELP_SYMBOL + CliMessages.NODE_RESTART,
                        CliMessages.NODE_RESTART_DESCRIPTION).
                addLine(CliMessages.NODE_START_HELP_SYMBOL + CliMessages.NODE_ENV_COMMAND,
                        CliMessages.NODE_ENV_DESCRIPTION);
        columns.print();
    }

    public static void executeCommand(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.mainMenuHelpCheck(arguments)) {
            help();
        } else if (arguments[0].toLowerCase().equals(CliMessages.NODE_INSTALL_COMMAND)) {
            nodeInstall(Arrays.copyOfRange(arguments, 1, arguments.length), false, null, null);
        } else if (arguments[0].toLowerCase().equals(CliMessages.NODE_REINSTALL_COMMAND)) {
            nodeReinstall(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (Arrays.asList(CliMessages.NODE_START, CliMessages.NODE_STOP, CliMessages.NODE_RESTART).contains(arguments[0].toLowerCase())) {
            nodeCommands(arguments);
        } else if (arguments[0].toLowerCase().equals(CliMessages.NODE_UNINSTALL_COMMAND)) {
            nodeUninstall(Arrays.copyOfRange(arguments, 1, arguments.length), false);
        } else if (arguments[0].toLowerCase().equals(CliMessages.NODE_INFO_COMMAND)) {
            nodeInfo(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.NODE_LIST_COMMAND)) {
            nodeList(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.NODE_REMOVE_COMMAND)) {
            nodeRemove(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else if (arguments[0].toLowerCase().equals(CliMessages.NODE_ENV_COMMAND)) {
            nodeEnv(Arrays.copyOfRange(arguments, 1, arguments.length));
        } else {
            CliHelper.printError(CliMessages.UNKNOWN_COMMAND);
            System.exit(1);
        }
    }

    //todo change parameters reading
    private static void nodeEnv(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        boolean isAfterDep = false;
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.NODE_ENV_PARAMS);
            return;
        } else if (arguments.length > 0 && CliMessages.NODE_ENV_AFTER_DEP.equals(arguments[0])) {
            isAfterDep = true;
            arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
        }
        File vBoxFolder = null;
        if (isAfterDep && arguments.length > 0 && arguments[0].startsWith(CliMessages.NODE_ENV_VBOX_DIR)) {
            String pathToVBoxFolder = BaseHelper.trimAndRemoveSubstring(arguments[0], CliMessages.NODE_ENV_VBOX_DIR);
            vBoxFolder = new File(pathToVBoxFolder);
            arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
        }
        if (arguments.length != 0) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.NODE_ENV_PARAMS);
            System.exit(1);
        }
        if (isAfterDep) {
            checkHardwareAfterDep(vBoxFolder);
        } else {
            checkHardware();
        }
    }

    private static void nodeReinstall(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.NODE_REINSTALL_PARAMS);
            return;
        } else if ((arguments.length == 1 && !arguments[0].equals("-f")) || arguments.length > 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.NODE_REINSTALL_PARAMS);
            System.exit(1);
        }
        if (System.getProperty("user.dir").contains(Paths.get(PropertiesHelper.userHome, ".gex").toString())) {
            CliHelper.printError(CliMessages.WORKING_DIRECTORY_RESTRICTION);
            return;
        }
        Boolean answer = CliHelper.getYesOrNoFromConsole(CliMessages.NODE_REINSTALL_QUESTION);
        if (!answer) {
            return;
        }
        String clusterID = PropertiesHelper.node.getProps().getClusterID();
        if (StringUtils.isBlank(clusterID)) {
            CliHelper.printError(CoreMessages.EMPTY_CLUSTER_ID);
            System.exit(1);
        }
        String customName = PropertiesHelper.node.getProps().getNodeName();
        if (StringUtils.isBlank(customName)) {
            customName = NodeLevelApi.nodeInfo().getName();
        }
        if (StringUtils.isBlank(customName)) {
            CliHelper.printError(CoreMessages.EMPTY_NODE_NAME);
            System.exit(1);
        }
        nodeUninstall(arguments, true);
        PropertiesHelper.node.fetchPropsInMem();
        nodeInstall(arguments, true, clusterID, customName);
    }

    private static void nodeInstall(String[] arguments, boolean isReinstall, String clusterID, String customName) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (!isReinstall) {
            if (CliHelper.helpCheck(arguments)) {
                LogHelper.print(CliMessages.USAGE + CliMessages.NODE_INSTALL_PARAMS);
                return;
            } else if (arguments.length == 0 || arguments.length > 2) {
                CliHelper.printError(CliMessages.USAGE + CliMessages.NODE_INSTALL_PARAMS);
                System.exit(1);
            }
            NodeInstallParameters nodeInstallParameters = new NodeInstallParameters(arguments);
            clusterID = nodeInstallParameters.getClusterID();
            customName = nodeInstallParameters.getNodeName();
        }
        //check node is not installed
        NodeLevelApi.installationNodeStatusCheck();
        CheckHardwareResult checkResult = checkHardware();
        if (!SystemUtils.IS_OS_LINUX) {
            VirtualBoxHelper virtualBoxHelper = VirtualBoxHelper.constructVirtualBoxHelper();
            VagrantHelper vagrantHelper = VagrantHelper.constructVagrantHelper();
            if (!virtualBoxHelper.isInstalled() || !vagrantHelper.isInstalled()) {
                LogHelper.print(CliMessages.VAGRANT_AND_VIRTUAL_BOX_EXPLANATION);
            }
            VirtualBoxLevel.checkVirtualBox();
            VagrantLevel.checkVagrant();
        }
        File vboxFolder = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            LogHelper.print(CoreMessages.VIRTUAL_BOX_HOME);
            LogHelper.print(CliMessages.VIRTUAL_BOX_HOME_QUESTION);
            Console console = System.console();
            String output = console.readLine();
            if (StringUtils.isNotBlank(output)) {
                try {
                    File tempVboxFolder = new File(output);
                    FileUtils.mkdir(tempVboxFolder, true);
                    vboxFolder = tempVboxFolder;
                } catch (Exception e) {
                    throw logger.logAndReturnException(CoreMessages.SET_VIRTUAL_BOX_HOME_ERROR, e, LogType.VBOX_MACHINE_FOLDER);
                }
            }
        }
        checkHardwareAfterDep(vboxFolder);

        LogHelper.print(CliMessages.NODE_INSTALLING);
        JsonObject body = new JsonObject();
        body.addProperty("vBoxMachineFolder", vboxFolder != null ? vboxFolder.toString() : null);
        if (StringUtils.isNotBlank(customName)) {
            body.addProperty("customName", customName);
        }
        body.addProperty("clusterID", clusterID);
        body.add("selectedNetInterface", GsonHelper.toJsonTree(checkResult.getNetworkAdapter()));
        MultivaluedMap<String, String> header = new MultivaluedHashMap<>();
        header.add("token", BasePropertiesHelper.getValidToken());
        JsonObject obj = Rest.sendLocalRequest(HttpMethod.POST, "/nodes", LogType.WEB_SERVER_ERROR, header, body, null);
        CliHelper.printRocket();
        if (obj.has(NodeProperties.NODE_NAME_PROPERTY_NAME) && !obj.get(NodeProperties.NODE_NAME_PROPERTY_NAME).isJsonNull()) {
            CliMessages.printNodeInstallationMessage(obj.get(NodeProperties.NODE_NAME_PROPERTY_NAME).getAsString());
        }
    }

    private static void nodeList(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.NODE_LIST_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.NODE_LIST_PARAMS);
            return;
        }
        printNodeList(NodeLevelApi.nodeList(arguments[0]));
    }

    private static void nodeCommands(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String actionString = arguments[0].toLowerCase();
        Action action;
        try {
            action = Action.valueOf(actionString);
        } catch (Exception e) {
            CliHelper.printError(CliMessages.INVALID_COMMAND);
            return;
        }
        arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CoreMessages.replaceTemplate(CliMessages.NODE_COMMANDS_PARAMS, actionString));
            LogHelper.print(CliMessages.NODE_COMMANDS_ADDITIONAL_PARAMS);
            return;
        } else if (arguments.length > 1) {
            CliHelper.printError(CliMessages.USAGE + CoreMessages.replaceTemplate(CliMessages.NODE_COMMANDS_PARAMS, actionString));
            CliHelper.printError(CliMessages.NODE_COMMANDS_ADDITIONAL_PARAMS);
            System.exit(1);
        }
        if (arguments.length == 1) {
            NodeLevelApi.nodeCommands(arguments[0], action);
        } else {
            NodeLevelApi.nodeCommands(action);
        }
        LogHelper.print(CliMessages.COMMAND_EXECUTED);
    }

    private static void nodeInfo(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.NODE_INFO_PARAMS);
            return;
        } else if (arguments.length > 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.NODE_INFO_PARAMS);
            System.exit(1);
        }
        Node node;
        if (arguments.length == 0) {
            node = NodeLevelApi.nodeInfo();
        } else {
            node = NodeLevelApi.nodeInfo(arguments[0]);
        }
        printNode(node);
    }

    private static void nodeUninstall(String[] arguments, boolean isReinstall) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (!isReinstall) {
            if (CliHelper.helpCheck(arguments)) {
                LogHelper.print(CliMessages.USAGE + CliMessages.NODE_UNINSTALL_PARAMS);
                return;
            } else if ((arguments.length == 1 && !arguments[0].equals(CliMessages.FORCE)) || arguments.length > 1) {
                CliHelper.printError(CliMessages.USAGE + CliMessages.NODE_UNINSTALL_PARAMS);
                System.exit(1);
            }
        }
        if (arguments.length == 1) {
            LogHelper.print(CliMessages.UNINSTALLING);
            NodeLevelApi.nodeUninstall(true);
        } else {
            if (!isReinstall) {
                Boolean answer = CliHelper.getYesOrNoFromConsole(CliMessages.NODE_UNINSTALL_QUESTION);
                if (!answer) {
                    return;
                }
            }
            LogHelper.print(CliMessages.UNINSTALLING);
            if (SystemUtils.IS_OS_WINDOWS) {
                HardwareHelper.checkGexdIsRunning();
            }
            LogHelper.print(NodeLevelApi.nodeUninstall(false));
        }
        LogHelper.print(CoreMessages.NODE_UNINSTALLED);
    }

    private static void nodeRemove(String[] arguments) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CliHelper.helpCheck(arguments)) {
            LogHelper.print(CliMessages.USAGE + CliMessages.NODE_REMOVE_PARAMS);
            return;
        } else if (arguments.length != 1) {
            CliHelper.printError(CliMessages.USAGE + CliMessages.NODE_REMOVE_PARAMS);
            System.exit(1);
        }
        Boolean answer = CliHelper.getYesOrNoFromConsole(CliMessages.NODE_REMOVE_QUESTION);
        if (answer) {
            NodeLevelApi.nodeRemove(arguments[0]);
            LogHelper.print(CliMessages.NODE_REMOVED);
        }

    }

    private static CheckHardwareResult checkHardware() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        CheckHardwareResult result = new CheckHardwareResult();
        boolean notUi = !PropertiesHelper.isUI();
        PrintStream ps;
        try {
            ps = new PrintStream(System.out, true, BaseHelper.getConsoleEncoding().name());
        } catch (UnsupportedEncodingException e) {
            throw logger.logAndReturnException(e, LogType.HARDWARE_INFO_ERROR);
        }
        try {
            LogHelper.print(CliMessages.NETWORK_CHECK, notUi);
            HardwareHelper.checkNetworkConnection();
            LogHelper.print(CliMessages.DONE, notUi);

            LogHelper.print(CliMessages.RABBIT_CHECK, notUi);
            ConnectionChecker.pingRabbit();
            LogHelper.print(CliMessages.DONE, notUi);

            LogHelper.print(CliMessages.GEXD_CHECK, notUi);
            HardwareHelper.checkGexdIsRunning();
            LogHelper.print(CliMessages.DONE, notUi);

            if (PropertiesHelper.isHostVirtual()) {
                if (SystemUtils.IS_OS_WINDOWS) {
                    LogHelper.print(CliMessages.HYPERV_CHECK, notUi);
                    HardwareHelper.checkHypervEnabled();
                    LogHelper.print(CliMessages.DONE, notUi);
                }

                LogHelper.print(CliMessages.VIRTUALIZATION_CHECK, notUi);
                HardwareHelper.checkVirtualizationEnabled();
                LogHelper.print(CliMessages.DONE, notUi);

                LogHelper.print(CliMessages.LOOK_FOR_NETWORK_CONNECTIONS, notUi);
                List<NetworkAdapter> networkAdapters = HardwareHelper.getBestNetworkAdapters();
                result.setAllNetworkAdapters(networkAdapters);
                // todo if not CLI and not 1
                if (networkAdapters.size() == 1) {
                    result.setNetworkAdapter(networkAdapters.get(0));
                    LogHelper.print(CliMessages.FOUND_NETWORK_CONNECTION + " " + result.getNetworkAdapter().getName(), notUi);
                    if (networkAdapters.get(0).getWifi()) {
                        LogHelper.print(CliMessages.WIFI_ADAPTER_CHOSEN, notUi);
                    }
                } else if (PropertiesHelper.isCLI()) {
                    result.setNetworkAdapter(selectAdapter(networkAdapters));
                }
                LogHelper.print(CliMessages.DONE, notUi);

                LogHelper.print(CliMessages.CPU_CHECK, notUi);
                HardwareHelper.checkMinimumCpuCoresCount();
                LogHelper.print(CliMessages.DONE, notUi);

                LogHelper.print(CliMessages.RAM_CHECK, notUi);
                HardwareHelper.checkMinimumRamCount();
                LogHelper.print(CliMessages.DONE, notUi);

                LogHelper.print(CliMessages.FREE_SPACE_DEP_CHECK, notUi);
                HardwareHelper.checkUsableSpaceInstallDependencies();
                LogHelper.print(CliMessages.DONE, notUi);
            }
        } catch (GexException e) {
            if (PropertiesHelper.isUI()) {
                CliHelper.printErrMessageForUi(e);
            } else {
                throw e;
            }
            return result;
        }

        if (PropertiesHelper.isUI()) {
            ps.println(GsonHelper.toJson(result));
        }
        return result;
    }

    private static void checkHardwareAfterDep(File vboxFolder) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        boolean notUi = !PropertiesHelper.isUI();
        if (PropertiesHelper.isHostVirtual()) {
            try {
                //todo check toString() and check stackTrace
                LogHelper.print(CliMessages.DKMS_CHECK, notUi);
                VirtualBoxHelper.constructVirtualBoxHelper().checkDKMS();
                LogHelper.print(CliMessages.DONE, notUi);
                logger.logInfo("Detailed system info: " + HardwareHelper.getComputerInfo(), LogType.HARDWARE_INFO);
                LogHelper.print(CliMessages.VAGRANT_BOX_CHECK, notUi);
                VagrantHelper.constructVagrantHelper().vagrantOldBoxesCheck();
                LogHelper.print(CliMessages.DONE, notUi);
                LogHelper.print(CliMessages.NODE_INST_SPACE_CHECK, notUi);
                HardwareHelper.checkUsableSpaceInstallNode(vboxFolder);
                LogHelper.print(CliMessages.DONE, notUi);
            } catch (GexException e) {
                if (PropertiesHelper.isUI()) {
                    CliHelper.printErrMessageForUi(e);
                } else {
                    throw e;
                }
                return;
            }
        }
        LogHelper.print("{}", PropertiesHelper.isUI());
    }

    private static NetworkAdapter selectAdapter(List<NetworkAdapter> networkAdapters) throws GexException {
        Columns columns = new Columns().addLine(CliMessages.NUMBER_SIGN, "Network adapter");
        for (int i = 0; i < networkAdapters.size(); i++) {
            columns.addLine(String.valueOf(i + 1), networkAdapters.get(i).getName());
        }
        columns.print();
        System.out.println();
        LogHelper.print(CliMessages.SELECT_ADAPTER);
        int selectedAdapterNum = -1;
        while (selectedAdapterNum == -1) {
            String input = System.console().readLine();
            try {
                int tmp = Integer.parseInt(input);
                if (tmp < 1 || tmp > networkAdapters.size())
                    throw new NumberFormatException();
                selectedAdapterNum = tmp - 1;
            } catch (NumberFormatException e) {
                System.out.println();
                LogHelper.print(CliMessages.INVALID_INPUT_TRY);
            }
        }
        System.out.println("Selected: " + networkAdapters.get(selectedAdapterNum).getName());
        if (networkAdapters.get(selectedAdapterNum).getWifi()) {
            LogHelper.print(CliMessages.WIFI_ADAPTER_CHOSEN);
        }
        return networkAdapters.get(selectedAdapterNum);
    }

    private static void printNodeList(List<Node> nodes) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CollectionUtils.isEmpty(nodes)) {
            LogHelper.print(CliMessages.NODE_EMPTY);
            return;
        }
        for (Node node : nodes) {
            System.out.println(CliMessages.DELIMITER);
            printNode(node);
            System.out.println(CliMessages.DELIMITER);
        }
    }

    private static void printNode(Node node) throws GexException {
        if (node == null) {
            return;
        }
        Columns columns = new Columns();
        if (StringUtils.isNotBlank(node.getId())) {
            columns.addLine(CliMessages.ID, node.getId());
        }
        if (StringUtils.isNotBlank(node.getName())) {
            columns.addLine(CliMessages.NAME, node.getName());
        }
        if (node.getNodeNumber() != null) {
            columns.addLine(CliMessages.NUMBER, node.getNodeNumber().toString());
        }
        if (StringUtils.isNotBlank(node.getIp())) {
            columns.addLine(CliMessages.IP, node.getIp());
        }
        if (StringUtils.isNotBlank(node.getHost())) {
            columns.addLine(CliMessages.HOST, node.getHost());
        }
        if (node.getPort() != null) {
            columns.addLine(CliMessages.PORT, node.getPort().toString());
        }
        if (StringUtils.isNotBlank(node.getClusterId())) {
            columns.addLine(CliMessages.CLUSTER_ID, node.getClusterId());
        }
        if (node.getHostType() != null) {
            columns.addLine(CliMessages.HOST_TYPE, node.getHostType().getName());
        }
        if (StringUtils.isNotBlank(node.getHadoopType())) {
            columns.addLine(CliMessages.HADOOP_TYPE, node.getHadoopType());
        }
        if (node.getState() != null) {
            columns.addLine(CliMessages.NODE_STATE, node.getState().toString().toLowerCase());
        }
        if (node.getStatus() != null) {
            columns.addLine(CliMessages.NODE_STATUS, node.getStatus().toString().toLowerCase());
            if (node.getStatusChanged() != null) {
                columns.addLine(CliMessages.NODE_STATUS_CHANGED, localDateTimeToString(node.getStatusChanged(),
                        DATE_TIME_FORMAT));
            }
        }
        if (node.getCounters() != null) {
            NodeCounters counters = node.getCounters();
            if (counters.getCPU() != null) {
                columns.addLine(CliMessages.CPU, counters.getCPU().toString());
            }
            if (counters.getMemory() != null) {
                columns.addLine(CliMessages.MEMORY, counters.getMemory().toString());
            }
        }
        columns.print();
    }

}
