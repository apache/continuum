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
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>
<html>
  <s:i18n name="localization.Continuum">
    <head>
        <title><s:text name="about.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><s:text name="about.section.title"/></h3>
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <tr class="b">
              <th><label class="label"><s:text name='about.version.label'/>:</label></th>
              <td><s:text name="about.version.number"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='about.buildnumber.label'/>:</label></th>
              <td><s:text name="about.buildnumber"/></td>
            </tr>
          </table>
          <redback:ifAuthorized permission="continuum-manage-configuration">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <h3><s:text name="about.platform.title"/></h3>
            <tr class="b">
              <th><label class="label"><s:text name='about.java.version'/>:</label></th>
              <td><s:property value="systemProperties['java.version']"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='about.java.vendor'/>:</label></th>
              <td><s:property value="systemProperties['java.vendor']"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='about.os.name'/>:</label></th>
              <td><s:property value="systemProperties['os.name']"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='about.os.arch'/>:</label></th>
              <td><s:property value="systemProperties['os.arch']"/></td>
            </tr>
          </table>
          </redback:ifAuthorized>
        </div>
      </div>
    </body>
  </s:i18n>
</html>
