/**
 * 
 */
package com.star.sud.user.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.star.sud.user.model.TRole;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.star.sud.exception.UserExistsException;
import com.star.sud.exception.UserNotFoundException;
import com.star.sud.user.dao.RoleDao;
import com.star.sud.user.dao.UserDao;
import com.star.sud.user.dto.UserDto;
import com.star.sud.user.model.RoleType;
import com.star.sud.user.model.TUser;
import com.star.sud.user.service.UserService;

/**
 * 
 *
 */
@Transactional
@Service("userService")
public class UserServiceImpl implements UserDetailsService, UserService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		TUser user = userDao.findByUsername(username);
		if (user == null)
			throw new UsernameNotFoundException("Invalid username or password.");

		Set<GrantedAuthority> grantedRoleAuthorities = user
                .getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toString().toUpperCase()))
                .collect(Collectors.toSet());

        Set<GrantedAuthority> grantedPermissionAuthorities = user
                .getRoles().stream().flatMap(role -> role.getPermissions().stream())
				.map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission.getName().toString().toUpperCase()))
				.collect(Collectors.toSet());

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.addAll(grantedRoleAuthorities);
        grantedAuthorities.addAll(grantedPermissionAuthorities);

		return new User(user.getUsername(), user.getPassword(), grantedAuthorities);
	}

	@Override
	public List<UserDto> getUsers() {
		List<UserDto> userDto = new ArrayList<UserDto>();
		userDao.findAll().iterator().forEachRemaining(user -> userDto.add(user.toUserDto()));
		return userDto;
	}

	@Override
	public UserDto findOne(long id) throws UserNotFoundException {

		TUser tUser = userDao.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User record not found for the id: " + id));

		return tUser.toUserDto();
	}

	@Override
	public void delete(long id) {
		userDao.deleteById(id);
	}

	@Override
	public UserDto createUser(UserDto userDto) throws UserExistsException {
		TUser tUser = userDao.findByUsername(userDto.getUsername());
		if (tUser != null)
			throw new UserExistsException("User name already exists!!");

		tUser = userDao.findByEmail(userDto.getEmail());
		if (tUser != null)
			throw new UserExistsException("Email Already Exists!!");

		tUser = new TUser();
		BeanUtils.copyProperties(userDto, tUser);
		tUser.setPassword(passwordEncoder.encode(userDto.getPassword()));

		tUser.setCreatedDate(Calendar.getInstance().getTime());
		tUser.setModifiedDate(Calendar.getInstance().getTime());
		tUser.setStatus('A');

		List<RoleType> roleTypes = userDto.getRole().stream().map(RoleType::valueOf).collect(Collectors.toList());
		userDto.setRole(roleTypes.stream().map(RoleType::name).collect(Collectors.toList()));
		tUser.setRoles(roleDao.findByNameIn(roleTypes));
		TUser save = userDao.save(tUser);
		userDto.setUserId(save.getUserId());
		return userDto;
	}

	@Override
	public UserDto updateUser(long id, UserDto userDto) throws UserNotFoundException, UserExistsException {
		TUser existing = userDao.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User record not found for the id: " + id));

		if (userDto.getUsername() != null && !userDto.getUsername().equals(existing.getUsername())) {
			TUser usernameMatch = userDao.findByUsername(userDto.getUsername());
			if (usernameMatch != null && !usernameMatch.getUserId().equals(id))
				throw new UserExistsException("User name already exists!!");
			existing.setUsername(userDto.getUsername());
		}

		if (userDto.getEmail() != null && !userDto.getEmail().equals(existing.getEmail())) {
			TUser emailMatch = userDao.findByEmail(userDto.getEmail());
			if (emailMatch != null && !emailMatch.getUserId().equals(id))
				throw new UserExistsException("Email Already Exists!!");
			existing.setEmail(userDto.getEmail());
		}

		if (userDto.getFirstName() != null)
			existing.setFirstName(userDto.getFirstName());
		if (userDto.getLastName() != null)
			existing.setLastName(userDto.getLastName());
		if (userDto.getPassword() != null && !userDto.getPassword().isEmpty())
			existing.setPassword(passwordEncoder.encode(userDto.getPassword()));

		if (!CollectionUtils.isEmpty(userDto.getRole())) {
			List<RoleType> roleTypes = userDto.getRole().stream().map(RoleType::valueOf).collect(Collectors.toList());
			existing.setRoles(roleDao.findByNameIn(roleTypes));
		}

		existing.setModifiedDate(Calendar.getInstance().getTime());
		TUser saved = userDao.save(existing);
		return saved.toUserDto();
	}

}
