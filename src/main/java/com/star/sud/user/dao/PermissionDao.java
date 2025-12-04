package com.star.sud.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.star.sud.user.model.PermissionType;
import com.star.sud.user.model.TPermission;

@Repository
public interface PermissionDao extends JpaRepository<TPermission, Long> {

	TPermission findByName(PermissionType name);
}

