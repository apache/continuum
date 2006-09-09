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

          <ww:iterator value="userPermissions" id="permission">

            <ww:property value="user.username"/>

            <ww:set name="view" value="<ww:property value='view'/>"/>
            <ww:set name="edit" value="<ww:property value='edit'/>"/>
            <ww:set name="delete" value="<ww:property value='delete'/>"/>
            <ww:set name="build" value="<ww:property value='build'/>"/>
            <ww:if test="view == true">
              <input type="checkbox" name="map['<ww:property value="user.username"/>view']" checked="true">
            </ww:if>
            <ww:else>
              <input type="checkbox" name="map['<ww:property value="user.username"/>view']">
            </ww:else>
            <ww:if test="edit == true">
              <input type="checkbox" name="map['<ww:property value="user.username"/>edit']" checked="true">
            </ww:if>
            <ww:else>
              <input type="checkbox" name="map['<ww:property value="user.username"/>edit']">
            </ww:else>
            <ww:if test="delete == true">
              <input type="checkbox" name="map['<ww:property value="user.username"/>delete']" checked="true">
            </ww:if>
            <ww:else>
              <input type="checkbox" name="map['<ww:property value="user.username"/>delete']">
            </ww:else>
            <ww:if test="build == true">
              <input type="checkbox" name="map['<ww:property value="user.username"/>build']" checked="true">
            </ww:if>
            <ww:else>
              <input type="checkbox" name="map['<ww:property value="user.username"/>build']">
            </ww:else>
            <br/>
          </ww:iterator>

          <input type="submit" value="Save"/>
        </form>
      </div>
    </body> 
  </ww:i18n>
</html>
