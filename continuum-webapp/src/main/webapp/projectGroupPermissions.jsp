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
          <jsp:param name="tab" value="summary"/>
        </jsp:include>

        <h3>Project Group Information</h3>
            
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <c1:data label="%{getText('projectView.project.name')}" name="projectGroup.name"/>
            <c1:data label="Group Id" name="projectGroup.groupId"/>
            <c1:data label="Description" name="projectGroup.description"/>
           </table>
        </div>

        <h3>Users</h3>

        <ec:table items="users"
                  var="user"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false">
          <ec:row highlightRow="true">
            <ec:column property="username" title="User Name" width="48%">
            </ec:column>
            <ec:column property="view" title="View" width="1%" sortable="false">
              <center>
                <ww:checkbox label="" name="viewPermission" value="${pageScope.user.view}" fieldValue="true"/>
              </center>
            </ec:column>
            <ec:column property="edit" title="Edit" width="1%" sortable="false">
              <center>
                <ww:checkbox label="" name="editPermission" value="${pageScope.user.edit}" fieldValue="true"/>
              </center>
            </ec:column>
            <ec:column property="delete" title="Delete" width="1%" sortable="false">
              <center>
                <ww:checkbox label="" name="deletePermission" value="${pageScope.user.delete}" fieldValue="true"/>
              </center>
            </ec:column>
            <ec:column property="build" title="Build" width="1%" sortable="false">
              <center>
                <ww:checkbox label="" name="buildPermission" value="${pageScope.user.build}" fieldValue="true"/>
              </center>
            </ec:column>
          </ec:row>
        </ec:table>

    </body> 
  </ww:i18n>
</html>
