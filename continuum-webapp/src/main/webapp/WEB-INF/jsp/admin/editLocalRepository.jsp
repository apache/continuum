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
<title><ww:text name="repository.page.title"/></title>
</head>
<body>
<div class="app">
  <div id="axial" class="h3">
    <h3><ww:text name="repository.page.title"/></h3>

    <div class="axial">
      <ww:form action="saveRepository" method="post" validate="true">
        <c:if test="${!empty actionErrors}">
          <div class="errormessage">
            <c:forEach items="${actionErrors}" var="actionError">
              <p><ww:text name="${actionError}"/></p>
            </c:forEach>
          </div>
        </c:if>

          <table>
            <ww:textfield label="%{getText('repository.name.label')}" name="repository.name" required="true" disabled="%{defaultRepo}"/>
            <ww:textfield label="%{getText('repository.location.label')}" name="repository.location" required="true" disabled="%{defaultRepo}"/>
            <ww:select label="%{getText('repository.layout.label')}" name="repository.layout" list="layouts" disabled="%{defaultRepo}"/>
          </table>
          <ww:hidden name="repository.id"/>
          <c:if test="${defaultRepo}">
            <ww:hidden name="repository.name"/>
            <ww:hidden name="repository.location"/>
            <ww:hidden name="repository.layout"/>
          </c:if>
          <div class="functnbar3">
            <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
          </div>
        
      </ww:form>
    </div>
  </div>
</div>

</body>
</ww:i18n>
</html>
