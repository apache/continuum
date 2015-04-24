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
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<html>

<s:i18n name="localization.Continuum">
  <head>
    <title>
      <s:text name="projectGroup.page.title"/>
    </title>
    <meta http-equiv="refresh" content="30"/>
    <script type="text/javascript">

      <s:url var="addM2ProjectUrl" action="addMavenTwoProjectInput" />
      <s:url var="addM1ProjectUrl" action="addMavenOneProjectInput" />
      <s:url var="addProjectUrl" action="addProjectInput" />

      function goToAddProject()
      {
        var projectTypes = document.getElementById( "preferredExecutor" );
        var type = projectTypes.value;

        if ( type != '' )
        {
          var form = document.forms[ "addNewProject" ];

          if ( type == 'maven2' )
          {
            form.action = '${addM2ProjectUrl}';
            form.projectType.value = "";
          }
          else if ( type == 'maven-1' )
          {
            form.action = '${addM1ProjectUrl}';
            form.projectType.value = "";
          }
          else if ( type == 'ant' )
          {
            form.action = '${addProjectUrl}';
            form.projectType.value = "ant";
          }
          else if ( type == 'shell' )
          {
            form.action = '${addProjectUrl}';
            form.projectType.value = "shell";
          }

          form.submit();
        }
        else
        {
          alert( "Please choose a project type to add from the dropdown list." );
        }
      }
    </script>
  </head>
  <body>
  <div id="h3">
    <s:action name="projectGroupTab" executeResult="true">
      <s:param name="tabName" value="'Summary'"/>
    </s:action>

    <s:if test="hasActionErrors()">
      <div class="errormessage">
        <s:actionerror/>
      </div>
    </s:if>
    <s:if test="hasActionMessages()">
      <div class="warningmessage">
        <s:actionmessage/>
      </div>
    </s:if>

    <h3><s:text name="projectGroup.information.title"><s:param value="projectGroup.name"/></s:text></h3>

    <div class="axial">
      <table border="1" cellspacing="2" cellpadding="3" width="100%">
        <tr class="b">
          <th><label class="label"><s:text name='projectGroup.name.label'/>:</label></th>
          <td><s:property value="projectGroup.name"/></td>
        </tr>
        <tr class="b">
          <th><label class="label"><s:text name='projectGroup.groupId.label'/>:</label></th>
          <td><s:property value="projectGroup.groupId"/></td>
        </tr>
        <tr class="b">
          <th><label class="label"><s:text name='projectGroup.description.label'/>:</label></th>
          <td><s:property value="projectGroup.description"/></td>
        </tr>
        <tr class="b">
          <th><label class="label"><s:text name='projectGroup.repository.label'/>:</label></th>
          <td><s:property value="projectGroup.localRepository.name"/></td>
        </tr>
        <s:if test="url != null">
            <s:url id="projectHomepageUrl" value="%{url}" includeContext="false" includeParams="none"/>
            <tr class="b">
              <th><label class="label"><s:text name='projectGroup.url.label'/>:</label></th>
              <td><a href="${projectHomepageUrl}"><s:property value="url"/></a></td>
            </tr>
        </s:if>
      </table>
    </div>

    <h3><s:text name="projectGroup.scmRoot.title"/></h3>
    <ec:table items="projectScmRoots"
              var="projectScmRoot"
              autoIncludeParameters="false"
              showExports="false"
              showPagination="false"
              showStatusBar="false"
              sortable="false"
              filterable="false">
      <ec:row highlightRow="true">
        <ec:column property="state" title="&nbsp;" width="1%" cell="org.apache.maven.continuum.web.view.StateCell"/>
        <ec:column property="scmRootAddress" title="projectGroup.scmRoot.label"/>  
      </ec:row>
    </ec:table>

    <redback:ifAnyAuthorized permissions="continuum-build-group,continuum-remove-group" resource="${projectGroup.name}">
      <h3><s:text name="projectGroup.actions.title"/></h3>

      <div class="functnbar3">
        <table>
          <tr>
            <td>
              <table>
                <redback:ifAuthorized permission="continuum-build-group" resource="${projectGroup.name}">
                  <s:form action="buildProjectGroup" theme="simple">
                    <s:hidden name="projectGroupId" />
                    <s:select theme="simple" name="buildDefinitionId" list="buildDefinitions"
                               listKey="value" listValue="key" headerKey="-1" headerValue="%{getText('projectGroup.buildDefinition.label')}" />                    
                    <input type="submit" name="build" value="<s:text name="projectGroup.buildGroup"/>"/>
                  </s:form>
                </redback:ifAuthorized>
              </table>
            </td>
            <td>
              <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroup.name}">
                <s:form action="editProjectGroup" theme="simple">
                  <s:hidden name="projectGroupId" />
                  <s:submit name="edit" value="%{getText('edit')}"/>
                </s:form>
              </redback:ifAuthorized>
            </td>
            <td>
              <redback:ifAuthorized permission="continuum-build-group" resource="${projectGroup.name}">
                <s:form action="releaseProjectGroup" theme="simple">
                  <s:hidden name="projectGroupId" />
                  <s:submit name="release" value="%{getText('release')}" />
                </s:form>
              </redback:ifAuthorized>
            </td>
            <td>
              <redback:ifAnyAuthorized permissions="continuum-add-project-to-group" resource="${projectGroup.name}">
                <s:form name="addNewProject">
                  <s:hidden name="disableGroupSelection" value="true"/>
                  <s:hidden name="selectedProjectGroup" value="%{projectGroup.id}"/>
                  <s:hidden name="projectGroupName" value="%{projectGroup.name}"/>
                  <s:hidden name="projectType" value=""/>
                </s:form>

                <s:select theme="simple" name="preferredExecutor" list="#@java.util.HashMap@{'maven2' : 'Add M2 Project', 'maven-1' : 'Add M1 Project', 'ant' : 'Add Ant Project', 'shell' : 'Add Shell Project'}"
                    headerValue="Choose the project type" headerKey="" emptyOption="true" />

                <input type="button" value="<s:text name="add"/>" onclick="goToAddProject()"/>
              </redback:ifAnyAuthorized>
            </td>
            <td>
              <redback:ifAuthorized permission="continuum-remove-group" resource="${projectGroup.name}">
                <s:form action="confirmRemoveProjectGroup" theme="simple">
                  <s:hidden name="projectGroupId" />
                  <s:submit name="remove" value="%{getText('projectGroup.deleteGroup')}" />
                </s:form>
              </redback:ifAuthorized>
            </td>
            <td>
              <redback:ifAuthorized permission="continuum-build-project-in-group" resource="${projectGroup.name}">
                <s:form action="cancelGroupBuild" theme="simple">
                  <s:hidden name="projectGroupId" />
                  <s:submit name="cancel" value="%{getText('projectGroup.cancelGroupBuild')}" />
                </s:form>
              </redback:ifAuthorized>
            </td>
          </tr>
        </table>
      </div>
    </redback:ifAnyAuthorized>

    <s:action name="projectSummary" executeResult="true" namespace="component">
      <s:param name="projectGroupId" value="projectGroupId" />
      <s:param name="projectGroupName" value="projectGroup.name" />
    </s:action>

  </div>
  </body>
</s:i18n>
</html>
