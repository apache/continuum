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
      <title><s:text name="projectEdit.page.title"/></title>
      <script type="text/javascript">
        function focus()
        {
          checkUseCache();
        }

        function checkUseCache()
        {
          var form = document.forms[ "editProject" ];

          if ( form.scmUrl.value.toLowerCase().indexOf( "scm:svn:" ) == 0 )
          {
            form.scmUseCache.disabled = false;
            form.scmUsername.disabled = form.scmUseCache.checked;
            form.scmPassword.disabled = form.scmUseCache.checked;
          }
          else
          {
            form.scmUseCache.disabled = true;
            form.scmUsername.disabled = false;
            form.scmPassword.disabled = false;
          }
        }
      </script>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><s:text name="projectEdit.section.title"/></h3>

        <div class="axial">
          <s:form name="editProject" action="projectSave" method="post" validate="true">
            <table>
              <tbody>
                <s:textfield label="%{getText('projectEdit.project.name.label')}" name="name" requiredLabel="true" size="100"/>
                <s:textfield label="%{getText('projectEdit.project.version.label')}" name="version" requiredLabel="true" size="100"/>
                <s:textfield label="%{getText('projectEdit.project.scmUrl.label')}" name="scmUrl" requiredLabel="true"
                             onchange="checkUseCache()" size="100"/>
                <s:checkbox label="%{getText('projectEdit.project.scmUseCache.label')}" name="scmUseCache"
                             onclick="checkUseCache()" size="100"/>
                <s:textfield label="%{getText('projectEdit.project.scmUsername.label')}" name="scmUsername" size="100"/>
                <s:password label="%{getText('projectEdit.project.scmPassword.label')}" name="scmPassword" size="100"/>
                <s:textfield label="%{getText('projectEdit.project.scmTag.label')}" name="scmTag" size="100"/>
              </tbody>
            </table>
            <div class="functnbar3">
              <s:submit value="%{getText('save')}" theme="simple"/>
              <input type="button" name="Cancel" value="<s:text name='cancel'/>" onclick="history.back();"/>
            </div>
            <s:hidden name="projectId"/>
          </s:form>
        </div>
      </div>
    </body>
  </s:i18n>
</html>
