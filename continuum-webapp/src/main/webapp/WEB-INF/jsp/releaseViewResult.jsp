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
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <s:i18n name="localization.Continuum">
    <head>
        <title><s:text name="releaseProject.page.title"/></title>
    </head>
    <body>
      <h2><s:text name="releaseViewResult.section.title"/></h2>

      <h4><s:text name="releaseViewResult.summary"/></h4>
      <div class="axial">
        <table border="1" cellspacing="2" cellpadding="3" width="100%">
          <tr class="b">
            <th><label class="label"><s:text name='releaseViewResult.projectName'/>:</label></th>
            <td><s:property value="projectName"/></td>
          </tr>
          <tr class="b">
            <th><label class="label"><s:text name='releaseViewResult.releaseGoal'/>:</label></th>
            <td><s:property value="releaseGoal"/></td>
          </tr>
          <tr class="b">
            <th><label class="label"><s:text name='releaseViewResult.startTime'/>:</label></th>
            <td><c1:date name="result.startTime"/></td>
          </tr>
          <tr class="b">
            <th><label class="label"><s:text name='releaseViewResult.endTime'/>:</label></th>
            <td><c1:date name="result.endTime"/></td>
          </tr>
          <tr class="b">
            <th><label class="label"><s:text name='releaseViewResult.state'/>:</label></th>
            <td>
              <s:if test="result.resultCode == 0">
                <s:text name="releaseViewResult.success"/>
              </s:if>
              <s:else>
                <s:text name="releaseViewResult.error"/>
              </s:else>
            </td>
          </tr>
          <tr class="b">
            <th><label class="label"><s:text name='releaseViewResult.username'/>:</label></th>
            <td><s:property value="username"/></td>
          </tr>
        </table>
      </div>

      <h4><s:text name="releaseViewResult.output"/></h4>
      <p>
        <s:if test="result.output == ''">
            <s:text name="releaseViewResult.noOutput"/>
        </s:if>
        <s:else>
          <div style="width:100%; height:500px; overflow:auto; border-style: solid; border-width: 1px">
            <pre><s:property value="result.output"/></pre>
          </div>
        </s:else>
      </p>
      <input type="button" value="<s:text name="back"/>" onClick="history.go(-1)">

    </body>
  </s:i18n>
</html>
