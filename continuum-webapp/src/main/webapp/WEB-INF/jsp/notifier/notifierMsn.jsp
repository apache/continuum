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
        <title>
            <s:text name="notifier.page.title">
                <s:param>MSN</s:param>
            </s:text>
        </title>
    </head>
    <body>
      <div id="axial" class="h3">

        <h3>
            <s:text name="notifier.section.title">
                <s:param>MSN</s:param>
            </s:text>
        </h3>

        <div class="axial">
          <s:form action="%{projectId > 0? 'msnProjectNotifierSave' : 'msnProjectGroupNotifierSave'}" method="post" validate="true">
            <s:hidden name="notifierId"/>
            <s:hidden name="projectId"/>
            <s:hidden name="projectGroupId"/>
            <s:hidden name="notifierType"/>
            <s:hidden name="fromGroupPage"/>
            <table>
              <tbody>
                <s:textfield label="%{getText('notifier.msn.login.label')}" name="login" requiredLabel="true" size="100"/>
                <s:password label="%{getText('notifier.msn.password.label')}" name="password" requiredLabel="true" size="100"/>
                <s:textfield label="%{getText('notifier.msn.address.label')}" name="address" requiredLabel="true" size="100"/>
                <s:checkbox label="%{getText('notifier.event.sendOnSuccess')}" name="sendOnSuccess" value="sendOnSuccess" fieldValue="true"/>
                <s:checkbox label="%{getText('notifier.event.sendOnFailure')}" name="sendOnFailure" value="sendOnFailure" fieldValue="true"/>
                <s:checkbox label="%{getText('notifier.event.sendOnError')}" name="sendOnError" value="sendOnError" fieldValue="true"/>
                <s:checkbox label="%{getText('notifier.event.sendOnWarning')}" name="sendOnWarning" value="sendOnWarning" fieldValue="true"/>
                <s:checkbox label="%{getText('notifier.event.sendOnScmFailure')}" name="sendOnScmFailure" value="sendOnScmFailure" fieldValue="true"/>
              </tbody>
            </table>
            <div class="functnbar3">
              <s:submit value="%{getText('save')}" theme="simple"/>
              <input type="button" name="Cancel" value="<s:text name='cancel'/>" onclick="history.back();"/>
            </div>
          </s:form>
        </div>
      </div>
    </body>
  </s:i18n>
</html>
