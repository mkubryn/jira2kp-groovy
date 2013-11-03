// author: mkubryn

JIRA_LOGIN_URL = "https://jira.7bulls.com/rest/gadget/1.0/login"
JIRA_GET_REPORT_URL = "https://jira.7bulls.com/secure/ConfigureReport!excelView.jspa?showDetails=true&endDate=3%2Flis%2F13&startDate=1%2Fpa%C5%BA%2F13&weekends=true&reportKey=jira-timesheet-plugin%3Areport&targetUser="


/*
 * Imports and grape
 */
@Grab(group='org.apache.httpcomponents', module='httpclient', version='4.3.1') 
import org.apache.http.impl.client.*
import org.apache.http.client.methods.*
import org.apache.http.client.entity.*
import org.apache.http.protocol.HTTP
import org.apache.http.message.BasicNameValuePair as Pair
import org.apache.http.util.EntityUtils
import groovy.transform.*

/*
 * MISC
 */
JIRA_REPORT_CHUNKS_COUNT = 7
JIRA_DATE_FORMAT = new java.text.SimpleDateFormat("dd/MMM/yy", new Locale("pl"))
KP_DATE_FORMAT = new java.text.SimpleDateFormat("dd.MM.yyyy")

def asBigDecimal = { str ->
    str.replaceAll(',','.') as BigDecimal
}

def readPasswordFromConsole = { question -> System.console().readPassword question }
def readFromConsole = { question -> System.console().readLine question }


/*
 * Report entry holder
 */
 @ToString
class ReportEntry { def project, issueType, issueId, date, user, timeSpent, description }


/*
 * Http communication with Jira
 */
client = new DefaultHttpClient()
def executeGet = { url ->  EntityUtils.toString(client.execute(new HttpGet(url)).getEntity()) }
def executePost = { url, params ->
    action = new HttpPost(url)
    action.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8))
    EntityUtils.toString(client.execute(action).getEntity())
}


/*
 * Jira timesheet parsing 
 */
def convertChunksToReportEntries = { reportChunks ->
    reportEntries = []
    
    (0..<(reportChunks.size() / JIRA_REPORT_CHUNKS_COUNT)).each { i ->
       from = i * JIRA_REPORT_CHUNKS_COUNT
       to = from + JIRA_REPORT_CHUNKS_COUNT - 1
       
       chunks = reportChunks[from..to]
       
       reportEntry = new ReportEntry(description: chunks[0], project: chunks[1], issueType: chunks[2], issueId: chunks[3], date: chunks[4], 
                                       user: chunks[5], timeSpent: chunks[6] )
       if(reportEntry.project)
           reportEntries << reportEntry
    }
    
    reportEntries
}
def parseReportHtml = { html ->
    chunks = []
    html.eachLine { line ->
        matcher = line =~ "<(td).*(td)>"
         
         // remove all the unnecessary parts of html report
        if(matcher.find())
            chunks << matcher.group().replaceAll("</td>", "").replaceAll("</a>", "").replaceAll("<td.*>", "").trim()
    }
    
    convertChunksToReportEntries(chunks)
}


/*
 * Script flow
 */
jiraUsername = readFromConsole("Jira login: ")
jiraPassword = readPasswordFromConsole("Jira password: ")

// 1. Login ang get response
loginResp = executePost(JIRA_LOGIN_URL, [new Pair('os_password', jiraPassword as String), new Pair('os_username', jiraUsername)])

// 2. Check response
if((loginResp ==~ '.*"loginSucceeded":true.*') == false) {
    println "Jira login failed. Response from Jira: " + loginResponse
    System.exit(1)
}

// 3. Get timesheet
timesheetHtml = executeGet(JIRA_GET_REPORT_URL)

// 4. Parse entries and group by project and date (here comes the real Groovy power!)
entries = parseReportHtml(timesheetHtml).groupBy ( {it.project}, { JIRA_DATE_FORMAT.parse(it.date) } ).sort()


/*
 * 5. Create report
 */
entries.each { project, entriesByDateMap ->

    println "\n\n[$project]"
    
    entriesByDateMap.each { date, reportEntries ->
        hoursSpent = reportEntries.sum { asBigDecimal(it.timeSpent) }
        println "\n${KP_DATE_FORMAT.format(date)} -- $jiraUsername -- ${hoursSpent}h"
        
        reportEntries.each { entry ->
            println "  ${entry.issueType}: ${entry.issueId} - ${entry.description} - (${entry.timeSpent}h)"
        }
    } 
}
