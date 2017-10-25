<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%
	String train_current = request.getParameter("dis_current");
%>

<div class="col-xs-6 col-sm-3 sidebar-offcanvas" id="sidebar">
	<div class="list-group">
		<a href="/nlpccd" class="list-group-item disambiguate">Disambiguate</a> 
			
		<a href="/nlpccd/dis/testfiles" class="list-group-item testfiles">Test Files</a>
	</div>
</div>
<!--/.sidebar-offcanvas-->

<script type="text/javascript">
	$('.<%=train_current%>').addClass('active');
</script>