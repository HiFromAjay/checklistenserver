//=====================================================
// Projekt: checklistenserver
// (c) Heike Winkelvoß
//=====================================================

package de.egladil.web.checklistenserver.filters;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import de.egladil.web.checklistenserver.config.ApplicationConfig;
import de.egladil.web.commons.error.AuthException;
import de.egladil.web.commons.error.SessionExpiredException;
import de.egladil.web.commons.utils.CommonStringUtils;
import de.egladil.web.commons.utils.CommonTimeUtils;

/**
 * SecurityFilter
 */
@ApplicationScoped
@Provider
@PreMatching
@Priority(100)
public class SecurityFilter implements ContainerRequestFilter {

	private static final Logger LOG = LogManager.getLogger(SecurityFilter.class.getName());

	private static final List<String> NO_CONTENT_PATHS = Arrays.asList(new String[] { "/favicon.ico" });

	@Context
	private HttpServletRequest servletRequest;

	@Inject
	private ApplicationConfig applicationConfig;

	@Override
	public void filter(final ContainerRequestContext requestContext) throws IOException {

		final String pathInfo = servletRequest.getPathInfo();
		if (NO_CONTENT_PATHS.contains(pathInfo)) {
			throw new NoContentException(pathInfo);
		}

		this.validateOriginAndRefererHeader();

		DecodedJWT jwt = new JWTProvider().getJWT(servletRequest.getHeader("Authorization"), applicationConfig);

		if (jwt != null) {
			checkIfLoggedIn(jwt, requestContext);
		}
	}

	private void checkIfLoggedIn(final DecodedJWT jwt, final ContainerRequestContext requestContext) {

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiresAt = CommonTimeUtils.transformFromDate(jwt.getExpiresAt());

		if (expiresAt.isBefore(now)) {
			throw new SessionExpiredException();
		}

		String subject = jwt.getSubject();
		requestContext.setProperty("USER_ID", subject);
	}

	/**
	 * Validiert die Header-Parameter 'Origin' und 'Referer'.
	 *
	 * @param response
	 * @param req
	 * @throws IOException
	 */
	private void validateOriginAndRefererHeader() throws IOException {
		final String origin = servletRequest.getHeader("Origin");
		final String referer = servletRequest.getHeader("Referer");

		LOG.debug("Origin = [{}], Referer = [{]}", origin, referer);

		if (StringUtils.isBlank(origin) && StringUtils.isBlank(referer)) {
			final String details = "Header Origin UND Referer fehlen";
			if (applicationConfig.isBlockOnMissingOriginReferer()) {
				logErrorAndThrow(details);
			}
		}

		if (!StringUtils.isBlank(origin)) {
			checkHeaderTarget(origin);
		}
		if (!StringUtils.isBlank(referer)) {
			checkHeaderTarget(referer);
		}
	}

	private void checkHeaderTarget(final String headerValue) throws IOException {
		final String extractedValue = CommonStringUtils.extractOrigin(headerValue);
		if (extractedValue == null) {
			return;
		}

		String targetOrigin = applicationConfig.getTargetOrigin();
		if (!targetOrigin.equals(extractedValue)) {
			final String details = "targetOrigin != extractedOrigin: [targetOrigin=" + targetOrigin + ", extractedOriginOrReferer="
				+ extractedValue + "]";
			logErrorAndThrow(details);
		}
	}

	/**
	 * Der Authentisierungsfehler wird geloggt und ein entsprechender Response erzeugt.
	 *
	 * @param request
	 * @param res
	 * @throws IOException
	 */
	private void logErrorAndThrow(final String details) throws IOException {
		final String dump = getRequesInfos();
		LOG.warn("Possible CSRF-Attack: {} - {}", details, dump);

		throw new AuthException();
	}

	private String getRequesInfos() {
		final Enumeration<String> headerNames = servletRequest.getHeaderNames();
		final StringBuffer sb = new StringBuffer();
		sb.append(" <--- Request Headers --- ");
		while (headerNames.hasMoreElements()) {
			final String headerName = headerNames.nextElement();
			sb.append(headerName);
			sb.append(":");
			final Enumeration<String> headerValues = servletRequest.getHeaders(headerName);
			if (headerValues.hasMoreElements()) {
				sb.append(headerValues.nextElement());
				sb.append(", ");
			}
			sb.append(" -- ");
		}
		sb.append(" Headers Request ---> ");
		final String dump = sb.toString();
		return dump;
	}

}
