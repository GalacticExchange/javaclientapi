package io.gex.cli;

import com.google.gson.JsonObject;
import io.gex.core.BaseHelper;
import io.gex.core.GsonHelper;
import io.gex.core.ValidationHelper;
import io.gex.core.exception.GexException;
import io.gex.core.log.LogHelper;
import io.gex.core.log.LogType;
import io.gex.core.log.LogWrapper;
import io.gex.core.model.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CliHelper {

    private final static LogWrapper logger = LogWrapper.create(CliHelper.class);

    final static String AUTH_PATH = "ASCII/auth.txt";
    final static String PLANET_PATH = "ASCII/planet.txt";
    final static String NODE_PATH = "ASCII/node.txt";
    static boolean isForceYes;

    static Boolean getYesOrNoFromConsole(String prompt) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (isForceYes) {
            return true;
        }
        Boolean answer = null;
        try {
            Console console = System.console();
            while (answer == null) {
                String output = console.readLine(prompt);
                if (output.toLowerCase().equals("y") || output.toLowerCase().equals("yes")) {
                    answer = true;
                } else if (output.toLowerCase().equals("n") || output.toLowerCase().equals("no")) {
                    answer = false;
                }
            }
        } catch (NullPointerException e) {
            printError(CliMessages.UNEXPECTED_END);
            System.exit(1);
        } catch (Exception e) {
            throw logger.logAndReturnException(CliMessages.READ_CONSOLE_ERROR, e, LogType.CONSOLE);
        }
        return answer;
    }

    // return not empty line
    static String getLineFromConsole(String prompt) {
        logger.trace("Entered " + LogHelper.getMethodName());
        String line = StringUtils.EMPTY;
        Boolean correct = false;
        try {
            Console console = System.console();
            while (!correct) {
                line = console.readLine(prompt);
                if (line.length() < 1) {
                    printError(CliMessages.EMPTY_INPUT);
                } else {
                    correct = true;
                }
            }
        } catch (NullPointerException e) {
            printError(CliMessages.UNEXPECTED_END);
            System.exit(1);
        }
        return line;
    }

    static String getPasswordFromConsole(String prompt) {
        logger.trace("Entered " + LogHelper.getMethodName());
        String password = StringUtils.EMPTY;
        Boolean correct = false;
        try {
            Console console = System.console();
            char passwordArray[];
            while (!correct) {
                passwordArray = console.readPassword(prompt);
                password = new String(passwordArray);
                if (password.length() < ValidationHelper.MIN_PASSWORD_LENGTH) {
                    CliMessages.printMinPasswordLengthMessage();
                } else if (password.length() > ValidationHelper.MAX_PASSWORD_LENGTH) {
                    CliMessages.printMaxPasswordLengthMessage();
                } else {
                    correct = true;
                }
            }
        } catch (NullPointerException e) {
            printError(CliMessages.UNEXPECTED_END);
            System.exit(1);
        }
        return password;
    }

    static String getAndConfirmPasswordFromConsole(String prompt) {
        logger.trace("Entered " + LogHelper.getMethodName());
        String password = StringUtils.EMPTY, confirmPassword;
        Boolean correct = false;
        try {
            Console console = System.console();
            char passwordArray[];
            while (!correct) {
                passwordArray = console.readPassword(prompt);
                password = new String(passwordArray);
                if (password.length() < ValidationHelper.MIN_PASSWORD_LENGTH) {
                    CliMessages.printMinPasswordLengthMessage();
                } else if (password.length() > ValidationHelper.MAX_PASSWORD_LENGTH) {
                    CliMessages.printMaxPasswordLengthMessage();
                } else {
                    correct = true;
                }
            }
            passwordArray = console.readPassword(CliMessages.PASSWORD_CONFIRM);
            confirmPassword = new String(passwordArray);
            if (!password.equals(confirmPassword))
                throw new IllegalArgumentException(CliMessages.PASSWORDS_MATCH_ERROR);
        } catch (NullPointerException e) {
            printError(CliMessages.UNEXPECTED_END);
            System.exit(1);
        }
        return password;
    }

    static void printASCIIArt(String fileName, String color) {
        logger.trace("Entered " + LogHelper.getMethodName());
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream(fileName);
        try {
            System.out.print(printColor(color));
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (Exception e) {
            logger.logWarn(CliMessages.ASCII_ERROR, LogType.GENERAL_ERROR);
        } finally {
            System.out.print(printColor(Color.ANSI_RESET));
        }
    }

    static void printError(String str) {
        logger.trace("Entered " + LogHelper.getMethodName());
        if (SystemUtils.IS_OS_WINDOWS) {
            System.err.println("\t" + str);
        } else {
            System.err.println(Color.ANSI_RED + "\t" + str + Color.ANSI_RESET);
        }
    }

    static String printColor(String color) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return StringUtils.EMPTY;
        } else {
            return color;
        }
    }

    static Boolean mainMenuApiCheck(String[] arguments) {
        return arguments.length == 1 && (arguments[0].toLowerCase().equals(CliMessages.API_LONG));
    }

    static Boolean mainMenuVersionCheck(String[] arguments) {
        return arguments.length == 1
                && (arguments[0].toLowerCase().equals(CliMessages.VERSION_SHORT) ||
                arguments[0].toLowerCase().equals(CliMessages.VERSION_LONG));
    }

    static Boolean mainMenuHelpCheck(String[] arguments) {
        return arguments.length == 0 || (arguments.length == 1
                && (arguments[0].toLowerCase().equals(CliMessages.HELP_SHORT) ||
                arguments[0].toLowerCase().equals(CliMessages.HELP_LONG)));
    }

    static Boolean helpCheck(String[] arguments) {
        return arguments.length == 1
                && (arguments[0].toLowerCase().equals(CliMessages.HELP_SHORT) ||
                arguments[0].toLowerCase().equals(CliMessages.HELP_LONG));
    }

    static String getRootPasswordFromConsole(String prompt) throws GexException {
        logger.trace("Entered " + LogHelper.getMethodName());
        String password = StringUtils.EMPTY;
        boolean correct = false;
        try {
            Console console = System.console();
            char passwordArray[];
            while (!correct) {
                passwordArray = console.readPassword(prompt);
                password = new String(passwordArray);
                correct = BaseHelper.checkRootPassword(password);
                if (!correct) {
                    LogHelper.print(CliMessages.WRONG_PASSWORD_TRY);
                    prompt = CliMessages.ROOT_PASSWORD;
                }
            }
        } catch (NullPointerException e) {
            printError(CliMessages.UNEXPECTED_END);
            System.exit(-1);
        }
        return password;
    }

    static void printErrMessageForUi(Exception e) {
        JsonObject res = new JsonObject();
        JsonObject err = new JsonObject();
        err.addProperty("message", e.getMessage());
        res.add("error", err);
        System.out.println(GsonHelper.toJson(res));
    }

    static void printRocket() {
        printASCIIArt(NODE_PATH, Color.ANSI_CYAN);
        System.out.println(printColor(Color.ANSI_RED) + "			\"\"\"\"\"\"");
        System.out.println("			 \"\"\"\"");
        System.out.println("			  \"\"" + printColor(Color.ANSI_RESET));
    }
}
