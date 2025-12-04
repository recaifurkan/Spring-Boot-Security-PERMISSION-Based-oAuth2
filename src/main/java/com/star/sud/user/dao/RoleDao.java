/**
 * 
 */
package com.star.sud.user.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.star.sud.user.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.star.sud.user.model.TRole;

/**
 * 
 *
 */
@Repository
public interface RoleDao extends JpaRepository<TRole, Long> {


	Set<TRole> findByNameIn(Collection<RoleType> roles);

}
