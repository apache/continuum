<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="addUser.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="addUser.section.title"/></h3>

        <div class="axial">
          <ww:form action="addUser.action" method="post">
            <table>
              <tbody>
                <ww:textfield label="%{getText('user.username')}" name="username" required="true"/>
                <ww:textfield label="%{getText('user.password')}" name="password" required="true"/>
                <ww:textfield label="%{getText('user.email')}" name="email" required="true"/>
              </tbody>
            </table>
            <div class="functnbar3">
              <c1:submitcancel value="%{getText('add')}" cancel="%{getText('cancel')}"/>
            </div>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
