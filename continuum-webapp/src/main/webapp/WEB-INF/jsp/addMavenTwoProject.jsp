<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<ww:i18n name="localization.Continuum">
<html>
    <head>
        <title><ww:text name="add.m2.project.page.title"/></title>
    </head>
    <body>
        <div class="app">
            <div id="axial" class="h3">
                <h3><ww:text name="add.m2.project.section.title"/></h3>
                <div class="axial">
                    <ww:form method="post" action="addMavenTwoProject.action" name="addMavenTwoProject" enctype="multipart/form-data">
                        <c:if test="${!empty actionErrors}">
                          <div class="errormessage">
                            <c:forEach items="${actionErrors}" var="actionError">
                              <p><ww:text name="${actionError}"/></p>
                            </c:forEach>
                          </div>
                        </c:if>
                        <table>
                          <tbody>
                            <ww:textfield label="%{getText('add.m2.project.m2PomUrl.label')}" name="m2PomUrl">
                                <ww:param name="desc">
                                <table cellspacing="0" cellpadding="0">
                                  <tbody>
                                    <tr>
                                      <td><ww:text name="add.m2.project.m2PomUrl.username.label"/>: </td>
                                      <td><input type="text" name="username" size="20" id="addMavenTwoProject_username"/><td>
                                    </tr>  
                                    <tr>
                                      <td><ww:text name="add.m2.project.m2PomUrl.password.label"/>: </td>
                                      <td><input type="password" name="password" size="20" id="addMavenTwoProject_password"/><td>
                                    </tr>  
                                  </tbody>
                                </table>  
                                  <p><ww:text name="add.m2.project.m2PomUrl.message"/></p>
                                </ww:param>
                            </ww:textfield>
                            <ww:label>
                              <ww:param name="after"><strong><ww:text name="or"/></strong></ww:param>
                            </ww:label>
                            <ww:file label="%{getText('add.m2.project.m2PomFile.label')}" name="m2PomFile">
                                <ww:param name="desc"><p><ww:text name="add.m2.project.m2PomFile.message"/></p></ww:param>
                            </ww:file>
                            <ww:if test="disableGroupSelection == true">
                              <ww:hidden name="selectedProjectGroup"/>
                              <ww:hidden name="disableGroupSelection"/>
                              <ww:textfield label="%{getText('add.m2.project.projectGroup')}" name="projectGroupName" disabled="true"/>
                            </ww:if>
                            <ww:else>
                              <ww:select label="%{getText('add.m2.project.projectGroup')}" name="selectedProjectGroup" list="projectGroups" listKey="id" listValue="name"/>
                            </ww:else>
                          </tbody>
                        </table>
                        <div class="functnbar3">
                          <c1:submitcancel value="%{getText('add')}" cancel="%{getText('cancel')}"/>
                        </div>
                    </ww:form>
                </div>
            </div>
        </div>
    </body>
</html>
</ww:i18n>
