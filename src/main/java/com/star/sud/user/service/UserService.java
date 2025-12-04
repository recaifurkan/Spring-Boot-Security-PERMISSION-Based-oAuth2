/**
 * 
 */
package com.star.sud.user.service;

import java.util.List;

import com.star.sud.exception.UserExistsException;
import com.star.sud.exception.UserNotFoundException;
import com.star.sud.user.dto.UserDto;

/**
 * 
 *
 */
public interface UserService {

	/**
	 * @return
	 */
	List<UserDto> getUsers();

	/**
	 * @param id
	 * @return
	 * @throws UserNotFoundException
	 */
	UserDto findOne(long id) throws UserNotFoundException;

	/**
	 * @param id
	 */
	void delete(long id);

	/**
	 * @param userDto
	 * @return
	 * @throws UserExistsException
	 */
	UserDto createUser(UserDto userDto) throws UserExistsException;

	/**
	 * @param id
	 * @param userDto
	 * @return
	 * @throws UserNotFoundException
	 * @throws UserExistsException
	 */
	UserDto updateUser(long id, UserDto userDto) throws UserNotFoundException, UserExistsException;

}
