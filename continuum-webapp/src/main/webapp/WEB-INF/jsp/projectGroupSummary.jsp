<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri="/plexusSecuritySystem" prefix="pss" %>

<html>

<ww:i18n name="localization.Continuum">
  <head>
    <title>
      <ww:text name="projectView.page.title"/>
    </title>
  </head>
  <body>
  <div id="h3">
    <ww:action name="projectGroupTab" executeResult="true">
      <ww:param name="tabName" value="'Summary'"/>
    </ww:action>

  <%--
        <jsp:include page="/WEB-INF/jsp/navigations/ProjectGroupMenu.jsp">
          <jsp:param name="tab" value="summary"/>
        </jsp:include>
      --%>
        <h3>Project Group Actions</h3>

    <div class="axial">
      <table border="1" cellspacing="2" cellpadding="3" width="100%">
        <c1:data label="%{getText('projectView.project.name')}" name="projectGroup.name"/>
        <c1:data label="Group Id" name="projectGroup.groupId"/>
        <c1:data label="Description" name="projectGroup.description"/>
      </table>
    </div>

    <pss:ifAnyAuthorized permissions="continuum-build-group,continuum-remove-group" resource="${projectGroup.name}">
      <h3>Project Group Actions</h3>

      <div class="functnbar3">
                    <table>
               <tr>
                    <td>
                      <pss:ifAuthorized permission="continuum-build-group" resource="${projectGroup.name}">
                        <form action="buildProjectGroup.action" method="post">
                            <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
                            <input type="submit" name="build" value="<ww:text name="build"/>"/>
                        </form>
                        </pss:ifAuthorized>
                    </td>
                    <td>
                      <pss:ifAuthorized permission="continuum-remove-group" resource="${projectGroup.name}">
                        <form action="removeProjectGroup.action" method="post">
                            <input type="hidden" name="projectGroupId" value="<ww:property value="projectGroupId"/>"/>
                            <input type="submit" name="remove" value="<ww:text name="delete"/>"/>
                        </form>
                      </pss:ifAuthorized>
                    </td>                    
                </tr>
            </table>
        </div>
    </pss:ifAnyAuthorized>
   
    <ww:action name="projectSummary" executeResult="true" namespace="component">
      <ww:param name="projectGroupId" value="%{projectGroupId}"/>
      <ww:param name="projectGroupName" value="%{projectGroup.name}"/>
    </ww:action>
  </div>
  </body>
</ww:i18n>
</html>
