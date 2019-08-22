//=====================================================
// Projekt: checklistenserver
// (c) Heike Winkelvoß
//=====================================================

package de.egladil.web.checklistenserver;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.auth.LoginConfig;

/**
 * ChecklistenServerApp
 */
@LoginConfig(authMethod = "MP-JWT")
@ApplicationPath("/checklisten-api")
public class ChecklistenServerApp extends Application {

	/**
	 *
	 */
	public ChecklistenServerApp() {
		System.out.println("==== ChecklistenServerApp created ====");
	}

}
