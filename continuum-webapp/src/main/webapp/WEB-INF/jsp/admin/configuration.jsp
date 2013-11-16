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
      <s:text name="configuration.page.title"/>
    </title>
  </head>
  <body>
  <div id="axial" class="h3">
    <h3>
      <s:text name="configuration.section.title"/>
    </h3>

    <div class="axial">
      <table border="1" cellspacing="2" cellpadding="3" width="100%">
        <tr class="b">
          <th><label class="label"><s:text name='configuration.workingDirectory.label'/>:</label></th>
          <td><s:property value="workingDirectory"/></td>
        </tr>
        <tr class="b">
          <th><label class="label"><s:text name='configuration.buildOutputDirectory.label'/>:</label></th>
          <td><s:property value="buildOutputDirectory"/></td>
        </tr>
        <tr class="b">
          <th><label class="label"><s:text name='configuration.releaseOutputDirectory.label'/>:</label></th>
          <td><s:property value="releaseOutputDirectory"/></td>
        </tr>
        <tr class="b">
          <th><label class="label"><s:text name='configuration.deploymentRepositoryDirectory.label'/>:</label></th>
          <td><s:property value="deploymentRepositoryDirectory"/></td>
        </tr>
        <tr class="b">
          <th><label class="label"><s:text name='configuration.baseUrl.label'/>:</label></th>
          <td><s:property value="baseUrl"/></td>
        </tr>
        <tr class="b">
          <th><label class="label"><s:text name='configuration.allowed.build.parallel'/>:</label></th>
          <td><s:property value="numberOfAllowedBuildsinParallel"/></td>
        </tr>
        <tr class="b">
          <th><label class="label"><s:text name='configuration.distributedBuildEnabled.label'/>:</label></th>
          <td><s:property value="distributedBuildEnabled"/></td>
        </tr>
      </table>
      <div class="functnbar3">
        <s:form action="configuration!input.action" method="post">
          <s:submit value="%{getText('edit')}"/>
        </s:form>
      </div>
    </div>
  </div>
  </body>
</s:i18n>
</html>
