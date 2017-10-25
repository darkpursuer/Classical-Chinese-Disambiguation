<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport"
	content="width=device-width, initial-scale=1, shrink-to-fit=no">
<meta name="author" content="Yi Zhang">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<link rel="icon" href="/nlpccd/resources/images/favicon.ico">

<script src="/nlpccd/resources/js/jquery-3.2.1.min.js"
	type="text/javascript"></script>
<script src="/nlpccd/resources/js/bootstrap.min.js"
	type="text/javascript"></script>
<script src="/nlpccd/resources/js/ajaxfileupload.js"
	type="text/javascript"></script>
<link href="/nlpccd/resources/css/bootstrap.min.css" rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-theme.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-reboot.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-grid.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/index.css" rel="stylesheet">

<title>Test Files</title>
</head>
<body>

	<jsp:include page="header.jsp" flush="true">
		<jsp:param name="current" value="disambiguate" />
	</jsp:include>

	<div class="container">

		<div class="row row-offcanvas row-offcanvas-right">
			<div class="col-xs-12 col-sm-9">
				<div class="jumbotron">
					<h1>Test Files</h1>
				</div>

				<div id="success_reminder" class="alert alert-success" role="alert"
					hidden="hidden">
					<strong>Testing has been started!</strong>
				</div>
				<div id="failed_reminder" class="alert alert-danger" role="alert"
					hidden="hidden">
					<strong>Error occurred when starting testing!</strong>
				</div>

				<div>
					<div>
						<h3>Upload test file: </h3>
					</div>
					<div class="input-group">
						<input type="file" id="file" name="file" class="form-control" />
						<span class="input-group-btn">
							<button class="btn btn-primary btn-upload-test" type="button"
								onclick="ajaxFileUpload();">Upload & Test</button>
						</span>
					</div>
				</div>

				<div class="table-test-records">
					<h3>Test Records</h3>
					<table class="table table-striped" id="records-table">
						<thead>
							<tr>
								<th width="30%">Date</th>
								<th width="10%">Count</th>
								<th width="15%">Status</th>
								<th width="25%">Progress</th>
								<th width="20%"></th>
							</tr>
						</thead>
						<tbody id="records-table-body">

						</tbody>
					</table>
				</div>

			</div>

			<span hidden="hidden" id="test_records">${records}</span>

			<jsp:include page="dis_side.jsp">
				<jsp:param value="testfiles" name="dis_current" />
			</jsp:include>
		</div>
	</div>

	<jsp:include page="footer.jsp" flush="true" />

</body>

<script type="text/javascript">
	var record_data = $('#test_records').text();
	var record_model = JSON.parse(record_data);

	load();

	function load() {
		var body = $('#records-table-body');

		for (var i = 0; i < record_model.length; i++) {
			var entry = record_model[i];

			var status = "";
			var progress = $('<div></div>');
			if (entry.isCompleted) {
				status = "Completed";
			} else {
				status = "Running";
				progress = $(
						'<div class="progress" id="bar-'+entry.no+'"></div>')
						.append(
								$('<div id="progress-'
										+ entry.no
										+ '" class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" style="width: '
										+ entry.progress + '%">'
										+ entry.progress + '%</div>'));
			}

			var tr = $('<tr id="tr-'+entry.no+'"></tr>');
			tr.append($('<td></td>').text(entry.date));
			tr.append($('<td></td>').text(entry.entryNum));
			tr.append($('<td id="td-status-'+entry.no+'"></td>').text(status));
			tr.append($('<td id="td-progress-'+entry.no+'"></td>').append(
					progress));
			tr.append($('<td></td>').append(create_dropdown(entry.no)));
			body.append(tr);

			if (!entry.isCompleted) {
				startTrack(entry.no);
			}
		}

		$('.file-delete').click(function() {
			var id = this.id;
			var index = id.substring("delete-test-file-".length);
			$.ajax({
				type : 'post',
				url : "/nlpccd/dis/testfile/delete/" + index,
				data : {},
				success : function(data) {
					location.reload();
				}
			});
		});
	}

	function ajaxFileUpload() {

		$.ajaxFileUpload({
			url : "/nlpccd/dis/uploadtestfile",
			secureuri : false,
			data : {},
			fileElementId : "file",
			dataType : "json",
			success : function(data) {
				handleResult(data);
			},
			error : function(data) {
				handleResult(data);
			}
		});

		function handleResult(data) {
			if (!data || data.responseText == "") {
				$('#failed_reminder').removeAttr("hidden");
				$('#success_reminder').attr("hidden", "hidden");
			} else {
				$('#failed_reminder').attr("hidden", "hidden");
				$('#success_reminder').removeAttr("hidden");
			}
			setTimeout(function() {
				location.reload();
			}, 2000);
		}
	}

	function startTrack(no) {
		var isCompleted = false;
		get_progress();

		function get_progress() {
			$.ajax({
				type : 'post',
				url : "/nlpccd/dis/test/progress/" + no,
				data : {
					no : no
				},
				success : function(data) {
					var model = JSON.parse(data);
					if (model.isCompleted) {
						complete(model);
					} else {
						set_progress(model.progress);
					}
					if (!isCompleted) {
						setTimeout(get_progress, 5000);
					}
				}
			});
		}

		function set_progress(num) {
			$('#progress-' + no).text(num + "%");
			$('#progress-' + no).css('width', num + "%");
		}

		function complete(model) {
			isCompleted = true;
			var parent = $('#td-progress-' + no);
			parent.empty();
			$('#td-status-' + no).text("Completed");
		}
	}

	function create_dropdown(index) {
		var dropdown = $('<div class="btn-group" role="group"></div>');
		dropdown
				.append($('<button id="btnGroupDrop'+index+'" type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Actions</button>'));
		dropdown
				.append($(
						'<div class="dropdown-menu" aria-labelledby="btnGroupDrop'+index+'"></div>')
						.append(
								$('<a class="dropdown-item file-detail" target="_blank" href="/nlpccd/dis/file/'+index+'" id="test-file-'+index+'">Details</a>'))
						.append(
								$('<a class="dropdown-item file-delete" href="#" id="delete-test-file-'+index+'">Delete</a>')));

		return dropdown;
	}
</script>
</html>