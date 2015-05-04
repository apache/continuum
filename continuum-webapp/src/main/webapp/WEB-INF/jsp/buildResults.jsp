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
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<html>
  <s:i18n name="localization.Continuum">
    <head>
        <title><s:text name="buildResults.page.title"/></title>
        <meta http-equiv="refresh" content="300"/>
    </head>
    <body>
      <div id="h3">

        <jsp:include page="/WEB-INF/jsp/navigations/ProjectMenu.jsp">
          <jsp:param name="tab" value="buildResults"/>
        </jsp:include>

        <h3>
            <s:text name="buildResults.section.title">
                <s:param><s:property value="project.name"/></s:param>
            </s:text>
        </h3>
        
        <c:if test="${!empty actionErrors}">
          <div class="errormessage">
            <s:iterator value="actionErrors">
              <p><s:property/></p>
            </s:iterator>
          </div>
        </c:if>

        <%@ include file="buildResultsPager.jspf" %>
        <s:form id="buildResultsForm" action="removeBuildResults" theme="simple">
          <s:token/>
          <s:set name="buildResults" value="buildResults" scope="request"/>
          <s:hidden name="projectGroupId"/>
          <s:hidden name="projectId"/>
          <ec:table items="buildResults"
                    var="buildResult"
                    autoIncludeParameters="false"
                    showExports="false"
                    showPagination="false"
                    showStatusBar="false"
                    filterable="false"
                    sortable="false">
            <ec:row highlightRow="true">
              <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
                <ec:column alias="selectedBuildResults" title=" " style="width:5px" filterable="false" sortable="false" headerCell="selectAll">
                  <input type="checkbox" name="selectedBuildResults" value="${buildResult.id}" />
                </ec:column>
              </redback:ifAuthorized>
              <ec:column property="buildNumberIfNotZero" title="buildResults.buildNumber">
                <c:out value="${pageScope.buildResult.buildNumber}"/>
              </ec:column>
              <ec:column property="state" headerStyle="text-align: center;" style="text-align: center;" title="buildResults.result" cell="org.apache.maven.continuum.web.view.buildresults.StateCell"/>
              <ec:column property="trigger" title="buildResult.trigger">
                <c:choose>
                  <c:when test="${pageScope.buildResult.trigger == 1}">
                    <s:text name="buildResult.trigger.1"/>
                  </c:when>
                  <c:otherwise>
                    <s:text name="buildResult.trigger.0"/>
                  </c:otherwise>
                </c:choose>
              </ec:column>
              <ec:column property="duration" title="buildResults.duration">
                <c:choose>
                  <c:when test="${buildResult.endTime gt 0}">
                    ${buildResult.durationTime}
                  </c:when>
                  <c:otherwise>
                    <s:text name="buildResults.startedSince"/> : <c:out value="${buildResult.elapsedTime}"/>
                  </c:otherwise>
                </c:choose>
              </ec:column>
              <ec:column property="startTime" title="buildResults.startTime" cell="date" format="yyyy-MM-dd HH:mm z"/>
              <ec:column property="endTime" title="buildResults.endTime" cell="date" format="yyyy-MM-dd HH:mm z"/>
              <ec:column property="buildDefinition.description" title="buildResults.buildDefinition.description" />
            </ec:row>
          </ec:table>
          <c:if test="${not empty buildResults}">
            <div class="functnbar3">
              <table>
                <tbody>
                  <tr>
                    <td>
                      <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
                        <input type="button" name="delete-project" value="<s:text name="delete"/>" onclick="document.forms.buildResultsForm.submit();" />
                      </redback:ifAuthorized>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </c:if>
        </s:form>
        <%@ include file="buildResultsPager.jspf" %>
      </div>
    </body>
  </s:i18n>
</html>
