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

<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

<html>
  <s:i18n name="localization.Continuum">
    <head>
      <title><s:text name="distributedBuilds.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <h3><s:text name="distributedBuilds.section.title"/></h3>
        <c:if test="${!empty actionErrors}">
          <div class="errormessage">
            <s:iterator value="actionErrors">
              <p><s:text name="<s:property/>" /></p>
            </s:iterator>
          </div>
        </c:if>
        <s:set name="distributedBuilds" value="distributedBuilds" scope="request"/>
        <ec:table items="distributedBuilds"
                  var="distributedBuild"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  sortable="false"
                  filterable="false">
          <ec:row>
            <ec:column property="projectName" title="distributedBuild.table.project"/>
            <ec:column property="url" title="distributedBuild.table.agentUrl"/>
            <ec:column property="cancelEntry" title="&nbsp;" width="1%">
              <s:url id="cancelUrl" action="cancelDistributedBuildProject" method="cancel" namespace="/">
                <s:param name="projectId">${pageScope.distributedBuild.projectId}</s:param>
                <s:param name="buildAgentUrl">${pageScope.distributedBuild.url}</s:param>
              </s:url>
              <s:a href="%{cancelUrl}"><img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0"></s:a>
            </ec:column>
          </ec:row>
        </ec:table>
      </div>
    </body>
  </s:i18n>
</html>