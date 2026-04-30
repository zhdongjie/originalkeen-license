package org.eu.originalkeen.license.runtime.support;

import de.schlichtherle.license.DefaultCipherParam;
import de.schlichtherle.license.DefaultLicenseParam;
import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseParam;
import org.eu.originalkeen.license.core.hardware.HardwareDataProvider;
import org.eu.originalkeen.license.core.keystore.FileKeyStoreParam;
import org.eu.originalkeen.license.core.manager.LicenseManagerAdapter;
import org.eu.originalkeen.license.model.LicenseCheckModel;
import org.eu.originalkeen.license.runtime.LicenseRuntime;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

public final class LicenseRuntimeTestSupport {

    private static final String SUBJECT = "demo-subject";
    private static final String ALIAS = "originalkeen-test";
    private static final String PASSWORD = "changeit1";
    private static final String DISTINGUISHED_NAME =
            "CN=OriginalKeen Test, OU=QA, O=OriginalKeen, L=Shanghai, ST=Shanghai, C=CN";

    private LicenseRuntimeTestSupport() {
    }

    public static LicenseRuntimeFixture createFixture(Path tempDir) throws Exception {
        Path issuerKeyStorePath = tempDir.resolve("originalkeen-license-issuer.p12");
        Path runtimeKeyStorePath = tempDir.resolve("originalkeen-license-runtime.p12");
        Path certificatePath = tempDir.resolve("originalkeen-license-runtime.cer");
        Path licensePath = tempDir.resolve("originalkeen-license-test.lic");
        String issuerPreferencesNode = preferencesNode("issuer");
        String runtimePreferencesNode = preferencesNode("runtime");

        createIssuerAndRuntimeKeyStores(issuerKeyStorePath, runtimeKeyStorePath, certificatePath);

        LicenseCheckModel expectedHardware = sampleHardware();
        FixedHardwareDataProvider hardwareDataProvider = new FixedHardwareDataProvider(expectedHardware);
        ExposedLicenseManagerAdapter issuer = new ExposedLicenseManagerAdapter(
                createLicenseParam(SUBJECT, issuerKeyStorePath, issuerPreferencesNode),
                hardwareDataProvider
        );

        byte[] licenseBytes = issuer.createLicense(buildLicenseContent(SUBJECT, expectedHardware));
        Files.write(licensePath, licenseBytes);

        return new LicenseRuntimeFixture(
                SUBJECT,
                ALIAS,
                PASSWORD,
                runtimeKeyStorePath,
                licensePath,
                expectedHardware,
                hardwareDataProvider,
                issuerPreferencesNode,
                runtimePreferencesNode
        );
    }

    public static Path createWorkspaceTempDirectory(String prefix) throws IOException {
        Path root = Path.of("target", "test-work");
        Files.createDirectories(root);
        return Files.createTempDirectory(root, prefix);
    }

    public static void deleteRecursively(Path path) throws IOException {
        if (path == null || Files.notExists(path)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(current -> {
                try {
                    Files.deleteIfExists(current);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private static void createIssuerAndRuntimeKeyStores(
            Path issuerKeyStorePath,
            Path runtimeKeyStorePath,
            Path certificatePath
    ) throws IOException, InterruptedException {
        String keytool = resolveKeytoolBinary();
        runKeytool(
                new ProcessBuilder(
                        keytool,
                        "-genkeypair",
                        "-alias",
                        ALIAS,
                        "-keyalg",
                        "DSA",
                        "-keysize",
                        "1024",
                        "-storetype",
                        "PKCS12",
                        "-keystore",
                        issuerKeyStorePath.toString(),
                        "-storepass",
                        PASSWORD,
                        "-keypass",
                        PASSWORD,
                        "-dname",
                        DISTINGUISHED_NAME,
                        "-validity",
                        "3650",
                        "-noprompt"
                ),
                "create issuer test keystore"
        );

        runKeytool(
                new ProcessBuilder(
                        keytool,
                        "-exportcert",
                        "-alias",
                        ALIAS,
                        "-keystore",
                        issuerKeyStorePath.toString(),
                        "-storepass",
                        PASSWORD,
                        "-file",
                        certificatePath.toString(),
                        "-noprompt"
                ),
                "export runtime test certificate"
        );

        runKeytool(
                new ProcessBuilder(
                        keytool,
                        "-importcert",
                        "-alias",
                        ALIAS,
                        "-storetype",
                        "PKCS12",
                        "-keystore",
                        runtimeKeyStorePath.toString(),
                        "-storepass",
                        PASSWORD,
                        "-file",
                        certificatePath.toString(),
                        "-noprompt"
                ),
                "create runtime public keystore"
        );
    }

    private static void runKeytool(ProcessBuilder processBuilder, String operation) throws IOException, InterruptedException {
        Process process = processBuilder.redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Failed to " + operation + ". keytool output: " + output);
        }
    }

    private static String resolveKeytoolBinary() {
        Path javaHome = Path.of(System.getProperty("java.home"));
        List<Path> candidates = List.of(
                javaHome.resolve("bin").resolve(executableName()),
                javaHome.getParent() == null ? javaHome.resolve(executableName()) : javaHome.getParent().resolve("bin").resolve(executableName())
        );

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate.toString();
            }
        }
        return executableName();
    }

    private static String executableName() {
        return System.getProperty("os.name", "").toLowerCase().startsWith("windows") ? "keytool.exe" : "keytool";
    }

    private static LicenseParam createLicenseParam(String subject, Path keyStorePath, String preferencesNode) {
        return new DefaultLicenseParam(
                subject,
                Preferences.userRoot().node(preferencesNode),
                new FileKeyStoreParam(LicenseRuntimeTestSupport.class, keyStorePath.toString(), ALIAS, PASSWORD, PASSWORD),
                new DefaultCipherParam(PASSWORD)
        );
    }

    private static LicenseContent buildLicenseContent(String subject, LicenseCheckModel expectedHardware) {
        Instant now = Instant.now();

        LicenseContent content = new LicenseContent();
        content.setHolder(new X500Principal(DISTINGUISHED_NAME));
        content.setIssuer(new X500Principal(DISTINGUISHED_NAME));
        content.setSubject(subject);
        content.setIssued(Date.from(now));
        content.setNotBefore(Date.from(now.minus(1, ChronoUnit.DAYS)));
        content.setNotAfter(Date.from(now.plus(30, ChronoUnit.DAYS)));
        content.setConsumerType("User");
        content.setConsumerAmount(1);
        content.setInfo("OriginalKeen runtime integration test license");
        content.setExtra(expectedHardware);
        return content;
    }

    private static LicenseCheckModel sampleHardware() {
        LicenseCheckModel model = new LicenseCheckModel();
        model.setProtocolVersion("1.0");
        model.setIpAddress(new ArrayList<>(List.of("10.0.0.8")));
        model.setMacAddress(new ArrayList<>(List.of("00-16-3E-08-1A-2B")));
        model.setCpuSerial("CPU-TEST-001");
        model.setMainBoardSerial("MB-TEST-001");
        return model;
    }

    private static String preferencesNode(String prefix) {
        return "/org/eu/originalkeen/license/tests/" + prefix + "-" + UUID.randomUUID();
    }

    public static final class LicenseRuntimeFixture implements AutoCloseable {

        private final String subject;
        private final String publicAlias;
        private final String publicPassword;
        private final Path keyStorePath;
        private final Path licensePath;
        private final LicenseCheckModel expectedHardware;
        private final HardwareDataProvider hardwareDataProvider;
        private final String issuerPreferencesNode;
        private final String runtimePreferencesNode;

        private LicenseRuntimeFixture(
                String subject,
                String publicAlias,
                String publicPassword,
                Path keyStorePath,
                Path licensePath,
                LicenseCheckModel expectedHardware,
                HardwareDataProvider hardwareDataProvider,
                String issuerPreferencesNode,
                String runtimePreferencesNode
        ) {
            this.subject = subject;
            this.publicAlias = publicAlias;
            this.publicPassword = publicPassword;
            this.keyStorePath = keyStorePath;
            this.licensePath = licensePath;
            this.expectedHardware = expectedHardware;
            this.hardwareDataProvider = hardwareDataProvider;
            this.issuerPreferencesNode = issuerPreferencesNode;
            this.runtimePreferencesNode = runtimePreferencesNode;
        }

        public LicenseRuntime createRuntime() {
            return LicenseRuntime.builder()
                    .subject(subject)
                    .licensePath(licensePath.toString())
                    .publicAlias(publicAlias)
                    .publicKeyStorePath(keyStorePath.toString())
                    .publicPassword(publicPassword)
                    .hardwareDataProvider(hardwareDataProvider)
                    .preferencesNodeName(runtimePreferencesNode)
                    .build();
        }

        public LicenseCheckModel getExpectedHardware() {
            return expectedHardware;
        }

        @Override
        public void close() {
            removeNode(issuerPreferencesNode);
            removeNode(runtimePreferencesNode);
        }

        private void removeNode(String nodeName) {
            try {
                Preferences preferences = Preferences.userRoot();
                if (preferences.nodeExists(nodeName)) {
                    preferences.node(nodeName).removeNode();
                    preferences.flush();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static final class FixedHardwareDataProvider implements HardwareDataProvider {

        private final LicenseCheckModel hardwareInfo;

        private FixedHardwareDataProvider(LicenseCheckModel hardwareInfo) {
            this.hardwareInfo = hardwareInfo;
        }

        @Override
        public LicenseCheckModel getHardwareInfo() {
            return hardwareInfo;
        }
    }

    private static final class ExposedLicenseManagerAdapter extends LicenseManagerAdapter {

        private ExposedLicenseManagerAdapter(LicenseParam param, HardwareDataProvider hardwareDataProvider) {
            super(param, hardwareDataProvider);
        }

        private byte[] createLicense(LicenseContent content) throws Exception {
            return super.create(content, getLicenseNotary());
        }
    }
}
