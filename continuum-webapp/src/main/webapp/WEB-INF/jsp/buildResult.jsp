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
<%@ taglib prefix="c1" uri="continuum" %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<html>
  <s:i18n name="localization.Continuum">
    <head>
        <title><s:text name="buildResult.page.title"/></title>
        <script type="text/javascript">
          <s:url id="outputAsyncUrl" action="buildOutputJSON" escapeAmp="false">
            <s:param name="projectId" value="projectId"/>
            <s:param name="buildId" value="buildId"/>
          </s:url>
          jQuery(document).ready(function($) {

            var buildInProgress = <s:property value="buildInProgress" />;
            var outputUrl = '<s:property value="#outputAsyncUrl" escapeHtml="false" />';
            var refreshPending = false;

            var $ta = $('#outputArea');

            function toggleOutput() {
              if ($ta.html()) {
                $('#noBuildOutput').hide();
                $('#buildOutput').show();
              } else {
                $('#buildOutput').hide();
                $('#noBuildOutput').show();
              }
            }
            toggleOutput();  // Show appropriate initial controls

            function scrollToBottom($textArea) {
              var newHeight = $textArea.attr('scrollHeight');
              $textArea.attr('scrollTop', newHeight);
            }
            scrollToBottom($ta);  // Scroll text area to bottom on intial page load

            function isScrolledToBottom($textArea) {
              return $textArea.attr('scrollHeight') - $textArea.attr('clientHeight') == $textArea.attr('scrollTop')
            }

            function showStatus(building, loading) {
              if (loading) {
                $ta.addClass('cmd-loading');
                $ta.removeClass('cmd-building');
              } else if (building) {
                $ta.addClass('cmd-building');
                $ta.removeClass('cmd-loading');
              } else {
                $ta.removeClass('cmd-building');
                $ta.removeClass('cmd-loading');
              }
            }
            showStatus(buildInProgress);

            setInterval(function() {
              if (buildInProgress && !refreshPending) {
                refreshPending = true;
                var autoScroll = isScrolledToBottom($ta);
                showStatus(buildInProgress, true);
                $.ajax({
                  url: outputUrl,
                  contentType: 'application/json;charset=utf-8',
                  success: function(data) {
                    parsed = JSON.parse(data);
                    var output = parsed.buildOutput;
                    buildInProgress = parsed.buildInProgress;
                    $ta.html(output);
                    toggleOutput();
                    if (autoScroll) {
                      scrollToBottom($ta);
                    }
                    if (!buildInProgress) {
                      location.reload();  // reload page when complete
                    }
                  },
                  complete: function() {
                    refreshPending = false;
                    showStatus(buildInProgress, false);
                  }
                });
              }
            }, 1000);
          });
        </script>
    </head>
    <body>
      <div id="h3">

        <jsp:include page="/WEB-INF/jsp/navigations/ProjectMenu.jsp"/>

        <h3>
            <s:text name="buildResult.section.title">
                <s:param><s:property value="project.name"/></s:param>
            </s:text>
        </h3>

        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <tr class="b">
              <th><label class="label"><s:text name='buildResult.startTime'/>:</label></th>
              <td><c1:date name="buildResult.startTime"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='buildResult.endTime'/>:</label></th>
              <td><c1:date name="buildResult.endTime"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='buildResult.duration'/>:</label></th>
              <td><s:if test="buildResult.endTime == 0"><s:text name="buildResult.startedSince"/></s:if> <s:property value="buildResult.durationTime"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='buildResult.trigger'/>:</label></th>
              <td><s:text name="buildResult.trigger.%{buildResult.trigger}"/></td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='buildResult.state'/>:</label></th>
              <td>${state}</td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='buildResult.buildNumber'/>:</label></th>
              <td>
                <s:if test="showBuildNumber">
                  <s:property value="buildResult.buildNumber"/>
                </s:if>
                <s:else>
                  &nbsp;
                </s:else>
              </td>
            </tr>
            <tr class="b">
              <th><label class="label"><s:text name='buildResult.username'/>:</label></th>
              <td><s:property value="buildResult.username"/></td>
            </tr>
            <s:if test="buildResult.buildUrl.length() > 0">
              <tr class="b">
                <th><label class="label"><s:text name='buildResult.buildUrl'/>:</label></th>
                <td><s:property value="buildResult.buildUrl"/></td>
              </tr>
            </s:if>
          </table>
        </div>
        <div class="functnbar3">
          <table>
            <tbody>
            <tr>
              <td>
                <redback:ifAuthorized permission="continuum-modify-group" resource="${projectGroupName}">
                  <s:form action="removeBuildResult" theme="simple">
                    <s:hidden name="projectId" />
                    <s:hidden name="buildId" />
                    <s:token/>
                    <s:if test="canDelete">
                      <input type="submit" name="delete-project" value="<s:text name="delete"/>"/>
                    </s:if>
                    <s:else>
                      <input type="submit" disabled="true" name="delete-project" value="<s:text name="delete"/>"/>
                    </s:else>
                  </s:form>
                </redback:ifAuthorized>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <h4><s:text name="buildResult.scmResult.changes"/></h4>
        <s:if test="buildResult.scmResult.changes != null && buildResult.scmResult.changes.size() > 0">
            <s:set name="changes" value="buildResult.scmResult.changes" scope="request"/>
            <ec:table items="changes"
                      autoIncludeParameters="false"
                      var="change"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="author" title="buildResult.scmResult.changes.author"/>
                <ec:column property="date" title="buildResult.scmResult.changes.date" cell="date"/>
                <ec:column property="comment" title="buildResult.scmResult.changes.comment" cell="escapeHtml" />
                <ec:column property="files" title="buildResult.scmResult.changes.files">
                    <s:iterator value="#attr.change.files">
                        <s:property value="name"/><br />
                    </s:iterator>
                </ec:column>
              </ec:row>
            </ec:table>
        </s:if>
        <s:else>
          <b><s:text name="buildResult.scmResult.noChanges"/></b>
        </s:else>

        <s:if test="changesSinceLastSuccess != null && changesSinceLastSuccess.size() > 0">
            <h4><s:text name="buildResult.changesSinceLastSuccess"/></h4>
            <s:set name="changes" value="changesSinceLastSuccess" scope="request"/>
            <ec:table items="changes"
                      autoIncludeParameters="false"
                      var="change"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="author" title="buildResult.changes.author"/>
                <ec:column property="date" title="buildResult.changes.date" cell="date"/>
                <ec:column property="comment" title="buildResult.changes.comment" />
                <ec:column property="files" title="buildResult.changes.files">
                    <s:iterator value="#attr.change.files">
                        <s:property value="name"/><br />
                    </s:iterator>
                </ec:column>
              </ec:row>
            </ec:table>
        </s:if>

        <h4><s:text name="buildResult.dependencies.changes"/></h4>
        <s:if test="buildResult.modifiedDependencies != null && buildResult.modifiedDependencies.size() > 0">
            <s:set name="dependencies" value="buildResult.modifiedDependencies" scope="request"/>
            <ec:table items="dependencies"
                      var="dep"
                      autoIncludeParameters="false"
                      showExports="false"
                      showPagination="false"
                      showStatusBar="false"
                      sortable="false"
                      filterable="false">
              <ec:row>
                <ec:column property="groupId" title="buildResult.dependencies.groupId"/>
                <ec:column property="artifactId" title="buildResult.dependencies.artifactId"/>
                <ec:column property="version" title="buildResult.dependencies.version"/>
              </ec:row>
            </ec:table>
        </s:if>
        <s:else>
          <b><s:text name="buildResult.dependencies.noChanges"/></b>
        </s:else>
        
        <h4><s:text name="buildResult.buildDefinition"/></h4>
          <table border="1" cellspacing="2" cellpadding="3" width="80%">
            <tbody>
              <s:if test="buildResult.buildDefinition.type='ant'">
                <tr class="b">
                  <th><s:text name="buildResult.buildDefinition.ant.label"/></th>
                  <td><s:property value="buildResult.buildDefinition.buildFile"/></td>
                </tr>               
              </s:if>
              <s:elseif test="buildResult.buildDefinition.type='shell'">
                <tr class="b">
                  <th><s:text name="buildResult.buildDefinition.shell.label"/></th>
                  <td><s:property value="buildResult.buildDefinition.buildFile"/></td>
                </tr>               
              </s:elseif>
              <s:else>
                <tr class="b">
                  <th><s:text name="buildResult.buildDefinition.maven.label"/></th>
                  <td><s:property value="buildResult.buildDefinition.buildFile"/></td>
                </tr>               
              </s:else>
              <tr class="b">
                <th><s:text name="buildResult.buildDefinition.goals"/></th>
                <td><s:property value="buildResult.buildDefinition.goals"/></td>
              </tr>
              <tr class="b">
                <th><s:text name="buildResult.buildDefinition.arguments"/></th>
                <td><s:property value="buildResult.buildDefinition.arguments"/></td>
              </tr>
              <tr class="b">
                <th><s:text name="buildResult.buildDefinition.buildFresh"/></th>
                <td><s:property value="buildResult.buildDefinition.buildFresh"/></td>
              </tr>
              <tr class="b">
                <th><s:text name="buildResult.buildDefinition.alwaysBuild"/></th>
                <td><s:property value="buildResult.buildDefinition.alwaysBuild"/></td>
              </tr>
              <tr class="b">
                <th><s:text name="buildResult.buildDefinition.defaultForProject"/></th>
                <td><s:property value="buildResult.buildDefinition.defaultForProject"/></td>
              </tr>
              <tr class="b">
                <th><s:text name="buildResult.buildDefinition.schedule"/></th>
                <td><s:property value="buildResult.buildDefinition.schedule.name"/></td>
              </tr>
              <s:if test="buildResult.buildDefinition.profile != null">
                <tr class="b">
                  <th><s:text name="buildResult.buildDefinition.profileName"/></th>
                  <td><s:property value="buildResult.buildDefinition.profile.name"/></td>
                </tr>          
              </s:if>
              <tr class="b">
                <th><s:text name="buildResult.buildDefinition.description"/></th>
                <td><s:property value="buildResult.buildDefinition.description"/></td>
              </tr>              
            </tbody>
          </table> 

        <s:if test="hasSurefireResults">
          <h4><s:text name="buildResult.generatedReports.title"/></h4>

          <s:url id="surefireReportUrl" action="surefireReport">
            <s:param name="projectId" value="projectId"/>
            <s:param name="buildId" value="buildId"/>
            <s:param name="projectName" value="projectName"/>
          </s:url>
          <s:a href="%{surefireReportUrl}"><s:text name="buildResult.generatedReports.surefire"/></s:a>
        </s:if>

        <s:if test="showBuildError">
          <h4><s:text name="buildResult.buildError"/></h4>
          <div class="cmd-output pre-wrap"><s:property value="buildResult.error"/></div>
        </s:if>

        <h4><s:text name="buildResult.buildOutput"/></h4>
        <p>
          <span id="noBuildOutput">
            <s:text name="buildResult.noOutput"/>
          </span>
          <div id="buildOutput" style="display: none;">
            <s:url id="buildOutputTextUrl" action="buildOutputText">
              <s:param name="projectId" value="projectId"/>
              <s:param name="buildId" value="buildId"/>
            </s:url>
            <s:a href="%{buildOutputTextUrl}"><s:text name="buildResult.buildOutput.text"/></s:a>
            <div id="outputArea" class="cmd-output cmd-window pre-wrap"><s:property value="buildOutput"/></div>
          </div>
        </p>
      </div>
    </body>
  </s:i18n>
</html>
