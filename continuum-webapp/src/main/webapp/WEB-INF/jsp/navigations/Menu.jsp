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

<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<ww:i18n name="localization.Continuum">
<div id="navcolumn">
  <div id="projectmenu" class="toolgroup">
    <div class="label">Continuum</div>
    <div>
      <div class="body">
        <ww:url id="aboutUrl" action="about" namespace="/" includeParams="none"/>
        <ww:a href="%{aboutUrl}">
          <ww:text name="menu.continuum.about"/>
        </ww:a>
      </div>
      <div class="body">
        <ww:url id="groupSummaryUrl" action="groupSummary" namespace="/" includeParams="none"/>
        <ww:a href="%{groupSummaryUrl}">
          <ww:text name="menu.continuum.showProjectGroups"/>
        </ww:a>
      </div>
    </div>
  </div>

  <redback:ifAuthorized permission="continuum-add-group">
    <div id="projectmenu" class="toolgroup">
      <div class="label">
        <ww:text name="menu.addProject"/>
      </div>
      <div>
        <div class="body">
          <ww:url id="addMavenTwoProjectUrl" action="addMavenTwoProjectInput" method="input" namespace="/"
                  includeParams="none"/>
          <ww:a href="%{addMavenTwoProjectUrl}">
            <ww:text name="menu.add.m2Project"/>
          </ww:a>
        </div>
        <div class="body">
          <ww:url id="addMavenOneProjectUrl" action="addMavenOneProjectInput" method="input" namespace="/"
                  includeParams="none"/>
          <ww:a href="%{addMavenOneProjectUrl}">
            <ww:text name="menu.add.m1Project"/>
          </ww:a>
        </div>
        <div class="body">
          <ww:url id="addAntProjectUrl" action="addProjectInput" namespace="/" includeParams="none">
            <ww:param name="projectType">ant</ww:param>
          </ww:url>
          <ww:a href="%{addAntProjectUrl}">
            <ww:text name="menu.add.antProject"/>
          </ww:a>
        </div>
        <div class="body">
          <ww:url id="addShellProjectUrl" action="addProjectInput" namespace="/" includeParams="none">
            <ww:param name="projectType">shell</ww:param>
          </ww:url>
          <ww:a href="%{addShellProjectUrl}">
            <ww:text name="menu.add.shellProject"/>
          </ww:a>
        </div>
      </div>
    </div>
  </redback:ifAuthorized>


  <redback:ifAnyAuthorized permissions="continuum-manage-build-templates,continuum-manage-schedules,continuum-manage-configuration,continuum-manage-users,continuum-manage-installations,continuum-manage-profiles,continuum-view-queues">
    <div id="projectmenu" class="toolgroup">
      <div class="label">
        <ww:text name="menu.administration"/>
      </div>
      <div>
        <redback:ifAuthorized permission="continuum-manage-schedules">
          <ww:url id="scheduleUrl" namespace="/" action="schedules" includeParams="none"/>
          <div class="body">
            <ww:a href="%{scheduleUrl}">
              <ww:text name="menu.administration.schedules"/>
            </ww:a>
          </div>
        </redback:ifAuthorized>
        <redback:ifAuthorized permission="continuum-manage-installations">
          <ww:url id="configurationUrl" action="installationsList" namespace="/admin" method="list" includeParams="none"/>
          <div class="body">
            <ww:a href="%{configurationUrl}">
              <ww:text name="menu.administration.installations"/>
            </ww:a>
          </div>
        </redback:ifAuthorized>
        <redback:ifAuthorized permission="continuum-manage-profiles">
          <ww:url id="configurationUrl" action="profilesList" namespace="/admin" method="list" includeParams="none"/>
          <div class="body">
            <ww:a href="%{configurationUrl}">
              <ww:text name="menu.administration.profile"/>
            </ww:a>
          </div> 
        </redback:ifAuthorized> 
        <redback:ifAuthorized permission="continuum-view-queues">
          <ww:url id="queueUrls" action="displayQueues" namespace="/admin" method="display" includeParams="none"/>
          <div class="body">
            <ww:a href="%{queueUrls}">
              <ww:text name="menu.administration.queues"/>
            </ww:a>
          </div> 
        </redback:ifAuthorized> 
        <redback:ifAuthorized permission="continuum-manage-build-templates">
          <ww:url id="buildDefinitionTemplatesUrl" action="buildDefinitionTemplates" namespace="/admin" includeParams="none"/>
          <div class="body">
            <ww:a href="%{buildDefinitionTemplatesUrl}">
              <ww:text name="menu.administration.buildDefinitionTemplates"/>
            </ww:a>
          </div> 
        </redback:ifAuthorized>        
        <redback:ifAuthorized permission="continuum-manage-configuration">               
          <ww:url id="configurationUrl" action="configuration" namespace="/admin" method="input" includeParams="none"/>
          <div class="body">
            <ww:a href="%{configurationUrl}">
              <ww:text name="menu.administration.configuration"/>
            </ww:a>
          </div>
          <ww:url id="configurationUrl" action="configureAppearance" namespace="/admin" includeParams="none"/>
          <div class="body">
            <ww:a href="%{configurationUrl}">
              <ww:text name="menu.administration.appearance"/>
            </ww:a>
          </div>
        </redback:ifAuthorized>
        <redback:ifAuthorized permission="continuum-manage-users">
          <ww:url id="userListUrl" action="userlist" namespace="/security" includeParams="none"/>
          <div class="body">
            <ww:a href="%{userListUrl}">
              <ww:text name="menu.administration.users"/>
            </ww:a>
          </div>
        </redback:ifAuthorized>
      </div>
    </div>
  </redback:ifAnyAuthorized>

  <div id="projectmenu" class="toolgroup">
    <div class="label">Legend</div>
    <div id="legend">
      <div id="litem1" class="body"><ww:text name="legend.buildNow"/></div>
      <div id="litem2" class="body"><ww:text name="legend.buildHistory"/></div>
      <div id="litem3" class="body"><ww:text name="legend.buildInProgress"/></div>
      <div id="litem4" class="body"><ww:text name="legend.workingCopy"/></div>
      <div id="litem5" class="body"><ww:text name="legend.checkingOutBuild"/></div>
      <div id="litem6" class="body"><ww:text name="legend.queuedBuild"/></div>
      <div id="litem7" class="body"><ww:text name="legend.cancelBuild"/></div>
      <div id="litem8" class="body"><ww:text name="legend.delete"/></div>
      <div id="litem9" class="body"><ww:text name="legend.edit"/></div>
      <div id="litem10" class="body"><ww:text name="legend.release"/></div>
      <div id="litem11" class="body"><ww:text name="legend.buildInSuccess"/></div>
      <div id="litem12" class="body"><ww:text name="legend.buildInFailure"/></div>
      <div id="litem13" class="body"><ww:text name="legend.buildInError"/></div>
    </div>
  </div>
</div>
</ww:i18n>
