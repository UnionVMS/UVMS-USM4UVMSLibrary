package eu.europa.ec.fisheries.uvms.rest.security;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import java.util.Collections;

import org.junit.Test;

/**
 * Tests for the {@link UserRoleRequestWrapper}.
 */
public class UserRoleRequestWrapperTest {

	private static final String USER_NAME = "snoopy";
	private static final String ROLE_NAME = "role-name";

	@Test
	public void testIsUserInRoleDelegatesWhenRolesAreNotSet() {
		HttpServletRequest mockRequest = mock(HttpServletRequest.class);
		when(mockRequest.isUserInRole(ROLE_NAME)).thenReturn(true);
		UserRoleRequestWrapper sut = new UserRoleRequestWrapper(mockRequest, USER_NAME);
		boolean result = sut.isUserInRole(ROLE_NAME);
		assertTrue(result);
		verify(mockRequest).isUserInRole(ROLE_NAME);
		verifyNoMoreInteractions(mockRequest);
	}

	@Test
	public void testIsUserInRoleWhenRolesAreSet() {
		HttpServletRequest mockRequest = mock(HttpServletRequest.class);
		UserRoleRequestWrapper sut = new UserRoleRequestWrapper(USER_NAME, Collections.singleton(ROLE_NAME), mockRequest);
		boolean result = sut.isUserInRole(ROLE_NAME);
		assertTrue(result);
		verify(mockRequest, never()).isUserInRole(ROLE_NAME);
		verifyNoMoreInteractions(mockRequest);
	}
}
