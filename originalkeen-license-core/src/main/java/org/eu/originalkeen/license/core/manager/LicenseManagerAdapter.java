package org.eu.originalkeen.license.core.manager;

import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseContentException;
import de.schlichtherle.license.LicenseManager;
import de.schlichtherle.license.LicenseNotary;
import de.schlichtherle.license.LicenseParam;
import de.schlichtherle.license.NoLicenseInstalledException;
import de.schlichtherle.xml.GenericCertificate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.originalkeen.license.core.constant.LicenseConstants;
import org.eu.originalkeen.license.core.hardware.HardwareDataProvider;
import org.eu.originalkeen.license.model.LicenseCheckModel;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@code LicenseManagerAdapter} is a customized extension of TrueLicense
 * {@link LicenseManager} that adds hardware-binding verification logic.
 *
 * <p>This class acts as a bridge between the TrueLicense core mechanism
 * and application-specific hardware validation rules. It delegates
 * hardware data collection to a {@link HardwareDataProvider} and
 * compares the runtime hardware information with the values embedded
 * in the license.</p>
 *
 * <p>Responsibilities of this adapter include:</p>
 * <ul>
 *   <li>Creating, installing, and verifying license keys</li>
 *   <li>Performing native TrueLicense validations such as time, signature, and integrity checks</li>
 *   <li>Validating hardware constraints such as IP address, MAC address,
 *       motherboard serial, and CPU serial</li>
 *   <li>Providing early warnings when a license is close to expiration</li>
 * </ul>
 *
 * <p>The hardware validation rules are intentionally designed to be
 * tolerant: if a specific hardware attribute is not defined in the
 * license, the corresponding validation will be skipped.</p>
 *
 * <p>This class is thread-safe. All critical operations related to
 * license creation, installation, and verification are synchronized
 * to prevent concurrent state corruption.</p>
 *
 * @author Original Keen
 * @see LicenseManager
 * @see HardwareDataProvider
 * @see LicenseCheckModel
 */
public class LicenseManagerAdapter extends LicenseManager {

    private static final Logger log = LogManager.getLogger(LicenseManagerAdapter.class);

    private final HardwareDataProvider hardwareDataProvider;

    /**
     * Constructs the adapter with the given TrueLicense parameters and hardware provider.
     *
     * @param param license parameters
     * @param hardwareDataProvider hardware data provider
     */
    public LicenseManagerAdapter(LicenseParam param, HardwareDataProvider hardwareDataProvider) {
        super(param);
        this.hardwareDataProvider = hardwareDataProvider;
    }

    /**
     * Returns the hardware information collected from the current server.
     *
     * @return hardware snapshot for the current server
     */
    public LicenseCheckModel getServerHardwareInfo() {
        return hardwareDataProvider.getHardwareInfo();
    }

    /**
     * Creates a license key from the provided content.
     *
     * @param content license content
     * @param notary license notary
     * @return encoded license key bytes
     * @throws Exception if the license cannot be created
     */
    @Override
    protected synchronized byte[] create(LicenseContent content, LicenseNotary notary) throws Exception {
        initialize(content);
        this.validateCreate(content);
        GenericCertificate certificate = notary.sign(content);
        return getPrivacyGuard().cert2key(certificate);
    }

    /**
     * Installs a license key.
     *
     * @param key license key bytes
     * @param notary license notary
     * @return validated license content after installation
     * @throws Exception if installation fails
     */
    @Override
    protected synchronized LicenseContent install(byte[] key, LicenseNotary notary) throws Exception {
        GenericCertificate certificate = getPrivacyGuard().key2cert(key);
        notary.verify(certificate);
        LicenseContent content = (LicenseContent) this.load(certificate.getEncoded());
        this.validate(content);
        setLicenseKey(key);
        setCertificate(certificate);
        return content;
    }

    /**
     * Verifies the installed license.
     *
     * @param notary license notary
     * @return validated license content after verification
     * @throws Exception if verification fails
     */
    @Override
    protected synchronized LicenseContent verify(LicenseNotary notary) throws Exception {
        byte[] key = getLicenseKey();
        if (key == null) {
            throw new NoLicenseInstalledException(getLicenseParam().getSubject());
        }
        GenericCertificate certificate = getPrivacyGuard().key2cert(key);
        notary.verify(certificate);
        LicenseContent content = (LicenseContent) this.load(certificate.getEncoded());
        this.validate(content);
        setCertificate(certificate);
        return content;
    }

    /**
     * Validates the license content during creation.
     *
     * @param content license content
     * @throws LicenseContentException if the content is invalid
     */
    protected synchronized void validateCreate(LicenseContent content) throws LicenseContentException {
        Date now = new Date();
        Date notBefore = content.getNotBefore();
        Date notAfter = content.getNotAfter();
        if (notAfter != null && now.after(notAfter)) {
            throw new LicenseContentException("License has expired");
        }
        if (notBefore != null && notAfter != null && notAfter.before(notBefore)) {
            throw new LicenseContentException("License start date cannot be after expiration date");
        }
    }

    /**
     * Validates the license content during verification.
     *
     * @param content license content
     * @throws LicenseContentException if validation fails
     */
    @Override
    protected synchronized void validate(LicenseContent content) throws LicenseContentException {
        // First run the native TrueLicense validation chain.
        super.validate(content);

        // Then validate the custom hardware bindings stored in the license.
        LicenseCheckModel expected = getExpected(content);
        LicenseCheckModel current = getCurrent();

        validateIp(expected, current);
        validateMac(expected, current);
        validateMainBoard(expected, current);
        validateCpu(expected, current);

        warnIfAboutToExpire(content);
    }

    /**
     * Loads {@link LicenseContent} from the encoded XML payload stored in the certificate.
     *
     * @param encoded XML encoded string
     * @return deserialized object
     */
    private Object load(String encoded) {
        try (
                BufferedInputStream inputStream = new BufferedInputStream(
                        new ByteArrayInputStream(encoded.getBytes(LicenseConstants.XML_CHARSET))
                );
                XMLDecoder decoder = new XMLDecoder(
                        new BufferedInputStream(inputStream, LicenseConstants.DEFAULT_BUFF_SIZE),
                        null,
                        null
                )
        ) {
            return decoder.readObject();
        } catch (UnsupportedEncodingException e) {
            log.error("Configured XML charset is not supported", e);
            throw new IllegalStateException(
                    "Failed to decode license content because the configured XML charset is unsupported",
                    e
            );
        } catch (Exception e) {
            log.error("Failed to decode license content from certificate payload", e);
            throw new IllegalStateException("Failed to decode license content from the certificate payload", e);
        }
    }

    /**
     * Extracts the expected hardware information from the license content.
     *
     * @param content license content
     * @return hardware snapshot embedded in the license
     * @throws LicenseContentException if the license does not contain hardware information
     */
    private LicenseCheckModel getExpected(LicenseContent content) throws LicenseContentException {
        Object extra = content.getExtra();
        if (!(extra instanceof LicenseCheckModel expected)) {
            throw new LicenseContentException("License does not contain hardware info");
        }
        return expected;
    }

    /**
     * Returns the current server hardware information.
     *
     * @return current server hardware snapshot
     * @throws LicenseContentException if the hardware information cannot be obtained
     */
    private LicenseCheckModel getCurrent() throws LicenseContentException {
        LicenseCheckModel current = hardwareDataProvider.getHardwareInfo();
        if (current == null) {
            throw new LicenseContentException("Cannot get server hardware info");
        }
        return current;
    }

    private void validateIp(LicenseCheckModel expected, LicenseCheckModel current) throws LicenseContentException {
        if (isNotMatched(expected.getIpAddress(), current.getIpAddress())) {
            throw new LicenseContentException("IP address not authorized");
        }
    }

    private void validateMac(LicenseCheckModel expected, LicenseCheckModel current) throws LicenseContentException {
        if (isNotMatched(expected.getMacAddress(), current.getMacAddress())) {
            throw new LicenseContentException("MAC address not authorized");
        }
    }

    /**
     * Checks whether the current list does not match the expected list.
     *
     * @param expectedList expected values from the license
     * @param currentList current values collected from the server
     * @return {@code true} when no value matches, otherwise {@code false}
     */
    private boolean isNotMatched(List<String> expectedList, List<String> currentList) {
        // If the license does not bind this hardware attribute, skip the check.
        if (expectedList == null || expectedList.isEmpty()) {
            return false;
        }

        // If the runtime value cannot be collected, treat it as a verification failure.
        if (currentList == null || currentList.isEmpty()) {
            return true;
        }

        Set<String> expectedSet = expectedList.stream()
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toSet());

        // A single matching runtime value is enough to satisfy the binding rule.
        boolean matched = currentList.stream()
                .map(s -> s.trim().toLowerCase())
                .anyMatch(expectedSet::contains);

        return !matched;
    }

    private void validateMainBoard(LicenseCheckModel expected, LicenseCheckModel current) throws LicenseContentException {
        if (serialNotMatch(expected.getMainBoardSerial(), current.getMainBoardSerial())) {
            throw new LicenseContentException("Main-board serial not authorized");
        }
    }

    private void validateCpu(LicenseCheckModel expected, LicenseCheckModel current) throws LicenseContentException {
        if (serialNotMatch(expected.getCpuSerial(), current.getCpuSerial())) {
            throw new LicenseContentException("CPU serial not authorized");
        }
    }

    /**
     * Checks whether the current serial number matches the expected one.
     *
     * @param expected expected serial number from the license
     * @param current current serial number collected from the server
     * @return {@code true} when the serial does not match, otherwise {@code false}
     */
    private boolean serialNotMatch(String expected, String current) {
        // If the license does not bind this serial number, skip the check.
        if (expected == null || expected.isBlank()) {
            return false;
        }
        return !expected.equalsIgnoreCase(current);
    }

    /**
     * Logs a warning when the license is close to expiration.
     *
     * @param content license content
     */
    private void warnIfAboutToExpire(LicenseContent content) {
        Date notAfter = content.getNotAfter();
        if (notAfter == null) {
            return;
        }

        long daysLeft = (notAfter.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
        if (daysLeft >= 0 && daysLeft < 15) {
            log.warn("===================== License =======================");
            log.warn("License is about to expire! Days left: {}. Expiration date: {}", daysLeft, notAfter);
            log.warn("Please contact administrator to update the license to avoid service interruption.");
            log.warn("=====================================================");
        }
    }
}
