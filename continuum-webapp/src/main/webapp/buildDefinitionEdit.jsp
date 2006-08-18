<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="buildDefinition.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="buildDefinition.section.title"/></h3>

        <div class="axial">
          
          <ww:form action="addProjectBuildDefinition" method="post">
            <ww:hidden name="buildDefinitionId"/>
            <ww:hidden name="projectId"/>

            <table>
              <tbody>
                <ww:if test="executor == 'ant'">
                  <ww:textfield label="%{getText('buildDefinition.buildFile.ant.label')}" name="buildFile"  required="true"/>
                </ww:if>
                <ww:elseif test="executor == 'shell'">
                  <ww:textfield label="%{getText('buildDefinition.buildFile.shell.label')}" name="buildFile" required="true"/>
                </ww:elseif>
                <ww:else>
                  <ww:textfield label="%{getText('buildDefinition.buildFile.maven.label')}" name="buildFile" required="true"/>
                </ww:else>

                <ww:if test="executor == 'ant'">
                  <ww:textfield label="%{getText('buildDefinition.goals.ant.label')}" name="goals"/>
                </ww:if>
                <ww:elseif test="executor == 'shell'">
                </ww:elseif>
                <ww:else>
                  <ww:textfield label="%{getText('buildDefinition.goals.maven.label')}" name="goals"/>
                </ww:else>

                <ww:textfield label="%{getText('buildDefinition.arguments.label')}" name="arguments"/>
                <ww:checkbox label="%{getText('buildDefinition.defaultForProject.label')}"  name="defaultForProject" value="defaultForProject" fieldValue="true"/>
                <ww:select label="%{getText('buildDefinition.schedule.label')}" name="scheduleId" list="schedules"/>
              </tbody>
            </table>
            <div class="functnbar3">
              <c1:submitcancel value="%{getText('save')}" cancel="%{getText('cancel')}"/>
            </div>
          </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
