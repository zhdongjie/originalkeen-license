package org.eu.originalkeen.license.core.hardware;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Linux implementation of {@link AbstractHardwareProvider}.
 * <p>
 * Retrieves CPU serial number and main-board serial number using system commands
 * and standard Linux files. This is used for license verification or hardware fingerprinting.
 * </p>
 */
public class LinuxHardwareProvider extends AbstractHardwareProvider {

    private static final Logger log = LogManager.getLogger(LinuxHardwareProvider.class);

    /**
     * Retrieves the CPU serial number on Linux systems.
     * <p>
     * Tries the following approaches in order:
     * 1. dmidecode command
     * 2. /proc/cpu-info (for embedded systems or specific distributions)
     * </p>
     *
     * @return CPU serial number as a String, empty if not found
     */
    @Override
    protected String getCpuSerial() {
        String serial = executeCommand("dmidecode -t processor | grep 'ID' | awk -F ':' '{print $2}' | head -n 1");

        if (serial.isEmpty()) {
            serial = executeCommand("cat /proc/cpuinfo | grep 'Serial' | awk -F ':' '{print $2}' | head -n 1");
        }
        return serial;
    }

    /**
     * Retrieves the main-board serial number on Linux systems.
     * <p>
     * Tries the following approaches in order:
     * 1. /sys/class/dmi/id/board_serial
     * 2. dmidecode command
     * 3. /sys/class/dmi/id/product_serial
     * </p>
     *
     * @return Main-board serial number as a String, empty if not found
     */
    @Override
    protected String getMainBoardSerial() {
        String serial = readFileContent("/sys/class/dmi/id/board_serial");

        if (serial.isEmpty()) {
            serial = executeCommand("dmidecode -t baseboard | grep 'Serial Number' | awk -F ':' '{print $2}' | head -n 1");
        }

        if (serial.isEmpty()) {
            serial = readFileContent("/sys/class/dmi/id/product_serial");
        }

        return serial;
    }

    /**
     * Executes a shell command and returns the first line of output.
     *
     * @param command Powershell command to execute
     * @return trimmed first line of output, or empty string if failed
     */
    private String executeCommand(String command) {
        String[] commandArray = {"/bin/sh", "-c", command};
        try {
            Process process = Runtime.getRuntime().exec(commandArray);
            process.waitFor();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    return line.trim();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to execute command: {}", command, e);
        }
        return "";
    }

    /**
     * Reads the entire content of a file as a single string.
     *
     * @param path file path
     * @return file content trimmed, or empty string if file is missing/unreadable
     */
    private String readFileContent(String path) {
        try {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                try (Stream<String> lines = Files.lines(file.toPath())) {
                    return lines.collect(Collectors.joining()).trim();
                }
            }
        } catch (Exception e) {
            // ignore exceptions, fallback to empty string
        }
        return "";
    }
}
