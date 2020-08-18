package liquibase.hub.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Environment implements HubModel {

    private UUID id;
    private String jdbcUrl;
    private String name;
    private String description;
    private ZonedDateTime createDate;
    private ZonedDateTime updateDate;
    private ZonedDateTime removeDate;

    private Project prj;

    public UUID getId() {
        return id;
    }

    public Environment setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public Environment setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }


    public String getName() {
        return name;
    }

    public Environment setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Environment setDescription(String description) {
        this.description = description;

        return this;
    }

    public ZonedDateTime getCreateDate() {
        return createDate;
    }

    public Environment setCreateDate(ZonedDateTime createDate) {
        this.createDate = createDate;
        return this;
    }

    public ZonedDateTime getUpdateDate() {
        return updateDate;
    }

    public Environment setUpdateDate(ZonedDateTime updateDate) {
        this.updateDate = updateDate;
        return this;
    }

    public ZonedDateTime getRemoveDate() {
        return removeDate;
    }

    public Environment setRemoveDate(ZonedDateTime removeDate) {
        this.removeDate = removeDate;
        return this;
    }


    public Project getPrj() {
        return prj;
    }

    public Environment setPrj(Project prj) {
        this.prj = prj;
        return this;
    }

    @Override
    public String toString() {
        return "Environment " + jdbcUrl + " (" + id + ")";
    }
}
