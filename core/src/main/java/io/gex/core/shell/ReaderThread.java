package io.gex.core.shell;

import com.jcraft.jsch.Channel;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;

import java.io.InputStream;

public class ReaderThread implements Runnable {

    private final static LogWrapper logger = LogWrapper.create(ReaderThread.class);
    private Channel channel;

    ReaderThread(Channel channel) {
        logger.trace("Entered " + LogHelper.getMethodName());
        this.channel = channel;
    }

    @Override
    public void run() {
        logger.trace("Entered " + LogHelper.getMethodName());
        int i;
        byte[] buffer = new byte[1024];
        try {
            InputStream in = channel.getInputStream();
            while ((i = in.read(buffer, 0, 1024)) >= 0) {
                if (i > 0)
                    System.out.write(buffer, 0, i);
            }
            SshConnectionManager.close();
        } catch (Exception e) {
            logger.logError(e,  LogType.SSH_ERROR);
        }
    }
}