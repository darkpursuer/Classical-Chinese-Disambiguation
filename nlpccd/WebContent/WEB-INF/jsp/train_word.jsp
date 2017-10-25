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
<link href="/nlpccd/resources/css/bootstrap.min.css" rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-theme.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-reboot.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-grid.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/train.css" rel="stylesheet">

<title>Word - ${word}</title>
</head>
<body>

	<jsp:include page="header.jsp" flush="true">
		<jsp:param name="current" value="train" />
	</jsp:include>

	<div class="container">

		<div class="row row-offcanvas row-offcanvas-right">
			<div class="col-xs-12 col-sm-9">
				<div class="jumbotron">
					<h1 id="word_header">${word}</h1>
				</div>

				<div id="train_success_reminder" class="alert alert-success"
					role="alert" hidden="hidden">
					<strong>Training has been started!</strong> Go to <a href="/nlpccd/train" target="_blank">Records</a> for tracking progress.
				</div>
				<div id="train_failed_reminder" class="alert alert-danger"
					role="alert" hidden="hidden">
					<strong>Error occurred when starting training! Maybe there is another running training.</strong>
				</div>

				<nav class="navbar navbar-toggleable-md navbar-light bg-faded"
					id="tool-bar">
				<div class="collapse navbar-collapse" id="navbarNav">
					<ul class="navbar-nav mr-auto">
						<li class="nav-item active"><button type="button"
								class="btn btn-secondary" id="collapse-all">Collapse
								All</button></li>
						<li class="nav-item"><button type="button"
								class="btn btn-secondary" id="expand-all">Expand All</button></li>
					</ul>
					<button type="button" class="btn btn-primary" id="btn-word-train">Train</button>
					<button type="button" class="btn btn-primary disabled"
						id="btn-sense-merge">Merge</button>
				</div>
				</nav>

				<div id="accordion" role="tablist" aria-multiselectable="true">
				</div>

				<div class="modal fade" id="merge-confirm" tabindex="-1"
					role="dialog" aria-labelledby="mergeConfirm" aria-hidden="true">
					<div class="modal-dialog" role="document">
						<div class="modal-content">
							<div class="modal-header">
								<h5 class="modal-title" id="mergeConfirm">Merge Senses</h5>
								<button type="button" class="close" data-dismiss="modal"
									aria-label="Close">
									<span aria-hidden="true">&times;</span>
								</button>
							</div>
							<div class="modal-body">
								<div class="form-group">
									<label class="form-control-label">Are you sure to merge
										these following senses?</label>
									<p id="merged-sense-display"></p>
								</div>
								<div class="form-group">
									<label class="form-control-label">The target sense:</label> <input
										type="text" class="form-control" id="target-sense">
								</div>
							</div>
							<div class="modal-footer">
								<button type="button" class="btn btn-secondary"
									data-dismiss="modal">Cancel</button>
								<button type="button" class="btn btn-primary"
									id="btn-start-merge">Merge!</button>
							</div>
						</div>
					</div>
				</div>

				<span id="word_model" hidden="hidden">${model}</span>
			</div>

			<jsp:include page="train_side.jsp">
				<jsp:param value="corpus" name="train_current" />
			</jsp:include>
		</div>
	</div>

	<jsp:include page="footer.jsp" flush="true" />

</body>

<script type="text/javascript">
	var checked = 0;
	var data = $('#word_model').text();
	var model = JSON.parse(data);

	load();

	$("h5.mb-0").click(function() {
		var id = this.id;
		var index = id.substring("header-".length);
		$('#collapse-' + index).toggle();
	});

	$('#collapse-all').click(function() {
		$('.collapse').hide();
	});

	$('#expand-all').click(function() {
		$('.collapse').show();
	});

	$('.check-span').click(function() {
		var id = this.id;
		var index = parseInt(id.substring("check-span-".length));

		var isChecked = $('#checkbox-' + index).attr("checked");
		$('#checkbox-' + index).attr("checked", !isChecked);
		refreshCheck(index, !isChecked);
	});

	$('.sense-check').click(function() {
		var id = this.id;
		var index = parseInt(id.substring("checkbox-".length));

		refreshCheck(index, this.checked);
	});

	function refreshCheck(index, isChecked) {
		if (isChecked) {
			checked += index + 1;
		} else {
			checked -= (index + 1);
		}

		if (checked == 0) {
			$('#btn-sense-merge').addClass("disabled");
		} else {
			$('#btn-sense-merge').removeClass("disabled");
		}
	}

	$('#btn-word-train').click(function() {
		var word = model.word;

		var words = [];
		words.push(word);
		
		$.ajax({
			type : 'post',
			url : "/nlpccd/train/words",
			data : {
				words: JSON.stringify(words)
			},
			success : function(data) {
				if (data && data != "null") {
					$('#train_failed_reminder').attr("hidden", "hidden");
					$('#train_success_reminder').removeAttr("hidden");
				} else {
					$('#train_failed_reminder').removeAttr("hidden");
					$('#train_success_reminder').attr("hidden", "hidden");
				}
			}
		});
	});

	$('#btn-sense-merge').click(function() {
		var senses = "";

		if (!checked) {
			return;
		}

		var checks = getChecked();

		var sense = model.senses[checks[0]];
		var maxLength = sense.length;
		var maxSense = sense;

		senses = sense;
		for (k = 1; k < checks.length; k++) {
			sense = model.senses[checks[k]];

			if (sense.length > maxLength) {
				maxLength = sense.length;
				maxSense = sense;
			}

			senses += ", " + sense;
		}

		$('#merged-sense-display').text(senses);
		$('#target-sense').val(maxSense);

		$('#merge-confirm').modal('show');
	});

	function getChecked() {
		var checks = [];
		$('.sense-check').each(function(index, object) {
			if (object.checked) {
				var id = object.id;
				var index = parseInt(id.substring("checkbox-".length));
				checks.push(index);
			}
		});
		return checks;
	}

	function load() {
		var len = model.senses.length;
		for (i = 0; i < len; i++) {
			var card = createCard(i, model.senses[i]);

			var table = createTable(model, model.senses[i]);
			card.children('.collapse').children('div').append(table);
			$('#accordion').append(card);
		}
	}

	function createCard(index, sense) {
		return $('<div class="card"></div>')
				.append(
						$(
								'<div class="card-header input-group" role="tab"></div>')
								.append(
										$(
												'<h5 class="mb-0 mr-auto" id="header-'+index+'"></h5>')
												.append(
														$(
																'<a class="collapsed collapse-header" data-toggle="collapse" data-parent="#accordion" aria-expanded="true"></a>')
																.text(
																		(index + 1)
																				+ ". "
																				+ sense)))
								.append(
										$('<span class="input-group-addon check-span" id="check-span-'+index+'"><input type="checkbox" class="sense-check" id="checkbox-'+index+'"></span>')))
				.append(
						$(
								'<div id="collapse-'+index+'" class="collapse" role="tabpanel"></div>')
								.append($('<div class="card-block"></div>')));
	}

	function createTable(model, sense) {
		var table = $('<table class="table table-striped"></table>').append(
				$('<thead></thead>').append(
						$('<tr></tr>').append($('<td></td>').text('#')).append(
								$('<td></td>').text('Context'))));
		var body = $('<tbody></tbody>');
		table.append(body);
		var len = model.contexts[sense].length;
		for (j = 0; j < len; j++) {
			var context = (model.contexts[sense])[j];
			var no = (model.nos[sense])[j];
			var index = parseInt((model.indexes[sense])[j]);

			var pre = context.substring(0, index);
			var suf = context.substring(index + model.word.length);

			var display = $('<span>' + pre + '<strong class="found-word">'
					+ model.word + '</strong>' + suf + '</span>');
			body.append($('<tr></tr>').append(
					$('<th scope="row" style="width: 10%"></th>').text(no))
					.append($('<td></td>').append(display)));
		}

		return table;
	}

	$('#btn-start-merge').click(function() {
		if (!checked) {
			return;
		}

		var checkIndexes = getChecked();
		var checks = [];
		for (k = 0; k < checkIndexes.length; k++) {
			checks.push(model.senses[checkIndexes[k]]);
		}

		var targetSense = $('#target-sense').val();

		var model_str = JSON.stringify({
			word : model.word,
			senses : checks,
			targetSense : targetSense
		});

		$.ajax({
			type : 'post',
			url : "/nlpccd/train/mergesenses",
			data : {
				data : model_str
			},
			success : function(data) {
				location.reload();
			}
		});
	});
</script>
</html>