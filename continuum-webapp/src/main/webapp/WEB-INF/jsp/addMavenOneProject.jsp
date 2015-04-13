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
        <title><s:text name="add.m1.project.page.title"/></title>
        <style>
            table.addProject td:nth-child(1) {
                width: 162px;
                text-align: right;
            }
        </style>
        <script>
            function enablePomMethod(method) {
                var $urlRow = jQuery('form tr:has(#pomUrlInput)');
                var $fileRow = jQuery('form tr:has(#pomFileInput)');
                if (method == 'FILE') {
                    $urlRow.hide();
                    $fileRow.show();
                } else {
                    $fileRow.hide();
                    $urlRow.show();
                }
            }
            jQuery(document).ready(function($) {
                var selectedMethod = $('input[name=pomMethod]:checked').val();
                enablePomMethod(selectedMethod);
                $('input[name=pomMethod]').click(function() {
                    enablePomMethod($(this).val());
                });
            });
        </script>
    </head>
    <body>
        <div class="app">
            <div id="axial" class="h3">
            <h3><s:text name="add.m1.project.section.title"/></h3>
                <div class="axial">
                    <s:form method="post" action="addMavenOneProject" name="addMavenOneProject" enctype="multipart/form-data">
                        <s:if test="hasActionErrors() || errorMessages.size() > 0">
                          <div class="errormessage">
                            <s:iterator value="actionErrors">
                              <p><s:property/></p>
                            </s:iterator>
                            <s:iterator value="errorMessages">
                              <p><s:property/></p>
                            </s:iterator>
                          </div>
                        </s:if>
                        <table class="addProject">
                          <tbody>
                            <s:radio name="pomMethod" list="pomMethodOptions"  label="%{getText('add.maven.project.pomMethod')}" />
                            <s:textfield id="pomUrlInput" label="%{getText('add.m1.project.m1PomUrl.label')}" requiredLabel="true" name="m1PomUrl" size="100">
                                <s:param name="after">
                                <table cellspacing="0" cellpadding="0">
                                  <tbody>
                                    <tr>
                                      <td><s:text name="add.m1.project.m1PomUrl.username.label"/>: </td>
                                      <td><s:textfield name="scmUsername" size="20" id="addMavenOneProject_scmUsername" theme="simple"/><td>
                                    </tr>  
                                    <tr>
                                      <td><s:text name="add.m1.project.m1PomUrl.password.label"/>: </td>
                                      <td><s:password name="scmPassword" size="20" id="addMavenOneProject_scmPassword" theme="simple"/><td>
                                    </tr>  
                                  </tbody>
                                    <tr>
                                      <td></td>
                                      <td><s:checkbox label="%{getText('projectEdit.project.scmUseCache.label')}" name="scmUseCache"/><td>
                                    </tr>
                                </table>  
                                  <p><s:text name="add.m1.project.m1PomUrl.message"/></p>
                                </s:param>
                            </s:textfield>
                            <s:file id="pomFileInput" label="%{getText('add.m1.project.m1PomFile.label')}" requiredLabel="true" name="m1PomFile" size="100" accept="application/xml,text/xml">
                                <s:param name="after"><p><s:text name="add.m1.project.m1PomFile.message"/></p></s:param>
                            </s:file>
                            <s:if test="disableGroupSelection">
                              <s:hidden name="selectedProjectGroup"/>
                              <s:hidden name="disableGroupSelection"/>
                              <s:textfield label="%{getText('add.m1.project.projectGroup')}" name="projectGroupName" disabled="true" size="100"/>
                            </s:if>
                            <s:else>
                              <s:select label="%{getText('add.m1.project.projectGroup')}" name="selectedProjectGroup" list="projectGroups" listKey="id" listValue="name"/>
                            </s:else>
                            <s:select label="%{getText('add.m1.project.buildDefinitionTemplate')}" name="buildDefinitionTemplateId"
                                       list="buildDefinitionTemplates" listKey="id" listValue="name" headerKey="-1" 
                                       headerValue="%{getText('add.m1.project.defaultBuildDefinition')}"/>                            
                          </tbody>
                        </table>
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
