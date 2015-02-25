<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Specify Parameters</title>

  <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
  
  <!-- Load in JQuery UI javascript and css to set general look and feel -->
  <script src="/api/jquery-ui/jquery-ui.js"></script>
  <link rel="stylesheet" href="/api/jquery-ui/jquery-ui.css">
  
  <!-- Load in Select2 files so can create fancy route selector -->
  <link href="/api/select2/select2.css" rel="stylesheet"/>
  <script src="/api/select2/select2.min.js"></script>
  
  <link rel="stylesheet" href="/api/css/general.css">
  
  <!-- Load in general transitime javascript library -->
  <script src="/api/javascript/transitime.js"></script>
  
  <style>
  label {width: 200px; float: left; text-align: right; margin-top: 4px; margin-right: 10px;}
  .param {margin-top: 10px;}
  #route {width:300px;}
  #submit {margin-top: 40px; margin-left: 200px;}
  .note {font-size: small;}
  
  </style>
</head>
<body>

<%@include file="/template/header.jsp" %>

<div id="title">
   Select Parameters for Prediction Accuracy CSV Download
</div>
   
<div id="mainDiv">
<form action="predAccuracyCsv.jsp" method="POST">
   <%-- For passing agency param to the report --%>
   <input type="hidden" name="a" value="<%= request.getParameter("a")%>">
   
   <jsp:include page="params/fromToDateTime.jsp" />
   
   <jsp:include page="params/route.jsp" />
    
    <input id="submit" type="submit" value="Run Report" />
  </form>
</div>

</body>
</html>