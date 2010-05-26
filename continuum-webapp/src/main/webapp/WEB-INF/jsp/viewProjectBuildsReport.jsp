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
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri="continuum" prefix="c1" %>

<html>
  <s:i18n name="localization.Continuum">
  <head>
    <title><s:text name="projectBuilds.report.title"/></title>
    <s:head/>
    <link rel="stylesheet" href="<c:url value='/css/no-theme/jquery-ui-1.7.2.custom.css'/>" type="text/css" />
    <script type="text/javascript" src="<c:url value='/js/jquery-1.3.2.min.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/js/jquery-ui-1.7.2.custom.min.js'/>"></script>
    <script type="text/javascript">
      $(document).ready(function()
      {
		  $('#startDate').datepicker()
		  $('#endDate').datepicker()
      });
    </script>
  </head>
  
  <body>
    <h3><s:text name="projectBuilds.report.section.title"/></h3>
 
    <s:form name="generateReportForm" action="generateProjectBuildsReport.action">
      <c:if test="${!empty actionErrors || !empty errorMessages}">
        <div class="errormessage">
          <s:iterator value="actionErrors">
            <p><s:property/></p>
          </s:iterator>
          <c:forEach items="${errorMessages}" var="errorMessage">
            <p>${errorMessage}</p>
          </c:forEach>
        </div>
      </c:if>
      
      <div class="axial">
        <table>
            <s:textfield label="%{getText('projectBuilds.report.startDate')}" name="startDate" id="startDate" size="20"/>
            <s:textfield label="%{getText('projectBuilds.report.endDate')}" name="endDate" id="endDate" size="20"/>
            <s:select label="%{getText('projectBuilds.report.buildStatus')}" name="buildStatus" list="buildStatuses"/>
            <s:textfield label="%{getText('projectBuilds.report.triggeredBy')}" name="triggeredBy" size="40"/>
            <s:textfield label="%{getText('projectBuilds.report.rowCount')}" name="rowCount" size="10"/>
        </table>
        <div class="functnbar3">
          <c1:submitcancel value="%{getText('projectBuilds.report.view')}" cancel="%{getText('cancel')}"/>
        </div>
      </div>
    </s:form>
    
    </p>

   	<div id="h3">
   	  <h3>Results</h3>
      <c:if test="${not empty projectBuilds}">
        <c:set var="prevPageUrl">
          <s:url action="generateProjectBuildsReport">    
            <s:param name="triggeredBy" value="%{#attr.triggeredBy}"/>
            <s:param name="buildStatus" value="%{#attr.buildStatus}"/>
            <s:param name="rowCount" value="%{#attr.rowCount}"/>
            <s:param name="startDate" value="%{#attr.startDate}"/>                      
            <s:param name="endDate" value="%{#attr.endDate}"/>
            <s:param name="page" value="%{#attr.page - 1}"/>
          </s:url>
        </c:set>
        <c:set var="nextPageUrl">
          <s:url action="generateProjectBuildsReport">    
            <s:param name="triggeredBy" value="%{#attr.triggeredBy}"/>
            <s:param name="buildStatus" value="%{#attr.buildStatus}"/>
            <s:param name="rowCount" value="%{#attr.rowCount}"/>
            <s:param name="startDate" value="%{#attr.startDate}"/>                      
            <s:param name="endDate" value="%{#attr.endDate}"/>          
            <s:param name="page" value="%{#attr.page + 1}"/>
          </s:url>
        </c:set>
        <c:choose>
          <c:when test="${page == 1}">                               
            <s:text name="projectBuilds.report.prev"/>
          </c:when>
          <c:otherwise>
            <a href="${prevPageUrl}">
              <s:text name="projectBuilds.report.prev"/>
            </a>
          </c:otherwise>
        </c:choose>

        <c:choose>
          <c:when test="${numPages > 11}">
            <c:choose>
              <c:when test="${(page - 5) < 0}">
                <c:set var="beginVal">0</c:set>
                <c:set var="endVal">10</c:set> 
              </c:when>			        
              <c:when test="${(page + 5) > (numPages - 1)}">
                <c:set var="beginVal">${(numPages - 1) - 10}</c:set>
                <c:set var="endVal">${numPages - 1}</c:set>
              </c:when>
              <c:otherwise>
                <c:set var="beginVal">${page - 5}</c:set>
                <c:set var="endVal">${page + 5}</c:set>
              </c:otherwise>
            </c:choose>  
          </c:when>
          <c:otherwise>
            <c:set var="beginVal">0</c:set>
            <c:set var="endVal">${numPages - 1}</c:set>
          </c:otherwise>
        </c:choose>

        <c:forEach var="i" begin="${beginVal}" end="${endVal}">      
          <c:choose>                   			    
            <c:when test="${i != (page - 1)}">
              <c:set var="specificPageUrl">
                <s:url action="generateProjectBuildsReport">    
                  <s:param name="triggeredBy" value="%{#attr.triggeredBy}"/>
            	  <s:param name="buildStatus" value="%{#attr.buildStatus}"/>
                  <s:param name="rowCount" value="%{#attr.rowCount}"/>
                  <s:param name="startDate" value="%{#attr.startDate}"/>                      
                  <s:param name="endDate" value="%{#attr.endDate}"/>          
                  <s:param name="page" value="%{#attr.i + 1}"/>
                </s:url>
              </c:set>
              <a href="${specificPageUrl}">${i + 1}</a>
            </c:when>
            <c:otherwise>		
              <b>${i + 1}</b>   
            </c:otherwise>				  			    
          </c:choose>      
        </c:forEach>

        <c:choose>
          <c:when test="${page == numPages}">
            <s:text name="projectBuilds.report.next"/>
          </c:when>
  	      <c:otherwise>
            <a href="${nextPageUrl}">
              <s:text name="projectBuilds.report.next"/>
            </a>
          </c:otherwise>   
        </c:choose>

        <s:set name="projectBuilds" value="projectBuilds" scope="request"/>
        <ec:table items="projectBuilds"
                    var="projectBuild"
                    showExports="false"
                    showPagination="false"
                    showStatusBar="false"
                    sortable="false"
                    filterable="false">
          <ec:row highlightRow="true">
            <ec:column property="projectGroupName" title="projectBuilds.report.projectGroup"/>
            <ec:column property="projectName" title="projectBuilds.report.project"/>
            <ec:column property="buildDate" title="projectBuilds.report.buildDate" cell="date"/>
            <ec:column property="buildTriggeredBy" title="projectBuilds.report.triggeredBy"/>
            <ec:column property="buildState" title="projectBuilds.report.buildStatus" cell="org.apache.maven.continuum.web.view.buildresults.StateCell"/>
          </ec:row>
        </ec:table>
      </c:if>
      <c:if test="${empty projectBuilds}">
        <s:text name="projectBuilds.report.noResult"/>
      </c:if>
    </div>
  </body>
  </s:i18n>
</html>