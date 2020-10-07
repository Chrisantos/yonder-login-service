package com.chriseze.login.resources;

import com.chriseze.login.auth.Authz;
import com.chriseze.login.restartifacts.BaseResponse;
import com.chriseze.login.restartifacts.ClientPojo;
import com.chriseze.login.restartifacts.GenericListResponse;
import com.chriseze.login.restartifacts.GenericResponse;
import com.chriseze.login.restartifacts.TalentPojo;
import com.chriseze.login.services.UserService;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authz
public class UserController {

    @Inject
    private UserService userService;

    @GET
    @Path("/talent/{id}")
    public GenericResponse<TalentPojo> getTalentById(@NotNull @PathParam("id") Long id) {
        return userService.getTalentById(id);
    }

    @GET
    @Path("/talent/")
    public GenericResponse<TalentPojo> getTalentByEmailOrName(@NotBlank @QueryParam("email") String emailOrName) {
        return userService.getTalentByEmailOrName(emailOrName);
    }

    @GET
    @Path("/talents")
    public GenericListResponse<TalentPojo> getAllTalents() {
        return userService.getAllTalents();
    }

    @PUT
    @Path("/talent")
    public BaseResponse editTalent(@Valid TalentPojo talentPojo) {
        return userService.updateTalent(talentPojo);
    }

    @GET
    @Path("/client/{id}")
    public GenericResponse<ClientPojo> getClientById(@NotNull @PathParam("id") Long id) {
        return userService.getClientById(id);
    }

    @GET
    @Path("/client/")
    public GenericResponse<ClientPojo> getClientByEmailOrName(@NotBlank @QueryParam("email") String emailOrName) {
        return userService.getClientByEmailOrName(emailOrName);
    }

    @GET
    @Path("/clients")
    public GenericListResponse<ClientPojo> getAllClients() {
        return userService.getAllClients();
    }

    @PUT
    @Path("/client")
    public BaseResponse editClient(@Valid ClientPojo clientPojo) {
        return userService.updateClient(clientPojo);
    }

    @POST
    @Path("/reset-password")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public BaseResponse resetUserPassword(
            @NotBlank @FormParam("email") String email,
            @NotBlank @FormParam("oldPassword") String oldPassword,
            @NotBlank @FormParam("newPassword") String newPassword,
            @NotBlank @FormParam("confNewPassword") String confNewPassword) {

        return userService.resetUserPassword(email, oldPassword, newPassword, confNewPassword);
    }

    //TODO Add 2FA for password change
    @POST
    @Path("/otp-request")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public BaseResponse requestOtp(@NotBlank @FormParam("phoneNumber") String phoneNumber) {
        return userService.requestOtp(phoneNumber);
    }

    @POST
    @Path("/otp-status")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public BaseResponse verifyOtp(@NotBlank @FormParam("phoneNumber") String phoneNumber, @NotBlank @FormParam("otp") String otp) {
        return userService.verifyOtp(phoneNumber, otp);
    }
}
