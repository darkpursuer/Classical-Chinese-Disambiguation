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
<script src="/nlpccd/resources/js/tether.js" type="text/javascript"></script>
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
<link href="/nlpccd/resources/css/train.css" rel="stylesheet">
<link href="/nlpccd/resources/css/glyphicons.css" rel="stylesheet">

<title>File Management</title>
</head>
<body>

	<jsp:include page="header.jsp" flush="true">
		<jsp:param name="current" value="train" />
	</jsp:include>

	<div class="container">

		<div class="row row-offcanvas row-offcanvas-right">
			<div class="col-xs-12 col-sm-9">
				<div class="jumbotron">
					<h1>Training File Management</h1>
				</div>

				<div id="train_success_reminder" class="alert alert-success"
					role="alert" hidden="hidden">
					<strong>File has been stored to the corpus!</strong>
				</div>
				<div id="train_failed_reminder" class="alert alert-danger"
					role="alert" hidden="hidden">
					<strong>The upload was failed!</strong>
				</div>

				<div class="block">
					<h5>Download corpus</h5>
					<div class="input-group">
						<span class="input-group-btn">
							<button id="btn-download-corpus" class="btn btn-primary"
								type="button"
								onclick="window.open('/nlpccd/train/corpus/download')">Download</button>
						</span>
					</div>
				</div>

				<div class="block">
					<h5>
						Upload new file <a tabindex="0" role="button"
							data-toggle="popover" data-trigger="focus" class="tag-info"
							data-content="The uploaded file can only be stored in the corpus correctly when it has the same format as the download corpus file. For example: 'word%context%index%sense'."><span
							class="glyphicon glyphicon-info-sign"></span></a>
					</h5>
					<div class="input-group">
						<input type="file" id="file" name="file" class="form-control" />
						<span class="input-group-btn">
							<button class="btn btn-secondary btn-upload" type="button"
								onclick="ajaxFileUpload();">Upload</button>
						</span>
					</div>
				</div>

				<div class="block">
					<table class="table table-striped" id="files_table">
						<thead>
							<tr>
								<th>File</th>
								<th>Size</th>
								<th>Upload Date</th>
								<th></th>
							</tr>
						</thead>
						<tbody id="files_table_body">
						</tbody>
					</table>
				</div>

				<span id="files_model" hidden="hidden">${files}</span>
			</div>

			<jsp:include page="train_side.jsp">
				<jsp:param value="files" name="train_current" />
			</jsp:include>
		</div>
	</div>

	<jsp:include page="footer.jsp" flush="true" />

</body>
<script type="text/javascript">
	var data = $('#files_model').text();
	var model = JSON.parse(data);

	load();

	function load() {
		var body = $('#files_table_body');

		for (var i = 0; i < model.length; i++) {
			var entry = model[i];
			var tr = $('<tr></tr>').append(
					$('<td id="entry-filename-'+i+'"></td>').text(
							entry.fileName));
			tr.append($('<td></td>').text(entry.size));
			tr.append($('<td></td>').text(entry.uploadDate));
			tr
					.append($('<td></td>').append(
							create_dropdown(i, entry.fileName)));
			body.append(tr);
		}

		$('.files-import').click(function() {

			var id = this.id;
			var index = id.substring("files-import-".length);
			var fileName = $('#entry-filename-' + index).text();

			$.ajax({
				type : 'post',
				url : "/nlpccd/train/file/load",
				data : {
					fileName : fileName
				},
				success : function(data) {
					if (data) {
						$('#train_failed_reminder').attr("hidden", "hidden");
						$('#train_success_reminder').removeAttr("hidden");
					} else {
						$('#train_failed_reminder').removeAttr("hidden");
						$('#train_success_reminder').attr("hidden", "hidden");
					}
				}
			});
		});

		$('.files-delete').click(function() {

			var id = this.id;
			var index = id.substring("files-delete-".length);
			var fileName = $('#entry-filename-' + index).text();

			$.ajax({
				type : 'post',
				url : "/nlpccd/train/file/delete",
				data : {
					fileName : fileName
				},
				success : function(data) {
					location.reload();
				}
			});
		});
	}

	function create_dropdown(index, fileName) {
		var dropdown = $('<div class="btn-group" role="group"></div>');
		dropdown
				.append($('<button id="btnGroupDrop'+index+'" type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Actions</button>'));
		dropdown
				.append($(
						'<div class="dropdown-menu" aria-labelledby="btnGroupDrop'+index+'"></div>')
						.append(
								$('<a class="dropdown-item files-import" href="#" id="files-import-'+index+'">Import</a>'))
						.append(
								$('<a class="dropdown-item files-download" target="_blank" href="/nlpccd/train/file/download/'+fileName+'">Download</a>'))
						.append(
								$('<a class="dropdown-item files-delete" href="#" id="files-delete-'+index+'">Delete</a>')));

		return dropdown;
	}

	function ajaxFileUpload() {

		$.ajaxFileUpload({
			url : "/nlpccd/train/upload",
			secureuri : false,
			data : {},
			fileElementId : "file",
			dataType : "json",
			success : function(data) {
				location.reload();
			},
			error : function(data) {
				location.reload();
			}
		});
	}

	$('.tag-info').popover({
		trigger : 'focus'
	});
</script>
</html>