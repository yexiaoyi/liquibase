package liquibase.util;

import liquibase.Scope;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateUtil {
    public static ZonedDateTime toZonedDateTime(Date date) {
        if (date == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(date.toInstant(), Scope.getCurrentScope().get(Scope.Attr.timezone, ZoneId.class));
    }
}
