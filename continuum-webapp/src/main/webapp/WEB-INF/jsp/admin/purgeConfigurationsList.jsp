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
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<html>
  <ww:i18n name="localization.Continuum">
    <head>
      <title><ww:text name="purgeConfigs.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <h3><ww:text name="purgeConfigs.repo.section.title"/></h3>
        <c:if test="${!empty actionErrors}">
          <div class="errormessage">
            <c:forEach items="${actionErrors}" var="actionError">
              <p><ww:text name="${actionError}"/></p>
            </c:forEach>
          </div>
        </c:if>
        <ww:set name="repoPurgeConfigs" value="repoPurgeConfigs" scope="request"/>
        <ec:table items="repoPurgeConfigs"
                  var="repoPurge"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  sortable="false"
                  filterable="false">
         <ec:row>
            <ec:column property="repository.name" title="purgeConfigs.table.repository">
              <redback:ifAuthorized permission="continuum-manage-repositories">
                <ww:url id="editRepositoryUrl" action="editRepository" namespace="/admin" includeParams="none">
                  <ww:param name="repository.id">${pageScope.repoPurge.repository.id}</ww:param>
                </ww:url>
                <ww:a href="%{editRepositoryUrl}">${pageScope.repoPurge.repository.name}</ww:a> 
              </redback:ifAuthorized>
              <redback:elseAuthorized>
                ${pageScope.repoPurge.repository.name}
              </redback:elseAuthorized>
            </ec:column>
            <ec:column property="daysOlder" title="purgeConfigs.table.daysOlder"/>
            <ec:column property="retentionCount" title="purgeConfigs.table.retentionCount"/>
            <ec:column property="deleteAll" title="purgeConfigs.table.deleteAll"/>
            <ec:column property="deleteReleasedSnapshots" title="purgeConfigs.table.deleteReleasedSnapshots"/>
            <ec:column property="schedule.name" title="purgeConfigs.table.schedule"/>
            <ec:column property="defaultPurge" title="purgeConfigs.table.default"/>
            <ec:column property="enabled" title="purgeConfigs.table.enabled"/>
            <ec:column property="description" title="purgeConfigs.table.description"/>
            <ec:column property="editActions" title="&nbsp;" width="1%">
                <ww:url id="editPurgeConfigUrl" action="editPurgeConfig">
                  <ww:param name="purgeConfigId" value="${pageScope.repoPurge.id}"/>
                </ww:url>
                <ww:a href="%{editPurgeConfigUrl}"><img src="<ww:url value='/images/edit.gif' includeParams="none"/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0" /></ww:a>
            </ec:column>
            <ec:column property="purgeActions" title="&nbsp;" width="1%">
                <ww:url id="purgeUrl" action="doPurge">
                  <ww:param name="purgeConfigId" value="${pageScope.repoPurge.id}"/>
                </ww:url>
                <ww:a href="%{purgeUrl}"><img src="<ww:url value='/images/purgenow.gif' includeParams="none"/>" alt="<ww:text name='purge'/>" title="<ww:text name='purge'/>" border="0" /></ww:a>
            </ec:column>
            <ec:column property="deleteActions" title="&nbsp;" width="1%">
                <ww:url id="removePurgeConfigUrl" action="removePurgeConfig">
                  <ww:param name="purgeConfigId" value="${pageScope.repoPurge.id}"/>                 
                </ww:url>
                <ww:a href="%{removePurgeConfigUrl}"><img src="<ww:url value='/images/delete.gif' includeParams="none"/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0"></ww:a>
            </ec:column>
          </ec:row>
        </ec:table>
      </div>
      <div class="functnbar3">
        <ww:form name="addRepoPurgeConfig" action="editPurgeConfig" method="post">
          <ww:hidden name="purgeType" value="repository"/>
          <ww:submit value="%{getText('add')}"/>
        </ww:form>
      </div>
        
      <div id="h3">
        <h3><ww:text name="purgeConfigs.dir.section.title"/></h3>
        <ww:set name="dirPurgeConfigs" value="dirPurgeConfigs" scope="request"/>
        <ec:table items="dirPurgeConfigs"
                  var="dirPurge"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  sortable="false"
                  filterable="false">
         <ec:row>
            <ec:column property="directoryType" title="purgeConfigs.table.directoryType"/>
            <ec:column property="daysOlder" title="purgeConfigs.table.daysOlder"/>
            <ec:column property="retentionCount" title="purgeConfigs.table.retentionCount"/>
            <ec:column property="deleteAll" title="purgeConfigs.table.deleteAll"/>
            <ec:column property="schedule.name" title="purgeConfigs.table.schedule"/>
            <ec:column property="defaultPurge" title="purgeConfigs.table.default"/>
            <ec:column property="enabled" title="purgeConfigs.table.enabled"/>
            <ec:column property="description" title="purgeConfigs.table.description"/>
            <ec:column property="editActions" title="&nbsp;" width="1%">
                <ww:url id="editPurgeConfigUrl" action="editPurgeConfig">
                  <ww:param name="purgeConfigId" value="${pageScope.dirPurge.id}"/>
                </ww:url>
                <ww:a href="%{editPurgeConfigUrl}"><img src="<ww:url value='/images/edit.gif' includeParams="none"/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0" /></ww:a>
            </ec:column>
            <ec:column property="purgeActions" title="&nbsp;" width="1%">
                <ww:url id="purgeUrl" action="doPurge">
                  <ww:param name="purgeConfigId" value="${pageScope.dirPurge.id}"/>
                </ww:url>
                <ww:a href="%{purgeUrl}"><img src="<ww:url value='/images/purgenow.gif' includeParams="none"/>" alt="<ww:text name='purge'/>" title="<ww:text name='purge'/>" border="0" /></ww:a>
            </ec:column>
            <ec:column property="deleteActions" title="&nbsp;" width="1%">
                <ww:url id="removePurgeConfigUrl" action="removePurgeConfig">
                  <ww:param name="purgeConfigId" value="${pageScope.dirPurge.id}"/>                 
                </ww:url>
                <ww:a href="%{removePurgeConfigUrl}"><img src="<ww:url value='/images/delete.gif' includeParams="none"/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0"></ww:a>
            </ec:column>
          </ec:row>
        </ec:table>
      </div>
      <div class="functnbar3">
        <ww:form name="addDirPurgeConfig" action="editPurgeConfig" method="post">
          <ww:hidden name="purgeType" value="directory"/>
          <ww:submit value="%{getText('add')}"/>
        </ww:form>
      </div>
    </body>
  </ww:i18n>
</html>