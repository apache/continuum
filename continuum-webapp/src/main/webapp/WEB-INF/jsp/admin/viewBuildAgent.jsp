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
      <title><s:text name="viewBuildAgent.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <h3><s:text name="viewBuildAgent.section.title"/></h3>
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <tr class="b">
              <th><label class="label"><s:text name='buildAgent.url.label'/>:</label></th>
              <td><s:property value="buildAgent.url"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='buildAgent.description.label'/>:</label></th>
              <td><s:property value="buildAgent.description"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='buildAgent.enabled.label'/>:</label></th>
              <td><s:property value="buildAgent.enabled"/></td>
            </tr>
          </table>
        </div>
        
        <h3><s:text name="viewBuildAgent.installations.title"/></h3>
        <ec:table items="installations"
				  var="installation"
          autoIncludeParameters="false"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  sortable="false"
                  filterable="false">
          <ec:row highlightRow="true">
            <ec:column property="name" title="viewBuildAgent.installations.name" style="white-space: nowrap" />
            <ec:column property="type" title="viewBuildAgent.installations.type" style="white-space: nowrap" />
            <ec:column property="varName" title="viewBuildAgent.installations.varName" style="white-space: nowrap" />
            <ec:column property="varValue" title="viewBuildAgent.installations.varValue" style="white-space: nowrap" />
          </ec:row>
        </ec:table>        	
      </div>
    </body>
  </s:i18n>
</html>