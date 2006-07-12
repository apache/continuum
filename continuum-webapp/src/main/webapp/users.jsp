<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="users.page.title"/></title>
    </head>
    <body>
      <div id="h3">
        <h3><ww:text name="users.section.title"/></h3>
        <ww:set name="users" value="users" scope="request"/>
        <ec:table items="users"
                  var="user"
                  showExports="false"
                  showPagination="false"
                  showStatusBar="false"
                  filterable="false">
         <ec:row>
            <ec:column property="username" title="user.username"/>
            <ec:column property="email" title="user.email"/>
            <ec:column property="actions" title="&nbsp;">
                <a href="${pageContext.request.contextPath}/editUser!edit.action?id=${pageScope.user.id}"><ww:text name="edit"/></a>
                &nbsp;
                <ww:text name="delete"/>
            </ec:column>
          </ec:row>
        </ec:table>
      </div>
      <div class="functnbar3">
        <ww:form action="addUser!default.action" method="post">
          <ww:submit value="%{getText('add')}"/>
        </ww:form>
    </div>
    </body>
  </ww:i18n>
</html>