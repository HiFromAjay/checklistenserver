//=====================================================
// Projekt: checklistenserver
// (c) Heike Winkelvoß
//=====================================================

package de.egladil.web.checklistenserver.dao;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

/**
* ChecklisteDaoTest
*/
public class ChecklisteDaoTest {

	@Test
	void testUniqueIdentityQuery() {
		// Arrange
		String identifierName = "hühnchen";
		ChecklisteDao dao = new ChecklisteDao();

		// Act
		String stmt = dao.getSubjectQuery(identifierName);

		// Assert
		assertEquals("select c from Checkliste c where c.kuerzel=:hühnchen", stmt);
	}
}
