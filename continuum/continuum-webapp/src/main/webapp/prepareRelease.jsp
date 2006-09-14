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
      <h2>Prepare Project Release</h2>
      <ww:form action="prepareRelease!doPrepare.action" method="post">
        <h3>Release Parameters</h3>
        <input type="hidden" name="projectId" value="<ww:property value="projectId"/>"/>
        <div class="axial">
          <table border="1" cellspacing="2" cellpadding="3" width="100%">
            <ww:textfield label="SCM Username" name="scmUsername"/>
            <ww:textfield label="SCM Password" name="scmPassword"/>
            <ww:textfield label="SCM Tag" name="scmTag"/>
            <ww:textfield label="SCM Tag Base" name="scmTagBase"/>
           </table>
        </div>

        <ww:iterator value="projects">
          <h3><ww:property value="name"/></h3>
          <input type="hidden" name="projectKeys" value="<ww:property value="key"/>">
          <div class="axial">
            <table border="1" cellspacing="2" cellpadding="3" width="100%">
              <tr>
                <th>Release Version*</th>
                <td>
                  <input type=text name="relVersions"
                         value="<ww:property value="release"/>" size="100">
                </td>
              </tr>
              <tr>
                <th>Next Development Version*</th>
                <td>
                  <input type=text name="devVersions"
                         value="<ww:property value="dev"/>" size="100">
                </td>
              </tr>
             </table>
           </div>
        </ww:iterator>

        <ww:submit/>
      </ww:form>
    </body>
  </ww:i18n>
</html>
