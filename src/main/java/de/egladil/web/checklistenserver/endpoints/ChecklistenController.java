//=====================================================
// Projekt: checklistenserver
// (c) Heike Winkelvoß
//=====================================================

package de.egladil.web.checklistenserver.endpoints;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egladil.web.checklistenserver.domain.ChecklisteDaten;
import de.egladil.web.checklistenserver.filters.JwtAuthz;
import de.egladil.web.checklistenserver.service.ChecklistenService;
import de.egladil.web.commons.payload.MessagePayload;
import de.egladil.web.commons.payload.ResponsePayload;

/**
 * ChecklistenController
 */
@RequestScoped
@Path("checklisten")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@JwtAuthz
public class ChecklistenController {

	private static final Logger LOG = LoggerFactory.getLogger(ChecklistenController.class.getSimpleName());

	@Inject
	private ChecklistenService checklistenService;

	@Context
	private UriInfo uriInfo;

	@Context
	private ContainerRequestContext containerRequestContext;

	@Inject
	@Metric(name = "checklisten_counter")
	private Counter checklistenCounter;

	@Inject
	@Metric(name = "name_length_histogram")
	private Histogram nameLength;

	@Inject
	@Metric(name = "checklisten_adding_meter")
	private Meter addMeter;

	@GET
	@Timed(name = "load_checklisten-timer")
	public Response getChecklisten() {

		List<ChecklisteDaten> checklisten = checklistenService.loadChecklisten();

		ResponsePayload payload = new ResponsePayload(MessagePayload.info("OK: Anzahl Checklisten: " + checklisten.size()),
			checklisten);

		LOG.info("{}: checklisten geladen", getPrincipalAbbreviated());

		return Response.ok().entity(payload).build();
	}

	@GET
	@Path("/{kuerzel}")
	public Response getCheckliste(@Context
	final ContainerRequestContext crc, @PathParam(value = "kuerzel")
	final String kuerzel) {
		ChecklisteDaten checkliste = checklistenService.getCheckliste(kuerzel);
		return Response.ok(checkliste).build();
	}

	@POST
	@Metered(name = "checklisten_adding_meter")
	public Response checklisteAnlegen(final ChecklisteDaten daten) {

		ChecklisteDaten result = checklistenService.checklisteAnlegen(daten.getTyp(), daten.getName());

		addMeter.mark();
		checklistenCounter.inc();
		nameLength.update(daten.getName().length());

		LOG.info("{}: checkliste anglegt", getPrincipalAbbreviated());

		URI uri = uriInfo.getBaseUriBuilder()
			.path(ChecklistenController.class)
			.path(ChecklistenController.class, "getCheckliste")
			.build(result.getKuerzel());

		ResponsePayload payload = new ResponsePayload(MessagePayload.info("erfolgreich angelegt"), result);
		return Response.created(uri)
			.entity(payload)
			.build();
	}

	@PUT
	@Path("/{kuerzel}")
	public Response checklisteAendern(@PathParam(value = "kuerzel")
	final String kuerzel, final ChecklisteDaten daten) {

		if (!kuerzel.equals(daten.getKuerzel())) {
			LOG.error("Konflikt: kuerzel= '{}', daten.kuerzel = '{}'", kuerzel, daten.getKuerzel());
			ResponsePayload payload = ResponsePayload.messageOnly(MessagePayload.error("Precondition Failed"));
			return Response.status(412)
				.entity(payload)
				.build();
		}

		ResponsePayload payload = checklistenService.checklisteAendern(daten, kuerzel);
		LOG.info("{}: checkliste geändert", getPrincipalAbbreviated());
		return Response.ok().entity(payload).build();

	}

	@DELETE
	@Path("/{kuerzel}")
	@Metered(name = "checklisten_removing_meter")
	public Response checklisteLoeschen(@PathParam(value = "kuerzel")
	final String kuerzel) {

		checklistenService.checklisteLoeschen(kuerzel);

		checklistenCounter.dec();

		ResponsePayload payload = ResponsePayload.messageOnly(MessagePayload.info("erfolgreich gelöscht"));

		LOG.info("{}: checkliste {} gelöscht", getPrincipalAbbreviated(), kuerzel);
		return Response.ok()
			.entity(payload)
			.build();
	}

	private Principal getPrincipal() {
		return containerRequestContext.getSecurityContext().getUserPrincipal();
	}

	private String getPrincipalAbbreviated() {
		Principal userPrincipal = getPrincipal();
		return userPrincipal != null ? userPrincipal.getName().substring(0, 8) : null;
	}

}
