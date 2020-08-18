package liquibase.hub.core

import liquibase.hub.model.HubChangeLog
import liquibase.hub.model.Project
import spock.lang.Specification

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class HttpClientTest extends Specification {
    def parseStream() {
        expect:
        new HttpClient().parseJson(new ByteArrayInputStream(input.getBytes()), Map.class).toString() == expected

        where:
        [input, type, expected] << [
                [
                        "{\"id\":\"48ed4771-bca3-4d28-afd9-8b67b8fd46d1\",\"subjectId\":\"9b46126a-50fb-b45a-bf16-f251bb8b24d5\",\"userName\":\"basic_user\",\"givenName\":null,\"middleName\":null,\"familyName\":null,\"phoneNumber\":null,\"email\":\"basic_user@mycompany.com\",\"url\":null,\"company\":null,\"active\":true,\"bio\":null,\"roles\":[{\"id\":\"b9c45b5c-382b-4a8c-079b-30dcf674ca1e\",\"name\":\"ORG_OWNER\",\"createDate\":\"2020-08-17T15:14:02.322834Z\",\"removeDate\":null}],\"createDate\":\"2020-08-17T15:14:02.322834Z\",\"removeDate\":null}",
                        Map.class,
                        "[id:48ed4771-bca3-4d28-afd9-8b67b8fd46d1, subjectId:9b46126a-50fb-b45a-bf16-f251bb8b24d5, userName:basic_user, givenName:null, middleName:null, familyName:null, phoneNumber:null, email:basic_user@mycompany.com, url:null, company:null, active:true, bio:null, roles:[[id:b9c45b5c-382b-4a8c-079b-30dcf674ca1e, name:ORG_OWNER, createDate:2020-08-17T15:14:02.322834Z, removeDate:null]], createDate:2020-08-17T15:14:02.322834Z, removeDate:null]"
                ],
                [
                        "{\"id\":\"ba5f95dc-6620-c455-9ae4-7d62f9ba920c\",\"fileName\":\"com/example/changelog.xml\",\"name\":\"com/example/changelog.xml\",\"scmLocation\":null,\"description\":null,\"prj\":{\"id\":\"121647ad-6dcc-40cc-aad0-ef1700daa3db\",\"name\":\"basic_user's Project\",\"org\":{\"id\":\"793bf1ca-5bd5-4ecb-a17b-818356e0665a\",\"name\":\"basic_user's Personal Organization\",\"type\":\"PERSONAL\"},\"createDate\":\"2020-08-17T15:14:02.322834Z\"},\"createDate\":\"2020-08-18T19:03:28.520323Z\",\"updateDate\":null}",
                        HubChangeLog,
                        "[id:ba5f95dc-6620-c455-9ae4-7d62f9ba920c, fileName:com/example/changelog.xml, name:com/example/changelog.xml, scmLocation:null, description:null, prj:[id:121647ad-6dcc-40cc-aad0-ef1700daa3db, name:basic_user's Project, org:[id:793bf1ca-5bd5-4ecb-a17b-818356e0665a, name:basic_user's Personal Organization, type:PERSONAL], createDate:2020-08-17T15:14:02.322834Z], createDate:2020-08-18T19:03:28.520323Z, updateDate:null]"
                ]
        ]
    }

    def writeAsJson() {
        when:
        def output = new ByteArrayOutputStream()
        new HttpClient().writeAsJson(new HubChangeLog(
                id: null,
                fileName: "com/example/changelog.xml",
                prj: new Project(
                        id: UUID.fromString("121647ad-6d05-40cc-aad0-ef1700daa3db"),
                        name: "Test project",
                        createDate: ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse("2010-03-15T13:06:20.34762-03:00"))

                )
        ), output)

        then:
        new String(output.toByteArray()).trim() == """
{
  "fileName": "com/example/changelog.xml",
  "prj": {
    "createDate": "2010-03-15T13:06:20.347620-03:00",
    "id": "121647ad-6d05-40cc-aad0-ef1700daa3db",
    "name": "Test project"
  }
}
""".trim()
    }
}
