package liquibase.hub.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Project implements HubModel {

    private UUID id;
    private String name;
    private ZonedDateTime createDate;

    @Override
    public UUID getId() {
        return id;
    }

    public Project setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Project setName(String name) {
        this.name = name;
        return this;
    }

    public ZonedDateTime getCreateDate() {
        return createDate;
    }

    public Project setCreateDate(ZonedDateTime createDate) {
        this.createDate = createDate;
        return this;

    }

    @Override
    public String toString() {
        return "Project " + getId() + " (" + getName() + ")";
    }
}
