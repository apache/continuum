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

<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<html>
  <s:i18n name="localization.Continuum">
    <head>
      <title><s:text name="buildQueue.page.title"/></title>
      <meta http-equiv="refresh" content="60"/>
    </head>
    <body>
      <s:form id="removeForm" action="none" method="post">
        <div id="h3">
          <h3>
            <s:text name="buildQueue.currentTask.section.title"/>
          </h3>  
          <table width="100%">
            <s:if test="%{currentBuildProjectTask != null}">
            <tbody>
              <tr>
                <th><s:text name="buildQueue.currentTask.projectName"/></th>
                <th><s:text name="buildQueue.currentTask.buildDefinition"/></th>
                <th>&nbsp;</th>
              </tr>
              <tr>
                <td width="50%"><s:property value="currentBuildProjectTask.projectName"/></td>
                <td width="49%"><s:property value="currentBuildProjectTask.buildDefinitionLabel"/></td>
                <td width="1%">
                <redback:ifAuthorized permission="continuum-manage-queues">
                  <s:url id="cancelUrl" action="cancelCurrentBuildTask" method="cancelCurrent" namespace="/">
                    <s:param name="projectId"><s:property value="currentBuildProjectTask.projectId"/></s:param>
                  </s:url>
                  <s:a href="%{cancelUrl}"><img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0"></s:a>
                </redback:ifAuthorized>
                <redback:elseAuthorized>
                  <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                </redback:elseAuthorized>
                </td>
              </tr>
            </tbody>
            </s:if>
            <s:else>
              <s:text name="buildQueue.no.currentTaks" />
            </s:else>
          </table>
        </div>    
        <div id="h3">
          <h3>
            <s:text name="buildQueue.section.title"/>
          </h3>  
            <c:if test="${not empty buildProjectTasks}">
              <ec:table items="buildProjectTasks"
                        var="buildProjectTask"
                        showExports="false"
                        showPagination="false"
                        showStatusBar="false"
                        sortable="false"
                        filterable="false">
                <ec:row highlightRow="true">
                  <redback:ifAuthorized permission="continuum-manage-queues">
                  <ec:column alias="selectedBuildTaskHashCodes" title="&nbsp;" style="width:5px" filterable="false" sortable="false" width="1%" headerCell="selectAll">
                    <input type="checkbox" name="selectedBuildTaskHashCodes" value="${buildProjectTask.hashCode}" />
                  </ec:column>              
                  </redback:ifAuthorized>
                  <ec:column property="projectName" title="buildQueue.currentTask.projectName" style="white-space: nowrap" width="49%"/>
                  <ec:column property="buildDefinitionLabel" title="buildQueue.currentTask.buildDefinition" style="white-space: nowrap" width="49%"/>
                  <ec:column property="cancelEntry" title="&nbsp;" width="1%">
                    <redback:ifAuthorized permission="continuum-manage-queues">
                    <s:url id="cancelUrl" action="removeBuildQueueEntry" method="remove" namespace="/">
                      <s:param name="projectId">${pageScope.buildProjectTask.projectId}</s:param>
                      <s:param name="buildDefinitionId">${pageScope.buildProjectTask.buildDefinitionId}</s:param>
                      <s:param name="trigger">${pageScope.buildProjectTask.trigger}</s:param>
                      <s:param name="projectName">${pageScope.buildProjectTask.projectName}</s:param>
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
            <c:if test="${empty buildProjectTasks}">
              <s:text name="buildQueue.empty"/>
            </c:if>
        </div>
        <c:if test="${not empty buildProjectTasks}">
          <div class="functnbar3">
            <table>
              <tbody>
                <tr>
                  <td>
                    <input type="submit" value="<s:text name="buildQueue.removeEntries"/>"
                           onclick="$('removeForm').action='removeBuildQueueEntries!removeBuildEntries.action';$('removeForm').submit();" /> 
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </c:if>
        
        
        <!-- checkout queue -->
        <div id="h3">
          <h3>
            <s:text name="checkoutQueue.currentTask.section.title"/>
          </h3>  
          <table width="100%">
            <s:if test="%{currentCheckOutTask != null}">
            <tbody>
              <tr>
                <th><s:text name="checkoutQueue.currentTask.projectName"/></th>
                <th>&nbsp;</th>
              </tr>
              <tr>
                <td width="99%"><s:property value="currentCheckOutTask.projectName"/></td>
                <td width="1%">
                <redback:ifAuthorized permission="continuum-manage-queues">
                  <s:url id="cancelUrl" action="cancelCurrentQueueTask" method="cancelCurrentCheckout" namespace="/">
                    <s:param name="projectId"><s:property value="currentCheckOutTask.projectId"/></s:param>
                  </s:url>
                  <s:a href="%{cancelUrl}"><img src="<s:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0"></s:a>
                </redback:ifAuthorized>
                <redback:elseAuthorized>
                  <img src="<s:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<s:text name='cancel'/>" title="<s:text name='cancel'/>" border="0">
                </redback:elseAuthorized>              
                </td>
              </tr>
            </tbody>
            </s:if>
            <s:else>
              <s:text name="checkoutQueue.no.currentTaks" />
            </s:else>
          </table>
        </div>    
        <div id="h3">
          <h3>
            <s:text name="checkoutQueue.section.title"/>
          </h3>  
            <c:if test="${!empty currentCheckOutTasks}">
              <ec:table items="currentCheckOutTasks"
                        var="currentCheckOutTask"
                        showExports="false"
                        showPagination="false"
                        showStatusBar="false"
                        sortable="false"
                        filterable="false">
                <ec:row highlightRow="true">
                  <redback:ifAuthorized permission="continuum-manage-queues">
                  <ec:column alias="selectedCheckOutTaskHashCodes" title="&nbsp;" style="width:5px" filterable="false" sortable="false" width="1%" headerCell="selectAll">
                    <input type="checkbox" name="selectedCheckOutTaskHashCodes" value="${currentCheckOutTask.hashCode}" />
                  </ec:column>              
                  </redback:ifAuthorized>
                  <ec:column property="projectName" title="Project Name" style="white-space: nowrap" width="98%"/>
                  <ec:column property="cancelEntry" title="&nbsp;" width="1%">
                  <redback:ifAuthorized permission="continuum-manage-queues">
                    <s:url id="cancelUrl" action="removeCheckoutQueueEntry" method="removeCheckout">
                      <s:param name="projectId">${pageScope.currentCheckOutTask.projectId}</s:param>
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
            <c:if test="${empty currentCheckOutTasks}">
              <s:text name="checkoutQueue.empty"/>
            </c:if>
        </div>
        <c:if test="${not empty currentCheckOutTasks}">
          <div class="functnbar3">
            <table>
              <tbody>
                <tr>
                  <td>
                    <redback:ifAuthorized permission="continuum-manage-queues">
                    <input type="submit" value="<s:text name="checkoutQueue.removeEntries"/>"
                           onclick="$('removeForm').action='removeCheckoutQueueEntries!removeCheckoutEntries.action';$('removeForm').submit();" />
                    </redback:ifAuthorized>
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