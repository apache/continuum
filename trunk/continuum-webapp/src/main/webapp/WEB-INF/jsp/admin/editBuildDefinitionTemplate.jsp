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
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="buildDefinition.template.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="buildDefinition.template.section.title"/></h3>

        <div class="axial">
          <ww:form action="saveBuildDefinitionTemplate" method="post" validate="true" name="buildDefinitionTemplate"
                   onsubmit="customOnsubmit_saveBuildDefinitionTemplate();">
            <c:choose>
            
              <c:when test="${!empty actionErrors}">
                <div class="errormessage">
                  <c:forEach items="${actionErrors}" var="actionError">
                    <p><ww:text name="${actionError}"/></p>
                  </c:forEach>
                </div>
                <input type="button" value="Back" onClick="history.go(-1)">
              </c:when>
  
              <c:when test="${empty actionErrors}">
                <table>
                  <tbody>
                    <ww:textfield label="%{getText('buildDefinitionTemplate.name')}" name="buildDefinitionTemplate.name" required="true"/>
                    <ww:optiontransferselect 
                        label="%{getText('buildDefinitionTemplate.builddefinitions.define')}"    
                        name="buildDefinitionIds"
                        list="buildDefinitions" 
                        listKey="id"
                        listValue="description"
                        headerKey="-1"
                        headerValue="%{getText('buildDefinitionTemplate.available.builddefinitions')}"
                        multiple="true"
                        emptyOption="false"
                        doubleName="selectedBuildDefinitionIds"
                        doubleList="buildDefinitionTemplate.buildDefinitions" 
                        doubleListKey="id"
                        doubleListValue="description"
                        doubleHeaderKey="-1"
                        doubleHeaderValue="%{getText('buildDefinitionTemplate.available.builddefinitions.used')}" 
                        doubleMultiple="true" 
                        doubleEmptyOption="false"
                        formName="buildDefinitionTemplate" />                    
                  </tbody>
                </table>
                <div class="functnbar3">
                  <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
                </div>
                <ww:hidden name="buildDefinitionTemplate.id"/>
                <ww:hidden name="buildDefinitionTemplate.continuumDefault"/>
              </c:when>
            
            </c:choose>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
