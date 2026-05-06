<%--
    Document   : screen
    Created on : 11 Novembre 2022
    Author     : Nicola De Nisco

    Questa JSP legge un parametro 'screen' dalla request e istanzia la
    corrispettiva screen richiedendo l'elaborazione.
    L'html elaborato viene inviato come risultato.
    NOTA: il layout viene ignorato; qui viene riportato solo e inicamente il contenuto dello screen.
    Permette di eseguire una screen per popolare una parte di pagina.
--%>

<%@page language="java"
        contentType="application/json; charset=UTF-8"
        pageEncoding="UTF-8"
        trimDirectiveWhitespaces="true"%>

<jsp:useBean id="actionBean" scope="session" class="org.sirio6.beans.ActionJspBean" />
<jsp:setProperty name="actionBean" property="*" />

<%
  actionBean.runScreenHtml(request, response, config, out);
%>
