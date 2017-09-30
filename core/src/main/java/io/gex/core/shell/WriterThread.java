package io.gex.core.shell;

import com.jcraft.jsch.Channel;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import jline.console.ConsoleReader;

import java.io.PrintStream;

public class WriterThread implements Runnable {

    private Channel channel;
    private final static LogWrapper logger = LogWrapper.create(WriterThread.class);

    WriterThread(Channel channel) {
        logger.trace("Entered " + LogHelper.getMethodName());
        this.channel = channel;
    }

    @Override
    public void run() {
        logger.trace("Entered " + LogHelper.getMethodName());
        try {
            PrintStream channelOut = new PrintStream(channel.getOutputStream());
            while (!channel.isClosed()) {
                int ch = new ConsoleReader().readCharacter();
                channelOut.write(ch);
                channelOut.flush();
            }
        } catch (Exception e) {
            logger.logError(e,  LogType.SSH_ERROR);
        }
    }
}