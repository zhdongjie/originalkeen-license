package org.eu.originalkeen.license.core.hardware;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Windows implementation of {@link AbstractHardwareProvider}.
 * <p>
 * Retrieves CPU serial number and main-board serial number using PowerShell or WMIC commands.
 * Supports modern Windows (PowerShell) and older versions (WMIC).
 * </p>
 */
public class WindowsHardwareProvider extends AbstractHardwareProvider {

    private static final Logger log = LogManager.getLogger(WindowsHardwareProvider.class);

    /**
     * Retrieves CPU serial number on Windows.
     * <p>
     * Attempts PowerShell first (modern Windows), then WMIC as a fallback (legacy support).
     * </p>
     *
     * @return CPU serial number as a String, empty if not found
     */
    @Override
    protected String getCpuSerial() {
        String serial = executePowerShell(
                "Get-CimInstance -ClassName Win32_Processor | Select-Object -ExpandProperty ProcessorId"
        );

        if (serial.isEmpty()) {
            serial = executeCommand("wmic cpu get processorid");
        }

        return serial;
    }

    /**
     * Retrieves main-board serial number on Windows.
     * <p>
     * Attempts PowerShell first, then WMIC as a fallback.
     * </p>
     *
     * @return Main-board serial number as a String, empty if not found
     */
    @Override
    protected String getMainBoardSerial() {
        String serial = executePowerShell(
                "Get-CimInstance -ClassName Win32_BaseBoard | Select-Object -ExpandProperty SerialNumber"
        );

        if (serial.isEmpty()) {
            serial = executeCommand("wmic baseboard get serialnumber");
        }

        return serial;
    }

    /**
     * Executes a PowerShell command and returns its output.
     *
     * @param command PowerShell command
     * @return trimmed first line of output, or empty string if failed
     */
    private String executePowerShell(String command) {
        String[] cmd = {"powershell", "-Command", command};
        return execute(cmd);
    }

    /**
     * Executes a Windows CMD/WMIC command and processes its output.
     *
     * @param command CMD/WMIC command
     * @return extracted result, or empty string if failed
     */
    private String executeCommand(String command) {
        String[] cmd = {"cmd", "/c", command};
        return extractFromWmicOutput(execute(cmd));
    }

    /**
     * Executes a system command and returns the raw output.
     *
     * @param command command array
     * @return trimmed output string, or empty if failed
     */
    private String execute(String[] command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        result.append(line.trim());
                        // PowerShell usually returns a single line, WMIC returns multiple
                        if ("powershell".equalsIgnoreCase(command[0])) break;
                        result.append("\n");
                    }
                }
                return result.toString().trim();
            }
        } catch (Exception e) {
            log.debug("Failed to execute command: {}", String.join(" ", command), e);
        }
        return "";
    }

    /**
     * Extracts the actual value from WMIC output by removing the header.
     *
     * @param output raw WMIC command output
     * @return processed value string
     */
    private String extractFromWmicOutput(String output) {
        if (output.isEmpty()) return "";

        String[] lines = output.split("\n");
        if (lines.length > 1) {
            return lines[1].trim(); // usually second line contains the value
        } else if (lines.length == 1 && !lines[0].contains(" ")) {
            return lines[0].trim();
        }

        return "";
    }
}
