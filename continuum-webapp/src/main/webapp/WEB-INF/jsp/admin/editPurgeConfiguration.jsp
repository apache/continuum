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

<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
  <head>
    <title><ww:text name="purgeConfig.page.title"/></title>
  </head>
  <body>
  <div class="app">
    <div id="axial" class="h3">
      <h3><ww:text name="purgeConfig.section.title"/></h3>

    <div class="axial">
      <ww:form action="savePurgeConfig" method="post" validate="true">
        <c:if test="${!empty actionErrors}">
          <div class="errormessage">
            <c:forEach items="${actionErrors}" var="actionError">
              <p><ww:text name="${actionError}"/></p>
            </c:forEach>
          </div>
        </c:if>
        <c:choose>
	    <c:when test="${(!empty repositories) || purgeType == 'directory' }">
          <table>
            <c:choose>
              <c:when test="${purgeType == 'repository'}">
                <ww:select label="%{getText('purgeConfig.repository.label')}" name="repositoryId" list="repositories" required="true"/>
              </c:when>
              <c:otherwise>
                <ww:select label="%{getText('purgeConfig.directoryType.label')}" name="directoryType" list="directoryTypes"/>
              </c:otherwise>
            </c:choose>
            <ww:textfield label="%{getText('purgeConfig.daysOlder.label')}" name="daysOlder"/>
            <ww:textfield label="%{getText('purgeConfig.retentionCount.label')}" name="retentionCount"/>
            <ww:checkbox label="%{getText('purgeConfig.deleteAll.label')}" name="deleteAll"/>
            <c:if test="${purgeType == 'repository'}">
              <ww:checkbox label="%{getText('purgeConfig.deleteReleasedSnapshots.label')}" name="deleteReleasedSnapshots"/>
            </c:if>
            <c:choose>
              <c:when test="${defaultPurgeConfiguration == true}">
                <ww:hidden name="defaultPurgeConfiguration"/>
                <ww:label label="%{getText('purgeConfig.defaultPurge.label')}" value="true"/>
              </c:when>
              <c:otherwise>
                <ww:checkbox label="%{getText('purgeConfig.defaultPurge.label')}" name="defaultPurgeConfiguration" value="defaultPurgeConfiguration" fieldValue="true"/>
              </c:otherwise>
            </c:choose>
            <ww:select label="%{getText('purgeConfig.schedule.label')}" name="scheduleId" list="schedules"
                       headerKey="-1" headerValue=""/>
            <ww:textfield label="%{getText('purgeConfig.description.label')}" name="description"/>
          </table>
          <ww:hidden name="purgeConfigId"/>
          <ww:hidden name="purgeType"/>
          <div class="functnbar3">
            <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
          </div>
        </c:when>
        <c:otherwise>
          <div class="warningmessage" style="color: red"><ww:text name="purgeConfig.no.repositories" /></div>
        </c:otherwise>
      </c:choose>
      </ww:form>
    </div>
  </div>
</div>

</body>
</ww:i18n>
</html>