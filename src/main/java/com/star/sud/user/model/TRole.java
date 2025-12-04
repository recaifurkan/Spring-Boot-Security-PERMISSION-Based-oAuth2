/**
 * 
 */
package com.star.sud.user.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * 
 *
 */
@Entity
@Table(name = "T_ROLES")
public class TRole {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ROLE_ID")
	private Long rleId;

	@Enumerated(EnumType.STRING)
	@Column(name = "ROLE_NAME")
	private RoleType name;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "STATUS")
	private char status;

	@Column(name = "CREATED_DATE")
	private Date createdDate;

	@Column(name = "MODIFIED_DATE")
	private Date modifiedDate;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "T_ROLE_PERMISSIONS", joinColumns = @JoinColumn(name = "ROLE_ID"), inverseJoinColumns = @JoinColumn(name = "PERMISSION_ID"))
	private Set<TPermission> permissions;

	// Properties
	/////////////////
	/**
	 * @return the name
	 */
	public RoleType getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(RoleType name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the status
	 */
	public char getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(char status) {
		this.status = status;
	}

	/**
	 * @return the rleId
	 */
	public Long getRleId() {
		return rleId;
	}

	/**
	 * @param rleId the rleId to set
	 */
	public void setRleId(Long rleId) {
		this.rleId = rleId;
	}

	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the modifiedDate
	 */
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * @param modifiedDate the modifiedDate to set
	 */
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Set<TPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<TPermission> permissions) {
		this.permissions = permissions;
	}

}
