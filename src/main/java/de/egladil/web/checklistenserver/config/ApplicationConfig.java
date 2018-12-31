//=====================================================
// Projekt: authenticationprovider
// (c) Heike Winkelvoß
//=====================================================

package de.egladil.web.checklistenserver.config;

import javax.enterprise.context.ApplicationScoped;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;

import de.egladil.web.commons.config.ConfigRootProvider;

/**
 * ApplicationConfig<br>
 * <br>
 * Attribut 'application-config' verweist auf den gleichnamigen Abschnitt in der config.yaml.
 */
@ApplicationScoped
@ConfigBundle("application-config")
public class ApplicationConfig implements ConfigRootProvider {

	private String configRoot;

	private String targetOrigin;

	private boolean blockOnMissingOriginReferer = false;

	private String authPublicKeyUrl;

	private int authPublicKeyVersion;

	private String nameDynamicConfigFile;

	@Override
	public String getConfigRoot() {
		return configRoot;
	}

	public void setConfigRoot(final String configRoot) {
		this.configRoot = configRoot;
	}

	public String getTargetOrigin() {
		return targetOrigin;
	}

	public void setTargetOrigin(final String targetOrigin) {
		this.targetOrigin = targetOrigin;
	}

	public boolean isBlockOnMissingOriginReferer() {
		return blockOnMissingOriginReferer;
	}

	public void setBlockOnMissingOriginReferer(final boolean blockOnMissingOriginReferer) {
		this.blockOnMissingOriginReferer = blockOnMissingOriginReferer;
	}

	public String getAuthPublicKeyUrl() {
		return authPublicKeyUrl;
	}

	public void setAuthPublicKeyUrl(final String authPublicKeyUrl) {
		this.authPublicKeyUrl = authPublicKeyUrl;
	}

	public int getAuthPublicKeyVersion() {
		return authPublicKeyVersion;
	}

	public void setAuthPublicKeyVersion(final int authPublicKeyVersion) {
		this.authPublicKeyVersion = authPublicKeyVersion;
	}

	@Override
	public String getNameDynamicConfigFile() {
		return nameDynamicConfigFile;
	}

	public void setNameDynamicConfigFile(final String nameDynamicConfigFile) {
		this.nameDynamicConfigFile = nameDynamicConfigFile;
	}
}
