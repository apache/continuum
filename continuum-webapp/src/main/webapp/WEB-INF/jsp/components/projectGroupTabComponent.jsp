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
<s:i18n name="localization.Continuum">

  <div id="h3">
    <div>
      <p style="border-top: 1px solid transparent; border-bottom: 1px solid #DFDEDE;">
        <s:url id="projectGroupSummaryUrl" action="projectGroupSummary" includeParams="none">
          <s:param name="projectGroupId" value="projectGroupId"/>
        </s:url>
        <s:url id="projectGroupMembersUrl" action="projectGroupMembers" includeParams="none">
          <s:param name="projectGroupId" value="projectGroupId"/>
        </s:url>
        <s:url id="projectGroupBuildDefinitionUrl" action="projectGroupBuildDefinition" includeParams="none">
          <s:param name="projectGroupId" value="projectGroupId"/>
        </s:url>
        <s:url id="projectGroupNotifierUrl" action="projectGroupNotifier" includeParams="none">
          <s:param name="projectGroupId" value="projectGroupId"/>
        </s:url>
        <s:url id="projectGroupReleaseResultsUrl" action="projectGroupReleaseResults" includeParams="none">
          <s:param name="projectGroupId" value="projectGroupId"/>
        </s:url>

        <s:set name="tabName" value="tabName"/>

            <s:if test="tabName != 'Summary'">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupSummaryUrl}"><s:text name="projectGroup.tab.summary"/></a>
            </s:if>
            <s:else>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><s:text name="projectGroup.tab.summary"/></b>
            </s:else>

            <s:if test="tabName != 'Members'">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupMembersUrl}"><s:text name="projectGroup.tab.members"/></a>
            </s:if>
            <s:else>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><s:text name="projectGroup.tab.members"/></b>
            </s:else>

            <s:if test="tabName != 'BuildDefinitions'">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupBuildDefinitionUrl}"><s:text name="projectGroup.tab.buildDefinitions"/></a>
            </s:if>
            <s:else>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><s:text name="projectGroup.tab.buildDefinitions"/></b>
            </s:else>

            <s:if test="tabName != 'Notifier'">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupNotifierUrl}"><s:text name="projectGroup.tab.notifiers"/></a>
            </s:if>
            <s:else>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><s:text name="projectGroup.tab.notifiers"/></b>
            </s:else>

            <s:if test="tabName != 'ReleaseResults'">
                <a style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em; text-decoration: none;" href="${projectGroupReleaseResultsUrl}"><s:text name="projectGroup.tab.releaseResults"/></a>
            </s:if>
            <s:else>
                <b style="border: 1px solid #DFDEDE; padding-left: 1em; padding-right: 1em;"><s:text name="projectGroup.tab.releaseResults"/></b>
            </s:else>

      </p>
    </div>
  </div>
</s:i18n>