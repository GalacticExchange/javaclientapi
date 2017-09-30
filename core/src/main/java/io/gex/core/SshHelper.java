package io.gex.core;


import com.jcraft.jsch.*;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.SshCredentials;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Properties;

public class SshHelper {
    private static final LogWrapper logger = LogWrapper.create(SshHelper.class);

    public static class SshResult {
        private String out;
        private String err;
        private int exitCode;

        public SshResult(String out, String err, int exitCode) {
            this.out = out;
            this.err = err;
            this.exitCode = exitCode;
        }

        public String getOut() {
            return out;
        }

        public void setOut(String out) {
            this.out = out;
        }

        public String getErr() {
            return err;
        }

        public void setErr(String err) {
            this.err = err;
        }

        public int getExitCode() {
            return exitCode;
        }

        public void setExitCode(int exitCode) {
            this.exitCode = exitCode;
        }
    }

    public static void sendFile(Session session, File fromFile, String toFile) throws JSchException, IOException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Channel channel = null;
        try {
            String command = "scp -p -t \"" + toFile + "\"";
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            try (OutputStream out = channel.getOutputStream();
                 InputStream in = channel.getInputStream()) {
                channel.connect();
                if (checkAck(in) != 0) {
                    throw new JSchException("Failed ACK");
                }

                command = "T" + (fromFile.lastModified() / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (fromFile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    throw new JSchException("Failed ACK");
                }

                // send "C0644 filesize filename", where filename should not include '/'
                command = "C0644 " + fromFile.length() + " ";
                String localPath = fromFile.getAbsolutePath();
                if (localPath.lastIndexOf('/') > 0) {
                    command += localPath.substring(localPath.lastIndexOf('/') + 1);
                } else {
                    command += localPath;
                }
                command += "\n";
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    throw new JSchException("Failed ACK");
                }

                // send a content of file
                byte[] buf = new byte[1024];
                try (FileInputStream fis = new FileInputStream(fromFile)) {
                    while (true) {
                        int len = fis.read(buf, 0, buf.length);
                        if (len <= 0) break;
                        out.write(buf, 0, len); //out.flush();
                    }
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
                if (checkAck(in) != 0) {
                    throw new JSchException("Failed ACK");
                }
            }
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    public static SshResult executeCommand(Session session, String command, boolean printToConsole, boolean retResult)
            throws IOException, JSchException {
        return executeCommand(session, command, false, null, printToConsole, retResult);
    }

    public static SshResult executeCommandWithSudo(Session session, String command, String sudoPass, boolean printToConsole,
                                                   boolean retResult) throws IOException, JSchException {
        return executeCommand(session, command, true, sudoPass, printToConsole, retResult);
    }

    private static SshResult executeCommand(Session session, String command, boolean sudo, String sudoPass, boolean printToConsole,
                                            boolean retResult) throws JSchException, IOException {
        logger.trace("Entered " + LogHelper.getMethodName());
        ChannelExec channel = null;
        StringBuilder builderErr = new StringBuilder(), builderOut = new StringBuilder();
        int exitCode;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            String finalCommand;
            if (sudo && StringUtils.isNotEmpty(sudoPass)) {
                finalCommand = "sudo -S -p '' " + command;
            } else if (sudo) {
                finalCommand = "sudo " + command;
            } else {
                finalCommand = command;
            }
            logger.logInfo(CoreMessages.EXECUTE_COMMAND + finalCommand, LogType.SSH);
            channel.setCommand(finalCommand);

            try (InputStream in = channel.getInputStream();
                 OutputStream out = channel.getOutputStream();
                 InputStream inErr = channel.getErrStream()) {
                channel.connect();

                if (sudo && StringUtils.isNotEmpty(sudoPass)) {
                    out.write((sudoPass + "\n").getBytes());
                    out.flush();
                }

                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                while (true) {
                    while (in.available() > 0 || inErr.available() > 0) {
                        if (in.available() > 0) {
                            int i = in.read(buffer, 0, bufferSize);
                            if (i >= 0) {
                                String line = new String(buffer, 0, i);
                                if (printToConsole) {
                                    System.out.print(line);
                                }
                                if (retResult) {
                                    builderOut.append(line);
                                }
                                logger.logInfo(line, LogType.SSH);
                            }
                        }
                        if (inErr.available() > 0) {
                            int i = inErr.read(buffer, 0, bufferSize);
                            if (i >= 0) {
                                String line = new String(buffer, 0, i);
                                if (printToConsole) {
                                    System.err.print(line);
                                }
                                if (retResult) {
                                    builderErr.append(line);
                                }
                                logger.logError(line, LogType.SSH);
                            }
                        }
                    }
                    if (channel.isClosed()) {
                        if (in.available() > 0) continue;
                        if (channel.getExitStatus() != 0 && !retResult) {
                            throw new JSchException(CoreMessages.ERROR_RETURN_CODE);
                        } else {
                            exitCode = channel.getExitStatus();
                        }
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        //ignore
                    }
                }
            }
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }

        return retResult ? new SshResult(builderOut.toString(), builderErr.toString(), exitCode) : null;
    }

    public static Session createSshSession(SshCredentials credentials) throws JSchException {
        logger.trace("Entered " + LogHelper.getMethodName());
        JSch jsch = new JSch();
        Session session = jsch.getSession(credentials.getUsername(), credentials.getHost(), credentials.getPort());
        if (credentials.getAuthMethod() == SshCredentials.AuthMethod.PASSWORD) {
            session.setPassword(credentials.getPassword());
        } else {
            jsch.addIdentity("gexAccess", credentials.getPrivateKey().getBytes(), null,
                    credentials.getPassphrase() != null ? credentials.getPassphrase().getBytes() : null);
        }
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        return session;
    }


    public static void testSshConn(SshCredentials credentials) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        Session session = null;
        try {
            session = SshHelper.createSshSession(credentials);
            session.connect();
        } catch (JSchException | RuntimeException e) {
            throw logger.logAndReturnException("Failed to connect to machine " + credentials.getHost() + ": " + e.getMessage(), LogType.SSH_ERROR);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }


    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }
}
