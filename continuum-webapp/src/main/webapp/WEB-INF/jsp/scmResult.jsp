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
      <title><s:text name="scmResult.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <s:action name="projectGroupTab" executeResult="true"/>
        <h3>
          <s:text name="scmResult.section.title"/>
        </h3>
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <tr class="b">
              <th><label class="label"><s:text name='scmResult.projectGroupName'/>:</label></th>
              <td><s:property value="projectGroupName"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='scmResult.scmRootAddress'/>:</label></th>
              <td><s:property value="projectScmRoot.scmRootAddress"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='scmResult.state'/>:</label></th>
              <td>${state}</td>
            </tr>
          </table>
        </div>
        
        <h4><s:text name="scmResult.scmError"/></h4>
        <div class="cmd-output pre-wrap"><s:property value="projectScmRoot.error"/></div>
      </div>
    </body>
  </s:i18n>
</html>
