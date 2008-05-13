<%--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib uri="/webwork" prefix="ww" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="workingCopy.page.title"/></title>
    </head>
    <body>
      <div id="h3">

        <jsp:include page="/WEB-INF/jsp/navigations/ProjectMenu.jsp">
          <jsp:param name="tab" value="workingCopy"/>
        </jsp:include>

        <h3>
            <ww:text name="workingCopy.section.title">
                <ww:param><ww:property value="projectName"/></ww:param>
            </ww:text>
        </h3>

        <ww:property value="output" escape="false"/>

        <%
            if ( request.getParameter( "file" ) != null )
            {
        %>
        <p>
        <ww:url id="workingCopyTextUrl" action="workingCopyFileText">
          <ww:param name="projectId" value="projectId"/>
          <ww:param name="projectName" value="projectName"/>
          <ww:param name="userDirectory" value="userDirectory"/>
          <ww:param name="file" value="file"/>
        </ww:url>
        <ww:a href="%{workingCopyTextUrl}"><ww:text name="workingCopy.currentFile.text"/></ww:a>
        </p>
        
        <form>
          <textarea rows="50" cols="100" readonly="true"><ww:property value="fileContent"/></textarea>
        </form>
        <%
            }
        %>

      </div>
    </body>
  </ww:i18n>
</html>
