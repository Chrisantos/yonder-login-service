package com.chriseze.login.repositories;

import com.chriseze.login.enums.ResponseEnum;
import com.chriseze.login.restartifacts.BaseResponse;
import com.chriseze.login.utils.ProxyUtil;
import com.chriseze.login.entities.*;
import com.chriseze.yonder.utils.repositories.AbstractBaseRepository;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class UserRepository extends AbstractBaseRepository {

    @PersistenceContext(unitName = "appService")
    private EntityManager entityManager;

    @Inject
    private ProxyUtil proxyUtil;

    protected static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }


    public BaseResponse saveEntity(Object obj) {
        BaseResponse response = new BaseResponse(ResponseEnum.SUCCESS);
        try {
            proxyUtil.executeWithNewTransaction(() -> create(obj));
        } catch (Exception e) {
            response.assignResponseEnum(ResponseEnum.ERROR);
        }
        return response;
    }

    public Admin getAdminByEmail(String email) {
        try {
            List<Admin> admin = entityManager.createNamedQuery(Admin.FIND_BY_EMAIL, Admin.class)
                    .setParameter("email", email).getResultList();

            if (admin != null && !admin.isEmpty()) {
                return admin.get(0);
            }
        } catch (Exception e) {
            logger.error("Error occured getting admin by email", e);
        }
        return null;
    }

    public List<Talent> getAllTalents() {
        List<Talent> talentList = new ArrayList<>();
        try {
            talentList = entityManager.createNamedQuery(Talent.FIND_ALL, Talent.class).getResultList();
        } catch (Exception e) {
            logger.error("Error occured getting all talent", e);
        }
        return talentList;
    }

    public Talent getTalentById(Long id) {
        return findById(Talent.class, id);
    }

    public Talent getTalentByEmailOrName(String param) {
        try {
            List<Talent> talent = entityManager.createNamedQuery(Talent.FIND_BY_EMAIL_OR_NAME, Talent.class)
                    .setParameter("param", param).getResultList();

            if (talent != null && !talent.isEmpty()) {
                return talent.get(0);
            }
        } catch (Exception e) {
            logger.error("Error occured getting all talents by email or name", e);
        }
        return null;
    }

    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        try {
            clients = entityManager.createNamedQuery(Client.FIND_ALL, Client.class).getResultList();
        } catch (Exception e) {
            logger.error("Error occured getting all clients", e);
        }
        return clients;
    }

    @SuppressWarnings("unchecked")
    public int countUserByEmail(String email) {
        try {
            List<Long> talents = entityManager.createQuery("select t.id from Talent t where t.email = :email")
                    .setParameter("email", email).getResultList();

            List<Long> clients = entityManager.createQuery("select c.id from Client c where c.email = :email")
                    .setParameter("email", email).getResultList();

            if (clients != null && !clients.isEmpty()) {
                talents.addAll(clients);
            }

            if (talents != null) {
                return talents.size();
            }

        } catch (Exception e) {
            logger.error("Error occurred counting users by email", e);
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public int countAdminByEmail(String email) {
        try {
            List<Long> admins = entityManager.createQuery("select a.id from Admin a where a.email = :email")
                    .setParameter("email", email).getResultList();

            if (admins != null) {
                return admins.size();
            }
        } catch (Exception e) {
            logger.error("Error occured counting admins by email", e);
        }
        return 0;
    }

    public Client getClientById(Long id) {
        return findById(Client.class, id);
    }

    public Client getClientByEmailOrName(String param) {
        try {
            List<Client> clients = entityManager.createNamedQuery(Client.FIND_BY_EMAIL_OR_NAME, Client.class)
                    .setParameter("param", param).getResultList();

            if (clients != null && !clients.isEmpty()) {
                return clients.get(0);
            }
        } catch (Exception e) {
            logger.error("Error occured getting client by email or name", e);
        }
        return null;
    }

    public List<Document> getDocumentByTalent(String email) {
        List<Document> documents = new ArrayList<>();
        try {
            documents = entityManager.createNamedQuery(Document.FIND_BY_TALENT, Document.class)
                    .setParameter("email", email).getResultList();
        } catch (Exception e) {
            logger.error("Error occured getting all documents by talent", e);
        }
        return documents;
    }

    public Document getDocumentByTitle(String title) {
        try {
            List<Document> documents = entityManager.createNamedQuery(Document.FIND_BY_TITLE, Document.class)
                    .setParameter("title", title).getResultList();

            if (documents != null && !documents.isEmpty()) {
                return documents.get(0);
            }
        } catch (Exception e) {
            logger.error("Error occured getting document by title", e);
        }
        return null;
    }

    public List<Skill> getAllSkillByTalent(String email) {
        List<Skill> skills = new ArrayList<>();
        try {
            skills = entityManager.createNamedQuery(Skill.FIND_ALL, Skill.class)
                    .setParameter("email", email).getResultList();
        } catch (Exception e) {
            logger.error("Error occured getting all skills by talent", e);
        }
        return skills;
    }

    public List<SocialMedia> getAllSocialMediaByTalent(String email) {
        List<SocialMedia> socialMedia = new ArrayList<>();
        try {
            socialMedia = entityManager.createNamedQuery(SocialMedia.FIND_ALL, SocialMedia.class)
                    .setParameter("email", email).getResultList();
        } catch (Exception e) {
            logger.error("Error occured getting all social media by talent", e);
        }
        return socialMedia;
    }

    public OtpStatusRecord getOtpRecord(String phoneNumber) {
        try {
            List<OtpStatusRecord> otpStatusRecords = entityManager.createNamedQuery(OtpStatusRecord.FIND_BY_PHONE_AND_USED, OtpStatusRecord.class)
                    .setParameter("phoneNumber", phoneNumber)
                    .setParameter("otpUsed", Boolean.FALSE)
                    .setFirstResult(0)
                    .setMaxResults(1)
                    .getResultList();

            if (otpStatusRecords != null && !otpStatusRecords.isEmpty()) {
                return otpStatusRecords.get(0);
            }
        } catch (Exception e) {
            logger.error("Error occured getting otp record by phone number", e);
        }
        return null;
    }


}
