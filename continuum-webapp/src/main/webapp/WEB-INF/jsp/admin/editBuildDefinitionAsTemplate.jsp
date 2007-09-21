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
          <ww:form action="saveBuildDefinitionAsTemplate" method="post" validate="true">
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
                    <ww:if test="buildDefinition.type == 'ant'">
                      <ww:textfield label="%{getText('buildDefinition.buildFile.ant.label')}" name="buildDefinition.buildFile"  required="true"/>
                    </ww:if>
                    <ww:elseif test="buildDefinition.type == 'shell'">
                      <ww:textfield label="%{getText('buildDefinition.buildFile.shell.label')}" name="buildDefinition.buildFile" required="true"/>
                    </ww:elseif>
                    <ww:else>
                      <ww:textfield label="%{getText('buildDefinition.buildFile.maven.label')}" name="buildDefinition.buildFile" required="true"/>
                    </ww:else>
    
                    <ww:if test="buildDefinition.type == 'ant'">
                      <ww:textfield label="%{getText('buildDefinition.goals.ant.label')}" name="buildDefinition.goals"/>
                    </ww:if>
                    <ww:elseif test="buildDefinition.type == 'shell'">
                    </ww:elseif>
                    <ww:else>
                      <ww:textfield label="%{getText('buildDefinition.goals.maven.label')}" name="buildDefinition.goals"/>
                    </ww:else>
    
                    <ww:textfield label="%{getText('buildDefinition.arguments.label')}" name="buildDefinition.arguments"/>
                    <ww:checkbox label="%{getText('buildDefinition.buildFresh.label')}" name="buildDefinition.buildFresh"/>
                    <ww:checkbox label="%{getText('buildDefinition.alwaysBuild.label')}" name="buildDefinition.alwaysBuild" />
                    <ww:select label="%{getText('buildDefinition.schedule.label')}" name="buildDefinition.schedule.id" list="schedules" listValue="name"
                               listKey="id"/>
                    <ww:if test="buildDefinition.profile == null">
                      <ww:select label="%{getText('buildDefinition.profile.label')}" name="buildDefinition.profile.id" list="profiles" listValue="name" 
                                 value="-1" listKey="id" headerKey="-1" headerValue=""/>
                    </ww:if>
                    <ww:else>
                      <ww:select label="%{getText('buildDefinition.profile.label')}" name="buildDefinition.profile.id" list="profiles" listValue="name" 
                                 listKey="id" headerKey="-1" headerValue=""/>
                    </ww:else>
                    <ww:select label="%{getText('buildDefinition.type.label')}" name="buildDefinition.type" list="buildDefinitionTypes"/>
                    <ww:textfield label="%{getText('buildDefinition.description.label')}" name="buildDefinition.description" required="true"/>
                  </tbody>
                </table>
                <div class="functnbar3">
                  <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
                </div>

                <ww:hidden name="buildDefinition.id"/>
                <ww:hidden name="buildDefinition.template" value="true"/>
              </c:when>
            
            </c:choose>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
