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
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<html>
<s:i18n name="localization.Continuum">
  <head>
    <title><s:text name="profilesList.page.title"/></title>
  </head>
  
  <div id="h3">
    <h3>
      <s:text name="profilesList.section.title"/>
    </h3>

    <s:if test="hasActionErrors()">
      <div class="errormessage">
        <s:actionerror/>
      </div>
    </s:if>
    <s:if test="hasActionMessages()">
      <div class="warningmessage">
        <s:actionmessage/>
      </div>
    </s:if>
        
    <s:if test="profiles.size() > 0">
    <ec:table items="profiles"
              var="profile"
              autoIncludeParameters="false"
              showExports="false"
              showPagination="false"
              showStatusBar="false"
              sortable="false"
              filterable="false">
      <ec:row highlightRow="true">
        <ec:column property="name" title="profile.name.label" style="white-space: nowrap" />
        <ec:column property="Installations" title="profilesList.installations.label" style="white-space: nowrap">
          <ul>
            <s:if test="#attr.profile.jdk != null">
              <li><s:property value="#attr.profile.jdk.name"/> (<s:property value="#attr.profile.jdk.type"/>)</li>
            </s:if>
            <s:if test="#attr.profile.builder != null">
              <li><s:property value="#attr.profile.builder.name"/> (<s:property value="#attr.profile.builder.type"/>)</li>
            </s:if>
            <s:if test="#attr.profile.environmentVariables != null">
              <s:iterator value="#attr.profile.environmentVariables">
                <li><s:property value="name" /></li>
              </s:iterator>
            </s:if>
          </ul>
        </ec:column>
        <c1:ifBuildTypeEnabled buildType="distributed">
          <ec:column property="buildAgentGroup" title="profilesList.buildAgentGroup.label" style="white-space: nowrap" />
        </c1:ifBuildTypeEnabled>                
        <ec:column property="id" title="&nbsp;" width="1%">
          <s:url var="editUrl" action="editBuildEnv">
            <s:param name="profile.id" value="#attr.profile.id" />
          </s:url>
          <s:a href="%{editUrl}">
            <img src="<s:url value='/images/edit.gif' includeParams="none"/>" alt="<s:text name='edit'/>" title="<s:text name='edit'/>" border="0" />
          </s:a>
        </ec:column>
        <ec:column property="id" title="&nbsp;" width="1%">
          <s:url var="deleteUrl" action="confirmDeleteBuildEnv">
            <s:param name="profile.id" value="#attr.profile.id" />
          </s:url>
          <s:a href="%{deleteUrl}">
            <img src="<s:url value='/images/delete.gif' includeParams="none"/>" alt="<s:text name='delete'/>" title="<s:text name='delete'/>" border="0" />
          </s:a>
        </ec:column>
      </ec:row>
    </ec:table>
    </s:if>
    <div class="functnbar3">
      <s:form action="addBuildEnv" method="post">
        <s:submit value="%{getText('add')}"/>
      </s:form>
    </div>    
  
  </div>
  
</s:i18n>
