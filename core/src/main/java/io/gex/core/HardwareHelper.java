package io.gex.core;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.gex.core.exception.ExceptionHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.NetworkAdapter;
import io.gex.core.rest.NodeLevelRest;
import io.gex.core.shell.Commands;
import io.gex.core.shell.ShellExecutor;
import io.gex.core.shell.ShellParameters;
import io.gex.core.vagrantHelper.VagrantHelper;
import io.gex.core.virutalBoxHelper.VirtualBoxHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import oshi.hardware.NetworkIF;
import oshi.json.SystemInfo;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.defaultString;

public class HardwareHelper {

    private final static LogWrapper logger = LogWrapper.create(HardwareHelper.class);
    private static String hostname;

    public static void checkVirtualizationEnabled() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (SystemUtils.IS_OS_WINDOWS_7 || SystemUtils.IS_OS_WINDOWS_2008) {
            NodeLevelRest.sendWebServerRequest("/virtualization");
        } else {
            checkVirtualizationEnabledInternal();
        }
    }

    /**
     * Internal check. Use checkVirtualizationEnabled()
     */
    public static void checkVirtualizationEnabledInternal() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (SystemUtils.IS_OS_WINDOWS) {
            String installPath = WindowsInstallInfoHelper.getInstallationPath();
            if (SystemUtils.IS_OS_WINDOWS_7 || SystemUtils.IS_OS_WINDOWS_2008) {
                File elevatePath = new File(installPath + "/usr/lib/gex/elevate.exe");
                File havToolPath = new File(installPath + "/usr/lib/gex/havtool/havtool.exe");
                String checkIfSupported = ShellExecutor.executeCommandOutput(ShellParameters.newBuilder(
                        Commands.cmd("\"" + elevatePath.getAbsolutePath() + "\"" + " -w " + "\"" +
                                havToolPath.getAbsolutePath() + "\"" + " /q && echo %errorlevel%")).setCheckExitCode(false).build());

                if (StringUtils.trimToEmpty(checkIfSupported).equals("0")) {
                    return;
                } else if (StringUtils.trimToEmpty(checkIfSupported).equals("1")) {
                    throw logger.logAndReturnException(CoreMessages.VIRTUALIZATION_NOT_SUPPORTED, LogType.CHECK_VIRTUALIZATION);
                } else if (StringUtils.trimToEmpty(checkIfSupported).equals("2")) {
                    throw logger.logAndReturnException(CoreMessages.VIRTUALIZATION_NOT_ENABLED, LogType.CHECK_VIRTUALIZATION);
                } else {
                    throw logger.logAndReturnException(CoreMessages.VIRTUALIZATION_UNEXP_EXCEPTION + (StringUtils.isBlank(checkIfSupported)
                            ? "" : ": " + checkIfSupported), LogType.CHECK_VIRTUALIZATION);
                }
            } else {
                File coreInfoPath = new File(installPath + "/usr/lib/gex/Coreinfo/Coreinfo.exe");
                String checkIfSupported = ShellExecutor.executeCommandOutput(ShellParameters.newBuilder(
                        Commands.cmd("\"" + coreInfoPath.getAbsolutePath() + "\"" + " -f /accepteula")).build());
                if (Pattern.compile("(VMX|SVM)\\s+\\*").matcher(checkIfSupported).find()) {
                    String checkIfEnabled = ShellExecutor.executeCommandOutput(ShellParameters.newBuilder(Commands.cmd(
                            "wmic cpu get VirtualizationFirmwareEnabled")).setCheckExitCode(false).build());
                    if (StringUtils.containsIgnoreCase(checkIfEnabled, "true")) {
                        return;
                    } else {
                        throw logger.logAndReturnException(CoreMessages.VIRTUALIZATION_NOT_ENABLED, LogType.CHECK_VIRTUALIZATION);
                    }
                } else {
                    throw logger.logAndReturnException(CoreMessages.VIRTUALIZATION_NOT_SUPPORTED, LogType.CHECK_VIRTUALIZATION);
                }
            }
        }

        if (SystemUtils.IS_OS_MAC) {
            String checkIfSupported = ShellExecutor.executeCommandOutput(ShellParameters.newBuilder(Commands.bash(
                    "sysctl -a | grep machdep.cpu.features")).setCheckExitCode(false).build());
            if (!StringUtils.containsIgnoreCase(checkIfSupported, "vmx")) {
                //if supported by proc it enables by default on mac
                throw logger.logAndReturnException(CoreMessages.VIRTUALIZATION_NOT_SUPPORTED, LogType.CHECK_VIRTUALIZATION);
            }
            return;
        }

        if (SystemUtils.IS_OS_LINUX) {
            if (PropertiesHelper.isService()) {
                throw logger.logAndReturnException(CoreMessages.COMMAND_DEPRECATED_ON_SERVICE, LogType.CHECK_VIRTUALIZATION);
            }
            // should not be executed under Gexd
            String checkIfSupported = ShellExecutor.executeCommandOutput(ShellParameters.newBuilder(Commands.bash(
                    (PropertiesHelper.isCLI() ? "sudo" : (BaseHelper.isCentos() ? "beesu" :
                            "gksudo -m \"ClusterGX requires admin password to check virtualization.\""))
                            + " sh /usr/lib/gex/checkVirtualization.sh")).setCheckExitCode(false).build());

            if (checkIfSupported.contains("Hardware acceleration can be used")) {
                return;
            } else if (checkIfSupported.contains("Your CPU does not support virtualization")) {
                throw logger.logAndReturnException(CoreMessages.VIRTUALIZATION_NOT_SUPPORTED, LogType.CHECK_VIRTUALIZATION);
            } else if (checkIfSupported.contains("is disabled by your BIOS")) {
                throw logger.logAndReturnException(CoreMessages.VIRTUALIZATION_NOT_ENABLED, LogType.CHECK_VIRTUALIZATION);
            } else {
                throw logger.logAndReturnException(CoreMessages.VIRTUALIZATION_UNEXP_EXCEPTION + (StringUtils.isBlank(checkIfSupported)
                        ? "" : ": " + checkIfSupported), LogType.CHECK_VIRTUALIZATION);
            }
        }

        throw logger.logAndReturnException(CoreMessages.UNSUPPORTED_OS, LogType.CHECK_VIRTUALIZATION);
    }

    public static void checkNetworkConnection() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (StringUtils.isBlank(getBestIPV4())) {
            throw logger.logAndReturnException(CoreMessages.NETWORK_CHECK, LogType.CONNECTION_ERROR);
        }
    }

    public static void checkGexdIsRunning() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        // check that gexd is running
        if (SystemUtils.IS_OS_WINDOWS) {
            List<String> cmd = Collections.singletonList(Paths.get(System.getenv("windir"), "System32", "tasklist.exe")
                    .toString());
            String pidInfo = ShellExecutor.executeCommandOutput(ShellParameters.newBuilder(cmd).setLogOutput(false).build());
            if (!pidInfo.contains("gexd.exe")) {
                throw logger.logAndReturnException(CoreMessages.GEXD_IS_NOT_RUNNING, LogType.CHECK_GEXD);
            }
        } else {
            List<String> cmd = Commands.bash("ps aux | grep [g]exd");
            Process p = ShellExecutor.getExecutionProcess(ShellParameters.newBuilder(cmd).build());
            try {
                if (p.waitFor() != 0) {
                    throw logger.logAndReturnException(SystemUtils.IS_OS_LINUX ?
                            CoreMessages.GEXD_IS_NOT_RUNNING_LINUX : CoreMessages.GEXD_IS_NOT_RUNNING, LogType.CHECK_GEXD);
                }
            } catch (InterruptedException e) {
                throw logger.logAndReturnException(CoreMessages.GEXD_CHECK_ERROR, e, LogType.CHECK_GEXD);
            }
        }

        // check that gexd web server is running
        try {
            NodeLevelRest.sendWebServerRequest("/itsalive");
        } catch (GexException e) {
            throw logger.logAndReturnException(CoreMessages.GEXD_WEB_SERVER_IS_NOT_RUNNING, LogType.CHECK_GEXD);
        }
    }


    public static String getBestIPV4() throws GexException {
        String iface = getBestIPV4(false); // try without WiFi first
        if (iface == null) {
            return getBestIPV4(true); // try with WiFi then
        }
        return iface;
    }


    private static String getBestIPV4(boolean useWiFi) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
            while (ni.hasMoreElements()) {
                NetworkInterface nextElement = ni.nextElement();
                String ifname = nextElement.getName().toLowerCase();
                if ((!ifname.startsWith("e") &&
                        (!useWiFi || !ifname.startsWith("w"))) ||
                        !nextElement.isUp() || nextElement.isVirtual() ||
                        nextElement.isLoopback() || nextElement.isPointToPoint() ||
                        StringUtils.containsIgnoreCase(nextElement.getDisplayName(), "virtual")) {
                    continue;
                }
                byte[] hardwareAddress = nextElement.getHardwareAddress();
                if (hardwareAddress == null || hardwareAddress.length == 0) {
                    continue;
                }
                logger.logInfo(nextElement.getName(), LogType.HARDWARE_INFO);
                Enumeration<InetAddress> addresses = nextElement.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address) {
                        logger.logInfo(address.getHostAddress(), LogType.HARDWARE_INFO);
                        return address.getHostAddress();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.HARDWARE_INFO_ERROR, e, LogType.HARDWARE_INFO_ERROR);
        }
    }

    private static JsonArray getNetworkInterfaces() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonArray netInterfaces = new JsonArray();
        try {
            NetworkIF[] networkIFS = new oshi.SystemInfo().getHardware().getNetworkIFs();
            for (NetworkIF ni : networkIFS) {
                NetworkInterface baseNetInterface = ni.getNetworkInterface();
                JsonObject jsonNi = new JsonObject();
                jsonNi.addProperty("name", ni.getName());
                jsonNi.addProperty("displayName", ni.getDisplayName());
                jsonNi.addProperty("mac", ni.getMacaddr());
                jsonNi.addProperty("isUp", baseNetInterface.isUp());
                jsonNi.addProperty("speed", ni.getSpeed());
                jsonNi.addProperty("mtu", ni.getMTU());
                jsonNi.addProperty("loopback", baseNetInterface.isLoopback());
                jsonNi.addProperty("pointToPoint", baseNetInterface.isPointToPoint());
                jsonNi.addProperty("multicast", baseNetInterface.supportsMulticast());
                JsonArray address = new JsonArray();
                for (InterfaceAddress i : baseNetInterface.getInterfaceAddresses()) {
                    JsonObject addressUnit = new JsonObject();
                    addressUnit.addProperty("hostAddress", i.getAddress().getHostAddress());
                    addressUnit.addProperty("prefixLength", i.getNetworkPrefixLength());
                    address.add(addressUnit);
                }
                jsonNi.add("addresses", address);
                netInterfaces.add(jsonNi);
            }
        } catch (Exception e) {
            throw logger.logAndReturnException(CoreMessages.HARDWARE_INFO_ERROR, e, LogType.HARDWARE_INFO_ERROR);
        }
        return netInterfaces;
    }

    private static String getHostname() throws GexException {
        if (StringUtils.isBlank(hostname)) {
            try {
                hostname = SystemUtils.IS_OS_LINUX ? FileUtils.readFileToString(new File("/etc/hostname"), UTF_8).trim()
                        : InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                throw logger.logAndReturnException(e, LogType.HARDWARE_INFO_ERROR);
            }
        }
        return hostname;
    }

    public static JsonObject getNodeInfo() {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject info = new JsonObject();
        try {
            info.addProperty("hostName", getHostname());
        } catch (Exception e) {
            logger.logError("Cannot get computer hostname", e, LogType.HARDWARE_INFO_ERROR);
        }
        try {
            info.addProperty("osName", System.getProperty("os.name"));
        } catch (Exception e) {
            logger.logError("Cannot get computer os name", e, LogType.HARDWARE_INFO_ERROR);
        }
        try {
            info.addProperty("osVersion", System.getProperty("os.version"));
        } catch (Exception e) {
            logger.logError("Cannot get computer os version", e, LogType.HARDWARE_INFO_ERROR);
        }
        try {
            info.add("networkInterfaces", getNetworkInterfaces());
        } catch (Exception e) {
            logger.logError("Cannot get network interfaces", e, LogType.HARDWARE_INFO_ERROR);
        }
        return info;
    }

    public static List<NetworkAdapter> getBestNetworkAdapters() throws GexException {
        List<NetworkAdapter> networkAdapters = getNetworkAdapters(false);
        if (CollectionUtils.isEmpty(networkAdapters)) {
            networkAdapters = getNetworkAdapters(true);
            if (CollectionUtils.isEmpty(networkAdapters)) {
                throw logger.logAndReturnException("Active network interface not found", LogType.HARDWARE_INFO_ERROR);
            }
        }
        return networkAdapters;
    }


    private static List<NetworkAdapter> getNetworkAdapters(boolean useWiFi) throws GexException {
        final String SOCKET_MESSAGE = "Error when check wired interface: ";
        final String GOT_LIST_OF_CONNECTIONS = "Got list of all connections";
        final String ERROR_GETTING_CONNECTIONS = "Error getting list of all connections";

        final List<NetworkAdapter> networkAdapters = new ArrayList<>();

        if (SystemUtils.IS_OS_LINUX) {
            try {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = networkInterfaces.nextElement();
                    String netName = networkInterface.getName().toLowerCase();
                    if ((!useWiFi && netName.startsWith("e") || useWiFi && netName.startsWith("w"))
                            && networkInterface.isUp()) {
                        networkAdapters.add(new NetworkAdapter(netName, useWiFi));
                    }
                }
            } catch (SocketException e) {
                throw logger.logAndReturnException(SOCKET_MESSAGE + e.getMessage(), e, LogType.CONNECTION_ERROR);
            }
        } else if (SystemUtils.IS_OS_MAC) {
            String result = ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.bash(
                    "networksetup -listallhardwareports")).setMessageSuccess(GOT_LIST_OF_CONNECTIONS).
                    setMessageError(ERROR_GETTING_CONNECTIONS).setPrintOutput(false).build());

            Matcher matcher = useWiFi ? Pattern.compile("(?<=Hardware Port: Wi-Fi\nDevice: )\\w+(?=\n)").matcher(result)
                    : Pattern.compile("(?<=Hardware Port: Ethernet\nDevice: )\\w+(?=\n)").matcher(result);
            List<String> networkInterfaceNames = new ArrayList<>();
            while (matcher.find()) {
                networkInterfaceNames.add(matcher.group());
            }

            try {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = networkInterfaces.nextElement();
                    if (networkInterfaceNames.contains(networkInterface.getName()) && networkInterface.isUp()) {
                        networkAdapters.add(new NetworkAdapter(networkInterface.getName(), useWiFi));
                    }
                }
            } catch (SocketException e) {
                throw logger.logAndReturnException(SOCKET_MESSAGE + e.getMessage(), e, LogType.CONNECTION_ERROR);
            }
        } else if (SystemUtils.IS_OS_WINDOWS) {
            String connectionsListStr = ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.cmd(
                    "chcp 437 && ipconfig /all")).setMessageSuccess(GOT_LIST_OF_CONNECTIONS).
                    setMessageError(ERROR_GETTING_CONNECTIONS).setPrintOutput(false).build());

            Matcher matcher = useWiFi ? Pattern.compile("(?<=Wireless LAN adapter ).+(?=\\:)").matcher(connectionsListStr)
                    : Pattern.compile("(?<=Ethernet adapter ).+(?=\\:)").matcher(connectionsListStr);

            List<String> networkConnectionIds = new ArrayList<>();
            while (matcher.find()) {
                networkConnectionIds.add(matcher.group());
            }

            String connectionsAvailable = ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.cmd(
                    "wmic nic get AdapterTypeId, Name, NetConnectionID, NetConnectionStatus, ServiceName /format:csv")).
                    setMessageSuccess("Check connection availability").setMessageError("Error checking connection availability").
                    setPrintOutput(false).build());
            List<CSVRecord> csvRecords;
            try {
                csvRecords = CSVParser.parse(connectionsAvailable, CSVFormat.DEFAULT).getRecords();
            } catch (IOException e) {
                throw logger.logAndReturnException("Cannot parse check connection command output: " + e.getMessage(), e,
                        LogType.CONNECTION_ERROR);
            }

            boolean hypervEthEnabled = csvRecords.stream().anyMatch(record -> "VMSMP".equals(record.get(5)));
            CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
            boolean nonAsciiEncoding = networkConnectionIds.stream().anyMatch(intName -> !encoder.canEncode(intName));

            if (nonAsciiEncoding) { //because of encoding issue in vagrant
                networkAdapters.add(new NetworkAdapter("unknown", false));
                return networkAdapters;
            }

            if (hypervEthEnabled) {
                for (CSVRecord record : csvRecords) {
                    if ("0".equals(record.get(1)) && "2".equals(record.get(4))) {
                        networkAdapters.add(new NetworkAdapter(record.get(2), useWiFi));
                    }
                }
            } else {
                for (CSVRecord record : csvRecords) {
                    if ("2".equals(record.get(4)) && !"VBoxNetAdp".equals(record.get(5)) && !"VMnetAdapter".equals(record.get(5))
                            && networkConnectionIds.contains(record.get(3))) {
                        networkAdapters.add(new NetworkAdapter(record.get(2), useWiFi));
                    }
                }
            }

            nonAsciiEncoding = networkAdapters.stream().anyMatch(adp -> !encoder.canEncode(adp.getName()));
            if (nonAsciiEncoding) { //because of encoding issue in vagrant
                networkAdapters.clear();
                networkAdapters.add(new NetworkAdapter("unknown", false));
            }
        }

        return networkAdapters;
    }

    public static void checkHypervEnabled() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());

        if (SystemUtils.IS_OS_WINDOWS) {
            NodeLevelRest.sendWebServerRequest("/hyperv/check");
        }
    }

    public static void checkHypervEnabledInternal() throws GexException {
        final String GOT_LIST_OF_WINDOWS_FEATURES = "Got list of windows features";
        final String ERROR_GETTING_LIST_OF_WINDOWS_FEATURES = "Got list of windows features";
        if (SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_WINDOWS_7 && !SystemUtils.IS_OS_WINDOWS_2008) {
            String featuresList = ShellExecutor.executeCommandOutputWithLog(ShellParameters.newBuilder(Commands.cmd(
                    "dism /online /get-features /English /format:table")).setMessageSuccess(GOT_LIST_OF_WINDOWS_FEATURES).
                    setMessageError(ERROR_GETTING_LIST_OF_WINDOWS_FEATURES).setPrintOutput(false).build());
            if (Pattern.compile("Microsoft-Hyper-V-All\\s+\\|\\s+Enabled").matcher(featuresList).find()) {
                throw logger.logAndReturnException(CoreMessages.HYPERV_ENABLED, LogType.CHECK_HYPERV);
            }
        }
    }

    public static void checkUsableSpaceInstallDependencies() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if ((SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC)
                && (!VirtualBoxHelper.constructVirtualBoxHelper().isInstalled() || !VagrantHelper.constructVagrantHelper().isInstalled())) {
            long dependenciesSize = SystemUtils.IS_OS_WINDOWS ? 314572800L : 209715200L; //300 mb or 200mb
            long tempDirUsableSpace = FileUtils.getTempDirectory().getUsableSpace();
            if (tempDirUsableSpace < dependenciesSize) {
                String message = "You don't have enough space in your temporary folder to download dependencies. You have "
                        + FileUtils.byteCountToDisplaySize(tempDirUsableSpace) + " free space but need "
                        + FileUtils.byteCountToDisplaySize(dependenciesSize) + ".";
                throw logger.logAndReturnException(message, LogType.HARDWARE_INFO_ERROR);
            }

            long dependenciesInstSize = 1073741824L; //1gb
            File installFolder = SystemUtils.IS_OS_WINDOWS ? new File(System.getenv("ProgramFiles")) : new File("/Library");
            long installFolderSpace = installFolder.getUsableSpace();
            if (installFolderSpace < dependenciesInstSize) {
                String message = "You don't have enough space to install dependencies. You have "
                        + FileUtils.byteCountToDisplaySize(installFolderSpace) + " free space but need "
                        + FileUtils.byteCountToDisplaySize(dependenciesInstSize) + ".";
                throw logger.logAndReturnException(message, LogType.HARDWARE_INFO_ERROR);
            }
        }
    }

    public static void checkUsableSpaceInstallNode(File vboxFolder) throws GexException { //todo create check for server mode
        logger.trace("Entered " + LogHelper.getMethodName());
        final long boxArchivedSize = 3221225472L; //3gb
        final long unpackedBoxSize = 10737418240L; //10gb
        final long vagrantBoxSize = 3221225472L;  //3gb

        Path vagrantHomePath = Paths.get(VagrantHelper.vagrantHome);
        File userHome = FileUtils.getUserDirectory();
        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
            boolean vagrantHomeInUserHome = vagrantHomePath.startsWith(userHome.getPath());
            if (vagrantHomeInUserHome) {
                long freeSpace = userHome.getUsableSpace();
                long needSpace = boxArchivedSize + unpackedBoxSize + vagrantBoxSize;
                if (freeSpace < needSpace) {
                    String message = "You don't have enough space to install node. You have "
                            + FileUtils.byteCountToDisplaySize(freeSpace) + " free space but need "
                            + FileUtils.byteCountToDisplaySize(needSpace) + ".";
                    throw logger.logAndReturnException(message, LogType.HARDWARE_INFO_ERROR);
                }
            } else {
                long freeSpaceHome = userHome.getUsableSpace();
                long needSpaceHome = boxArchivedSize + unpackedBoxSize;
                if (freeSpaceHome < needSpaceHome) {
                    String message = "You don't have enough space to install node. You have "
                            + FileUtils.byteCountToDisplaySize(freeSpaceHome) + " free space but need "
                            + FileUtils.byteCountToDisplaySize(needSpaceHome) + ".";
                    throw logger.logAndReturnException(message, LogType.HARDWARE_INFO_ERROR);
                }

                long freeSpaceVagrant = vagrantHomePath.toFile().getUsableSpace();
                if (freeSpaceVagrant < vagrantBoxSize) {
                    String message = "You don't have enough space in VAGRANT_HOME to install node. You have "
                            + FileUtils.byteCountToDisplaySize(freeSpaceVagrant) + " free space but need "
                            + FileUtils.byteCountToDisplaySize(vagrantBoxSize) + ".";
                    throw logger.logAndReturnException(message, LogType.HARDWARE_INFO_ERROR);
                }
            }
        } else if (SystemUtils.IS_OS_WINDOWS) {
            Map<String, Long> needSpace = new HashMap<>();
            needSpace.put(FilenameUtils.getPrefix(userHome.getAbsolutePath()), boxArchivedSize);
            String vagrantHomeDisk = FilenameUtils.getPrefix(vagrantHomePath.toFile().getAbsolutePath());
            needSpace.put(vagrantHomeDisk, needSpace.getOrDefault(vagrantHomeDisk, 0L) + vagrantBoxSize);
            String vBoxDisk = FilenameUtils.getPrefix(vboxFolder.getAbsolutePath());
            needSpace.put(vBoxDisk, needSpace.getOrDefault(vBoxDisk, 0L) + unpackedBoxSize);

            for (Map.Entry<String, Long> entry : needSpace.entrySet()) {
                long freeSpace = new File(entry.getKey()).getUsableSpace();
                if (freeSpace < entry.getValue()) {
                    String message = "You don't have enough space on disk " + entry.getKey() + " to install node. You have "
                            + FileUtils.byteCountToDisplaySize(freeSpace) + " free space but need "
                            + FileUtils.byteCountToDisplaySize(entry.getValue()) + ".";
                    throw logger.logAndReturnException(message, LogType.HARDWARE_INFO_ERROR);
                }
            }
        }
    }

    public static JsonObject getComputerInfo() {
        logger.trace("Entered " + LogHelper.getMethodName());
        JsonObject result = new JsonObject();

        JsonObject baseInfo = new JsonObject();
        try {
            baseInfo.add("systemInfo", getSystemInfo());
        } catch (Exception e) {
            baseInfo.add("error", createError(e));
        }
        result.add("baseInfo", baseInfo);

        JsonObject vbox = new JsonObject();
        try {
            VirtualBoxHelper virtualBoxHelper = VirtualBoxHelper.constructVirtualBoxHelper();
            vbox.addProperty("version", virtualBoxHelper.getVersion());
        } catch (Exception e) {
            vbox.add("error", createError(e));
        }
        result.add("vbox", vbox);


        JsonObject vagrant = new JsonObject();
        try {
            VagrantHelper vagrantHelper = VagrantHelper.constructVagrantHelper();
            vagrant.addProperty("version", vagrantHelper.getVersion());
        } catch (Exception e) {
            vagrant.add("error", createError(e));
        }
        result.add("vagrant", vagrant);

        return result;
    }

    private static JsonObject createError(Exception ex) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", ex.getMessage());
        jsonObject.addProperty("stackTrace", ExceptionHelper.getStackTraceString(ex));
        return jsonObject;
    }

    @SuppressWarnings("deprecation")
    private static JsonObject getSystemInfo() throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        final int systemInfoExecTime = 5;

        FutureTask<JsonObject> futureTask = new FutureTask<>(() -> { //need another thread because sometimes on linux getDisks info crashes on linux
            SystemInfo systemInfo = new SystemInfo();
            Properties properties = new Properties();
            properties.put("operatingSystem.processes", "false");
            properties.put("hardware.displays", "false");
            properties.put("hardware.usbDevices", "false");
            properties.put("hardware.sensors", "false");

            String sysInfoJson = systemInfo.toJSON(properties).toString();
            try {
                return new JsonParser().parse(sysInfoJson).getAsJsonObject();
            } catch (Exception e) {
                throw logger.logAndReturnException("Cannot parse system info: " + e.getMessage() + " JSON: " +
                        sysInfoJson, LogType.HARDWARE_INFO_ERROR);
            }
        });

        Thread checkSystemInfoThread = new Thread(futureTask);
        checkSystemInfoThread.start();

        try {
            return futureTask.get(systemInfoExecTime, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            checkSystemInfoThread.interrupt();
            throw logger.logAndReturnException("Get system info execution stuck. " + defaultString(e.getMessage()), e, LogType.HARDWARE_INFO_ERROR);
        } catch (InterruptedException e) {
            throw logger.logAndReturnException("Get system info is interrupted. " + defaultString(e.getMessage()), e, LogType.HARDWARE_INFO_ERROR);
        } catch (ExecutionException e) {
            throw logger.logAndReturnException("Cannot get system info. " + defaultString(e.getMessage()), e, LogType.HARDWARE_INFO_ERROR);
        } finally {
            try {
                checkSystemInfoThread.join(1000);
            } catch (InterruptedException e) {
                //do nothing
            }
            if (checkSystemInfoThread.isAlive()) {
                checkSystemInfoThread.stop(); // need to force stopping thread
            }
        }
    }

    public static void checkMinimumCpuCoresCount() throws GexException {
        oshi.SystemInfo systemInfo = new oshi.SystemInfo();
        int minimumCpuCores = 2;
        int currentCpuCores = systemInfo.getHardware().getProcessor().getLogicalProcessorCount();
        if (currentCpuCores < minimumCpuCores) {
            throw logger.logAndReturnException("You don't have enough CPU cores to install node. You have "
                    + currentCpuCores + " CPU cores but need " + minimumCpuCores + ".", LogType.HARDWARE_INFO_ERROR);
        }
    }

    public static void checkMinimumRamCount() throws GexException {
        oshi.SystemInfo systemInfo = new oshi.SystemInfo();
        long minimumRamSize = 4294967296L; //4gb
        long currentRamSize = systemInfo.getHardware().getMemory().getTotal();
        if (currentRamSize < minimumRamSize) {
            throw logger.logAndReturnException("You don't have enough memory to install node. You have "
                    + FileUtils.byteCountToDisplaySize(currentRamSize) + " memory but need "
                    + FileUtils.byteCountToDisplaySize(minimumRamSize) + ".", LogType.HARDWARE_INFO_ERROR);
        }
    }
}
