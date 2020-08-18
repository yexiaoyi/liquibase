package liquibase.util

import org.codehaus.groovy.runtime.NullObject
import spock.lang.Specification
import spock.lang.Unroll

import java.text.ParseException
import java.time.LocalDateTime
import java.time.ZonedDateTime

class ISODateFormatTest extends Specification {

    private ISODateFormat dateFormat = new ISODateFormat();

    @Unroll("#featureName #input")
    def "parse"() {
        expect:
        dateFormat.format(dateFormat.parse(input)) == expected

        where:
        input                              | expected
        null                               | null
        "12:15:53"                         | "12:15:53"
        "2011-04-21"                       | "2011-04-21"
        "2011-04-21 10:13"                 | "2011-04-21T10:13:00"
        "2011-04-21T10:13"                 | "2011-04-21T10:13:00"
        "2011-04-21T09:13:40"              | "2011-04-21T09:13:40"
        "2011-04-21 09:13:40"              | "2011-04-21T09:13:40"
        "2011-04-21T10:13:40"              | "2011-04-21T10:13:40"
        "2011-04-21 10:13:40"              | "2011-04-21T10:13:40"
        "2011-04-21T18:13:40"              | "2011-04-21T18:13:40"

        "2012-09-12T09:47:54.664"          | "2012-09-12T09:47:54.664"
        "2011-04-21T10:13:40.12"           | "2011-04-21T10:13:40.12"
        "2011-04-21T10:13:40.044"          | "2011-04-21T10:13:40.044"
        "2011-04-21T10:13:40.01234567"     | "2011-04-21T10:13:40.01234567"

        //converts timezones
        "2011-04-21T10:13:40.084004Z"      | "2011-04-21T05:13:40.084004"
        "2011-04-21T10:13:40.084004-06:00" | "2011-04-21T11:13:40.084004"
    }

    @Unroll("#featureName #input")
    def parseDateTime() {
        when:
        def temporal = dateFormat.parseDateTime(input)

        then:
        temporal.toString() == expected
        temporal.getClass() == type

        where:
        input                       | expected                    | type
        null                        | "null"                      | NullObject
        "2011-04-21T18:13:40"       | "2011-04-21T18:13:40"       | LocalDateTime
        "2011-04-21T18:13:40Z"      | "2011-04-21T18:13:40Z"      | ZonedDateTime
        "2011-04-21T18:13:40-05:00" | "2011-04-21T18:13:40-05:00" | ZonedDateTime
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
