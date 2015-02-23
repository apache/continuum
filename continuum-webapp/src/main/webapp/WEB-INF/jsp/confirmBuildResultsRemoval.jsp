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
        <title><s:text name="buildResult.delete.confirmation.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><s:text name="buildResult.delete.confirmation.section.title"/></h3>
        <div class="axial">
        <s:if test="hasActionMessages()">
          <div class="warningmessage">
            <p>
              <s:actionmessage/>
            </p>        
          </div>
        </s:if>
        <!-- in this case we come from the build result edit -->
        <s:form action="%{buildId ? 'removeBuildResult' : 'removeBuildResults'}" theme="simple">
          <s:token/>
          <s:hidden name="projectGroupId"/>
          <s:hidden name="projectId"/>
          <s:if test="buildId">
            <s:hidden name="buildId"/>
          </s:if>
          <s:hidden name="confirmed" value="true"/>
          <s:if test="selectedBuildResults">
            <s:iterator value="selectedBuildResults" var="resultId">
              <s:hidden name="selectedBuildResults" value="%{resultId}" />
            </s:iterator>
          </s:if>
          <s:else>
            <s:hidden name="selectedBuildResults" value="buildId" />
          </s:else>
          
          <s:actionerror/>

          <div class="warningmessage">
            <p>
              <strong>
                <s:text name="buildResult.delete.confirmation.message">
                  <s:param><s:property value="%{selectedBuildResults.size}"/></s:param>
                </s:text>
              </strong>
            </p>
          </div>

          <div class="functnbar3">
            <s:if test="buildId > 0">
              <s:submit value="%{getText('delete')}" theme="simple"/>
            </s:if>
            <s:elseif test="selectedBuildResults.size > 0">
              <s:submit value="%{getText('delete')}" theme="simple"/>
            </s:elseif>
            <input type="button" name="Cancel" value="<s:text name='cancel'/>" onclick="history.back();"/>
          </div>
        </s:form>
        </div>
      </div>
    </body>
  </s:i18n>
</html>
