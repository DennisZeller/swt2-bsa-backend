package de.bogenliga.application.services.v1.user.service;

import de.bogenliga.application.business.mannschaftsmitglied.api.types.MannschaftsmitgliedDO;
import de.bogenliga.application.business.user.api.UserComponent;
import de.bogenliga.application.business.user.api.UserRoleComponent;
import de.bogenliga.application.business.user.api.UserProfileComponent;
import de.bogenliga.application.business.user.api.types.UserDO;
import de.bogenliga.application.business.user.api.types.UserProfileDO;
import de.bogenliga.application.business.user.api.types.UserRoleDO;
import de.bogenliga.application.business.user.api.types.UserWithPermissionsDO;
import de.bogenliga.application.common.errorhandling.ErrorCode;
import de.bogenliga.application.common.service.ServiceFacade;
import de.bogenliga.application.common.service.UserProvider;
import de.bogenliga.application.common.validation.Preconditions;
import de.bogenliga.application.services.common.errorhandling.ErrorDTO;
import de.bogenliga.application.services.v1.user.mapper.UserDTOMapper;
import de.bogenliga.application.services.v1.user.mapper.UserRoleDTOMapper;
import de.bogenliga.application.services.v1.user.mapper.UserProfileDTOMapper;
import de.bogenliga.application.services.v1.user.model.*;
import de.bogenliga.application.springconfiguration.security.WebSecurityConfiguration;
import de.bogenliga.application.springconfiguration.security.jsonwebtoken.JwtTokenProvider;
import de.bogenliga.application.springconfiguration.security.permissions.RequiresOwnIdentity;
import de.bogenliga.application.springconfiguration.security.permissions.RequiresPermission;
import de.bogenliga.application.springconfiguration.security.types.UserPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * I´m a REST resource and handle configuration CRUD requests over the HTTP protocol.
 *
 * @author Andre Lehnert, eXXcellent solutions consulting & software gmbh
 * @see <a href="https://en.wikipedia.org/wiki/Create,_read,_update_and_delete">Wikipedia - CRUD</a>
 * @see <a href="https://en.wikipedia.org/wiki/Representational_state_transfer">Wikipedia - REST</a>
 * @see <a href="https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol">Wikipedia - HTTP</a>
 * @see <a href="https://en.wikipedia.org/wiki/Design_by_contract">Wikipedia - Design by contract</a>
 * @see <a href="https://spring.io/guides/gs/actuator-service/">
 * Building a RESTful Web Service with Spring Boot Actuator</a>
 * @see <a href="https://www.baeldung.com/building-a-restful-web-service-with-spring-and-java-based-configuration">
 * Build a REST API with Spring 4 and Java Config</a>
 * @see <a href="https://www.baeldung.com/spring-autowire">Guide to Spring @Autowired</a>
 */
@RestController
@CrossOrigin
@RequestMapping("v1/user")
public class UserService implements ServiceFacade {

    private static final String PRECONDITION_MSG_USER_ID = "User ID must not be null or negative";
    private static final String PRECONDITION_MSG_ROLE_ID = "User Role ID must not be null or negative";
    private static final String PRECONDITION_MSG_USER_EMAIL = "Benutzer email must not be null";

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final JwtTokenProvider jwtTokenProvider;

    private final WebSecurityConfiguration webSecurityConfiguration;

    private final UserComponent userComponent;

    private final UserRoleComponent userRoleComponent;

    private final UserProfileComponent userProfileComponent;


    @Autowired
    public UserService(final JwtTokenProvider jwtTokenProvider,
                       //final AuthenticationManager authenticationManager
                       final WebSecurityConfiguration webSecurityConfiguration,
                       final UserComponent userComponent,
                       final UserRoleComponent userRoleComponent,
                       final UserProfileComponent userProfileComponent) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.webSecurityConfiguration = webSecurityConfiguration;
        this.userComponent = userComponent;
        this.userRoleComponent = userRoleComponent;
        this.userProfileComponent = userProfileComponent;
    }


    @RequestMapping(
            method = RequestMethod.POST,
            value = "/signin",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity login(@RequestBody final UserCredentialsDTO credentials) {
        Preconditions.checkNotNull(credentials, "Credentials must not be null");
        Preconditions.checkNotNullOrEmpty(credentials.getUsername(), "Username must not be null or empty");
        Preconditions.checkNotNullOrEmpty(credentials.getPassword(), "Password must not be null or empty");

        ErrorDTO errorDetails = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    credentials.getUsername(),
                    credentials.getPassword());
            authenticationToken.setDetails(credentials.getCode());
                    //credentials.getCode());
            final Authentication authentication = webSecurityConfiguration.authenticationManagerBean()
                    .authenticate(authenticationToken);

            if (authentication.isAuthenticated()) {
                // create payload
                final UserWithPermissionsDO userWithPermissionsDO = (UserWithPermissionsDO) authentication.getPrincipal();

                if(userWithPermissionsDO.isActive()) {
                    final UserSignInDTO userSignInDTO = UserDTOMapper.toUserSignInDTO.apply(userWithPermissionsDO);
                    userSignInDTO.setJwt(jwtTokenProvider.createToken(authentication));

                    final HttpHeaders headers = new HttpHeaders();
                    headers.add("Authorization", "Bearer " + userSignInDTO.getJwt());

                    return ResponseEntity.status(HttpStatus.OK).headers(headers).body(userSignInDTO);
                } else {
                    errorDetails = new ErrorDTO(ErrorCode.INVALID_SIGN_IN_CREDENTIALS, "Sign in failed");
                    return new ResponseEntity<>(errorDetails, HttpStatus.UNPROCESSABLE_ENTITY);
                }
            } else {
                if (authentication.getDetails() != null) {
                    errorDetails = (ErrorDTO) authentication.getDetails();
                }
            }
        } catch (final Exception e) { // NOSONAR
            LOG.warn("An error occured while SignIn of user {}", credentials.getUsername(), e);
        }

        // return error details from authentication or a default error
        errorDetails = errorDetails != null ? errorDetails : new ErrorDTO(ErrorCode.INVALID_SIGN_IN_CREDENTIALS,
                "Sign in failed");
        return new ResponseEntity<>(errorDetails, HttpStatus.UNPROCESSABLE_ENTITY);
    }


    @RequestMapping(
            method = RequestMethod.GET,
            value = "/me",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO whoAmI(final HttpServletRequest requestWithHeader) {
        final String jwt = JwtTokenProvider.resolveToken(requestWithHeader);

        return jwtTokenProvider.resolveUserSignInDTO(jwt);
    }


    /**
     * Returns the user profile for a given id.
     *
     * @param id
     *
     * @return
     */
    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresOwnIdentity
    public UserProfileDTO getUserProfileById(@PathVariable("id") final Long id) {
        Preconditions.checkNotNull(id, "UserID must not be null.");
        Preconditions.checkArgument(id >= 0, "UserID must not be negative.");

        LOG.debug("Receive 'getUserProfileById' request with ID '{}'", id);

        final UserProfileDO userProfileDO = userProfileComponent.findById(id);
        return UserProfileDTOMapper.toDTO.apply(userProfileDO);
    }


    /**
     * I persist a new password for the current user and return this user entry.
     * <p>
     * Usage:
     * <pre>{@code Request: PUT /v1/user
     * Body:
     * {
     *    "id": "app.bogenliga.frontend.autorefresh.active",
     *    "value": "true"
     * }
     * }</pre>
     * <pre>{@code Response:
     *  {
     *    "id": "app.bogenliga.frontend.autorefresh.active",
     *    "value": "true"
     *  }
     * }</pre>
     *
     * @param uptcredentials of the request body
     *
     * @return {@link UserDTO} as JSON
     */


    @RequestMapping(
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermission(UserPermission.CAN_MODIFY_SYSTEMDATEN)
    public UserDTO update(final HttpServletRequest requestWithHeader,
                          @RequestBody final UserChangeCredentialsDTO uptcredentials) {
        Preconditions.checkNotNull(uptcredentials, "Credentials must not be null");
        Preconditions.checkNotNullOrEmpty(uptcredentials.getPassword(), "Password must not be null or empty");
        Preconditions.checkNotNullOrEmpty(uptcredentials.getNewPassword(), "New password must not be null or empty");

        ErrorDTO errorDetails = null;

        //update password is limited to own password,
        // therefore we get the current user id based on system utils

        final String jwt = jwtTokenProvider.resolveToken(requestWithHeader);
        final Long userId = jwtTokenProvider.getUserId(jwt);

        final UserDO userDO = new UserDO();
        userDO.setId(userId);

        //update password
        final UserDO userUpdatedDO = userComponent.update(userDO, uptcredentials.getPassword(),
                uptcredentials.getNewPassword(), userId);

        //prepare return DTO
        final UserDTO userUpdatedDTO = UserDTOMapper.toUserDTO.apply(userUpdatedDO);
        return userUpdatedDTO;
    }


    /**
     * Service to update multiple user roles. It will remove all roles that are not send in this request
     * @param requestWithHeader
     * @param updatedUserRoles there is a single UserRoleDTO for every role that is send to this service
     * @return
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/uptRoles",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermission(UserPermission.CAN_MODIFY_SYSTEMDATEN)
    public List<UserRoleDTO> updateRoles(final HttpServletRequest requestWithHeader,
                                  @RequestBody final List<UserRoleDTO> updatedUserRoles) {
        Preconditions.checkNotNull(updatedUserRoles, "UserRole-Definition must not be null");
        Preconditions.checkNotNull(updatedUserRoles.get(0).getId(), PRECONDITION_MSG_USER_ID);
        Preconditions.checkNotNull(updatedUserRoles.get(0).getRoleId(), PRECONDITION_MSG_ROLE_ID);

        ErrorDTO errorDetails = null;

        final String jwt = jwtTokenProvider.resolveToken(requestWithHeader);
        final Long userId = jwtTokenProvider.getUserId(jwt);

        List<UserRoleDO> updatedUserRolesDo = new ArrayList<>();
        for(UserRoleDTO userRoleDTO : updatedUserRoles) {
            final UserRoleDO userRoleDO = new UserRoleDO();
            userRoleDO.setId(userRoleDTO.getId());
            userRoleDO.setRoleId(userRoleDTO.getRoleId());
            updatedUserRolesDo.add(userRoleDO);
        }
        final List<UserRoleDO> userRoleUpdatedDO = userRoleComponent.update(updatedUserRolesDo, userId);

        List<UserRoleDTO> userRoleUpdatedDTO = new ArrayList<>();
        for(UserRoleDO userRoleDO : userRoleUpdatedDO){
            userRoleUpdatedDTO.add(UserRoleDTOMapper.toDTO.apply(userRoleDO));
        }

        return userRoleUpdatedDTO;
    }


    /**
     * I return all user entries of the database.
     * <p>
     * <p>
     * Usage:
     * <pre>{@code Request: GET /v1/user}</pre>
     * <p>
     * [ { "id": "app.bogenliga.frontend.autorefresh.active", "value": "true" }, { "id":
     * "app.bogenliga.frontend.autorefresh.interval", "value": "10" } ] }
     * </pre>
     *
     * @return list of {@link UserDTO} as JSON
     */
    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermission(UserPermission.CAN_READ_SYSTEMDATEN)
    public List<UserRoleDTO> findAll() {
        final List<UserRoleDO> userRoleDOList = userRoleComponent.findAll();
        return userRoleDOList.stream().map(UserRoleDTOMapper.toDTO).collect(Collectors.toList());
    }


    /**
     * I return a specific user-role entries of the database.
     * <p>
     * <p>
     * Usage:
     * <pre>{@code Request: GET /v1/user/userrole/id}</pre>
     * <p>
     * [ { "id": "app.bogenliga.frontend.autorefresh.active", "value": "true" }, { "id":
     * "app.bogenliga.frontend.autorefresh.interval", "value": "10" } ] }
     * </pre>
     *
     * @return list of {@link UserDTO} as JSON
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/userrole/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermission(UserPermission.CAN_READ_DEFAULT)
    public List<UserRoleDTO> getUserRoleById(@PathVariable("id") final long id) {
        Preconditions.checkArgument(id >= 0, "ID must not be negative.");

        LOG.debug("Receive 'getUserRoleById' request with ID '{}'", id);

        final List<UserRoleDO> userRoleDOlist = userRoleComponent.findById(id);
        List<UserRoleDTO> userRoleDTOS = new ArrayList<>();
        for(UserRoleDO userRoleDO : userRoleDOlist){
            userRoleDTOS.add(UserRoleDTOMapper.toDTO.apply(userRoleDO));
        }
        return userRoleDTOS;
    }


    /**
     * I persist a new user and return this user entry.
     * <p>
     * Usage:
     * <pre>{@code Request: POST /v1/user
     * Body:
     * {
     *    "id": "app.bogenliga.frontend.autorefresh.active",
     *    "value": "true"
     * }
     * }</pre>
     * <pre>{@code Response:
     *  {
     *    "id": "app.bogenliga.frontend.autorefresh.active",
     *    "value": "true"
     *  }
     * }</pre>
     *
     * @param userCredentialsDTO of the request body
     *
     * @return {@link UserDTO} as JSON
     */


    @RequestMapping(method = RequestMethod.POST,
            value = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresPermission(UserPermission.CAN_MODIFY_SYSTEMDATEN)
    public UserDTO create(final HttpServletRequest requestWithHeader,
                          @RequestBody final UserCredentialsDTO userCredentialsDTO) {

        Preconditions.checkNotNull(userCredentialsDTO, "User Credentials must not be null");
        Preconditions.checkNotNull(userCredentialsDTO.getUsername(), PRECONDITION_MSG_USER_ID);
        Preconditions.checkNotNull(userCredentialsDTO.getPassword(), PRECONDITION_MSG_USER_EMAIL);

        LOG.debug("Receive 'create' request with username '{}', password '{}', using2FA {}",
                userCredentialsDTO.getUsername(),
                userCredentialsDTO.getPassword(),
                userCredentialsDTO.isUsing2FA());

        userCredentialsDTO.getCode();

        final String jwt = jwtTokenProvider.resolveToken(requestWithHeader);
        final Long userId = jwtTokenProvider.getUserId(jwt);

        // user anlegen
        final UserDO userCreatedDO = userComponent.create(userCredentialsDTO.getUsername(),
                userCredentialsDTO.getPassword(), userId, userCredentialsDTO.isUsing2FA());
        //default rolle anlegen (User)
        final UserRoleDO userRoleCreatedDO = userRoleComponent.create(userCreatedDO.getId(), userId);
        return UserDTOMapper.toDTO.apply(userCreatedDO);
    }


    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    @RequiresPermission(UserPermission.CAN_DELETE_SYSTEMDATEN)
    public UserDTO deactivate(@PathVariable("id") final long id, final Principal principal) {
        Preconditions.checkArgument(id >= 0, "Id must not be negative.");

        LOG.debug("Receive 'delete' request with Id '{}'", id);

        // allow value == null, the value will be ignored
        final UserDO userDO = new UserDO();
        userDO.setId(id);

        final UserDO existingDO = userComponent.findById(id);

        final UserDO userUpdatedDO = userComponent.update(userDO, !existingDO.isActive());
        final UserDTO userUpdatedDTO = UserDTOMapper.toUserDTO.apply(userUpdatedDO);
        return userUpdatedDTO;
    }

}
