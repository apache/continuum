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
      <title><s:text name="buildAgent.page.title"/></title>
    </head>
    <body>
    <div class="app">
      <div id="axial" class="h3">
        <h3><s:text name="buildAgent.section.title"/></h3>

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
          <s:form action="saveBuildAgent" method="post" validate="true">
            
            <table>
              <s:hidden name="type"/>
                <s:if test="type == 'new'">
                  <s:textfield label="%{getText('buildAgent.url.label')}" name="buildAgent.url" requiredLabel="true" size="100"/>
                </s:if>
                <s:else>
                  <s:hidden name="buildAgent.url"/>
                  <s:textfield label="%{getText('buildAgent.url.label')}" name="buildAgent.url" requiredLabel="true" disabled="true" size="100"/>
                </s:else>
              <s:textfield label="%{getText('buildAgent.description.label')}" name="buildAgent.description" size="100"/>
              <s:checkbox label="%{getText('buildAgent.enabled.label')}" name="buildAgent.enabled" value="buildAgent.enabled" fieldValue="true"/>
            </table>
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