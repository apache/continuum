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
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<html>
  <ww:i18n name="localization.Continuum">
    <head>
      <title><ww:text name="projectGroup.page.title"/></title>
    </head>

    <body>
      <div id="h3">

      <ww:action name="projectGroupTab" executeResult="true">
        <ww:param name="tabName" value="'ReleaseResults'"/>
      </ww:action>
    
      <h3><ww:text name="projectGroup.releaseResults.section.title"><ww:param>${projectGroup.name}</ww:param></ww:text></h3>
      
      <form id="releaseResultsForm" action="removeReleaseResults.action" method="post">
        <ww:hidden name="projectGroupId"/>
        <ec:table items="releaseResults"
                var="result"
                showExports="false"
                showPagination="false"
                showStatusBar="false"
                filterable="false"
                sortable="false">
          <ec:row highlightRow="true">
            <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroup.name}">
              <ec:column alias="selectedReleaseResults" title=" " style="width:5px" filterable="false" sortable="false" headerCell="selectAll">
                <input type="checkbox" name="selectedReleaseResults" value="${result.id}" />
              </ec:column>
            </redback:ifAuthorized>
            <ec:column property="project.name" title="releaseResults.project"/>
            <ec:column property="releaseGoal" title="releaseResults.releaseGoal"/>
            <ec:column property="startTime" title="releaseResults.startTime" cell="date"/>
            <ec:column property="endTime" title="releaseResults.endTime" cell="date"/>
            <ec:column property="resultCode" title="releaseResults.state">
              <ww:if test="${pageScope.result.resultCode == 0}">
                <ww:text name="releaseViewResult.success"/>
              </ww:if>
              <ww:else>
                <ww:text name="releaseViewResult.error"/>
              </ww:else>
            </ec:column>
            <ec:column property="actions" title="&nbsp;">
               <ww:url id="viewReleaseResultUrl" action="viewReleaseResult">
                 <ww:param name="releaseResultId" value="${pageScope.result.id}"/>
                 <ww:param name="projectGroupId" value="${projectGroupId}"/>
               </ww:url>
               <ww:a href="%{viewReleaseResultUrl}"><ww:text name="releaseResults.viewResult"/></ww:a>
             </ec:column>
          </ec:row>
        </ec:table>
        <ww:if test="${not empty releaseResults}">
          <div class="functnbar3">
            <table>
              <tbody>
                <tr>
                  <td>
                    <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroup.name}">
                      <input type="button" name="delete-release-results" value="<ww:text name="delete"/>" onclick="document.forms.releaseResultsForm.submit();" />
                    </redback:ifAuthorized>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </ww:if>
      </form>
      </div>
    </body>
  </ww:i18n>
</html>