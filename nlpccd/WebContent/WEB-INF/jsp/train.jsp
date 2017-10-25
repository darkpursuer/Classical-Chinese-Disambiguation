<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport"
	content="width=device-width, initial-scale=1, shrink-to-fit=no">
<meta name="author" content="Yi Zhang">
<link rel="icon" href="/nlpccd/resources/images/favicon.ico">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
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

<title>Training Records</title>
</head>
<body>

	<jsp:include page="header.jsp" flush="true">
		<jsp:param name="current" value="train" />
	</jsp:include>

	<div class="container">

		<div class="row row-offcanvas row-offcanvas-right">

			<div class="col-xs-12 col-sm-9">
				<div class="jumbotron">
					<h1>Training Records</h1>
				</div>
				<div>
					<div id="train_success_reminder" class="alert alert-success"
						role="alert" hidden="hidden">
						<strong>Training has been completed!</strong>
					</div>
					<div id="train_failed_reminder" class="alert alert-danger"
						role="alert" hidden="hidden">
						<strong>Error occurred when training!</strong>
					</div>

					<div class="progress" id="train-progress-bar" hidden="hidden">
						<div id="train-progress"
							class="progress-bar progress-bar-striped progress-bar-animated"
							role="progressbar" style="width: 0%">0%</div>
					</div>
					<button id="btn-start-train" type="button"
						class="btn btn-primary btn-lg btn-block" hidden="hidden">Start
						All Training!</button>
				</div>
				<div class="table-responsive table-train-records">
					<h3>Training Records</h3>
					<table class="table table-striped" id="records-table">
						<thead>
							<tr>
								<th>Date</th>
								<th>Status</th>
								<th>Scope</th>
								<th>Message</th>
							</tr>
						</thead>
						<tbody id="records-table-body">

						</tbody>
					</table>
				</div>
			</div>

			<span hidden="hidden" id="train_records">${records}</span>

			<jsp:include page="train_side.jsp">
				<jsp:param value="index" name="train_current" />
			</jsp:include>
		</div>
		<!--/row-->
	</div>

	<jsp:include page="footer.jsp" flush="true" />

</body>
<script type="text/javascript">
	var progress_no;
	var isCompleted = true;
	var record_data = $('#train_records').text();
	var record_model = JSON.parse(record_data);

	load();

	function load() {
		$.ajax({
			type : 'post',
			url : "/nlpccd/train/progress/current",
			data : {},
			success : function(data) {
				if (data && data != "null") {
					var model = JSON.parse(data);
					progress_no = model.no;
					start();
				} else {
					$('#btn-start-train').removeAttr("hidden");
				}
			}
		});

		var body = $('#records-table-body');

		for (var i = 0; i < record_model.length; i++) {
			var entry = record_model[i];

			var status = "";
			switch (entry.status) {
			case 1:
				status = "Running";
				break;
			case 2:
				status = "Completed";
				break;
			case 3:
				status = "Failed";
				break;
			}

			var scopes = entry.scope;
			var scope = "All";
			if (scopes) {
				scope = scopes[0];
				for (var k = 1; k < scopes.length; k++) {
					scope += ", " + scopes[k];
				}
			}

			var message = entry.error;
			if (message == "null") {
				message = "";
			}

			var tr = $('<tr></tr>').append($('<td></td>').text(entry.date));
			tr.append($('<td></td>').text(status));
			tr.append($('<td></td>').text(scope));
			tr.append($('<td></td>').text(message));
			body.append(tr);
		}
	}

	$('#btn-start-train').click(function() {

		$.ajax({
			type : 'post',
			url : "/nlpccd/train",
			data : {},
			success : function(data) {
				progress_no = data;
				start();
			}
		});
	});

	function start() {
		$('#btn-start-train').attr("hidden", "hidden");
		$('#train-progress-bar').removeAttr("hidden");
		isCompleted = false;
		get_progress();
	}

	function get_progress() {
		$.ajax({
			type : 'post',
			url : "/nlpccd/train/progress",
			data : {
				no : progress_no
			},
			success : function(data) {
				var model = JSON.parse(data);
				if (model.status == 1) {
					set_progress(model.progress);
				} else {
					complete(model);
				}
			}
		});
		if (!isCompleted) {
			setTimeout(get_progress, 5000);
		}
	}

	function set_progress(num) {
		$('#train-progress').text(num + "%");
		$('#train-progress').css('width', num + "%");
	}

	function complete(model) {
		isCompleted = true;
		$('#train-progress-bar').attr("hidden", "hidden");
		$('#btn-start-train').removeAttr("hidden");
		$('#train-result').removeAttr("hidden");
		if (model.status == 2) {
			$('#train_failed_reminder').attr("hidden", "hidden");
			$('#train_success_reminder').removeAttr("hidden");
		} else if (model.status = 3) {
			$('#train_failed_reminder').removeAttr("hidden");
			$('#train_success_reminder').attr("hidden", "hidden");

			$('#train_failed_reminder').empty();
			$('#train_failed_reminder').append(
					$('<strong>Error occurred when training! Reason: '
							+ model.error + '</strong>'));
		}
		setTimeout(function() {
			location.reload();
		}, 2000);
	}
</script>
</html>