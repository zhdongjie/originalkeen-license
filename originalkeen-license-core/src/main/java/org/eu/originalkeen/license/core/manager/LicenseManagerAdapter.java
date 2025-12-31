package org.eu.originalkeen.license.core.manager;

import de.schlichtherle.license.*;
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
 *   <li>Performing native TrueLicense validations (time, signature, integrity)</li>
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
     * Constructor for LicenseManagerAdapter
     *
     * @param param                 License parameters
     * @param hardwareDataProvider  Hardware data provider
     */
    public LicenseManagerAdapter(LicenseParam param, HardwareDataProvider hardwareDataProvider) {
        super(param);
        this.hardwareDataProvider = hardwareDataProvider;
    }

    /**
     * Get the server's hardware information
     *
     * @return LicenseCheckModel containing hardware info
     */
    public LicenseCheckModel getServerHardwareInfo() {
        return hardwareDataProvider.getHardwareInfo();
    }

    /**
     * Create a license key from LicenseContent
     *
     * @param content License content
     * @param notary  License notary
     * @return byte array of license key
     * @throws Exception any creation exception
     */
    @Override
    protected synchronized byte[] create(LicenseContent content, LicenseNotary notary) throws Exception {
        initialize(content);
        this.validateCreate(content);
        GenericCertificate certificate = notary.sign(content);
        return getPrivacyGuard().cert2key(certificate);
    }

    /**
     * Install a license key
     *
     * @param key    license key bytes
     * @param notary license notary
     * @return LicenseContent after installation
     * @throws Exception any installation exception
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
     * Verify the installed license
     *
     * @param notary license notary
     * @return LicenseContent after verification
     * @throws Exception any verification exception
     */
    @Override
    protected synchronized LicenseContent verify(LicenseNotary notary) throws Exception {
        byte[] key = getLicenseKey();
        if (null == key) {
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
     * Validate the license content during creation
     *
     * @param content LicenseContent
     * @throws LicenseContentException if invalid
     */
    protected synchronized void validateCreate(LicenseContent content) throws LicenseContentException {
        Date now = new Date();
        Date notBefore = content.getNotBefore();
        Date notAfter = content.getNotAfter();
        if (null != notAfter && now.after(notAfter)) {
            throw new LicenseContentException("License has expired");
        }
        if (null != notBefore && null != notAfter && notAfter.before(notBefore)) {
            throw new LicenseContentException("License start date cannot be after expiration date");
        }
    }

    /**
     * Validate the license content during verification
     *
     * @param content LicenseContent
     * @throws LicenseContentException if validation fails
     */
    @Override
    protected synchronized void validate(LicenseContent content) throws LicenseContentException {
        // 1. True license native validation (time, signature, etc.)
        super.validate(content);

        // 2. Read hardware info bound in License
        LicenseCheckModel expected = getExpected(content);

        // 3. Read current server hardware info
        LicenseCheckModel current = getCurrent();

        // 4. Validate hardware rules
        validateIp(expected, current);
        validateMac(expected, current);
        validateMainBoard(expected, current);
        validateCpu(expected, current);

        // 5. Warn if license is about to expire
        warnIfAboutToExpire(content);
    }

    /**
     * Load LicenseContent from encoded XML string
     *
     * @param encoded XML encoded string
     * @return deserialized object
     */
    private Object load(String encoded) {
        try (
                BufferedInputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(encoded.getBytes(LicenseConstants.XML_CHARSET)));
                XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(inputStream, LicenseConstants.DEFAULT_BUFF_SIZE), null, null)
        ) {
            return decoder.readObject();
        } catch (UnsupportedEncodingException e) {
            log.error("XML Charset unsupported", e);
        } catch (Exception e) {
            log.error("XML Decode failed", e);
        }
        return null;
    }

    /**
     * Extract expected hardware info from license content
     *
     * @param content LicenseContent
     * @return LicenseCheckModel
     * @throws LicenseContentException if not present
     */
    private LicenseCheckModel getExpected(LicenseContent content) throws LicenseContentException {
        Object extra = content.getExtra();
        if (!(extra instanceof LicenseCheckModel expected)) {
            throw new LicenseContentException("License does not contain hardware info");
        }
        return expected;
    }

    /**
     * Get current server hardware info
     *
     * @return LicenseCheckModel
     * @throws LicenseContentException if you cannot get
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
     * Check if current list does not match expected list
     *
     * @param expectedList expected values
     * @param currentList  current values
     * @return true if not match, false if match
     */
    private boolean isNotMatched(List<String> expectedList, List<String> currentList) {
        // License rule not bound → treat as match
        if (expectedList == null || expectedList.isEmpty()) {
            return false;
        }

        // Cannot get current values → treat as mismatch
        if (currentList == null || currentList.isEmpty()) {
            return true;
        }

        Set<String> expectedSet = expectedList.stream()
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toSet());

        // Any current value matches expected → match
        boolean matched = currentList.stream()
                .map(s -> s.trim().toLowerCase())
                .anyMatch(expectedSet::contains);

        return !matched; // true = not match
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
     * Check if serial number does not match
     *
     * @param expected expected serial
     * @param current  current serial
     * @return true if not match, false if match
     */
    private boolean serialNotMatch(String expected, String current) {
        // License not bound → treat as match
        if (expected == null || expected.isBlank()) {
            return false;
        }
        return !expected.equalsIgnoreCase(current);
    }

    /**
     * Warn if license is about to expire
     *
     * @param content LicenseContent
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
