package io.gex.core.shell;

import io.gex.core.BaseHelper;
import io.gex.core.CoreMessages;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public class ShellExecutor {

    private final static LogWrapper logger = LogWrapper.create(ShellExecutor.class);
    private final static int bufferSize = 512;

    public static Process getExecutionProcess(ShellParameters params) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (CollectionUtils.isEmpty(params.getCmd())) {
            throw logger.logAndReturnException(CoreMessages.EMPTY_COMMAND, LogType.SHELL_ERROR);
        }
        if (params.isPrintCommand()) {
            logger.logInfo(CoreMessages.EXECUTE_COMMAND + params.getCmdString(), LogType.SHELL);
        }
        try {
            ProcessBuilder ps = new ProcessBuilder(params.getCmd());
            if (StringUtils.isNotBlank(params.getDir())) {
                ps.directory(new File(params.getDir()));
                logger.logInfo(CoreMessages.IN_DIR + params.getDir(), LogType.SHELL);
            }
            if (MapUtils.isNotEmpty(params.getNewEnv())) {
                Map<String, String> env = ps.environment();
                env.putAll(params.getNewEnv());
            }
            if (params.isRedirectErrorStream()) {
                ps.redirectErrorStream(true);
            }
            return ps.start();
        } catch (Exception e) {
            throw logger.logAndReturnException(params.isPrintCommand() ?
                    CoreMessages.getExecutionCommandMessage(params.getCmdString()) : CoreMessages.SHELL_ERROR, e, LogType.SHELL_ERROR);
        }
    }

    public static void executeCommand(ShellParameters params) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Process p = getExecutionProcess(params);
        StringBuilder output = new StringBuilder();
        try {
            InputStream in = p.getInputStream();
            byte[] buffer = new byte[bufferSize];
            if (in != null) {
                int i;
                while ((i = in.read(buffer, 0, bufferSize)) >= 0) {
                    if (i > 0) {
                        String line = new String(buffer, 0, i, BaseHelper.getConsoleEncoding());
                        output.append(line).append("\n");
                        logger.logInfo(line, LogType.SHELL);
                        if (params.isPrintOutput()) {
                            System.out.print(line);
                        }
                    }
                }
            }
            if (p.waitFor() != 0 && params.isCheckExitCode()) {
                throw new InterruptedException(CoreMessages.NON_ZERO);
            }
        } catch (Exception e) {
            throw logger.logAndReturnException((params.isPrintCommand() ? CoreMessages.getExecutionCommandMessage(params.getCmdString()) :
                    CoreMessages.SHELL_ERROR) + (StringUtils.isBlank(output) ? "" : ":\n" + output), e, LogType.SHELL_ERROR);
        }
    }

    public static String executeCommandOutputWithLog(ShellParameters params) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Process p = getExecutionProcess(params);
        StringBuilder output = new StringBuilder();
        try {
            InputStream in = p.getInputStream();
            byte[] buffer = new byte[bufferSize];
            if (in != null) {
                int i;
                while ((i = in.read(buffer, 0, bufferSize)) >= 0) {
                    if (i > 0) {
                        String line = new String(buffer, 0, i, BaseHelper.getConsoleEncoding());
                        output.append(line);
                        logger.logInfo(line, LogType.SHELL);
                        if (params.isPrintOutput()) {
                            System.out.print(line);
                        }
                    }
                }
            }
            if (p.waitFor() != 0 && params.isCheckExitCode()) {
                throw new InterruptedException(CoreMessages.NON_ZERO);
            }
            logger.logInfo(params.getMessageSuccess(), params.getTypeSuccess() == null ? LogType.SHELL : params.getTypeSuccess());
            return output.toString();
        } catch (Exception e) {
            throw logger.logAndReturnException(params.getMessageError() + "\n" + (params.isPrintCommand() ?
                    CoreMessages.getExecutionCommandMessage(params.getCmdString()) : CoreMessages.SHELL_ERROR) +
                    (StringUtils.isBlank(output) ? "" : ":\n" + output), e, params.getTypeError() == null ? LogType.SHELL : params.getTypeError());
        }
    }

    //todo replace this method ?
    public static String executeCommandOutput(ShellParameters params) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Process process = ShellExecutor.getExecutionProcess(params);
        String resString = StringUtils.EMPTY;
        StringBuilder output = new StringBuilder();
        try {
            InputStream in = process.getInputStream();
            byte[] buffer = new byte[bufferSize];
            if (in != null) {
                int i;
                while ((i = in.read(buffer, 0, bufferSize)) >= 0) {
                    if (i > 0) {
                        String line = new String(buffer, 0, i, BaseHelper.getConsoleEncoding());
                        if (params.isLogOutput()) {
                            logger.logInfo(line, LogType.SHELL);
                        }
                        output.append(line);
                    }
                }
                resString = output.toString();
            }
            if (process.waitFor() != 0 && params.isCheckExitCode()) {
                throw new InterruptedException(CoreMessages.NON_ZERO);
            }
            return resString;
        } catch (Exception e) {
            throw logger.logAndReturnException((params.isPrintCommand() ? CoreMessages.getExecutionCommandMessage(params.getCmdString()) :
                    CoreMessages.SHELL_ERROR) + (StringUtils.isBlank(output) ? "" : ":\n" + output), e, LogType.SHELL_ERROR);
        }
    }

    // only for getConsoleEncoding()
    public static String executeCommandOutputNoEncoding(ShellParameters params) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Process process = ShellExecutor.getExecutionProcess(params);
        String resString = StringUtils.EMPTY;
        StringBuilder output = new StringBuilder();
        try {
            InputStream in = process.getInputStream();
            byte[] buffer = new byte[bufferSize];
            if (in != null) {
                int i;
                while ((i = in.read(buffer, 0, bufferSize)) >= 0) {
                    if (i > 0) {
                        output.append(new String(buffer, 0, i));
                    }
                }
                resString = output.toString();
                if (StringUtils.isNotBlank(resString)) {
                    logger.logInfo(resString, LogType.SHELL);
                }
            }
            if (process.waitFor() != 0) {
                throw new InterruptedException(CoreMessages.NON_ZERO);
            }
            return resString;
        } catch (Exception e) {
            throw logger.logAndReturnException((params.isPrintCommand() ? CoreMessages.getExecutionCommandMessage(params.getCmdString()) :
                    CoreMessages.SHELL_ERROR) + (StringUtils.isBlank(output) ? "" : ":\n" + output), e, LogType.SHELL_ERROR);
        }
    }

    public static boolean waitProcessFinished(Process process) {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (process.getInputStream() != null) {
            byte[] buffer = new byte[1024];
            try {
                while (process.getInputStream().read(buffer, 0, 1024) >= 0) {
                }
            } catch (Exception e) {
                logger.logError(e, LogType.SHELL_ERROR);
            }
        }
        int result;
        try {
            result = process.waitFor();
        } catch (Exception e) {
            logger.logError(e, LogType.SHELL_ERROR);
            return false;
        }
        return result == 0;
    }
}
