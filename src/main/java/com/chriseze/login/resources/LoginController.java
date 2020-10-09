package com.chriseze.login.resources;

import com.chriseze.login.restartifacts.BaseResponse;
import com.chriseze.login.restartifacts.ClientPojo;
import com.chriseze.login.restartifacts.GenericResponse;
import com.chriseze.login.restartifacts.TalentPojo;
import com.chriseze.login.services.UserService;
import com.chriseze.login.utils.SecurityUtil;
import com.chriseze.yonder.utils.entities.Admin;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginController {

    @Inject
    private UserService userService;

    @Inject
    private SecurityUtil securityUtil;

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("/admin")
    public BaseResponse saveAdmin(@Valid Admin admin) {
        return userService.saveAdmin(admin);
    }

    @POST
    @Path("/admin/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public BaseResponse adminLogin(@NotBlank @FormParam("email") String email, @NotBlank @FormParam("password") String password) {
        return userService.adminLogin(email, password);
    }

    @POST
    @Path("/talent/new")
    public BaseResponse saveTalent(@Valid TalentPojo talentPojo) {
        return userService.saveTalent(talentPojo);
    }

    @POST
    @Path("/talent/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response talentLogin(@NotBlank @FormParam("email") String email, @NotBlank @FormParam("password") String password) {
        GenericResponse<TalentPojo> response = userService.talentLogin(email, password);

        String token = generateToken(email);
        return Response.ok(response).header(HttpHeaders.AUTHORIZATION, SecurityUtil.BEARER + " " + token).build();
    }

    @POST
    @Path("/client/new")
    public BaseResponse saveClient(@Valid ClientPojo clientPojo) {
        return userService.saveClient(clientPojo);
    }

    @POST
    @Path("/client/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response clientLogin(@NotBlank @FormParam("email") String email, @NotBlank @FormParam("password") String password) {
        GenericResponse<ClientPojo> response = userService.clientLogin(email, password);

        String token = generateToken(email);
        return Response.ok(response).header(HttpHeaders.AUTHORIZATION, SecurityUtil.BEARER + " " + token).build();
    }

    @POST
    @Path("/forgot-password")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public BaseResponse userForgotPassword(
            @NotBlank @FormParam("email") String email,
            @NotBlank @FormParam("newPassword") String newPassword,
            @NotBlank @FormParam("newPassword") String confNewPassword) {

        return userService.userForgotPassword(email, newPassword, confNewPassword);
    }

    private String generateToken(String email) {
        Key securityKey = securityUtil.getSecurityKey();

        String issuer = uriInfo.getBaseUri().toString();
        String audience = uriInfo.getAbsolutePath().toString();

        return Jwts.builder().setSubject(email).setIssuedAt(new Date())
                .setIssuer(issuer)
                .setAudience(audience)
                .setExpiration(securityUtil.toDate(LocalDateTime.now().plusHours(2)))
                .signWith(SignatureAlgorithm.HS512, securityKey).compact();
    }
}
