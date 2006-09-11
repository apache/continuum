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
        <form action="projectGroupPermissions!save.action">

          username - view - edit - delete - build - admin<br/>

          <ww:iterator value="userPermissions" id="permission">

            <ww:property value='user.username'/>
            <ww:hidden name="userNames" value="%{user.username}"/>

            <ww:set name="read" value="<ww:property value='read'/>"/>
            <ww:set name="write" value="<ww:property value='write'/>"/>
            <ww:set name="delete" value="<ww:property value='delete'/>"/>
            <ww:set name="execute" value="<ww:property value='execute'/>"/>
            <ww:set name="administer" value="<ww:property value='administer'/>"/>

            <ww:if test="read == true">
              <input type="checkbox" name="map['<ww:property value="user.username"/>.read']" checked="true">
            </ww:if>
            <ww:else>
              <input type="checkbox" name="map['<ww:property value="user.username"/>.read']">
            </ww:else>
            <ww:if test="write == true">
              <input type="checkbox" name="map['<ww:property value="user.username"/>.write']" checked="true">
            </ww:if>
            <ww:else>
              <input type="checkbox" name="map['<ww:property value="user.username"/>.write']">
            </ww:else>
            <ww:if test="delete == true">
              <input type="checkbox" name="map['<ww:property value="user.username"/>.delete']" checked="true">
            </ww:if>
            <ww:else>
              <input type="checkbox" name="map['<ww:property value="user.username"/>.delete']">
            </ww:else>
            <ww:if test="execute == true">
              <input type="checkbox" name="map['<ww:property value="user.username"/>.execute']" checked="true">
            </ww:if>
            <ww:else>
              <input type="checkbox" name="map['<ww:property value="user.username"/>.execute']">
            </ww:else>
            <ww:if test="administer == true">
              <input type="checkbox" name="map['<ww:property value="user.username"/>.administer']" checked="true">
            </ww:if>
            <ww:else>
              <input type="checkbox" name="map['<ww:property value="user.username"/>.administer']">
            </ww:else>

            <br/>
          </ww:iterator>

          <input type="submit" value="Save"/>
        </form>
      </div>
    </body> 
  </ww:i18n>
</html>
