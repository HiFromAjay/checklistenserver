// =====================================================
// Project: checklistenserver
// (c) Heike Winkelvoß
// =====================================================
package de.egladil.web.checklistenserver.service;

import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egladil.web.checklistenserver.error.ChecklistenRuntimeException;
import de.egladil.web.checklistenserver.error.ClientAuthException;
import de.egladil.web.checklistenserver.error.InaccessableEndpointException;
import de.egladil.web.checklistenserver.error.LogmessagePrefixes;
import de.egladil.web.checklistenserver.restclient.TokenExchangeRestClient;
import de.egladil.web.commons_validation.payload.MessagePayload;
import de.egladil.web.commons_validation.payload.OAuthClientCredentials;
import de.egladil.web.commons_validation.payload.ResponsePayload;

/**
 * TokenExchangeService
 */
@ApplicationScoped
public class TokenExchangeService {

	private static final Logger LOG = LoggerFactory.getLogger(TokenExchangeService.class);

	@Inject
	@ConfigProperty(name = "auth.client-id")
	String clientId;

	@Inject
	@ConfigProperty(name = "auth.client-secret")
	String clientSecret;

	@Inject
	@RestClient
	TokenExchangeRestClient tokenExchangeClient;

	public String exchangeTheOneTimeToken(final String oneTimeToken) {

		final String nonce = UUID.randomUUID().toString();

		OAuthClientCredentials clientCredentials = OAuthClientCredentials.create(clientId, clientSecret, nonce);

		try {

			Response response = tokenExchangeClient.exchangeOneTimeTokenWithJwt(oneTimeToken, clientCredentials);

			ResponsePayload responsePayload = response.readEntity(ResponsePayload.class);

			return this.checkNonceAndExtractTheJwt(nonce, responsePayload);

		} catch (WebApplicationException e) {

			ResponsePayload responsePayload = e.getResponse().readEntity(ResponsePayload.class);

			MessagePayload messagePayload = responsePayload.getMessage();

			String message = "Konnte das oneTimeToken nicht gegen das JWT tauschen: " + messagePayload.getMessage();

			LOG.error(message + e.getMessage(), e);

			throw new ChecklistenRuntimeException(message + e.getMessage(), e);

		} catch (ProcessingException processingException) {

			LOG.error("endpoint authprovider ist nicht erreichbar");

			throw new InaccessableEndpointException("Der Endpoint authprovider ist nicht erreichbar. ");
		}
	}

	private String checkNonceAndExtractTheJwt(final String expectedNonce, final ResponsePayload responsePayload) {

		MessagePayload messagePayload = responsePayload.getMessage();

		if (messagePayload.isOk()) {

			@SuppressWarnings("unchecked")
			Map<String, String> dataMap = (Map<String, String>) responsePayload.getData();
			String responseNonce = dataMap.get("nonce");

			if (!expectedNonce.equals(responseNonce)) {

				{

					LOG.error(LogmessagePrefixes.BOT + "zurückgesendetes nonce stimmt nicht");
					throw new ClientAuthException();
				}
			}

			return dataMap.get("jwt");
		} else {

			LOG.error("Authentisierung des Clients hat nicht geklappt: {} - {}", messagePayload.getLevel(),
				messagePayload.getMessage());
			throw new ClientAuthException();
		}
	}

}
