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
<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<html>
  <ww:i18n name="localization.Continuum">
    <head>
      <title><ww:text name="buildQueue.page.title"/></title>
      <meta http-equiv="refresh" content="60"/>
    </head>
    <body>
      <ww:form id="removeForm" action="none" method="post">
        <div id="h3">
          <h3>
            <ww:text name="buildQueue.currentTask.section.title"/>
          </h3>  
          <table width="100%">
            <ww:if test="currentBuildProjectTask != null">
            <tbody>
              <tr>
                <th><ww:text name="buildQueue.currentTask.projectName"/></th>
                <th><ww:text name="buildQueue.currentTask.buildDefinition"/></th>
                <th>&nbsp;</th>
              </tr>
              <tr>
                <td width="50%"><ww:property value="currentBuildProjectTask.projectName"/></td>
                <td width="49%"><ww:property value="currentBuildProjectTask.buildDefinitionLabel"/></td>
                <td width="1%">
                <redback:ifAuthorized permission="continuum-manage-queues">
                  <ww:url id="cancelUrl" action="cancelCurrentBuildTask" method="cancelCurrent" namespace="/">
                    <ww:param name="projectId"><ww:property value="currentBuildProjectTask.projectId"/></ww:param>
                  </ww:url>      
                  <ww:a href="%{cancelUrl}"><img src="<ww:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<ww:text name='cancel'/>" title="<ww:text name='cancel'/>" border="0"></ww:a>              
                </redback:ifAuthorized>
                <redback:elseAuthorized>
                  <img src="<ww:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<ww:text name='cancel'/>" title="<ww:text name='cancel'/>" border="0">
                </redback:elseAuthorized>
                </td>
              </tr>
            </tbody>
            </ww:if>
            <ww:else>
              <ww:text name="buildQueue.no.currentTaks" />
            </ww:else>
          </table>
        </div>    
        <div id="h3">
          <h3>
            <ww:text name="buildQueue.section.title"/>
          </h3>  
            <ww:if test="${not empty buildProjectTasks}">
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
                    <ww:url id="cancelUrl" action="removeBuildQueueEntry" method="remove" namespace="/">
                      <ww:param name="projectId">${pageScope.buildProjectTask.projectId}</ww:param>
                      <ww:param name="buildDefinitionId">${pageScope.buildProjectTask.buildDefinitionId}</ww:param>
                      <ww:param name="trigger">${pageScope.buildProjectTask.trigger}</ww:param>
                      <ww:param name="projectName">${pageScope.buildProjectTask.projectName}</ww:param>
                    </ww:url>      
                    <ww:a href="%{cancelUrl}"><img src="<ww:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<ww:text name='cancel'/>" title="<ww:text name='cancel'/>" border="0"></ww:a>
                    </redback:ifAuthorized>
                    <redback:elseAuthorized>
                      <img src="<ww:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<ww:text name='cancel'/>" title="<ww:text name='cancel'/>" border="0">
                    </redback:elseAuthorized>    
                  </ec:column>             
                </ec:row>
              </ec:table>
            </ww:if>
            <ww:else>
              <ww:text name="buildQueue.empty"/>
            </ww:else>
        </div>
        <ww:if test="${not empty buildProjectTasks}">
          <div class="functnbar3">
            <table>
              <tbody>
                <tr>
                  <td>
                    <input type="submit" value="<ww:text name="buildQueue.removeEntries"/>" 
                           onclick="$('removeForm').action='removeBuildQueueEntries!removeBuildEntries.action';$('removeForm').submit();" /> 
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </ww:if>      
        
        
        <!-- checkout queue -->
        <div id="h3">
          <h3>
            <ww:text name="checkoutQueue.currentTask.section.title"/>
          </h3>  
          <table width="100%">
            <ww:if test="currentCheckOutTask != null">
            <tbody>
              <tr>
                <th><ww:text name="checkoutQueue.currentTask.projectName"/></th>
                <th>&nbsp;</th>
              </tr>
              <tr>
                <td width="99%"><ww:property value="currentCheckOutTask.projectName"/></td>
                <td width="1%">
                <redback:ifAuthorized permission="continuum-manage-queues">
                  <ww:url id="cancelUrl" action="cancelCurrentQueueTask" method="cancelCurrentCheckout" namespace="/">
                    <ww:param name="projectId"><ww:property value="currentCheckOutTask.projectId"/></ww:param>
                  </ww:url>      
                  <ww:a href="%{cancelUrl}"><img src="<ww:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<ww:text name='cancel'/>" title="<ww:text name='cancel'/>" border="0"></ww:a>
                </redback:ifAuthorized>
                <redback:elseAuthorized>
                  <img src="<ww:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<ww:text name='cancel'/>" title="<ww:text name='cancel'/>" border="0">
                </redback:elseAuthorized>              
                </td>
              </tr>
            </tbody>
            </ww:if>
            <ww:else>
              <ww:text name="checkoutQueue.no.currentTaks" />
            </ww:else>
          </table>
        </div>    
        <div id="h3">
          <h3>
            <ww:text name="checkoutQueue.section.title"/>
          </h3>  
            <ww:if test="${not empty currentCheckOutTasks}">
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
                    <ww:url id="cancelUrl" action="removeCheckoutQueueEntry" method="removeCheckout">
                      <ww:param name="projectId">${pageScope.currentCheckOutTask.projectId}</ww:param>
                    </ww:url>      
                    <ww:a href="%{cancelUrl}"><img src="<ww:url value='/images/cancelbuild.gif' includeParams="none"/>" alt="<ww:text name='cancel'/>" title="<ww:text name='cancel'/>" border="0"></ww:a>
                  </redback:ifAuthorized>
                  <redback:elseAuthorized>
                    <img src="<ww:url value='/images/cancelbuild_disabled.gif' includeParams="none"/>" alt="<ww:text name='cancel'/>" title="<ww:text name='cancel'/>" border="0">
                  </redback:elseAuthorized>    
                  </ec:column>             
                </ec:row>
              </ec:table>
            </ww:if>
            <ww:else>
              <ww:text name="checkoutQueue.empty"/>
            </ww:else>
        </div>
        <ww:if test="${not empty currentCheckOutTasks}">
          <div class="functnbar3">
            <table>
              <tbody>
                <tr>
                  <td>
                    <redback:ifAuthorized permission="continuum-manage-queues">
                    <input type="submit" value="<ww:text name="checkoutQueue.removeEntries"/>" 
                           onclick="$('removeForm').action='removeCheckoutQueueEntries!removeCheckoutEntries.action';$('removeForm').submit();" />
                    </redback:ifAuthorized>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </ww:if>            
      </ww:form>
    </body>
  </ww:i18n>
</html>