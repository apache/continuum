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
    <title><ww:text name="installationsList.page.title"/></title>
  </head>
  
  <div id="h3">
    <h3>
      <ww:text name="installationsList.section.title"/>
    </h3>  

    <ww:if test="${not empty installations}">
    <ec:table items="installations"
              var="installation"
              showExports="false"
              showPagination="false"
              showStatusBar="false"
              sortable="false"
              filterable="false">
      <ec:row highlightRow="true">
        <ec:column property="name" title="Name" style="white-space: nowrap" />
        <ec:column property="type" title="Type" style="white-space: nowrap" />
        <ec:column property="varName" title="Env Var Name" style="white-space: nowrap" />
        <ec:column property="varValue" title="Value/Path" style="white-space: nowrap" />
        
        <ec:column property="id" title="&nbsp;" width="1%">
          <a href="editInstallation!edit.action?installation.installationId=<c:out value="${installation.installationId}"/>">
            <img src="<ww:url value='/images/edit.gif'/>" alt="<ww:text name='edit'/>" title="<ww:text name='edit'/>" border="0" />
          </a>  
        </ec:column>   
        <ec:column property="id" title="&nbsp;" width="1%">
          <a href="deleteInstallation!delete.action?installation.installationId=<c:out value="${installation.installationId}"/>">
            <img src="<ww:url value='/images/delete.gif'/>" alt="<ww:text name='delete'/>" title="<ww:text name='delete'/>" border="0" />
          </a>  
        </ec:column>             
      </ec:row>
    </ec:table>
    </ww:if>
    <div class="functnbar3">
      <ww:form action="addInstallation!input.action" method="post">
        <ww:submit value="%{getText('add')}"/>
      </ww:form>
    </div>  
  
  </div>
  
</ww:i18n>
