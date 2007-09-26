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
<ww:i18n name="localization.Continuum">
  <ww:if test="${not empty buildProjectTasks}">
    <ec:table items="buildProjectTasks"
              var="buildProjectTask"
              showExports="false"
              showPagination="false"
              showStatusBar="false"
              sortable="false"
              filterable="false">
      <ec:row highlightRow="true">
        <ec:column property="projectName" title="Project Name" style="white-space: nowrap" />
        <ec:column property="cancelEntry" title="&nbsp;" width="1%">
          <ww:url id="cancelUrl" action="removeBuildQueueEntry" method="remove" namespace="/">
            <ww:param name="projectId">${pageScope.buildProjectTask.projectId}</ww:param>
            <ww:param name="buildDefinitionId">${pageScope.buildProjectTask.buildDefinitionId}</ww:param>
            <ww:param name="trigger">${pageScope.buildProjectTask.trigger}</ww:param>
            <ww:param name="projectName">${pageScope.buildProjectTask.projectName}</ww:param>
          </ww:url>      
          <ww:a href="%{cancelUrl}"><img src="<ww:url value='/images/cancelbuild.gif'/>" alt="<ww:text name='cancel'/>" title="<ww:text name='cancel'/>" border="0"></ww:a>    
        </ec:column>             
      </ec:row>
    </ec:table>
  </ww:if>
  <ww:else>
    <ww:text name="buildQueue.empty"/>
  </ww:else>
</ww:i18n>