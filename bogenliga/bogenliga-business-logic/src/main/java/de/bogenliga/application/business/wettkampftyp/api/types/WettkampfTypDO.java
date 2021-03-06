package de.bogenliga.application.business.wettkampftyp.api.types;

import java.time.OffsetDateTime;
import java.util.Objects;
import de.bogenliga.application.common.component.types.CommonDataObject;
import de.bogenliga.application.common.component.types.DataObject;

/**
 * Contains the values of the wettkampftyp business entity.
 *
 * @author Arthur Huber
 */
public class WettkampfTypDO extends CommonDataObject implements DataObject {


    private static final long serialVersionUID = -3541537678685603149L;
    private Long id;
    private String name;

    public WettkampfTypDO (final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public WettkampfTypDO(final Long id, final String name,
                       final OffsetDateTime createdAtUtc, final Long createdByUserId, final Long version) {
        this.id = id;
        this.name = name;

        this.createdAtUtc = createdAtUtc;
        this.createdByUserId = createdByUserId;
        this.version = version;

    }


    /**
     * Constructor with optional parameters
     *
     * @param id
     * @param createdAtUtc
     * @param createdByUserId
     * @param lastModifiedAtUtc
     * @param lastModifiedByUserId
     * @param version
     */
    public WettkampfTypDO(final Long id, final String name,
                       final OffsetDateTime createdAtUtc, final Long createdByUserId,
                       final OffsetDateTime lastModifiedAtUtc,
                       final Long lastModifiedByUserId,
                       final Long version) {
       this(id, name, createdAtUtc, createdByUserId, version);

       this.lastModifiedAtUtc = lastModifiedAtUtc;
       this.lastModifiedByUserId = lastModifiedByUserId;

    }


    /**
     * Constructor with id for deleting existing entries
     *
     * @param id
     */
    public WettkampfTypDO(final Long id) {
        this.id = id;
    }


    /* Autogenerated getter and setters */


    public Long getId() {
        return id;
    }


    public void setId(final Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, name,
                createdByUserId, lastModifiedAtUtc,
                lastModifiedByUserId, version);
    }


    @Override
    public boolean equals(final Object o) {
        if (o instanceof WettkampfTypDO) {
            final WettkampfTypDO that = (WettkampfTypDO) o;
            return (Objects.equals(getId(), that.getId()) &&
                    Objects.equals(getName(), that.getName()));
        }
        return false;

    }

}
