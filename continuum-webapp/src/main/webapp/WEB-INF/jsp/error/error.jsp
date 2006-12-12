<%--
  ~ Copyright 2005-2006 The Apache Software Foundation.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ taglib prefix="ww" uri="/webwork" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>Error Occurred</title>
  <ww:head />
  <script language=javascript type='text/javascript'>
    <!--
    var state = 'none';
    
    function showhide(layer_ref)
    {
      if (state == 'block')
      {
        state = 'none';
      }
      else
      {
        state = 'block';
      }
      if (document.all)
      { //IS IE 4 or 5 (or 6 beta)
        eval( "document.all." + layer_ref + ".style.display = state");
      }
      if (document.layers)
      { //IS NETSCAPE 4 or below
        document.layers[layer_ref].display = state;
      }
      if (document.getElementById &&!document.all)
      {
        hza = document.getElementById(layer_ref);
        hza.style.display = state;
      }
    }
    -->
  </script>
</head>

<body>
  <div id="h3">
    <h3>Error Occurred</h3>

    <div class="errormessage">
      <ww:property value="exception"/>
    </div>
    <p><a href="#" onclick="showhide('stacktrace');">Show/hide Stack Trace</a></p>
    <div id="stacktrace" style="display: none;">
      <pre>
        <ww:property value="exceptionStack"/>
      </pre>
    </div>
  </div>
</body>
</html>
