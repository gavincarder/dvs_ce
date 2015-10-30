<?xml version="1.0" ?>

<TestCase name="CreateSeedata" version="5">

<meta>
   <create version="7.5.1" buildNumber="7.5.1.418" author="wanpe05" date="07/21/2014" host="WANPE05-X220" />
   <lastEdited version="7.5.2" buildNumber="7.5.2.235" author="devjo01" date="05/06/2015" host="DEVJO01W7A" />
</meta>

<id>5AA8C47210A711E4A658F0DEF1B0B458</id>
<Documentation>Put documentation of the Test Case here.</Documentation>
<IsInProject>true</IsInProject>
<sig>ZWQ9MyZ0Y3Y9NSZsaXNhdj03LjUuMiAoNy41LjIuMjM1KSZub2Rlcz0tMTQ5NDQ2NzI4Mg==</sig>
<subprocess>false</subprocess>

<initState>
</initState>

<resultState>
</resultState>

<deletedProps>
</deletedProps>

    <Node name="Initializations" log=""
          type="com.itko.lisa.test.ScriptNode" 
          version="1" 
          uid="2122D4DCAB0E11E4BD790050568360BA" 
          think="500-1S" 
          useFilters="true" 
          quiet="false" 
          next="Output Log Message" > 


      <!-- Assertions -->
<CheckResult assertTrue="true" name="Any Exception Then Fail" type="com.itko.lisa.dynexec.CheckInvocationEx">
<log>Assertion name: Any Exception Then Fail checks for: true  is of type: Assert on Invocation Exception.</log>
<then>fail</then>
<valueToAssertKey></valueToAssertKey>
        <param>.*</param>
</CheckResult>

<onerror>abort</onerror>
<script>import java.text.DateFormat;&#13;&#10;import java.text.SimpleDateFormat;&#13;&#10;import java.util.Calendar;&#13;&#10;&#13;&#10;// Note time test started&#13;&#10;DateFormat dateFormat = new SimpleDateFormat(&quot;yyyyMMdd_HHmmss&quot;);&#13;&#10;Calendar cal = Calendar.getInstance();&#13;&#10;String TestResultDataTime = dateFormat.format(cal.getTime());&#13;&#10;testExec.setStateObject(&quot;TestTimestamp&quot;, TestResultDataTime);&#13;&#10;&#13;&#10;// Set Input and output files&#13;&#10;String FileName = new String(&quot;CreateSeedData&quot;);&#13;&#10;testExec.setStateObject(&quot;OutputFileName&quot;, FileName);&#13;&#10;// Next line commented out since using default value BookshopInit.xlsx&#13;&#10;// testExec.setStateObject(&quot;TestDataFile&quot;, FileName);&#13;&#10;&#13;&#10;// Initialize StepsToSkip&#13;&#10;Integer StepsToSkip = new Integer(-1);&#13;&#10;testExec.setStateObject(&quot;StepsToSkip&quot;, StepsToSkip);&#13;&#10;&#13;&#10;// Assume success&#13;&#10;testExec.setStateObject(&quot;OverallStatus&quot;, new String(&quot;Success&quot;) );</script>
    </Node>


    <Node name="Output Log Message" log=""
          type="com.itko.lisa.test.TestNodeLogger" 
          version="1" 
          uid="610A5F1F10A711E4A658F0DEF1B0B458" 
          think="500-1S" 
          useFilters="true" 
          quiet="true" 
          next="Set Headers" > 


      <!-- Data Sets -->
<readrec>Read Rows from Excel File</readrec>
<log>== TEST {{testCase}} #{{TestNum}} ==&#13;&#10;&#13;&#10;{{TestDescription}}&#13;&#10;&#13;&#10;{{RequestMethod}} {{URL}}&#13;&#10;</log>
    </Node>


    <Node name="Set Headers" log=""
          type="com.itko.lisa.test.ScriptNode" 
          version="1" 
          uid="B1C29685E9CB11E48F6F0050568360BA" 
          think="500-1S" 
          useFilters="true" 
          quiet="false" 
          next="Output Log Message" > 


      <!-- Assertions -->
<CheckResult assertTrue="true" name="Any Exception Then Fail" type="com.itko.lisa.dynexec.CheckInvocationEx">
<log>Assertion name: Any Exception Then Fail checks for: true  is of type: Assert on Invocation Exception.</log>
<then>fail</then>
<valueToAssertKey></valueToAssertKey>
        <param>.*</param>
</CheckResult>

<CheckResult assertTrue="true" name="Ensure Property Matches Expression" type="com.itko.lisa.test.CheckResultPropRegEx">
<log>Assertion name: Ensure Property Matches Expression checks for: true  is of type: Property Value Expression.</log>
<then>Check RequestMethod</then>
<valueToAssertKey></valueToAssertKey>
        <prop>StepsToSkip</prop>
        <param>-1</param>
</CheckResult>

<onerror>abort</onerror>
<script>// Supported Header:&#13;&#10;// Accept, Prefer, OData-Version, OData-MaxVersion&#13;&#10;// ** This system ignores any unknown/unsupported header ** &#13;&#10;&#13;&#10;// First check to see if we need to process this at all&#13;&#10;Integer StepsToSkip = testExec.getStateObject(&quot;StepsToSkip&quot;);&#13;&#10;&#13;&#10;int intSkipVal = StepsToSkip.intValue();&#13;&#10;&#13;&#10;if ( intSkipVal &gt;= 0 ) {&#13;&#10;    // System.out.println( &quot;Decrementing StepsToSkip.  Current Value = &quot; + StepsToSkip.toString());&#13;&#10;&#13;&#10;    intSkipVal = intSkipVal - 1;&#13;&#10;    testExec.setStateObject(&quot;StepsToSkip&quot;, StepsToSkip.valueOf(intSkipVal) );&#13;&#10;}&#13;&#10;&#13;&#10;if ( intSkipVal &lt; 0 ) {&#13;&#10;    System.out.println( &quot;Executing Test # &quot; + testExec.getStateObject(&quot;TestNum&quot;).toString());&#13;&#10;&#13;&#10;    // First, init all supported header values into LISA State, otherwise header would get incorrectly defined even if not set&#13;&#10;    testExec.setStateObject(&quot;Header-Accept&quot;, &quot;&quot;);&#13;&#10;    testExec.setStateObject(&quot;Header-OData-Version&quot;, &quot;&quot;);&#13;&#10;    testExec.setStateObject(&quot;Header-OData-MaxVersion&quot;, &quot;&quot;);&#13;&#10;    testExec.setStateObject(&quot;Header-Prefer&quot;, &quot;&quot;);&#13;&#10;&#13;&#10;    // set headers from input&#13;&#10;    String strHeaders=testExec.getStateObject(&quot;Headers&quot;).toString();&#13;&#10;    if (strHeaders == null || strHeaders.isEmpty())&#13;&#10;    return;&#13;&#10;&#13;&#10;    String[] strHeadersToken = strHeaders.split(&quot;&amp;&quot;);&#13;&#10;    for (String header: strHeadersToken){&#13;&#10;        String[] headerToken = header.split(&quot;=&quot;, 2);&#13;&#10;        String headerKey = headerToken[0].toString();&#13;&#10;        String headerValue = (headerToken.length &gt; 1)? headerToken[1].toString().trim() : null;&#13;&#10;        switch (headerKey){&#13;&#10;            case &quot;Accept&quot;:&#13;&#10;                if (headerValue != null)&#13;&#10;                    testExec.setStateObject(&quot;Header-Accept&quot;, headerValue);&#13;&#10;                break;&#13;&#10;            case &quot;OData-Version&quot;:&#13;&#10;                if (headerValue != null)&#13;&#10;                    testExec.setStateObject(&quot;Header-OData-Version&quot;, headerValue);&#13;&#10;                break;&#13;&#10;            case &quot;OData-MaxVersion&quot;:&#13;&#10;                if (headerValue != null)&#13;&#10;                    testExec.setStateObject(&quot;Header-OData-MaxVersion&quot;, headerValue);&#13;&#10;                break;&#13;&#10;            case &quot;Prefer&quot;:&#13;&#10;                if (headerValue != null)&#13;&#10;                    testExec.setStateObject(&quot;Header-Prefer&quot;, headerValue);&#13;&#10;                break;&#13;&#10;            default:&#13;&#10;                break;&#13;&#10;        }&#13;&#10;    }&#13;&#10;}&#13;&#10;   &#13;&#10;</script>
    </Node>


    <Node name="Write out an overall status" log=""
          type="com.itko.lisa.utils.WritePropsNode" 
          version="1" 
          uid="90F02549118411E48473F0DEF1B0B458" 
          think="500-1S" 
          useFilters="true" 
          quiet="true" 
          next="end" > 

<file>{{OutputFolder}}/{{OutputFileName}}_{{TestTimestamp}}.txt</file>
<encoding>DEFAULT</encoding>
<props>
    <Parameter>
    <key>Summary Status</key>
    <value>=== Overall Status of Test Run: {{OverallStatus}} ===</value>
    </Parameter>
</props>
    </Node>


    <Node name="Check RequestMethod" log=""
          type="com.itko.lisa.test.ScriptNode" 
          version="1" 
          uid="90C2280D10AC11E4A658F0DEF1B0B458" 
          think="500-1S" 
          useFilters="true" 
          quiet="false" 
          next="Output Log Message" > 


      <!-- Assertions -->
<CheckResult assertTrue="true" name="Any Exception Then Fail" type="com.itko.lisa.dynexec.CheckInvocationEx">
<log>Assertion name: Any Exception Then Fail checks for: true  is of type: Assert on Invocation Exception.</log>
<then>fail</then>
<valueToAssertKey></valueToAssertKey>
        <param>.*</param>
</CheckResult>

<CheckResult assertTrue="true" name="Is GET operation" type="com.itko.lisa.test.CheckResultPropRegEx">
<log>Assertion name: Is GET operation checks for: true  is of type: Property Value Expression.</log>
<then>REST GET</then>
<valueToAssertKey></valueToAssertKey>
        <prop>RequestMethod</prop>
        <param>GET</param>
</CheckResult>

<CheckResult assertTrue="true" name="Is POST operation" type="com.itko.lisa.test.CheckResultPropRegEx">
<log>Assertion name: Is POST operation checks for: true  is of type: Property Value Expression.</log>
<then>REST POST</then>
<valueToAssertKey></valueToAssertKey>
        <prop>RequestMethod</prop>
        <param>POST</param>
</CheckResult>

<CheckResult assertTrue="true" name="Is PUT operation" type="com.itko.lisa.test.CheckResultPropRegEx">
<log>Assertion name: Is PUT operation checks for: true  is of type: Property Value Expression.</log>
<then>REST PUT</then>
<valueToAssertKey></valueToAssertKey>
        <prop>RequestMethod</prop>
        <param>PUT</param>
</CheckResult>

<CheckResult assertTrue="true" name="Is Delete operation" type="com.itko.lisa.test.CheckResultPropRegEx">
<log>Assertion name: Is Delete operation checks for: true  is of type: Property Value Expression.</log>
<then>REST DELETE</then>
<valueToAssertKey></valueToAssertKey>
        <prop>RequestMethod</prop>
        <param>DELETE</param>
</CheckResult>

<CheckResult assertTrue="true" name="If DONE then stop run" type="com.itko.lisa.test.CheckResultPropRegEx">
<log>DONE</log>
<then>Write out an overall status</then>
<valueToAssertKey></valueToAssertKey>
        <prop>RequestMethod</prop>
        <param>DONE</param>
</CheckResult>

<onerror>abort</onerror>
<script>import java.sql.*;&#13;&#10;&#13;&#10;//HTTPTransaction trans=(HTTPTransaction)testExec.getStateObject(&quot;lisa.vse.http.current.transaction&quot;);&#13;&#10;&#13;&#10;//testExec.setStateObject(&quot;RequestMethod&quot;,trans.getRequestMethod());</script>
    </Node>


    <Node name="REST POST" log=""
          type="com.itko.lisa.ws.rest.RESTNode" 
          version="3" 
          uid="8DC1232310B411E4854CF0DEF1B0B458" 
          think="500-1S" 
          useFilters="true" 
          quiet="false" 
          next="Check Result" > 


      <!-- Filters -->
      <Filter type="com.itko.lisa.test.FilterSaveResponse">
        <valueToFilterKey>lisa.REST POST.rsp</valueToFilterKey>
      <prop>ActualResponse</prop>
      </Filter>

      <Filter type="com.itko.lisa.test.FilterProperty2Property">
        <valueToFilterKey>lisa.REST POST.http.responseCode</valueToFilterKey>
      <toProp>HTTP_Status</toProp>
      <pre>false</pre>
      <post>true</post>
      </Filter>

<url>{{PROTOCOL}}://{{HOST}}:{{PORT}}/{{ENDPOINT}}/{{URL}}</url>
<content>{{RequestBody}}</content>
<content-type>application/json</content-type>
<data-type>text</data-type>
      <header field="OData-Version" value="{{Header-OData-Version}}" />
      <header field="OData-MaxVersion" value="{{Header-OData-MaxVersion}}" />
      <header field="Prefer" value="{{Header-Prefer}}" />
      <header field="Accept" value="{{Header-Accept}}" />
<httpMethod>POST</httpMethod>
<onError>abort</onError>
    </Node>


    <Node name="Check Result" log=""
          type="com.itko.lisa.test.ScriptNode" 
          version="1" 
          uid="CF71A7A010B311E4854CF0DEF1B0B458" 
          think="500-1S" 
          useFilters="true" 
          quiet="false" 
          next="Write Properties to File testRecord.csv" > 


      <!-- Assertions -->
<CheckResult assertTrue="true" name="Any Exception Then Fail" type="com.itko.lisa.dynexec.CheckInvocationEx">
<log>Assertion name: Any Exception Then Fail checks for: true  is of type: Assert on Invocation Exception.</log>
<then>fail</then>
<valueToAssertKey></valueToAssertKey>
        <param>.*</param>
</CheckResult>

<onerror>abort</onerror>
<script>import java.util.*;&#13;&#10;import java.util.regex.Pattern;&#13;&#10;import java.io.File;&#13;&#10;import java.io.IOException;&#13;&#10;&#13;&#10;import org.apache.commons.io.FileUtils;&#13;&#10;&#13;&#10;static String strStatusFailure = &quot;Failed&quot;;&#13;&#10;static String strStatusPass = &quot;Passed&quot;;&#13;&#10;&#13;&#10;void  setFailure(String strRespMsg)&#13;&#10;{&#13;&#10;    String strActionOnFail=testExec.getStateObject(&quot;ActionOnFail&quot;).toString();&#13;&#10;    if ( strActionOnFail.contains(&quot;Ignore&quot;) ) {&#13;&#10;        strStatusFailure = strStatusFailure.concat(&quot; (Ignored)&quot;);&#13;&#10;    }&#13;&#10;    else {&#13;&#10;        testExec.setStateObject(&quot;OverallStatus&quot;, strStatusFailure);&#13;&#10;    }&#13;&#10;    System.out.println(strStatusFailure);&#13;&#10;    testExec.setStateObject(&quot;TestStatus&quot;, strStatusFailure);&#13;&#10;    String strSkipOnFailCount = testExec.getStateObject(&quot;SkipOnFailCount&quot;).toString();&#13;&#10;    if ( strSkipOnFailCount.length() &gt; 0 ) {&#13;&#10;        testExec.setStateObject(&quot;StepsToSkip&quot;, new Integer(strSkipOnFailCount));&#13;&#10;    }&#13;&#10;}&#13;&#10;&#13;&#10;// main&#13;&#10;try&#13;&#10;{&#13;&#10;    String strRespMsg=null;&#13;&#10;&#9;&#13;&#10;&#9;// check response code/status&#13;&#10;    String strExpectedStatus=testExec.getStateObject(&quot;ExpectedStatus&quot;).toString();&#13;&#10;    String strActualStatus=testExec.getStateObject(&quot;HTTP_Status&quot;).toString();&#13;&#10;    if ( 0 != strExpectedStatus.compareTo(strActualStatus) ) {&#13;&#10;        strRespMsg=&quot;Incorrect response status: [&quot;+strExpectedStatus+&quot;] != [&quot;+strActualStatus+&quot;]&quot;;&#13;&#10;&#9;&#9;//testExec.setStateObject(&quot;scenarioStatus&quot;,&quot;Failed: &quot; + strRespMsg);&#13;&#10;        setFailure(strRespMsg);&#13;&#10;&#9;&#9;return;  // return if return code failed to match&#13;&#10;    }&#13;&#10;&#9;&#13;&#10;&#9;// check response body&#13;&#10;    String strExpectedResp=testExec.getStateObject(&quot;ExpectedResponse&quot;).toString();&#13;&#10;    String strActualResp=testExec.getStateObject(&quot;ActualResponse&quot;).toString();&#13;&#10;&#13;&#10;    // Remove carriage return charachters JIC &#13;&#10;    strExpectedResp = strExpectedResp.replaceAll(&quot;\\r&quot;, &quot;&quot;);&#13;&#10;    strActualResp = strActualResp.replaceAll(&quot;\\r&quot;, &quot;&quot;);&#13;&#10;    // Consolidate leading/trailing whitespace and new-lines into one space&#13;&#10;    strExpectedResp = strExpectedResp.replaceAll(&quot;\\s*\\n+\\s*&quot;, &quot; &quot;);&#13;&#10;    strActualResp = strActualResp.replaceAll(&quot;\\s*\\n+\\s*&quot;, &quot; &quot;);&#13;&#10;&#13;&#10;    // System.out.println(strExpectedResp.length());&#13;&#10;    // System.out.println(strActualResp.length());&#13;&#10;    // System.out.println(&quot;===================Expected=========================&quot;);&#13;&#10;    // System.out.println(strExpectedResp+&quot;&lt;&lt;&lt;&quot;);&#13;&#10;    // System.out.println(&quot;===================Actual===========================&quot;);&#13;&#10;    // System.out.println(strActualResp+&quot;&lt;&lt;&lt;&quot;);&#13;&#10;    // System.out.println(&quot;===================End==============================&quot;);&#13;&#10;&#13;&#10;    // File file = new File(&quot;C:\\strExpectedResp.txt&quot;);&#13;&#10;    // FileUtils.writeStringToFile(file, strExpectedResp);&#13;&#10;    // File file2 = new File(&quot;C:\\strActualResp.txt&quot;);&#13;&#10;    // FileUtils.writeStringToFile(file2, strActualResp);&#13;&#10;&#13;&#10;    if (!Pattern.matches(strExpectedResp.trim(), strActualResp.trim())){&#13;&#10;        strRespMsg=&quot;Incorrect response body&quot;;&#13;&#10;&#9;&#9;//testExec.setStateObject(&quot;scenarioStatus&quot;,&quot;Failed: &quot; + strRespMsg);&#13;&#10;        setFailure(strRespMsg);&#13;&#10;&#9;&#9;return;  &#13;&#10;    }&#13;&#10;&#13;&#10;    // set scenario status to pass&#13;&#10;    testExec.setStateObject(&quot;TestStatus&quot;,strStatusPass);&#13;&#10;}&#13;&#10;catch(Exception e)&#13;&#10;{&#13;&#10;   System.out.println(e.getMessage());&#13;&#10;   //testExec.setStateObject(&quot;scenarioStatus&quot;,&quot;Exception&quot;);&#13;&#10;   setFailure(&quot;Exception&quot;);&#13;&#10;}</script>
    </Node>


    <Node name="Write Properties to File testRecord.csv" log=""
          type="com.itko.lisa.utils.WritePropsNode" 
          version="1" 
          uid="90F02549118411E48473F0DEF1B0B458" 
          think="500-1S" 
          useFilters="true" 
          quiet="true" 
          next="Write out test summary" > 

<file>{{OutputFolder}}/{{OutputFileName}}_{{TestTimestamp}}.csv</file>
<encoding>DEFAULT</encoding>
<props>
    <Parameter>
    <key>TestNum</key>
    <value>{{TestNum}}</value>
    </Parameter>
    <Parameter>
    <key>Description</key>
    <value>{{TestDescription}}</value>
    </Parameter>
    <Parameter>
    <key>TestStatus</key>
    <value>{{TestStatus}}</value>
    </Parameter>
    <Parameter>
    <key>RequestMethod</key>
    <value>{{RequestMethod}}</value>
    </Parameter>
    <Parameter>
    <key>URL</key>
    <value>{{URL}}</value>
    </Parameter>
    <Parameter>
    <key>RequestBody</key>
    <value>{{RequestBody}}</value>
    </Parameter>
    <Parameter>
    <key>HTTP_Status</key>
    <value>{{HTTP_Status}}</value>
    </Parameter>
    <Parameter>
    <key>ExpectedStatus</key>
    <value>{{ExpectedStatus}}</value>
    </Parameter>
    <Parameter>
    <key>ActualResponse</key>
    <value>{{ActualResponse}}</value>
    </Parameter>
    <Parameter>
    <key>ExpectedResponse</key>
    <value>{{ExpectedResponse}}</value>
    </Parameter>
</props>
    </Node>


    <Node name="Write out test summary" log=""
          type="com.itko.lisa.utils.WritePropsNode" 
          version="1" 
          uid="90F02549118411E48473F0DEF1B0B458" 
          think="500-1S" 
          useFilters="true" 
          quiet="true" 
          next="Output Log Message" > 

<file>{{OutputFolder}}/{{OutputFileName}}_{{TestTimestamp}}.txt</file>
<encoding>DEFAULT</encoding>
<props>
    <Parameter>
    <key>TestNum</key>
    <value>{{TestNum}}</value>
    </Parameter>
    <Parameter>
    <key>Description</key>
    <value>    {{TestDescription}}     </value>
    </Parameter>
    <Parameter>
    <key>TestStatus</key>
    <value>{{TestStatus}}</value>
    </Parameter>
</props>
    </Node>


    <Node name="REST PUT" log=""
          type="com.itko.lisa.ws.rest.RESTNode" 
          version="3" 
          uid="B33AE89C10B311E4854CF0DEF1B0B458" 
          think="500-1S" 
          useFilters="true" 
          quiet="false" 
          next="Check Result" > 


      <!-- Filters -->
      <Filter type="com.itko.lisa.test.FilterSaveResponse">
        <valueToFilterKey>lisa.REST PUT.rsp</valueToFilterKey>
      <prop>ActualResponse</prop>
      </Filter>

      <Filter type="com.itko.lisa.test.FilterProperty2Property">
        <valueToFilterKey>lisa.REST PUT.http.responseCode</valueToFilterKey>
      <toProp>HTTP_Status</toProp>
      <pre>false</pre>
      <post>true</post>
      </Filter>

<url>{{PROTOCOL}}://{{HOST}}:{{PORT}}/{{ENDPOINT}}/{{URL}}</url>
<content>{{RequestBody}}</content>
<content-type>application/json</content-type>
<data-type>text</data-type>
      <header field="OData-Version" value="{{Header-OData-Version}}" />
      <header field="OData-MaxVersion" value="{{Header-OData-MaxVersion}}" />
      <header field="Prefer" value="{{Header-Prefer}}" />
      <header field="Accept" value="{{Header-Accept}}" />
<httpMethod>PUT</httpMethod>
<onError>abort</onError>
    </Node>


    <Node name="REST DELETE" log=""
          type="com.itko.lisa.ws.rest.RESTNode" 
          version="3" 
          uid="B93C486B10B811E4854CF0DEF1B0B458" 
          think="500-1S" 
          useFilters="true" 
          quiet="false" 
          next="Check Result" > 


      <!-- Filters -->
      <Filter type="com.itko.lisa.test.FilterSaveResponse">
        <valueToFilterKey>lisa.REST DELETE.rsp</valueToFilterKey>
      <prop>ActualResponse</prop>
      </Filter>

      <Filter type="com.itko.lisa.test.FilterProperty2Property">
        <valueToFilterKey>lisa.REST DELETE.http.responseCode</valueToFilterKey>
      <toProp>HTTP_Status</toProp>
      <pre>false</pre>
      <post>true</post>
      </Filter>

<url>{{PROTOCOL}}://{{HOST}}:{{PORT}}/{{ENDPOINT}}/{{URL}}</url>
<data-type>text</data-type>
      <header field="OData-Version" value="{{Header-OData-Version}}" />
      <header field="OData-MaxVersion" value="{{Header-OData-MaxVersion}}" />
      <header field="Prefer" value="{{Header-Prefer}}" />
      <header field="Accept" value="{{Header-Accept}}" />
<httpMethod>DELETE</httpMethod>
<onError>abort</onError>
    </Node>


    <Node name="REST GET" log=""
          type="com.itko.lisa.ws.rest.RESTNode" 
          version="3" 
          uid="431F113210B211E4854CF0DEF1B0B458" 
          think="500-1S" 
          useFilters="true" 
          quiet="false" 
          next="Check Result" > 


      <!-- Filters -->
      <Filter type="com.itko.lisa.test.FilterSaveResponse">
        <valueToFilterKey>lisa.REST GET.rsp</valueToFilterKey>
      <prop>ActualResponse</prop>
      </Filter>

      <Filter type="com.itko.lisa.test.FilterProperty2Property">
        <valueToFilterKey>lisa.REST GET.http.responseCode</valueToFilterKey>
      <toProp>HTTP_Status</toProp>
      <pre>false</pre>
      <post>true</post>
      </Filter>

<url>{{PROTOCOL}}://{{HOST}}:{{PORT}}/{{ENDPOINT}}/{{URL}}</url>
<data-type>text</data-type>
      <header field="OData-Version" value="{{Header-OData-Version}}" />
      <header field="OData-MaxVersion" value="{{Header-OData-MaxVersion}}" />
      <header field="Prefer" value="{{Header-Prefer}}" />
      <header field="Accept" value="{{Header-Accept}}" />
<httpMethod>GET</httpMethod>
<onError>abort</onError>
    </Node>


    <Node name="end" log=""
          type="com.itko.lisa.test.NormalEnd" 
          version="1" 
          uid="5AA8EB8810A711E4A658F0DEF1B0B458" 
          think="0h" 
          useFilters="true" 
          quiet="true" 
          next="fail" > 

    </Node>


    <Node name="fail" log=""
          type="com.itko.lisa.test.Abend" 
          version="1" 
          uid="5AA8EB8610A711E4A658F0DEF1B0B458" 
          think="0h" 
          useFilters="true" 
          quiet="true" 
          next="abort" > 

    </Node>


    <Node name="abort" log=""
          type="com.itko.lisa.test.AbortStep" 
          version="1" 
          uid="5AA8EB8410A711E4A658F0DEF1B0B458" 
          think="0h" 
          useFilters="true" 
          quiet="true" 
          next="" > 

    </Node>


    <DataSet type="com.itko.lisa.test.ExcelDataFile" name="Read Rows from Excel File" atend="Write out an overall status" local="false" random="false" maxItemsToFetch="100" >
<sample>rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAAAB3CAAAAAEAAAAAeA==</sample>
    <location>{{TestDataFolder}}/{{TestDataFile}}</location>
    <sheetname>sheet1</sheetname>
    </DataSet>

</TestCase>
