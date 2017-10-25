<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<style type="text/css">
body {
	/*padding-top: 20px;*/
	padding-bottom: 20px;
}

.navbar {
	margin-bottom: 20px;
}
</style>

<%
	String current = request.getParameter("current");
%>

<nav class="navbar navbar-inverse bg-inverse navbar-toggleable-md">
	<div class="container">
		<button class="navbar-toggler navbar-toggler-right" type="button"
			data-toggle="collapse" data-target="#navbarsExampleContainer"
			aria-controls="navbarsExampleContainer" aria-expanded="false"
			aria-label="Toggle navigation">
			<span class="navbar-toggler-icon"></span>
		</button>
		<a class="navbar-brand" href="/nlpccd">CLASSICAL CHINESE
			DISAMBIGUATION</a>

		<div class="collapse navbar-collapse" id="navbarsExampleContainer">
			<ul class="navbar-nav mr-auto">
				<li class="nav-item disambiguate"><a class="nav-link"
					href="/nlpccd">Disambiguate </a></li>
				<li class="nav-item train"><a class="nav-link"
					href="/nlpccd/train">Train</a></li>
				<li class="nav-item optmz"><a class="nav-link"
					href="/nlpccd/optmz">Optimization</a></li>
				<li class="nav-item configs"><a class="nav-link"
					href="/nlpccd/configs">Configurations</a></li>
			</ul>
		</div>
	</div>
</nav>

<script type="text/javascript">
	$('.<%=current%>').addClass('active');
</script>