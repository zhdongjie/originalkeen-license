package org.eu.originalkeen.license.core.manager;

import de.schlichtherle.license.CipherParam;
import de.schlichtherle.license.KeyStoreParam;
import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.LicenseNotary;
import de.schlichtherle.license.LicenseParam;
import org.eu.originalkeen.license.core.hardware.HardwareDataProvider;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LicenseManagerAdapterTest {

    @Test
    void reloadIfNeededShouldSkipUnchangedFileAndReloadChangedFile() throws Exception {
        TestableLicenseManagerAdapter manager = new TestableLicenseManagerAdapter();
        Path licenseFile = Files.createTempFile("license-manager-adapter", ".lic");

        try {
            Files.writeString(licenseFile, "license-v1", StandardCharsets.UTF_8);
            Files.setLastModifiedTime(licenseFile, FileTime.fromMillis(System.currentTimeMillis()));

            LicenseContent first = manager.reloadIfNeeded(licenseFile, null);
            assertNotNull(first, "The first load should install the file content.");
            assertEquals(1, manager.getInstallCount(), "The first load must install the license.");

            LicenseContent unchanged = manager.reloadIfNeeded(licenseFile, null);
            assertNull(unchanged, "An unchanged file should not trigger reinstall.");
            assertEquals(1, manager.getInstallCount(), "Install count should remain unchanged when the file is unchanged.");

            Files.writeString(licenseFile, "license-v2", StandardCharsets.UTF_8);
            Files.setLastModifiedTime(licenseFile, FileTime.fromMillis(System.currentTimeMillis() + 2_000L));

            LicenseContent reloaded = manager.reloadIfNeeded(licenseFile, null);
            assertNotNull(reloaded, "A changed file should trigger reload.");
            assertEquals(2, manager.getInstallCount(), "Install count should increase after a real file change.");
        } finally {
            Files.deleteIfExists(licenseFile);
        }
    }

    @Test
    void reloadShouldDelegateToInstallPath() throws Exception {
        TestableLicenseManagerAdapter manager = new TestableLicenseManagerAdapter();

        LicenseContent content = manager.reload("manual-license".getBytes(StandardCharsets.UTF_8), null);

        assertNotNull(content, "Reload should return the installed content.");
        assertEquals(1, manager.getInstallCount(), "Reload should execute exactly one install path.");
    }

    private static final class TestableLicenseManagerAdapter extends LicenseManagerAdapter {

        private int installCount;

        private TestableLicenseManagerAdapter() {
            super(mockLicenseParam(), mock(HardwareDataProvider.class));
        }

        @Override
        protected LicenseContent install(byte[] key, LicenseNotary notary) {
            installCount++;
            return new LicenseContent();
        }

        private int getInstallCount() {
            return installCount;
        }

        private static LicenseParam mockLicenseParam() {
            LicenseParam licenseParam = mock(LicenseParam.class);
            KeyStoreParam keyStoreParam = mock(KeyStoreParam.class);
            CipherParam cipherParam = mock(CipherParam.class);

            when(licenseParam.getSubject()).thenReturn("test-subject");
            when(licenseParam.getPreferences()).thenReturn(Preferences.userRoot().node("originalkeen-license-test"));
            when(licenseParam.getKeyStoreParam()).thenReturn(keyStoreParam);
            when(licenseParam.getCipherParam()).thenReturn(cipherParam);

            when(keyStoreParam.getAlias()).thenReturn("test-alias");
            when(keyStoreParam.getStorePwd()).thenReturn("change123");
            when(keyStoreParam.getKeyPwd()).thenReturn("change123");
            try {
                when(keyStoreParam.getStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
            } catch (Exception ignored) {
            }

            when(cipherParam.getKeyPwd()).thenReturn("change123");
            return licenseParam;
        }
    }
}
