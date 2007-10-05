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

<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="buildResult.delete.confirmation.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="buildResult.delete.confirmation.section.title"/></h3>
        <div class="axial">
        <ww:if test="hasActionMessages()">
          <div class="warningmessage">
            <p>
              <ww:actionmessage/>
            </p>        
          </div>
        </ww:if>
        <!-- in this case we come from the build result edit -->
        <ww:if test="buildId">
          <form action="removeBuildResult.action" method="post">
        </ww:if>
        <ww:else>
          <form action="removeBuildResults.action" method="post">
        </ww:else>
          <ww:hidden name="projectGroupId"/>
          <ww:hidden name="projectId"/>
          <ww:hidden name="buildId"/>
          <ww:hidden name="confirmed" value="true"/>
          <ww:if test="selectedBuildResults">
          <ww:iterator value="selectedBuildResults">
            <input type="hidden" value="<ww:property/>" name="selectedBuildResults" />
          </ww:iterator>
          </ww:if>
          <ww:else>
            <input type="hidden" value="<ww:property value="buildId"/>" name="selectedBuildResults" />
          </ww:else>
          
          <ww:actionerror/>

          <div class="warningmessage">
            <p>
              <strong>
                <ww:text name="buildResult.delete.confirmation.message">
                  <ww:param><ww:property value="%{selectedBuildResults.size}"/></ww:param>
                </ww:text>
              </strong>
            </p>
          </div>

          <div class="functnbar3">
            <ww:if test="buildId > 0">
              <c1:submitcancel value="%{getText('delete')}" cancel="%{getText('cancel')}"/>
            </ww:if>
            <ww:elseif test="selectedBuildResults.size > 0">
              <c1:submitcancel value="%{getText('delete')}" cancel="%{getText('cancel')}"/>
            </ww:elseif>
            <ww:else>
              <input type="submit" value="<ww:text name="cancel"/>" onClick="history.back()"/> 
            </ww:else>
          </div>
        </form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
