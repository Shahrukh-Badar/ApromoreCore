package org.apromore.dao.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.beans.factory.annotation.Configurable;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * ProcessFragmentMap generated by hbm2java
 */
@Entity
@Table(name = "process_fragment_map")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Configurable("processFragmentMap")
public class ProcessFragmentMap implements Serializable {

    /**
     * Hard coded for interoperability.
     */
    private static final long serialVersionUID = -9072538487638485548L;

    private Integer id;
    private FragmentVersion fragmentVersion;
    private ProcessModelVersion processModelVersion;


    /**
     * Public Constructor.
     */
    public ProcessFragmentMap() { }


    /**
     * returns the Id of this Object.
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return this.id;
    }

    /**
     * Sets the Id of this Object
     * @param id the new Id.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fragmentVersionId")
    public FragmentVersion getFragmentVersion() {
        return this.fragmentVersion;
    }

    public void setFragmentVersion(final FragmentVersion newFragmentVersion) {
        this.fragmentVersion = newFragmentVersion;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processModelVersionId")
    public ProcessModelVersion getProcessModelVersion() {
        return this.processModelVersion;
    }

    public void setProcessModelVersion(final ProcessModelVersion newProcessModelVersion) {
        this.processModelVersion = newProcessModelVersion;
    }

}


