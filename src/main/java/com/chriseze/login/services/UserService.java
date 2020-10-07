package com.chriseze.login.services;

import com.chriseze.login.enums.OtpResponseEnum;
import com.chriseze.login.enums.OtpStatus;
import com.chriseze.login.enums.ResponseEnum;
import com.chriseze.login.restartifacts.BaseResponse;
import com.chriseze.login.restartifacts.ClientPojo;
import com.chriseze.login.restartifacts.DocumentPojo;
import com.chriseze.login.restartifacts.GenericListResponse;
import com.chriseze.login.restartifacts.GenericResponse;
import com.chriseze.login.restartifacts.OtpPojo;
import com.chriseze.login.restartifacts.SocialMediaPojo;
import com.chriseze.login.restartifacts.TalentPojo;
import com.chriseze.login.repositories.UserRepository;
import com.chriseze.login.utils.ProxyUtil;
import com.chriseze.login.utils.SecurityUtil;
import com.chriseze.login.utils.Utils;
import com.chriseze.login.entities.*;
import com.chriseze.yonder.utils.enums.*;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import java.io.Serializable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class UserService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private ProxyUtil proxyUtil;

    @Inject
    private SecurityUtil securityUtil;

    private final Utils utils = new Utils();

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public BaseResponse saveAdmin(Admin admin) {
        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
        if (admin == null) {
            return response;
        }

        int count = userRepository.countAdminByEmail(admin.getEmail());

        if (count > 0) {
            return response;
        }

        if (admin.getId() == null) {
            Map<String, String> credMap = securityUtil.hashPassword(admin.getPassword());

            admin.setPassword(credMap.get(SecurityUtil.HASHED_PASSWORD_KEY));
            admin.setSalt(credMap.get(SecurityUtil.SALT_KEY));

            response = proxyUtil.executeWithNewTransaction(() -> userRepository.saveEntity(admin));
            credMap.clear();
        }

        return response;
    }

    public BaseResponse adminLogin(String email, String password) {
        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
        if (StringUtils.isBlank(email) || StringUtils.isBlank(password)) {
            return response;
        }

        Admin admin = proxyUtil.executeWithNewTransaction(() -> userRepository.getAdminByEmail(email));
        if (admin == null) {
            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
            return response;
        }

        if (!securityUtil.passwordsMatch(admin.getPassword(), admin.getSalt(), password)) {
            response.assignResponseEnum(ResponseEnum.INVALID_EMAIL_OR_PASSWORD);
            return response;
        }

        response.assignResponseEnum(ResponseEnum.SUCCESSFUL_LOGIN);
        return response;
    }

    public BaseResponse saveTalent(TalentPojo talentPojo) {
        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
        if (talentPojo == null) {
            return response;
        }

        if (talentPojo.getEmail() == null || !isEmailValid(talentPojo.getEmail())) {
            response.setDescription("Supplied email address is invalid");
            return response;
        }

        int count =  userRepository.countUserByEmail(talentPojo.getEmail());
        if (count > 0) {
            response.assignResponseEnum(ResponseEnum.DUPLICATE_USER);
            return response;
        }

        if (talentPojo.getDocuments() == null || talentPojo.getDocuments().isEmpty()) {
            response.setDescription("Documents must be provided");
            return response;
        }

        Talent talent = new Talent();
        talent.setEmail(talentPojo.getEmail());
        talent.setAddress(talentPojo.getAddress());
        talent.setIndustry(EnumUtils.getEnum(Industry.class, talentPojo.getIndustry()));

        if (StringUtils.isNotBlank(talentPojo.getGender())) {
            talent.setGender(EnumUtils.getEnum(Gender.class, talentPojo.getGender()));
        }

        if (StringUtils.isNotBlank(talentPojo.getStatus())) {
            talent.setStatus(EnumUtils.getEnum(StatusEnum.class, talentPojo.getStatus()));
        }

        if (StringUtils.isNotBlank(talentPojo.getPhoneNumber())) {
            talent.setPhoneNumber(talentPojo.getPhoneNumber().trim());
        }

        talent.setBio(talentPojo.getBio());
        talent.setHourlyRate(talentPojo.getHourlyRate());
        talent.setName(talentPojo.getName());
        talent.setLevel(Level.ROOKIE);

        Map<String, String> credMap = securityUtil.hashPassword(talentPojo.getPassword());

        talent.setPassword(credMap.get(SecurityUtil.HASHED_PASSWORD_KEY));
        talent.setSalt(credMap.get(SecurityUtil.SALT_KEY));

        Set<Skill> skillSet = new HashSet<>();
        for (String name : talentPojo.getSkills()) {
            Skill skill = new Skill();
            skill.setName(name);
            skill.setTalent(talent);
            skillSet.add(skill);
        }
        talent.setSkills(skillSet);

        Set<SocialMedia> socialMediaSet = new HashSet<>();
        for (SocialMediaPojo socialMediaPojo : talentPojo.getSocialMedia()) {
            SocialMedia socialMedia = new SocialMedia();
            socialMedia.setName(socialMediaPojo.getName());
            socialMedia.setHandle(socialMediaPojo.getHandle());
            socialMedia.setTalent(talent);
            socialMediaSet.add(socialMedia);
        }
        talent.setSocialMedia(socialMediaSet);

        Set<Document> documentSet = new HashSet<>();
        for (DocumentPojo documentPojo : talentPojo.getDocuments()) {
            Document document = new Document();
            document.setTitle(documentPojo.getTitle());
            document.setDocument(documentPojo.getDocument());
            document.setTalent(talent);
            documentSet.add(document);
        }
        talent.setDocuments(documentSet);

        response = proxyUtil.executeWithNewTransaction(() -> userRepository.saveEntity(talent));
        credMap.clear();

        return response;
    }

    public GenericResponse<TalentPojo> talentLogin(String email, String password) {
        GenericResponse<TalentPojo> response = new GenericResponse<>(ResponseEnum.ERROR);
        if (StringUtils.isBlank(email) || StringUtils.isBlank(password)) {
            return response;
        }

        Talent talent = proxyUtil.executeWithNewTransaction(() -> userRepository.getTalentByEmailOrName(email));
        if (talent == null) {
            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
            return response;
        }

        if (!securityUtil.passwordsMatch(talent.getPassword(), talent.getSalt(), password)) {
            response.assignResponseEnum(ResponseEnum.INVALID_EMAIL_OR_PASSWORD);
            return response;
        }

        response.assignResponseEnum(ResponseEnum.SUCCESS);
        response.setResult(getTalentDetails(talent));
        return response;
    }

    public GenericListResponse<TalentPojo> getAllTalents() {
        GenericListResponse<TalentPojo> response = new GenericListResponse<>();

        List<Talent> talentList = proxyUtil.executeWithNewTransaction(() -> userRepository.getAllTalents());
        if (talentList == null || talentList.isEmpty()) {
            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
            return response;
        }

        List<TalentPojo> talentPojoList = new ArrayList<>();
        for (Talent talent : talentList) {
            talentPojoList.add(getTalent(talent));
        }

        response.assignResponseEnum(ResponseEnum.SUCCESS);
        response.setResults(talentPojoList);
        return response;
    }

    public GenericResponse<TalentPojo> getTalentById(Long id) {
        GenericResponse<TalentPojo> response = new GenericResponse<>(ResponseEnum.ERROR);
        if (id == null) {
            return response;
        }

        Talent talent = proxyUtil.executeWithNewTransaction(() -> userRepository.getTalentById(id));
        if (talent == null) {
            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
            return response;
        }

        response.assignResponseEnum(ResponseEnum.SUCCESS);
        response.setResult(getTalentDetails(talent));
        return response;
    }

    public GenericResponse<TalentPojo> getTalentByEmailOrName(String emailOrName) {
        GenericResponse<TalentPojo> response = new GenericResponse<>(ResponseEnum.ERROR);
        if (StringUtils.isBlank(emailOrName)) {
            return response;
        }

        Talent talent = proxyUtil.executeWithNewTransaction(() -> userRepository.getTalentByEmailOrName(emailOrName));
        if (talent == null) {
            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
            return response;
        }

        response.assignResponseEnum(ResponseEnum.SUCCESS);
        response.setResult(getTalentDetails(talent));
        return response;
    }

    public BaseResponse updateTalent(TalentPojo talentPojo) {
        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
        if (talentPojo == null) {
            return response;
        }

        Talent talent = proxyUtil.executeWithNewTransaction(() -> userRepository.getTalentByEmailOrName(talentPojo.getEmail()));
        if (talent == null) {
            response.setDescription("No user found with such user ID");
            return response;
        }

        if (talentPojo.getSocialMedia() != null && !talentPojo.getSocialMedia().isEmpty()) {
            Set<SocialMedia> socialMediaSet = new HashSet<>();
            for (SocialMediaPojo socialMediaPojo : talentPojo.getSocialMedia()) {
                SocialMedia socialMedia = new SocialMedia();
                socialMedia.setName(socialMediaPojo.getName());
                socialMedia.setHandle(socialMediaPojo.getHandle());
                socialMedia.setTalent(talent);
                socialMediaSet.add(socialMedia);
            }
            talent.setSocialMedia(socialMediaSet);
        }

        if (talentPojo.getSkills() != null && !talentPojo.getSkills().isEmpty()) {
            Set<Skill> skillSet = new HashSet<>();
            for (String name : talentPojo.getSkills()) {
                Skill skill = new Skill();
                skill.setName(name);
                skill.setTalent(talent);
                skillSet.add(skill);
            }
            talent.setSkills(skillSet);
        }

        if (talentPojo.getDocuments() != null && !talentPojo.getDocuments().isEmpty()) {
            Set<Document> documentSet = new HashSet<>();
            for (DocumentPojo documentPojo : talentPojo.getDocuments()) {
                Document document = new Document();
                document.setTitle(documentPojo.getTitle());
                document.setDocument(documentPojo.getDocument());
                document.setTalent(talent);
                documentSet.add(document);
            }
            talent.setDocuments(documentSet);
        }

        talent.setName(talentPojo.getName());
        talent.setHourlyRate(talentPojo.getHourlyRate());
        talent.setBio(talentPojo.getBio());
        talent.setAddress(talentPojo.getAddress());
        talent.setGender(EnumUtils.getEnum(Gender.class, talentPojo.getGender()));
        talent.setPhoneNumber(talentPojo.getPhoneNumber());
        talent.setIndustry(EnumUtils.getEnum(Industry.class, talentPojo.getIndustry()));

        Talent updatedTalent = proxyUtil.executeWithNewTransaction(() -> userRepository.update(talent));
        if (updatedTalent == null) {
            response.setDescription("Couldn't update user's records");
            return response;
        }

        response.setCode(ResponseEnum.SUCCESS.getCode());
        response.setDescription("User's records updated successfully");
        return response;
    }

    public BaseResponse saveClient(ClientPojo clientPojo) {
        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
        if (clientPojo == null) {
            return response;
        }

        if (clientPojo.getEmail() == null || !isEmailValid(clientPojo.getEmail())) {
            response.setDescription("Supplied email address is invalid");
            return response;
        }

        int count =  userRepository.countUserByEmail(clientPojo.getEmail());
        if (count > 0) {
            response.assignResponseEnum(ResponseEnum.DUPLICATE_USER);
            return response;
        }

        Client client = new Client();
        client.setEmail(clientPojo.getEmail());
        client.setAddress(clientPojo.getAddress());

        if (StringUtils.isNotBlank(clientPojo.getGender())) {
            client.setGender(EnumUtils.getEnum(Gender.class, clientPojo.getGender()));
        }

        if (StringUtils.isNotBlank(clientPojo.getPhoneNumber())) {
            client.setPhoneNumber(clientPojo.getPhoneNumber().trim());
        }

        client.setName(clientPojo.getName());

        Map<String, String> credMap = securityUtil.hashPassword(clientPojo.getPassword());

        client.setPassword(credMap.get(SecurityUtil.HASHED_PASSWORD_KEY));
        client.setSalt(credMap.get(SecurityUtil.SALT_KEY));

        response = proxyUtil.executeWithNewTransaction(() -> userRepository.saveEntity(client));
        credMap.clear();

        return response;
    }

    public GenericResponse<ClientPojo> clientLogin(String email, String password) {
        GenericResponse<ClientPojo> response = new GenericResponse<>(ResponseEnum.ERROR);
        if (StringUtils.isBlank(email) || StringUtils.isBlank(password)) {
            return response;
        }

        Client client = proxyUtil.executeWithNewTransaction(() -> userRepository.getClientByEmailOrName(email));
        if (client == null) {
            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
            return response;
        }

        if (!securityUtil.passwordsMatch(client.getPassword(), client.getSalt(), password)) {
            response.assignResponseEnum(ResponseEnum.INVALID_EMAIL_OR_PASSWORD);
            return response;
        }



        response.assignResponseEnum(ResponseEnum.SUCCESS);
        response.setResult(getClient(client));
        return response;
    }

    public GenericListResponse<ClientPojo> getAllClients() {
        GenericListResponse<ClientPojo> response = new GenericListResponse<>();

        List<Client> clientList = proxyUtil.executeWithNewTransaction(() -> userRepository.getAllClients());
        if (clientList == null || clientList.isEmpty()) {
            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
            return response;
        }

        List<ClientPojo> clientPojoList = new ArrayList<>();
        for (Client client : clientList) {
            clientPojoList.add(getClient(client));
        }

        response.assignResponseEnum(ResponseEnum.SUCCESS);
        response.setResults(clientPojoList);
        return response;
    }

    public GenericResponse<ClientPojo> getClientById(Long id) {
        GenericResponse<ClientPojo> response = new GenericResponse<>(ResponseEnum.ERROR);
        if (id == null) {
            return response;
        }

        Client cl = proxyUtil.executeWithNewTransaction(() -> userRepository.getClientById(id));
        if (cl == null) {
            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
            return response;
        }

        response.assignResponseEnum(ResponseEnum.SUCCESS);
        response.setResult(getClient(cl));
        return response;
    }

    public GenericResponse<ClientPojo> getClientByEmailOrName(String emailOrName) {
        GenericResponse<ClientPojo> response = new GenericResponse<>(ResponseEnum.ERROR);
        if (StringUtils.isBlank(emailOrName)) {
            return response;
        }

        Client client = proxyUtil.executeWithNewTransaction(() -> userRepository.getClientByEmailOrName(emailOrName));
        if (client == null) {
            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
            return response;
        }

        response.assignResponseEnum(ResponseEnum.SUCCESS);
        response.setResult(getClient(client));
        return response;
    }

    public BaseResponse updateClient(ClientPojo clientPojo) {
        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
        if (clientPojo == null) {
            return response;
        }

        Client client = proxyUtil.executeWithNewTransaction(() -> userRepository.getClientByEmailOrName(clientPojo.getEmail()));
        if (client == null) {
            response.setDescription("No user found with such email address");
            return response;
        }

        client.setName(clientPojo.getName());
        client.setAddress(clientPojo.getAddress());
        client.setGender(EnumUtils.getEnum(Gender.class, clientPojo.getGender()));
        client.setPhoneNumber(clientPojo.getPhoneNumber());

        Client updatedClient = proxyUtil.executeWithNewTransaction(() -> userRepository.update(client));
        if (updatedClient == null) {
            response.setDescription("Couldn't update user's records");
            return response;
        }

        response.setCode(ResponseEnum.SUCCESS.getCode());
        response.setDescription("User's records updated successfully");
        return response;
    }

    private boolean isEmailValid(String email) {
        if (!StringUtils.isBlank(email)) {
            Pattern pattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
            Matcher matcher = pattern.matcher(email);
            return matcher.matches();
        }
        return false;
    }

//    public BaseResponse talentForgotPassword(String email, String newPassword) {
//        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
//        if (StringUtils.isBlank(email) || StringUtils.isBlank(newPassword)) {
//            return response;
//        }
//
//        Talent talent = proxyUtil.executeWithNewTransaction(() -> userRepository.getTalentByEmailOrName(email));
//        if (talent == null) {
//            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
//            return response;
//        }
//
//        Talent updatedTalent = updateTalentPassword(talent, newPassword);
//        if (updatedTalent == null) {
//            response.setDescription("An error occurred while trying to update new password");
//            return response;
//        }
//
//        response.assignResponseEnum(ResponseEnum.SUCCESS);
//        return response;
//    }
//
//    public BaseResponse clientForgotPassword(String email, String newPassword) {
//        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
//        if (StringUtils.isBlank(email) || StringUtils.isBlank(newPassword)) {
//            return response;
//        }
//
//        Client client = proxyUtil.executeWithNewTransaction(() -> userRepository.getClientByEmailOrName(email));
//        if (client == null) {
//            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
//            return response;
//        }
//
//        Client updatedClient = updateClientPassword(client, newPassword);
//        if (updatedClient == null) {
//            response.setDescription("An error occurred while trying to update new password");
//            return response;
//        }
//
//        response.assignResponseEnum(ResponseEnum.SUCCESS);
//        return response;
//    }

    public BaseResponse userForgotPassword(String email, String newPassword, String confNewPassword) {
        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
        if (StringUtils.isBlank(email) || StringUtils.isBlank(newPassword) || StringUtils.isBlank(confNewPassword)) {
            return response;
        }

        Talent talent = proxyUtil.executeWithNewTransaction(() -> userRepository.getTalentByEmailOrName(email));
        Client client = null;
        if (talent == null) {
            client = proxyUtil.executeWithNewTransaction(() -> userRepository.getClientByEmailOrName(email));
        }

        if (talent == null && client == null) {
            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
            return response;
        }

        if (!StringUtils.equalsIgnoreCase(newPassword, confNewPassword)) {
            response.setDescription("new password doesn't match with the confirm new password.");
            return response;
        }

        Talent updatedTalent = null;
        Client updatedClient = null;
        if (talent != null) {
            updatedTalent = updateTalentPassword(talent, newPassword);
        } else {
            updatedClient = updateClientPassword(client, newPassword);
        }

        if (updatedTalent == null && updatedClient == null) {
            response.setDescription("An error occurred while trying to update new password");
            return response;
        }

        response.assignResponseEnum(ResponseEnum.SUCCESS);
        return response;
    }

//    public BaseResponse talentRestPassword(String email, String oldPassword, String newPassword, String confNewPassword) {
//        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
//        if (StringUtils.isBlank(email) || StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword) || StringUtils.isBlank(confNewPassword) ) {
//            return response;
//        }
//
//        Talent talent = proxyUtil.executeWithNewTransaction(() -> userRepository.getTalentByEmailOrName(email));
//        if (talent == null) {
//            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
//            return response;
//        }
//
//        if (!securityUtil.passwordsMatch(talent.getPassword(), talent.getSalt(), oldPassword)) {
//            throw new SecurityException("Email or password not valid");
//        }
//
//        if (StringUtils.equalsIgnoreCase(oldPassword, newPassword)) {
//            response.setDescription("Old and new passwords cannot be the same.");
//            return response;
//        }
//
//        if (!StringUtils.equalsIgnoreCase(newPassword, confNewPassword)) {
//            response.setDescription("new password doesn't match with the confirm new password.");
//            return response;
//        }
//
//        Talent updatedTalent = updateTalentPassword(talent, newPassword);
//        if (updatedTalent == null) {
//            response.setDescription("An error occurred while trying to update new password");
//            return response;
//        }
//
//        response.assignResponseEnum(ResponseEnum.SUCCESS);
//        return response;
//    }

//    public BaseResponse clientResetPassword(String email, String oldPassword, String newPassword, String confNewPassword) {
//        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
//        if (StringUtils.isBlank(email) || StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword) || StringUtils.isBlank(confNewPassword) ) {
//            return response;
//        }
//
//        Client client = proxyUtil.executeWithNewTransaction(() -> userRepository.getClientByEmailOrName(email));
//        if (client == null) {
//            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
//            return response;
//        }
//
//        if (!securityUtil.passwordsMatch(client.getPassword(), client.getSalt(), oldPassword)) {
//            throw new SecurityException("Email or password not valid");
//        }
//
//        if (StringUtils.equalsIgnoreCase(oldPassword, newPassword)) {
//            response.setDescription("Old and new passwords cannot be the same.");
//            return response;
//        }
//
//        if (!StringUtils.equalsIgnoreCase(newPassword, confNewPassword)) {
//            response.setDescription("new password doesn't match with the confirm new password.");
//            return response;
//        }
//
//        Client updatedClient = updateClientPassword(client, newPassword);
//        if (updatedClient == null) {
//            response.setDescription("An error occurred while trying to update new password");
//            return response;
//        }
//
//        response.assignResponseEnum(ResponseEnum.SUCCESS);
//        return response;
//    }

    public BaseResponse resetUserPassword(String email, String oldPassword, String newPassword, String confNewPassword) {
        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
        if (StringUtils.isBlank(email) || StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword) || StringUtils.isBlank(confNewPassword) ) {
            return response;
        }

        Talent talent = proxyUtil.executeWithNewTransaction(() -> userRepository.getTalentByEmailOrName(email));

        Client client = null;
        if (talent == null) {
            client = proxyUtil.executeWithNewTransaction(() -> userRepository.getClientByEmailOrName(email));
        }

        if (talent == null && client == null) {
            response.assignResponseEnum(ResponseEnum.NO_USER_FOUND);
            return response;
        }

        String password = talent != null? talent.getPassword() : client.getPassword();
        String salt = talent != null? talent.getSalt() : client.getSalt();

        if (!securityUtil.passwordsMatch(password, salt, oldPassword)) {
            response.assignResponseEnum(ResponseEnum.INVALID_EMAIL_OR_PASSWORD);
            return response;
        }

        if (StringUtils.equalsIgnoreCase(oldPassword, newPassword)) {
            response.setDescription("Old and new passwords cannot be the same.");
            return response;
        }

        if (!StringUtils.equalsIgnoreCase(newPassword, confNewPassword)) {
            response.setDescription("new password doesn't match with the confirm new password.");
            return response;
        }

        Talent updatedTalent = null;
        Client updatedClient = null;
        if (talent != null) {
            updatedTalent = updateTalentPassword(talent, newPassword);
        } else {
            updatedClient = updateClientPassword(client, newPassword);
        }

        if (updatedTalent == null && updatedClient == null) {
            response.setDescription("An error occurred while trying to update new password");
            return response;
        }

        response.assignResponseEnum(ResponseEnum.SUCCESS);
        return response;
    }

    private Talent updateTalentPassword(Talent talent, String newPassword) {
        Map<String, String> credMap = securityUtil.hashPassword(newPassword);

        talent.setPassword(credMap.get(SecurityUtil.HASHED_PASSWORD_KEY));
        talent.setSalt(credMap.get(SecurityUtil.SALT_KEY));
        credMap.clear();

        return proxyUtil.executeWithNewTransaction(() -> userRepository.update(talent));
    }

    private Client updateClientPassword(Client client, String newPassword) {
        Map<String, String> credMap = securityUtil.hashPassword(newPassword);

        client.setPassword(credMap.get(SecurityUtil.HASHED_PASSWORD_KEY));
        client.setSalt(credMap.get(SecurityUtil.SALT_KEY));
        credMap.clear();

        return proxyUtil.executeWithNewTransaction(() -> userRepository.update(client));
    }

    public GenericResponse<OtpPojo> requestOtp(String phoneNumber) {
        GenericResponse<OtpPojo> response = new GenericResponse<>(ResponseEnum.ERROR);
        if (StringUtils.isBlank(phoneNumber)) {
            return response;
        }

        String resp = proxyUtil.executeWithNewTransaction(() -> {
            String result = "::";
            try {
                result = generateOTP(phoneNumber);
            } catch (PersistenceException | SecurityException | IllegalStateException e) {
                logger.error("An error occurred saving token", e);
            }
            return result;
        });

        String[] respSplit = resp.split(":");
        if (respSplit[0] == null) {
            response.setDescription("An error occurred while generating OTP. Please try again");
            return response;
        }

        OtpPojo otpPojo = new OtpPojo();
        otpPojo.setOtp(respSplit[1]);
        otpPojo.setPhoneNumber(phoneNumber);

        String message = "Your one-time password is " + respSplit[1] + ". It expires in " + respSplit[2] + " minutes.";
        logger.debug("OTS Log: message is {}", message);

        boolean sendSMS = sendSms(phoneNumber, message);
        if (sendSMS) {
            message = OtpResponseEnum.SENT_TO_USER.getMessage();
            response.setResult(otpPojo);
            response.setDescription(message);
            response.setCode(ResponseEnum.SUCCESS.getCode());
            logger.debug("OTS Log: message is {}", message);
        } else {
            message = OtpResponseEnum.ERROR_GENERATING.getMessage();
            response.setDescription(message);
            response.setCode(ResponseEnum.SUCCESS.getCode());
            logger.debug("OTS Log: message is {}", message);
        }

        return response;
    }

    private String generateOTP(String phoneNumber) {
        int otpLength = 10;

        String otpToken = utils.generateDigits(otpLength);

        long otpExpirationTime = 5;
        OtpStatusRecord otpStatusRecord = new OtpStatusRecord();
        otpStatusRecord.setPhoneNumber(phoneNumber);
        otpStatusRecord.setOtp(otpToken);
        otpStatusRecord.setOtpUsed(Boolean.FALSE);

        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(otpExpirationTime);
        otpStatusRecord.setExpirationTime(localDateTime);

        Serializable serializable = null;
        try {
            proxyUtil.executeWithNewTransaction(() -> userRepository.create(otpStatusRecord));
            serializable = otpStatusRecord.getId();
            logger.info("OTP Status record created for phoneNumber {}", phoneNumber);
        } catch (PersistenceException | SecurityException | IllegalStateException e) {
            logger.error("Error while generatiing otp", e);
        }

        return serializable + ":" + otpToken + ":" + otpExpirationTime;
    }

    private boolean sendSms(String phoneNumber, String msg) {
        Twilio.init(Utils.ACCOUNT_SID, Utils.AUTH_TOKEN);

        try {
            phoneNumber = "+234".concat(phoneNumber.replaceFirst("0*", ""));
            String fromNumber = "+12107916712";

            PhoneNumber recipient  = new PhoneNumber(phoneNumber);
            PhoneNumber sender     = new PhoneNumber(fromNumber);

            MessageCreator messageCreator = Message.creator(recipient, sender,  msg);
            Message sms     = messageCreator.create();
            String  sentId  = sms.getSid();

            return sentId != null && !sentId.isEmpty();
        } catch (Exception e) {
            logger.error("Error sending sms", e);
        }
        return false;
    }

    public BaseResponse verifyOtp(String phoneNumber, String otp) {
        BaseResponse response = new BaseResponse(ResponseEnum.ERROR);
        if (StringUtils.isBlank(phoneNumber) || StringUtils.isBlank(otp)) {
            return response;
        }

        OtpStatus otpStatus = getOtpStatus(phoneNumber, otp);

        String message;
        switch (otpStatus) {
            case ERROR:
                message = OtpResponseEnum.ERROR_VERIFYING.getMessage();
                break;
            case EXPIRED:
                message = OtpResponseEnum.EXPIRED_OTP.getMessage();
                break;
            case INVALID:
                message = OtpResponseEnum.NO_OTP_RECORD.getMessage();
                break;
            case VALID:
                message = OtpResponseEnum.VALID_OTP.getMessage();
                break;
            default:
                message = OtpResponseEnum.ERROR_VERIFYING.getMessage();
        }
        response.setDescription(message);
        response.setCode(ResponseEnum.SUCCESS.getCode());
        return response;
    }

    private OtpStatus getOtpStatus(String phoneNumber, String otp) {
        OtpStatusRecord otpStatusRecord = proxyUtil.executeWithNewTransaction(() -> userRepository.getOtpRecord(phoneNumber));
        if (otpStatusRecord == null) {
            return OtpStatus.ERROR;
        }

        if (!StringUtils.equalsIgnoreCase(otpStatusRecord.getOtp(), otp)) {
            return OtpStatus.ERROR;
        }

        OtpStatus otpStatus;
        LocalDateTime now = LocalDateTime.now();
        if (otpStatusRecord.getExpirationTime().isBefore(now)) {
            otpStatus = OtpStatus.EXPIRED;
        } else {
            otpStatus = OtpStatus.VALID;
            if (otp == null || otpStatusRecord == null || otpStatusRecord.getOtp() == null || !otp.equalsIgnoreCase(otpStatusRecord.getOtp())) {
                otpStatus = OtpStatus.INVALID;
            }
        }

        otpStatusRecord.setOtpUsed(true);
        otpStatusRecord.setTimeUsed(now);

        try {
            proxyUtil.executeWithNewTransaction(() -> userRepository.update(otpStatusRecord));

        } catch (PersistenceException | SecurityException | IllegalStateException e) {
            logger.error("Error verifying OTP", e);
        }
        return otpStatus;
    }

    private TalentPojo getTalentDetails(Talent talent) {
        TalentPojo talentPojo = new TalentPojo();
        talentPojo.setName(talent.getName());
        talentPojo.setAddress(talent.getAddress());
        talentPojo.setBio(talent.getBio());
        talentPojo.setEmail(talent.getEmail());
        talentPojo.setGender(talent.getGender().name());
        talentPojo.setHourlyRate(talent.getHourlyRate());
        talentPojo.setPhoneNumber(talent.getPhoneNumber());

        Set<Recommendation> recommendations = talent.getRecommendations();
        if (recommendations != null && !recommendations.isEmpty()) {

            Set<String> projectsRecommended = new HashSet<>();
            for (Recommendation recommendation : recommendations) {
                projectsRecommended.add(recommendation.getProject().getTitle());
            }
            talentPojo.setRecommendations(projectsRecommended);
        }

        Set<String> skills = new HashSet<>();
        if (talent.getSkills() != null) {
            talent.getSkills().forEach(skill -> skills.add(skill.getName()));
        }
        talentPojo.setSkills(skills);

        Set<SocialMediaPojo> socialMediaSet = new HashSet<>();
        if (talent.getSocialMedia() != null) {
            talent.getSocialMedia().forEach(socialMedia ->
                    socialMediaSet.add(new SocialMediaPojo(socialMedia.getName(), socialMedia.getHandle())));
        }
        talentPojo.setSocialMedia(socialMediaSet);

        return talentPojo;
    }

    private TalentPojo getTalent(Talent talent) {
        TalentPojo talentPojo = new TalentPojo();
        talentPojo.setName(talent.getName());
        talentPojo.setEmail(talent.getEmail());
        talentPojo.setGender(talent.getGender().name());
        talentPojo.setHourlyRate(talent.getHourlyRate());
        talentPojo.setPhoneNumber(talent.getPhoneNumber());
        return talentPojo;
    }

    private ClientPojo getClient(Client client) {
        ClientPojo clientPojo = new ClientPojo();
        clientPojo.setName(client.getName());
        clientPojo.setAddress(client.getAddress());
        clientPojo.setEmail(client.getEmail());
        clientPojo.setGender(client.getGender().name());
        clientPojo.setPhoneNumber(client.getPhoneNumber());
        return clientPojo;
    }


}
