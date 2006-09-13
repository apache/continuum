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
        <div class="eXtremeTable">
          <form action="projectGroupPermissions!save.action">
            <table id="ec_table" border="1" cellspacing="2" cellpadding="3" class="tableRegion" width="100%">
              <thead>
                <tr>
                  <td class="tableHeader"><ww:text name="projectView.username"/></td>
                  <td class="tableHeader"><center><ww:text name="projectView.role.view"/></center></td>
                  <td class="tableHeader"><center><ww:text name="projectView.role.edit"/></center></td>
                  <td class="tableHeader"><center><ww:text name="projectView.role.delete"/></center></td>
                  <td class="tableHeader"><center><ww:text name="projectView.role.build"/></center></td>
                  <td class="tableHeader"><center><ww:text name="projectView.role.administer"/></center></td>
                </tr>
              </thead>
              <tbody class="tableBody">
              <ww:iterator value="users" id="user" status="rowCounter">

                <ww:set name="view" value="<ww:property value='view'/>"/>
                <ww:set name="edit" value="<ww:property value='edit'/>"/>
                <ww:set name="delete" value="<ww:property value='delete'/>"/>
                <ww:set name="build" value="<ww:property value='build'/>"/>
                <ww:set name="administer" value="<ww:property value='administer'/>"/>

                <tr class="<ww:if test="#rowCounter.odd == true">odd</ww:if><ww:else>even</ww:else>">
                <td><ww:property value="user.username"/></td>
                <td width=100>
                  <center>
                    <ww:if test="read == true">
                      <input type="checkbox" name="map['<ww:property value="user.username"/>.read']" checked="true">
                    </ww:if>
                    <ww:else>
                      <input type="checkbox" name="map['<ww:property value="user.username"/>.read']">
                    </ww:else>
                  </center>
                </td>
                <td width=100>
                  <center>
                    <ww:if test="write == true">
                      <input type="checkbox" name="map['<ww:property value="user.username"/>.write']" checked="true">
                    </ww:if>
                    <ww:else>
                      <input type="checkbox" name="map['<ww:property value="user.username"/>.write']">
                    </ww:else>
                  </center>
                </td>
                <td width=100>
                  <center>
                    <ww:if test="delete == true">
                      <input type="checkbox" name="map['<ww:property value="user.username"/>.delete']" checked="true">
                    </ww:if>
                    <ww:else>
                      <input type="checkbox" name="map['<ww:property value="user.username"/>.delete']">
                    </ww:else>
                  </center>
                </td>
                <td width=100>
                  <center>
                    <ww:if test="execute == true">
                      <input type="checkbox" name="map['<ww:property value="user.username"/>.execute']" checked="true">
                    </ww:if>
                    <ww:else>
                      <input type="checkbox" name="map['<ww:property value="user.username"/>.execute']">
                    </ww:else>
                  </center>
                </td>
                <td width=100>
                  <center>
                    <ww:if test="administer == true">
                      <input type="checkbox" name="map['<ww:property value="user.username"/>.administer']" checked="true">
                    </ww:if>
                    <ww:else>
                      <input type="checkbox" name="map['<ww:property value="user.username"/>.administer']">
                    </ww:else>
                  </center>
                </td>
                </tr>
              </ww:iterator>
              </tbody>
            </table>
            <input type="submit" value="Save"/>
          </form>
        </div>
      </div>
    </body> 
  </ww:i18n>
</html>
