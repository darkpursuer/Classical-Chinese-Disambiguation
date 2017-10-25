<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%
	String train_current = request.getParameter("train_current");
%>

<div class="col-xs-6 col-sm-3 sidebar-offcanvas" id="sidebar">
	<div class="list-group">
		<a href="/nlpccd/train" class="list-group-item index">Training Records</a> 
			
		<a href="/nlpccd/train/corpus" class="list-group-item corpus">Corpus</a>
		
		<a href="/nlpccd/train/files" class="list-group-item files">File Management</a> 
	</div>
</div>
<!--/.sidebar-offcanvas-->

<script type="text/javascript">
	$('.<%=train_current%>').addClass('active');
</script>