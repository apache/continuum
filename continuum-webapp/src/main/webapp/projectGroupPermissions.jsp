<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="continuum" prefix="c1" %>
 
<html>
  <ww:i18n name="localization.Continuum">
    <head>
      <title><ww:text name="projectView.page.title"/></title>
    </head>
 
    <body>
      <div id="h3">
 
        <jsp:include page="/navigations/ProjectGroupMenu.jsp">
          <jsp:param name="tab" value="permissions"/>
        </jsp:include>
 
        <h3>Users</h3>
 
        <ww:form id="myForm" action="projectGroupPermissions!save.action" method="post">
          <ec:table items="users" 
                    showExports="false"
                    showPagination="false"
                    showStatusBar="false"
                    filterable="false"
                    action="projectGroupPermissions!save.action"
                    form="myForm">
            <ec:row highlightRow="true">
              <ec:column property="user.username" title="User Name" width="48%" />
              <ec:column property="view" title="View" width="1%" sortable="false" cell="org.apache.maven.continuum.web.view.ProjectGroupPermissionsCell" />
              <ec:column property="edit" title="Edit" width="1%" sortable="false" cell="org.apache.maven.continuum.web.view.ProjectGroupPermissionsCell" />
              <ec:column property="delete" title="Delete" width="1%" sortable="false" cell="org.apache.maven.continuum.web.view.ProjectGroupPermissionsCell" />
              <ec:column property="build" title="Build" width="1%" sortable="false" cell="org.apache.maven.continuum.web.view.ProjectGroupPermissionsCell" />
            </ec:row>
          </ec:table>
          <ww:submit value="Save"/>
        </ww:form>
      </div>
    </body> 
  </ww:i18n>
</html>