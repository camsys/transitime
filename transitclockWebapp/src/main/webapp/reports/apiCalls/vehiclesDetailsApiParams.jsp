<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@include file="/template/includes.jsp" %>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Specify Parameters</title>

  <!-- Load in Select2 files so can create fancy route selector -->
  <link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/css/select2.min.css" rel="stylesheet" />
  <script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0/js/select2.min.js"></script>
  
  <link href="../params/reportParams.css" rel="stylesheet"/>

  <script>
    function execute() {
      var selectedRouteId = $("#route").val();
      var format = $('input:radio[name=format]:checked').val();
  	  var key = $("#apiKey").val().trim();

      var isValid = true;

      // Validate key
      if (!key) {
          $("#apiKey").addClass('is-invalid');
          isValid = false;
      } else {
          $("#apiKey").removeClass('is-invalid');
      }

      if(!isValid){
          return;
      }

      var url = apiUrlKeyPrefix + key + apiUrlAgencyPrefix + "/command/vehiclesDetails?r=" + selectedRouteId + "&format=" + format;

   	  // Actually do the API call
   	  location.href = url;
    }
  </script>
  
</head>
<body>

<%@include file="/template/header.jsp" %>

<div id="title">
   Select Parameters for Vehicles Details API 
</div>
   
<div id="mainDiv">
    <%-- API Key Input --%>
    <jsp:include page="../params/apiKeyInput.jsp" />

   <%-- Create route selector --%>
   <jsp:include page="../params/routeAllOrSingle.jsp" />
   
   <%-- Create json/xml format radio buttons --%>
   <jsp:include page="../params/jsonXmlFormat.jsp" />
   
   <%-- Create submit button --%> 
   <jsp:include page="../params/submitApiCall.jsp" />
</div>

</body>
</html>