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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

<html>
<head>
  <title>
    <decorator:title/>
  </title>
  <link rel="stylesheet" type="text/css" href="<s:url value="/css/tigris.css" includeParams="none"/>" media="screen"/>
  <link rel="stylesheet" type="text/css" href="<s:url value="/css/continuum.css" includeParams="none"/>" media="screen"/>
  <link rel="stylesheet" type="text/css" href="<s:url value="/css/print.css" includeParams="none"/>" media="print"/>
  <link rel="stylesheet" type="text/css" href="<s:url value="/css/extremecomponents.css" includeParams="none"/>" media="screen"/>
  <link rel="icon" href="<s:url value="/favicon.ico" includeParams="none"/>" sizes="16x16 32x32 48x48 64x64" type="image/vnd.microsoft.icon">
  <script type="text/javascript" src="<c:url value='/js/jquery-1.3.2.min.js'/>"></script>
  <script language="javascript" type="text/javascript">
    $.noConflict();
  </script>
  <script type="text/javascript" src="<s:url value="/js/prototype.js" includeParams="none"/>"></script>
  <s:head/>
  <decorator:head/>
</head>

<body onload="<decorator:getProperty property="body.onload" />" marginwidth="0" marginheight="0" class="composite">
<s:include value="/WEB-INF/jsp/navigations/DefaultTop.jsp"/>

<table id="main" border="0" cellpadding="4" cellspacing="0" width="100%">
  <tbody>
    <tr valign="top">
      <td id="leftcol" width="180">
        <br/> <br/>
        <s:include value="/WEB-INF/jsp/navigations/Menu.jsp"/>
      </td>
      <td width="86%">
        <br/>

        <div id="bodycol">
          <div class="app">
            <decorator:body/>
          </div>
        </div>
      </td>
    </tr>
  </tbody>
</table>

<s:action name="bottom" executeResult="true"/>

<script language="javascript">
    <!--
    if ( document.forms[0] )
    {
        if ( Form.findFirstElement( document.forms[0] ) )
        {
            Form.focusFirstElement( document.forms[0] ).activate;
        }
    }
    -->
</script>
</body>
</html>
