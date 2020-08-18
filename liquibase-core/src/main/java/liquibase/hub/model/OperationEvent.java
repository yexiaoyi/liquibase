package liquibase.hub.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public class OperationEvent implements HubModel {

    private UUID id;
    private String eventType;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;

    private OperationEventLog operationEventLog;
    private OperationEventStatus operationEventStatus;

    public OperationEvent() {

    }

    @Override
    public UUID getId() {
        return id;
    }

    public OperationEvent setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getEventType() {
        return eventType;
    }

    public OperationEvent setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public OperationEvent setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public OperationEvent setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public OperationEventLog getOperationEventLog() {
        return operationEventLog;
    }

    public OperationEvent setOperationEventLog(OperationEventLog operationEventLog) {
        this.operationEventLog = operationEventLog;
        return this;
    }

    public OperationEventStatus getOperationEventStatus() {
        return operationEventStatus;
    }

    public OperationEvent setOperationEventStatus(OperationEventStatus operationEventStatus) {
        this.operationEventStatus = operationEventStatus;
        return this;
    }

    public static class OperationEventLog implements HubModel {
        private UUID id;
        private String logMessage;
        private ZonedDateTime createDate;
        private ZonedDateTime timestampLog;

        @Override
        public UUID getId() {
            return id;
        }

        public OperationEventLog setId(UUID id) {
            this.id = id;
            return this;
        }

        public String getLogMessage() {
            return logMessage;
        }

        public OperationEventLog setLogMessage(String logMessage) {
            this.logMessage = logMessage;
            return this;
        }

        public ZonedDateTime getCreateDate() {
            return createDate;
        }

        public OperationEventLog setCreateDate(ZonedDateTime createDate) {
            this.createDate = createDate;
            return this;
        }

        public ZonedDateTime getTimestampLog() {
            return timestampLog;
        }

        public OperationEventLog setTimestampLog(ZonedDateTime timestampLog) {
            this.timestampLog = timestampLog;
            return this;
        }
    }

    public static class OperationEventStatus implements HubModel {
        private UUID id;
        private String operationEventStatusType;
        private String statusMessage;

        @Override
        public UUID getId() {
            return id;
        }

        public OperationEventStatus setId(UUID id) {
            this.id = id;
            return this;
        }

        public String getOperationEventStatusType() {
            return operationEventStatusType;
        }

        public OperationEventStatus setOperationEventStatusType(String operationEventStatusType) {
            this.operationEventStatusType = operationEventStatusType;
            return this;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public OperationEventStatus setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }
    }
}
