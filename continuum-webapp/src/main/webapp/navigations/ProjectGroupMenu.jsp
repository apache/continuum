<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

<div>
  <p style="border-top: 1px solid transparent; border-bottom: 1px solid #DFDEDE;">

    <ww:url id="projectGroupSummaryUrl" action="projectGroupSummary">
      <ww:param name="projectGroupId" value="projectGroupId"/>
    </ww:url>
    <ww:url id="projectGroupMembersUrl" action="projectGroupMembers">
      <ww:param name="projectGroupId" value="projectGroupId"/>
    </ww:url>
    <ww:url id="projectGroupBuildDefinitionUrl" action="projectGroupBuildDefinition">
      <ww:param name="projectGroupId" value="projectGroupId"/>
    </ww:url>
    <ww:url id="projectGroupNotifierUrl" action="projectGroupNotifier">
      <ww:param name="projectGroupId" value="projectGroupId"/>
    </ww:url>
<%--
    <ww:url id="projectGroupPermissionsUrl" action="projectGroupPermissions">
      <ww:param name="projectGroupId" value="projectGroupId"/>
    </ww:url>
--%>

    <ww:a cssClass="tabMenuEnabled" href="%{projectGroupSummaryUrl}">Summary</ww:a>
    <ww:a cssClass="tabMenuEnabled" href="%{projectGroupManageUrl}">Manage</ww:a>
    <b class="tabMenuDisabled">Build Definition</b>
    <ww:a cssStyle="tabMenuEnabled" href="%{projectGroupNotifierUrl}">Notifier</ww:a>
  </p>
</div>
