<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="/tld/extremecomponents" prefix="ec" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="releaseProject.page.title"/></title>
    </head>
    <body>
      <h2>Continuum Release</h2>
      <h3><ww:property value="name"/></h3>
      <div class="axial">
        <table border="1" cellspacing="2" cellpadding="3" width="100%">
          <tr>
            <th>&nbsp;</th>
            <th>Phase</th>
          </tr>
          <ww:iterator value="listener.phases">
            <tr>
              <ww:if test="listener.completedPhases.contains( listener.phases )">
                <td>done</td>
              </ww:if>
              <ww:else>
                <td>not done</td>
              </ww:else>
              <td><ww:property/></td>
            </tr>
          </ww:iterator>
        </table>
      </div>
    </body>
  </ww:i18n>
</html>
