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

<html>
  <ww:i18n name="localization.Continuum">
    <head>
      <title><ww:text name="buildDefinition.templates.page.title"/></title>
    </head>
    
    <body>
    <div>
      
      <h3>
        <ww:text name="buildDefinition.templates.section.title"/>
      </h3>
      <ec:table items="templates"
                var="template"
                showExports="false"
                showPagination="false"
                showStatusBar="false"
                filterable="false"
                sortable="false">
        <ec:row>
          <ec:column property="name" title="buildDefinition.template.name"/>
          <ec:column property="editAction" title="&nbsp;" width="1%">
            <ww:url id="editUrl" action="editBuildDefinitionTemplate" method="edit" namespace="/">
              <ww:param name="buildDefinitionTemplate.id">${pageScope.template.id}</ww:param>
            </ww:url>
            <ww:a href="%{editUrl}"><img src="<ww:url value='/images/edit.gif' includeParams="none"/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0"></ww:a>          
          </ec:column>  
          <!-- TODO list attached buildDefs -->
          <ec:column property="deleteAction" title="&nbsp;" width="1%">
            <ww:if test="${template.continuumDefault == true}">
              <img src="<ww:url value='/images/delete_disabled.gif' includeParams="none"/>" alt="<ww:text name='disabled'/>" title="<ww:text name='disabled'/>" border="0" />
            </ww:if>
            <ww:else>
              <ww:url id="deleteUrl" action="deleteDefinitionTemplate" method="delete" namespace="/">
                <ww:param name="buildDefinitionTemplate.id">${pageScope.template.id}</ww:param>
              </ww:url>
              <ww:a href="%{deleteUrl}"><img src="<ww:url value='/images/delete.gif' includeParams="none"/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0"></ww:a>
            </ww:else>          
          </ec:column>                     
        </ec:row>  
      </ec:table> 
      <div class="functnbar3">
        <ww:form action="buildDefinitionTemplate!input.action" method="post">
          <ww:submit value="%{getText('add')}"/>
        </ww:form>
      </div>      
      <h3>
        <ww:text name="buildDefinition.templates.buildDefinitions.section.title"/>
      </h3>
      <ec:table items="buildDefinitionSummaries"
                var="buildDefinitionSummary"
                showExports="false"
                showPagination="false"
                showStatusBar="false"
                filterable="false"
                sortable="false">
        <ec:row>
          <ec:column property="goals" title="buildDefinition.template.buildDefinition.goals"/>
          <ec:column property="arguments" title="buildDefinition.template.buildDefinition.arguments"/>
          <!-- change the label for shell -->
          <ec:column property="buildFile" title="buildDefinition.template.buildDefinition.buildFile"/>
          <ec:column property="scheduleName" title="buildDefinition.template.buildDefinition.schedule"/>
          <ec:column property="profileName" title="buildDefinition.template.buildDefinition.profile"/>
          <ec:column property="isBuildFresh" title="buildDefinition.template.buildDefinition.buildFresh"/>
          <ec:column property="isDefault" title="buildDefinition.template.buildDefinition.default"/>
          <ec:column property="description" title="buildDefinition.template.buildDefinition.description"/>
          <ec:column property="type" title="buildDefinition.template.buildDefinition.type"/>
          <ec:column property="editAction" title="&nbsp;" width="1%">
            <ww:url id="editUrl" action="editBuildDefinitionAsTemplate" method="editBuildDefinition" namespace="/">
              <ww:param name="buildDefinition.id">${pageScope.buildDefinitionSummary.id}</ww:param>
            </ww:url>
            <ww:a href="%{editUrl}"><img src="<ww:url value='/images/edit.gif' includeParams="none"/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0"></ww:a>          
          </ec:column>          
          <ec:column property="deleteAction" title="&nbsp;" width="1%">
            <ww:if test="${buildDefinitionSummary.isDefault == true}">
              <img src="<ww:url value='/images/delete_disabled.gif' includeParams="none"/>" alt="<ww:text name='disabled'/>" title="<ww:text name='disabled'/>" border="0" />
            </ww:if>
            <ww:else>
              <ww:url id="deleteUrl" action="deleteBuildDefinitionAsTemplate" method="deleteBuildDefinition" namespace="/">
                <ww:param name="buildDefinition.id">${pageScope.buildDefinitionSummary.id}</ww:param>
              </ww:url>
              <ww:a href="%{deleteUrl}"><img src="<ww:url value='/images/delete.gif' includeParams="none"/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0"></ww:a>
            </ww:else>          
          </ec:column>
        </ec:row>  
      </ec:table>      
      
      <div class="functnbar3">
        <ww:form action="buildDefinitionAsTemplate!inputBuildDefinition.action" method="post">
          <ww:submit value="%{getText('add')}"/>
        </ww:form>
      </div>           
      
    </div>
    </body>
  </ww:i18n>
</html> 