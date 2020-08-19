package liquibase.util

import liquibase.Scope
import org.codehaus.groovy.runtime.NullObject
import spock.lang.Specification
import spock.lang.Unroll

import java.text.ParseException
import java.time.*

class ISODateFormatTest extends Specification {

    private ISODateFormat dateFormat = new ISODateFormat();
    private String scopeId

    def setup() {
        scopeId = Scope.enter([
                (Scope.Attr.timezone.name()): ZoneId.of("-03:00")
        ])
    }

    def cleanup() {
        Scope.exit(scopeId)
    }

    @Unroll("#featureName #input")
    def "parsing"() {
        when:
        def temporal = dateFormat.parseDateTime(input)

        then:
        String.valueOf(temporal) == expectedTemporal
        temporal.getClass() == type
        String.valueOf(dateFormat.parse(input)).matches(expectedSimple)

        where:
        input                              | expectedSimple                 | expectedTemporal                   | type
        null                               | "null"                         | "null"                             | NullObject
        "12:15:53"                         | "12:15:53"                     | "12:15:53"                         | LocalTime
        "2011-04-21"                       | "2011-04-21"                   | "2011-04-21"                       | LocalDate
        "2011-04-21 10:13"                 | "2011-04-21 10:13:00.0"        | "2011-04-21T10:13"                 | LocalDateTime
        "2011-04-21T10:13"                 | "2011-04-21 10:13:00.0"        | "2011-04-21T10:13"                 | LocalDateTime
        "2011-04-21T09:13:40"              | "2011-04-21 09:13:40.0"        | "2011-04-21T09:13:40"              | LocalDateTime
        "2011-04-21 09:13:40"              | "2011-04-21 09:13:40.0"        | "2011-04-21T09:13:40"              | LocalDateTime
        "2011-04-21T10:13:40"              | "2011-04-21 10:13:40.0"        | "2011-04-21T10:13:40"              | LocalDateTime
        "2011-04-21 10:13:40"              | "2011-04-21 10:13:40.0"        | "2011-04-21T10:13:40"              | LocalDateTime
        "2011-04-21T18:13:40"              | "2011-04-21 18:13:40.0"        | "2011-04-21T18:13:40"              | LocalDateTime

        "2012-09-12T09:47:54.664"          | "2012-09-12 09:47:54.664"      | "2012-09-12T09:47:54.664"          | LocalDateTime
        "2011-04-21T10:13:40.12"           | "2011-04-21 10:13:40.12"       | "2011-04-21T10:13:40.120"          | LocalDateTime
        "2011-04-21T10:13:40.044"          | "2011-04-21 10:13:40.044"      | "2011-04-21T10:13:40.044"          | LocalDateTime
        "2011-04-21T10:13:40.01234567"     | "2011-04-21 10:13:40.01234567" | "2011-04-21T10:13:40.012345670"    | LocalDateTime

        //converts timezones
        "2011-04-21T10:13:40.084004Z"      | /.*13:40.084004/               | "2011-04-21T10:13:40.084004Z"      | ZonedDateTime
        "2011-04-21T10:13:40.084004-06:00" | /.*13:40.084004/               | "2011-04-21T10:13:40.084004-06:00" | ZonedDateTime
    }

    @Unroll
    def "parse fails on invalid formats"() {
        when:
        dateFormat.parse(input)

        then:
        ParseException e = thrown()

        e.message.startsWith("Unparseable date: \"$input\"")

        where:
        input | notes
        "a"   | null
    }
}
