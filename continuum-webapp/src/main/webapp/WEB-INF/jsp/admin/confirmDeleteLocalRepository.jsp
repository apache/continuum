<%--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib uri="/webwork" prefix="ww" %>
<%@ taglib uri="continuum" prefix="c1" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<html>
  <ww:i18n name="localization.Continuum">
    <head>
        <title><ww:text name="deleteRepository.page.title"/></title>
    </head>
    <body>
      <div id="axial" class="h3">
        <h3><ww:text name="deleteRepository.section.title"/></h3>
        <div class="axial">
        <ww:form action="removeRepository" method="post">
          <ww:hidden name="repository.id"/>
          <ww:hidden name="confirmed" value="true"/>
          <ww:actionerror/>

          <div class="warningmessage">
            <p>
              <strong>
                <ww:text name="deleteRepository.confirmation.message">
                  <ww:param><ww:property value="%{repository.name}"/></ww:param>
                </ww:text>
              </strong>
            </p>
          </div>

          <div class="functnbar3">
            <c1:submitcancel value="%{getText('delete')}" cancel="%{getText('cancel')}"/>
          </div>
        </ww:form>
        </div>
      </div>
    </body>
  </ww:i18n>
</html>
