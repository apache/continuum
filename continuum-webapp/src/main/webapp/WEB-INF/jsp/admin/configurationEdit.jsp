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
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<html>
<s:i18n name="localization.Continuum">
  <head>
    <title>
      <s:text name="configuration.page.title"/>
    </title>
    <script language="javascript">
      function setSecretPassword()
      {
        var form = document.forms[ "editConfiguration" ];

        if ( form.distributedBuildEnabled.checked == true )
        {
          form.sharedSecretPassword.disabled = false;
        }
        else
        {
          form.sharedSecretPassword.disabled = true;
        }
      }
    </script>
  </head>

  <body>
  <div id="axial" class="h3">
    <h3>
      <s:text name="configuration.section.title"/>
    </h3>

    <s:form name="editConfiguration" action="configuration_save" method="post">

      <c:if test="${!empty actionErrors}">
        <div class="errormessage">
          <s:iterator value="actionErrors">
            <p><s:property/></p>
          </s:iterator>
        </div>
      </c:if>

      <div class="axial">

        <table>
          <tbody>

            <s:textfield label="%{getText('configuration.workingDirectory.label')}" name="workingDirectory"
                          requiredLabel="true" size="100">
              <s:param name="after"><p>
                <s:text name="configuration.workingDirectory.message"/>
              </p></s:param>
            </s:textfield>

            <s:textfield label="%{getText('configuration.buildOutputDirectory.label')}" name="buildOutputDirectory"
                          requiredLabel="true" size="100">
              <s:param name="after"><p>
                <s:text name="configuration.buildOutputDirectory.message"/>
              </p></s:param>
            </s:textfield>

            <s:textfield label="%{getText('configuration.releaseOutputDirectory.label')}" name="releaseOutputDirectory"
            			  requiredLabel="%{requireReleaseOutput}" size="100">
              <s:param name="after"><p>
                <s:text name="configuration.releaseOutputDirectory.message"/>
              </s:param>
            </s:textfield>

            <s:textfield label="%{getText('configuration.deploymentRepositoryDirectory.label')}"
                          name="deploymentRepositoryDirectory" size="100">
              <s:param name="after"><p>
                <s:text name="configuration.deploymentRepositoryDirectory.message"/>
              </p></s:param>
            </s:textfield>

            <s:textfield label="%{getText('configuration.baseUrl.label')}" name="baseUrl" requiredLabel="true" size="100">
              <s:param name="after"><p>
                <s:text name="configuration.baseUrl.message"/>
              </p></s:param>
            </s:textfield>

            <s:textfield label="%{getText('configuration.allowed.build.parallel')}" name="numberOfAllowedBuildsinParallel" size="10">
              <s:param name="after"><p>
                <s:text name="configuration.allowed.build.paralle.message"/>
              </p></s:param>
            </s:textfield>

            <%--
            <s:checkbox label="%{getText('configuration.disable.parallel.builds')}" name="requireParallelBuilds" requiredLabel="true"/>
            --%>

            <s:checkbox label="%{getText('configuration.distributedBuildEnabled.label')}" name="distributedBuildEnabled" onclick="setSecretPassword();"/>

            <s:password label="%{getText('configuration.sharedSecretPassword.label')}" name="sharedSecretPassword" disabled="%{!distributedBuildEnabled}" showPassword="true" size="100">
              <s:param name="after">
                <p>
                  <s:text name="configuration.sharedSecretPassword.message"/>
                </p>
              </s:param>
            </s:password>

            <s:hidden name="requireReleaseOutput"/>
          </tbody>
        </table>
        <div class="functnbar3">
          <s:submit value="%{getText('save')}" theme="simple"/>
          <input type="button" name="Cancel" value="<s:text name='cancel'/>" onclick="history.back();"/>
        </div>

      </div>
    </s:form>
  </div>
  </body>
</s:i18n>
</html>
