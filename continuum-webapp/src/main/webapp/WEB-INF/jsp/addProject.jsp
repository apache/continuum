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
<s:i18n name="localization.Continuum">
<html>
    <head>
        <title>
        <s:if test="projectType == \"shell\"">
            <s:text name="add.shell.project.page.title"/>
        </s:if>
        <s:else>
            <s:text name="add.ant.project.page.title"/>
        </s:else>
        </title>
    </head>
    <body>
        <div class="app">
            <div id="axial" class="h3">
                <h3>
                    <s:if test="projectType == \"shell\"">
                        <s:text name="add.shell.project.section.title"/>
                    </s:if>
                    <s:else>
                        <s:text name="add.ant.project.section.title"/>
                    </s:else>
                </h3>
                <div class="axial">
                    <s:form method="post" action="addProject" validate="true">
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
                        <table>
                          <tbody>
                            <s:textfield label="%{getText('projectName.label')}" name="projectName" requiredLabel="true" size="100">
                                <s:param name="after"><p><s:text name="projectName.message"/></p></s:param>
                            </s:textfield>
                            <s:textfield label="%{getText('projectDescription.label')}" name="projectDescription" size="100">
                                <s:param name="after"><p><s:text name="projectDescription.message"/></p></s:param>
                            </s:textfield>
                            <s:textfield label="%{getText('projectVersion.label')}" name="projectVersion" requiredLabel="true" size="100">
                                <s:param name="after"><p><s:text name="projectVersion.message"/></p></s:param>
                            </s:textfield>
                            <s:textfield label="%{getText('projectScmUrl.label')}" name="projectScmUrl" requiredLabel="true" size="100">
                                <s:param name="after"><p><s:text name="projectScmUrl.message"/></p></s:param>
                            </s:textfield>
                            <s:textfield label="%{getText('projectScmUsername.label')}" name="projectScmUsername" size="100">
                                <s:param name="after"><p><s:text name="projectScmUsername.message"/></p></s:param>
                            </s:textfield>
                            <s:password label="%{getText('projectScmPassword.label')}" name="projectScmPassword" size="100">
                                <s:param name="after"><p><s:text name="projectScmPassword.message"/></p></s:param>
                            </s:password>
                            <s:textfield label="%{getText('projectScmTag.label')}" name="projectScmTag" size="100">
                                <s:param name="after"><p><s:text name="projectScmTag.message"/></p></s:param>
                            </s:textfield>
                            <s:checkbox label="%{getText('projectScmUseCache.label')}" name="projectScmUseCache"/>

                            <s:if test="disableGroupSelection">
                              <s:hidden name="selectedProjectGroup"/>
                              <s:hidden name="disableGroupSelection"/>
                              <s:textfield label="%{getText('projectGroup.name.label')}" name="projectGroupName" disabled="true" size="100"/>
                            </s:if>
                            <s:else>
                              <s:select label="%{getText('projectGroup.name.label')}" name="selectedProjectGroup" list="projectGroups" listKey="id" listValue="name"/>
                            </s:else>

                            <s:if test="projectGroups.size() > 0">
                              <s:hidden name="emptyProjectGroups" value="false"/>
                            </s:if>
                            <s:else>
                              <s:hidden name="emptyProjectGroups" value="true"/>
                            </s:else>

                            <s:select label="%{getText('add.project.buildDefinitionTemplate')}" name="buildDefinitionTemplateId"
                                       list="buildDefinitionTemplates" listKey="id" listValue="name" headerKey="-1" 
                                       headerValue="%{getText('add.project.defaultBuildDefinition')}"/>                             
                          </tbody>
                        </table>
                        <input type="hidden" name="projectType" value="<s:property value="projectType"/>">
                        <div class="functnbar3">
                          <s:submit value="%{getText('add')}" theme="simple"/>
                          <input type="button" name="Cancel" value="<s:text name='cancel'/>" onclick="history.back();"/>
                        </div>
                    </s:form>
                </div>
            </div>
        </div>
    </body>
</html>
</s:i18n>
