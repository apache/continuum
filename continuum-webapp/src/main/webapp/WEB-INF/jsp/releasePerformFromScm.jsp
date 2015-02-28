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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
  <s:i18n name="localization.Continuum">
    <head>
        <title><s:text name="releasePerformFromScm.page.title"/></title>
    </head>
    <body>
      <h3><s:text name="releasePerformFromScm.section.title"/></h3>
      <s:form action="releasePerformFromScm" validate="true">
        <tr><td>
        <s:hidden name="projectId"/>
        </td></tr>
        <tr><td>
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <s:textfield label="%{getText('releasePerformFromScm.scmUrl.label')}" name="scmUrl" size="100"/>
            <s:textfield label="%{getText('releasePerformFromScm.scmUsername.label')}" name="scmUsername" size="100"/>
            <s:password label="%{getText('releasePerformFromScm.scmPassword.label')}" name="scmPassword" size="100"/>
            <s:textfield label="%{getText('releasePerformFromScm.scmTag.label')}" name="scmTag" size="100"/>
            <c:if test="${!empty (scmTagBase)}">
              <s:textfield label="%{getText('releasePerformFromScm.scmTagBase.label')}" name="scmTagBase" size="100"/>
            </c:if>
            <s:textfield label="%{getText('releasePerformFromScm.goals.label')}" name="goals" size="100"/>
            <s:textfield label="%{getText('releasePrepare.arguments.label')}" name="arguments" size="100"/>
            <s:checkbox label="%{getText('releasePerformFromScm.useReleaseProfile.label')}" name="useReleaseProfile"/>
            <s:select label="%{getText('releasePerformFromScm.buildEnvironment.label')}" name="profileId" list="profiles" listValue="name"
                       listKey="id" headerKey="-1" headerValue=""/>
          </table>
        </div>
        <s:submit value="%{getText('submit')}"/>
        </tr></td>
      </s:form>
    </body>
  </s:i18n>
</html>
