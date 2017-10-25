<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport"
	content="width=device-width, initial-scale=1, shrink-to-fit=no">
<meta name="author" content="Yi Zhang">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Type"
	content="application/json; charset=UTF-8">

<link rel="icon" href="/nlpccd/resources/images/favicon.ico">

<script src="/nlpccd/resources/js/jquery-3.2.1.min.js"
	type="text/javascript"></script>
<script src="/nlpccd/resources/js/bootstrap.min.js"
	type="text/javascript"></script>
<link href="/nlpccd/resources/css/bootstrap.min.css" rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-theme.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-reboot.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-grid.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/train.css" rel="stylesheet">

<title>Training Corpus</title>
</head>
<body>

	<jsp:include page="header.jsp" flush="true">
		<jsp:param name="current" value="train" />
	</jsp:include>

	<div class="container">

		<div class="row row-offcanvas row-offcanvas-right">
			<div class="col-xs-12 col-sm-9">
				<div class="jumbotron">
					<h1>Training Corpus</h1>
				</div>

				<div class="input-group">
					<input id="word-input" type="text" class="form-control"
						placeholder="Search word..."> <span
						class="input-group-btn">
						<button class="btn btn-success" id="word-search-btn" type="button">Search</button>
					</span>
				</div>
				<div class="table-responsive table-words">
					<table class="table table-striped" id="result_table">
						<thead>
							<tr>
								<th>#</th>
								<th>Word</th>
								<th>Sense Count</th>
								<th>Context Count</th>
							</tr>
						</thead>
						<tbody id="result_table_body">

						</tbody>
					</table>

					<nav class="page_index">
					<ul class="pagination">
						<li class="page-item"><a class="page-link"
							onclick="prevPage()" aria-label="Previous"> <span
								aria-hidden="true">&laquo;</span> <span class="sr-only">Previous</span>
						</a></li>
						<li class="page-item"><a class="page-link specified_page"
							id="header_index">1</a></li>
						<li class="page-item"><input type="text"
							class="input_page_index" id="current_index"></input></li>
						<li class="page-item"><a class="page-link specified_page"
							id="footer_index">1</a></li>
						<li class="page-item"><a class="page-link"
							onclick="nextPage()" aria-label="Next"> <span
								aria-hidden="true">&raquo;</span> <span class="sr-only">Next</span>
						</a></li>
					</ul>
					</nav>
				</div>
			</div>

			<div class="modal fade" id="loading-modal" tabindex="-1"
				role="dialog" aria-labelledby="mergeConfirm" aria-hidden="true">
				<div class="modal-dialog" role="document">
					<div class="modal-content">
						<div class="modal-body">
							<div class="form-group">
								<label class="form-control-label">Loading...</label>
							</div>
						</div>
					</div>
				</div>
			</div>

			<jsp:include page="train_side.jsp">
				<jsp:param value="corpus" name="train_current" />
			</jsp:include>
		</div>
	</div>

	<jsp:include page="footer.jsp" flush="true" />

</body>

<script type="text/javascript">
	var pageIndex = 1;
	var pageSize = 20;

	load();

	function load() {
		var query = $("#word-input").val();

		var model_str = JSON.stringify({
			searchString : query,
			pageIndex : (pageIndex - 1),
			pageSize : pageSize
		});

		$('#loading-modal').modal('show');

		$.ajax({
			type : 'post',
			url : "/nlpccd/train/getwords",
			data : {
				query : model_str
			},
			success : function(data) {
				show_result(data);
				setTimeout(function() {
					$('#loading-modal').modal('hide');
				}, 500);
			}
		});
	}

	function show_result(data) {
		var model = JSON.parse(data);

		var totalPages = parseInt(model.totalCount / pageSize) + 1;
		pageIndex = model.pageIndex + 1;

		$("#footer_index").text(totalPages);
		$("#current_index").val(pageIndex);

		var base = (pageIndex - 1) * pageSize;

		var body = $("#result_table_body");
		body.empty();
		for (i = 0; i < model.data.length; i++) {
			var entry = model.data[i];
			var tr = $('<tr></tr>');
			tr.append($('<td></td>').text(entry.index + 1 + base));
			tr
					.append($('<td></td>')
							.append(
									$(
											'<a target="_blank" href="/nlpccd/train/word/' + entry.word + '"></a>')
											.text(entry.word)));
			tr.append($('<td></td>').text(entry.senseCount));
			tr.append($('<td></td>').text(entry.contextCount));
			body.append(tr);
		}
	}

	$("#word-search-btn").click(function() {
		pageIndex = 1;
		load();
	});

	$("#word-input").keypress(function(event) {
		var keyCode = event.which;
		if (keyCode == 13) {
			pageIndex = 1;
			load();
		}
	});

	$("#current_index").keypress(function(event) {
		var keyCode = event.which;
		if (keyCode == 46 || (keyCode >= 48 && keyCode <= 57))
			return true;
		else if (keyCode == 13) {
			pageIndex = parseInt($("#current_index").val());
			load();
		} else
			return false;
	}).focus(function() {
		this.style.imeMode = 'disabled';
	});

	function prevPage() {
		if (pageIndex > 1) {
			pageIndex--;
			load();
		}
	}

	function nextPage() {
		pageIndex++;
		load();
	}

	$(".specified_page").click(function() {
		pageIndex = parseInt(this.text);
		load();
	});
</script>
</html>