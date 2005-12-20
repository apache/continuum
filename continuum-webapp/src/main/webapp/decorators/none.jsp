<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>

<html>
  <head>
    <title><decorator:title/></title>
    <link rel="stylesheet" type="text/css" href="<ww:url value="/css/tigris.css"/>" media="screen" />
    <link rel="stylesheet" type="text/css" href="<ww:url value="/css/print.css"/>" media="print" />
    <link rel="stylesheet" type="text/css" href="<ww:url value="/css/extremecomponents.css"/>" media="screen" />

    <script src="<ww:url value="/scripts/tigris.js"/>" type="text/javascript"></script>
    <decorator:head/>
  </head>

  <body onload="focus()" marginwidth="0" marginheight="0" class="composite">
    <ww:include value="/navigations/DefaultTop.jsp"/>

    <table id="main" border="0" cellpadding="4" cellspacing="0" width="100%">
      <tbody>
        <tr valign="top">
          <td>
            <div id="bodycol">
                <div class="app">
                  <decorator:body/>
                </div>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <ww:include value="/navigations/DefaultBottom.jsp"/>
  </body>
</html>
