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
      <h3>Continuum Release</h3>
      <ww:form action="releaseProject" method="post">
        <p>
          <input name="goal" type="radio" value="prepare" checked/>Prepare project for release
          <br/>
          <input name="goal" type="radio" value="perform"/>Perform project release
          <br/>
          &nbsp;&nbsp;&nbsp;
          <select name="preparedReleaseId">
            <ww:if test="preparedReleaseName != null">
              <option selected value="<ww:property value="preparedReleaseId"/>">
                <ww:property value="preparedReleaseName"/>
              </option>
            </ww:if>
            <option value="">Provide release descriptor</option>
          </select>
          <br/>
        </p>
        <input name="projectId" type="hidden" value="<ww:property value="projectId"/>"/>
        <ww:submit value="Submit"/>
      </ww:form>
    </body>
  </ww:i18n>
</html>
