//=====================================================
// Projekt: checklistenserver
// (c) Heike Winkelvoß
//=====================================================

package de.egladil.web.checklistenserver.endpoints;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.kumuluz.ee.logs.cdi.Log;
import com.kumuluz.ee.logs.cdi.LogParams;

import de.egladil.web.checklistenserver.domain.Checkliste;
import de.egladil.web.checklistenserver.domain.ChecklisteDaten;
import de.egladil.web.checklistenserver.payload.MessagePayload;
import de.egladil.web.checklistenserver.payload.ResponsePayload;
import de.egladil.web.checklistenserver.service.ChecklistenService;

/**
 * ChecklistenResource
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Log(LogParams.METRICS)
@RequestScoped
@Path("checklisten")
public class ChecklistenResource {

	private static final String RESOURCE_BASE_URL = "/cl/checklisten/";

	@Inject
	private ChecklistenService checklistenService;

	@GET
	public Response getChecklisten() {

		List<Checkliste> checklisten = checklistenService.loadChecklisten();

		ResponsePayload payload = new ResponsePayload(MessagePayload.info("OK: Anzahl Checklisten: " + checklisten.size()),
			checklisten);

		return Response.ok().entity(payload).build();
	}

	@POST
	public Response checklisteAnlegen(final ChecklisteDaten daten) {

		ChecklisteDaten result = checklistenService.checklisteAnlegen(daten);
		ResponsePayload payload = new ResponsePayload(MessagePayload.info("erfolgreich angelegt"), result);
		// TODO: hier die BaseUrl vom Server lesen. und davorklatschen
		return Response.status(201).entity(payload).header("Location", result.getLocation(RESOURCE_BASE_URL)).build();
	}

	public Response checklisteAendern(@PathParam(value = "kuerzel")
	final String kuerzel, final ChecklisteDaten daten) {

		ResponsePayload payload = checklistenService.checklisteAendern(daten);
		return Response.ok().entity(payload).build();

	}

	@DELETE
	@Path("/{kuerzel}")
	public Response checklisteLoeschen(@PathParam(value = "kuerzel")
	final String kuerzel) {

		checklistenService.checklisteLoeschen(kuerzel);
		ResponsePayload payload = ResponsePayload.messageOnly(MessagePayload.info("erfolgreich gelöscht"));

		return Response.ok().entity(payload).build();
	}

}
