<%--
    Document   : action
    Created on : 1 Giugno 2026
    Author     : Nicola De Nisco

    Rimuove i parametri di chiamata inserendoli nella cache in sessione.
    Restituisce una URL senza parametri per usi successivi.
--%>

<%@page language="java"
        contentType="application/json; charset=UTF-8"
        pageEncoding="UTF-8"
        trimDirectiveWhitespaces="true"%>

<jsp:useBean id="HideParamsJspBean" scope="session" class="org.sirio6.beans.HideParamsJsp" />
<jsp:setProperty name="HideParamsJspBean" property="*" />

<%
  HideParamsJspBean.runAction(request, response, config, out);
%>

