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
      <title><s:text name="buildAgentGroup.page.title"/></title>
    </head>
    <body>
    <div class="app">
      <div id="axial" class="h3">
        <h3><s:text name="buildAgentGroup.section.title"/></h3>

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

        <div class="axial">
          <s:form action="saveBuildAgentGroup" method="post"  name="buildAgentGroup">

            <table>
              <s:hidden name="typeGroup"/>
                <s:if test="typeGroup == 'new'">
                  <s:textfield label="%{getText('buildAgentGroup.name.label')}" name="buildAgentGroup.name" requiredLabel="true" size="100"/>
                </s:if>
                <s:else>
                  <s:hidden name="buildAgentGroup.name"/>
                  <s:textfield label="%{getText('buildAgentGroup.name.label')}" name="buildAgentGroup.name" requiredLabel="true" disabled="true" size="100"/>
                </s:else>
            </table>

              <s:if test="buildAgents.size() > 0 || selectedBuildAgentIds.size() > 0">
                <table>
                  <s:optiontransferselect
                        label="%{getText('buildAgentGroup.buildAgents.define')}"    
                        name="buildAgentIds"
                        list="buildAgents" 
                        listKey="url"
                        listValue="url"
                        headerKey="hk-1"
                        headerValue="%{getText('buildAgentGroup.available.buildAgents')}"
                        multiple="true"
                        emptyOption="false"
                        doubleName="selectedBuildAgentIds"
                        doubleList="buildAgentGroup.buildAgents" 
                        doubleListKey="url"
                        doubleListValue="url"
                        doubleHeaderKey="hk-1"
                        doubleHeaderValue="%{getText('buildAgentGroup.available.buildAgents.used')}" 
                        doubleMultiple="true" 
                        doubleEmptyOption="false"
                        formName="buildAgentGroup"
                        addAllToRightOnclick="selectAllOptionsExceptSome(document.getElementById('saveBuildAgentGroup_selectedBuildAgentIds'), 'key', 'hk-1');"
                        addToRightOnclick="selectAllOptionsExceptSome(document.getElementById('saveBuildAgentGroup_buildAgentIds'), 'key', 'hk-1');selectAllOptionsExceptSome(document.getElementById('saveBuildAgentGroup_selectedBuildAgentIds'), 'key', 'hk-1');"
                        addAllToLeftOnclick="selectAllOptionsExceptSome(document.getElementById('saveBuildAgentGroup_buildAgentIds'), 'key', 'hk-1');"
                        addToLeftOnclick="selectAllOptionsExceptSome(document.getElementById('saveBuildAgentGroup_buildAgentIds'), 'key', 'hk-1');selectAllOptionsExceptSome(document.getElementById('saveBuildAgentGroup_selectedBuildAgentIds'), 'key', 'hk-1');"
                        />
                </table>
              </s:if>
              <s:else>
                <div class="errormessage">
                  <s:text name="buildAgents.empty"/>
                </div>
              </s:else>

            <div class="functnbar3">
              <s:submit value="%{getText('save')}" theme="simple"/>
              <input type="button" name="Cancel" value="<s:text name='cancel'/>" onclick="history.back();"/>
            </div>
          </s:form>
        </div>
      </div>
    </div>
  </s:i18n>
 </html>