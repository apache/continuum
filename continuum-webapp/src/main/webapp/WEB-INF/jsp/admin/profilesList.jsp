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
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>

<html>
<ww:i18n name="localization.Continuum">
  <head>
    <title><ww:text name="profilesList.page.title"/></title>
  </head>
  
  <div id="h3">
    <h3>
      <ww:text name="profilesList.section.title"/>
    </h3>

    <c:if test="${!empty actionErrors}">
      <div class="errormessage">
        <c:forEach items="${actionErrors}" var="actionError">
          <p><ww:text name="${actionError}"/></p>
        </c:forEach>
      </div>
    </c:if>
        
    <ww:if test="${not empty profiles}">
    <ec:table items="profiles"
              var="profile"
              showExports="false"
              showPagination="false"
              showStatusBar="false"
              sortable="false"
              filterable="false">
      <ec:row highlightRow="true">
        <ec:column property="name" title="Name" style="white-space: nowrap" />
        <ec:column property="Installations" title="Installations" style="white-space: nowrap">
          <ul>
            <c:if test='${profile.jdk != null}'>
              <li><c:out value="${profile.jdk.name}"/> (<c:out value="${profile.jdk.type}"/>)</li>
            </c:if>
            <c:if test='${profile.builder != null}'>
              <li><c:out value="${profile.builder.name}"/> (<c:out value="${profile.builder.type}"/>)</li>
            </c:if>
            <c:if test='${profile.environmentVariables != null}'>
              <c:forEach var="envVar" items="${profile.environmentVariables}"> 
                <li><c:out value="${envVar.name}" /></li>
              </c:forEach>
            </c:if>
          <ul>
        </ec:column>
        <ec:column property="id" title="&nbsp;" width="1%">
          <a href="editBuildEnv!edit.action?profile.id=<c:out value="${pageScope.profile.id}"/>">
            <img src="<ww:url value='/images/edit.gif' includeParams="none"/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0" />
          </a>                    
        </ec:column>
        <ec:column property="id" title="&nbsp;" width="1%">
          <a href="confirmDeleteBuildEnv!confirmDelete.action?profile.id=<c:out value="${pageScope.profile.id}"/>">
            <img src="<ww:url value='/images/delete.gif' includeParams="none"/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0" />
          </a>                    
        </ec:column>        
      </ec:row>
    </ec:table>
    </ww:if>
    <div class="functnbar3">
      <ww:form action="addBuildEnv!input.action" method="post">
        <ww:submit value="%{getText('add')}"/>
      </ww:form>
    </div>    
  
  </div>
  
</ww:i18n>
