package com.chriseze.login.entities;

import com.chriseze.yonder.utils.base.AbstractBaseEntity;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "RECOMMENDATION")
public class Recommendation extends AbstractBaseEntity {
    private static final long serialVersionUID = -8071351217939652646L;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TALENT", nullable = false)
    private Talent talent;

    @PrePersist
    public void setCreateDate() {
        this.setCreateDate(LocalDate.now());
    }
}
