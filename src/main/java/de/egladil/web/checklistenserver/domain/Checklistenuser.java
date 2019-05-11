//=====================================================
// Projekt: checklistenserver
// (c) Heike Winkelvoß
//=====================================================

package de.egladil.web.checklistenserver.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.egladil.web.commons.payload.HateoasPayload;

/**
 * Checklistenuser
 */
@Entity
@Table(name = "user")
public class Checklistenuser implements Checklistenentity {

	/* serialVersionUID */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	@JsonIgnore
	private Long id;

	@NotNull
	@Size(min = 1, max = 36)
	@Column(name = "UUID")
	private String uuid;

	@Version
	@Column(name = "VERSION")
	@JsonIgnore
	private int version;

	@Transient
	private Set<String> roles = new HashSet<>();

	@Transient
	private HateoasPayload hateoasPayload;

	/**
	 * Erzeugt eine Instanz von Checklistenuser
	 */
	public Checklistenuser() {
		roles.add("user");
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	@Override
	public HateoasPayload getHateoasPayload() {
		return hateoasPayload;
	}

	@Override
	public void setHateoasPayload(final HateoasPayload hateoasPayload) {
		this.hateoasPayload = hateoasPayload;
	}

	/**
	 * Rolle werden momentan konstant codiert und nicht in der DB abgelegt.
	 *
	 * @return Set
	 */
	public Set<String> getRoles() {
		return roles;
	}
}
