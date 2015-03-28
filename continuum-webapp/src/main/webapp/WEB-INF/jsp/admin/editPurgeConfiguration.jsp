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
    <title><s:text name="purgeConfig.page.title"/></title>
  </head>
  <body>
  <div class="app">
    <div id="axial" class="h3">
      <h3><s:text name="purgeConfig.section.title"/></h3>

    <div class="axial">
      <s:form action="savePurgeConfig" method="post" validate="true">
        <s:if test="hasActionErrors()">
          <div class="errormessage">
            <s:iterator value="actionErrors">
              <p><s:property/></p>
            </s:iterator>
          </div>
        </s:if>
        <s:if test="repositories.size() > 0 || purgeType == 'directory'">
          <table>
            <s:if test="purgeType == 'repository'">
                <s:select label="%{getText('purgeConfig.repository.label')}" name="repositoryId" list="repositories" requiredLabel="true"/>
            </s:if>
            <s:else>
                <s:select label="%{getText('purgeConfig.directoryType.label')}" name="directoryType" list="directoryTypes"/>
            </s:else>
            <s:textfield label="%{getText('purgeConfig.daysOlder.label')}" name="daysOlder" size="100"/>
            <s:textfield label="%{getText('purgeConfig.retentionCount.label')}" name="retentionCount" size="100"/>
            <s:checkbox label="%{getText('purgeConfig.deleteAll.label')}" name="deleteAll"/>
            <s:if test="purgeType == 'repository'">
              <s:checkbox label="%{getText('purgeConfig.deleteReleasedSnapshots.label')}" name="deleteReleasedSnapshots"/>
            </s:if>
            <s:if test="defaultPurgeConfiguration">
                <s:hidden name="defaultPurgeConfiguration"/>
                <s:label label="%{getText('purgeConfig.defaultPurge.label')}" value="true"/>
            </s:if>
            <s:else>
                <s:checkbox label="%{getText('purgeConfig.defaultPurge.label')}" name="defaultPurgeConfiguration" value="defaultPurgeConfiguration" fieldValue="true"/>
            </s:else>
            <s:select label="%{getText('purgeConfig.schedule.label')}" name="scheduleId" list="schedules"
                       headerKey="-1" headerValue=""/>
            <s:textfield label="%{getText('purgeConfig.description.label')}" name="description" size="100"/>
            <s:checkbox label="%{getText('purgeConfig.enabled.label')}" name="enabled"/>
          </table>
          <s:hidden name="purgeConfigId"/>
          <s:hidden name="purgeType"/>
          <div class="functnbar3">
            <s:submit value="%{getText('save')}" theme="simple"/>
            <input type="button" name="Cancel" value="<s:text name='cancel'/>" onclick="history.back();"/>
          </div>
        </s:if>
        <s:else>
          <div class="warningmessage" style="color: red"><s:text name="purgeConfig.no.repositories" /></div>
        </s:else>
      </s:form>
    </div>
  </div>
</div>

</body>
</s:i18n>
</html>