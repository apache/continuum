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
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title><ww:text name="groups.page.title"/></title>
    <meta http-equiv="refresh" content="300"/>
  </head>

  <body>
  <div id="h3">

    <ww:if test="${infoMessage != null}">
       <p>${infoMessage}</p>
    </ww:if>
    <ww:else>
       <h3><ww:text name="groups.page.section.title"/>Project Groups</h3>
    </ww:else>
  
    <ww:if test="${empty groups}">
      <ww:text name="groups.page.list.empty"/>
    </ww:if>

    <ww:if test="${not empty groups}">

    <ec:table items="groups"
              var="group"
              showExports="false"
              showPagination="false"
              showStatusBar="false"
              sortable="false"
              filterable="false">
      <ec:row highlightRow="true">
        <ec:column property="name" title="groups.table.name" width="20%" style="white-space: nowrap">
          <a href="<ww:url  action="projectGroupSummary" namespace="/"><ww:param name="projectGroupId" value="%{'${group.id}'}"/></ww:url>">${group.name}</a>
        </ec:column>
        <ec:column property="groupId" title="groups.table.groupId" width="20%"/>
        <ec:column property="numProjects" title="groups.table.nbProjects" format="0" width="1%" style="text-align: right" calc="total" calcTitle="groups.table.summary"/>
        <ec:column property="numSuccesses" title="&nbsp;" format="0" width="2%" style="text-align: right" headerClass="calcHeaderSucces" calc="total" />
        <ec:column property="numFailures" title="&nbsp;" format="0" width="2%" style="text-align: right" headerClass="calcHeaderFailure" calc="total" />
        <ec:column property="numErrors" title="&nbsp;" format="0" width="2%" style="text-align: right" headerClass="calcHeaderError" calc="total"/>
      </ec:row>
    </ec:table>
    </ww:if>
    <redback:ifAuthorized permission="continuum-add-group">
      <div class="functnbar3">
        <table>
          <tr>
            <td>
              <form action="<ww:url  action='addProjectGroup' method='input' namespace='/' />" method="post">
                <input type="submit" name="addProjectGroup" value="<ww:text name="projectGroup.add.section.title"/>"/>
              </form>
            </td>
          </tr>
        </table>
      </div>
    </redback:ifAuthorized>
  </div>
  </body>
</ww:i18n>
</html>
