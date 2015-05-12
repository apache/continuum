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
<html>
  <s:i18n name="localization.Continuum">
    <head>
        <title><s:text name="buildDefinition.template.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><s:text name="buildDefinition.template.section.title"/></h3>

        <s:if test="hasActionErrors()">
            <div class="errormessage">
                <s:actionerror/>
            </div>
            <input type="button" value="Back" onClick="history.go(-1)">
        </s:if>
        <s:if test="hasActionMessages()">
            <div class="warningmessage">
                <s:actionmessage/>
            </div>
        </s:if>

        <div class="axial">
          <s:form action="saveBuildDefinitionTemplate" method="post" name="buildDefinitionTemplate" validate="false">
              <s:if test="!hasActionErrors()">
                <table>
                  <tbody>
                    <s:textfield label="%{getText('buildDefinitionTemplate.name')}" name="buildDefinitionTemplate.name" requiredLabel="true" size="100"/>
                    <s:optiontransferselect
                        label="%{getText('buildDefinitionTemplate.builddefinitions.define')}"    
                        name="buildDefinitionIds"
                        list="buildDefinitions" 
                        listKey="id"
                        listValue="description"
                        headerKey="hk-1"
                        headerValue="%{getText('buildDefinitionTemplate.available.builddefinitions')}"
                        multiple="true"
                        emptyOption="false"
                        doubleName="selectedBuildDefinitionIds"
                        doubleList="buildDefinitionTemplate.buildDefinitions" 
                        doubleListKey="id"
                        doubleListValue="description"
                        doubleHeaderKey="hk-1"
                        doubleHeaderValue="%{getText('buildDefinitionTemplate.available.builddefinitions.used')}" 
                        doubleMultiple="true" 
                        doubleEmptyOption="false"
                        formName="buildDefinitionTemplate"
                        addAllToRightOnclick="selectAllOptionsExceptSome(document.getElementById('saveBuildDefinitionTemplate_selectedBuildDefinitionIds'), 'key', 'hk-1');"
                        addToRightOnclick="selectAllOptionsExceptSome(document.getElementById('saveBuildDefinitionTemplate_buildDefinitionIds'), 'key', 'hk-1');selectAllOptionsExceptSome(document.getElementById('saveBuildDefinitionTemplate_selectedBuildDefinitionIds'), 'key', 'hk-1');"
                        addAllToLeftOnclick="selectAllOptionsExceptSome(document.getElementById('saveBuildDefinitionTemplate_buildDefinitionIds'), 'key', 'hk-1');"
                        addToLeftOnclick="selectAllOptionsExceptSome(document.getElementById('saveBuildDefinitionTemplate_buildDefinitionIds'), 'key', 'hk-1');selectAllOptionsExceptSome(document.getElementById('saveBuildDefinitionTemplate_selectedBuildDefinitionIds'), 'key', 'hk-1');"
                        />
                  </tbody>
                </table>
                <div class="functnbar3">
                  <s:submit value="%{getText('save')}" theme="simple"/>
                  <input type="button" name="Cancel" value="<s:text name='cancel'/>" onclick="history.back();"/>
                </div>
                <s:hidden name="buildDefinitionTemplate.id"/>
                <s:hidden name="buildDefinitionTemplate.continuumDefault"/>
              </s:if>
          </s:form>
        </div>
      </div>
    </body>
  </s:i18n>
</html>

