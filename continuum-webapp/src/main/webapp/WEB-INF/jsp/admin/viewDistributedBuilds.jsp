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
      <title><s:text name="distributedBuilds.page.title"/></title>
    </head>
    <body>
      <s:form id="removeForm" action="none" method="post">
        <div id="h3">
          <h3><s:text name="distributedBuilds.section.title"/></h3>
          <c:if test="${!empty actionErrors}">
            <div class="errormessage">
              <s:iterator value="actionErrors">
                <p><s:text name="<s:property/>" /></p>
              </s:iterator>
            </div>
          </c:if>
          <c:if test="${not empty distributedBuildSummary}">
            <s:set name="distributedBuildSummary" value="distributedBuildSummary" scope="request"/>
            <ec:table items="distributedBuildSummary"
                      var="distributedBuild"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="projectGroupName" title="distributedBuild.table.projectGroupName"/>
                <ec:column property="scmRootAddress" title="distributedBuild.table.scmRootAddress"/>
                <ec:column property="url" title="distributedBuild.table.agentUrl"/>
                <ec:column property="cancelEntry" title="&nbsp;" width="1%">
                  <c:choose>
                    <c:when test="${pageScope.distributedBuild.cancelEnabled}">
                      <s:url id="cancelUrl" action="cancelDistributedBuild" method="cancelDistributedBuild" namespace="/">
                        <s:param name="projectGroupId">${pageScope.distributedBuild.projectGroupId}</s:param>
                        <s:param name="scmRootAddress">${pageScope.distributedBuild.scmRootAddress}</s:param>
                        <s:param name="buildAgentUrl">${pageScope.distributedBuild.url}</s:param>
                      </s:url>
                      <redback:ifAuthorized permission="continuum-manage-queues">
                        <s:a href="%{cancelUrl}"><img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0"></s:a>
                      </redback:ifAuthorized>
                      <redback:elseAuthorized>
                        <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                      </redback:elseAuthorized>
                    </c:when>
                    <c:otherwise>
                      <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                    </c:otherwise>
                  </c:choose>
                </ec:column>
              </ec:row>
            </ec:table>
          </c:if>
          <c:if test="${empty distributedBuildSummary}">
            <s:text name="distributedBuilds.empty"/>
          </c:if>
        </div>
        <div id="h3">
          <h3>
            <s:text name="distributedBuilds.buildQueue.section.title"/>
          </h3>
          <c:if test="${not empty distributedBuildQueues}">
            <ec:table items="distributedBuildQueues"
                      var="distributedBuildQueue"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <redback:ifAuthorized permission="continuum-manage-queues">
                  <ec:column alias="selectedDistributedBuildTaskHashCodes" title="&nbsp;" style="width:5px" filterable="false" sortable="false" width="1%" headerCell="selectAll">
                    <input type="checkbox" name="selectedDistributedBuildTaskHashCodes" value="${pageScope.distributedBuildQueue.hashCode}" />
                  </ec:column>              
                </redback:ifAuthorized>
                <ec:column property="projectGroupName" title="distributedBuild.table.projectGroupName"/>
                <ec:column property="scmRootAddress" title="distributedBuild.table.scmRootAddress"/>
                <ec:column property="cancelEntry" title="&nbsp;" width="1%">
                  <redback:ifAuthorized permission="continuum-manage-queues">
                    <s:url id="cancelUrl" action="removeDistributedBuildEntry" method="removeDistributedBuildEntry" namespace="/">
                      <s:param name="projectGroupId">${pageScope.distributedBuildQueue.projectGroupId}</s:param>
                      <s:param name="scmRootAddress">${pageScope.distributedBuildQueue.scmRootAddress}</s:param>
                    </s:url>
                    <s:a href="%{cancelUrl}"><img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0"></s:a>
                  </redback:ifAuthorized>
                  <redback:elseAuthorized>
                    <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                  </redback:elseAuthorized>
                </ec:column>
              </ec:row>
            </ec:table>
          </c:if>
        </div>
        <c:if test="${not empty distributedBuildQueues}">
          <div class="functnbar3">
            <table>
              <tbody>
                <tr>
                  <td>
                    <input type="submit" value="<s:text name="distributedBuilds.removeEntries"/>"
                           onclick="$('removeForm').action='removeDistributedBuildEntries!removeDistributedBuildEntries.action';$('removeForm').submit();" /> 
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </c:if>
      </s:form>
    </body>
  </s:i18n>
</html>