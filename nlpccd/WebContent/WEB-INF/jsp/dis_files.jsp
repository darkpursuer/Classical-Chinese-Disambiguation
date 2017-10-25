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
<link href="/nlpccd/resources/css/bootstrap.min.css" rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-theme.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-reboot.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/bootstrap-grid.min.css"
	rel="stylesheet">
<link href="/nlpccd/resources/css/index.css" rel="stylesheet">
<link href="/nlpccd/resources/css/glyphicons.css" rel="stylesheet">

<title>Test File Detail</title>
</head>
<body>

	<jsp:include page="header.jsp" flush="true">
		<jsp:param name="current" value="disambiguate" />
	</jsp:include>

	<div class="container">

		<div class="row row-offcanvas row-offcanvas-right">
			<div class="col-xs-12 col-sm-9">
				<div class="jumbotron">
					<h1>Test File Detail</h1>
				</div>
				<table class="table table-striped" id="testfile_details_table">
					<tbody id="testfile_details_table_body">
						<tr>
							<th>Date</th>
							<td id="td-detail-date"></td>
						</tr>
						<tr>
							<th>Count</th>
							<td id="td-detail-count"></td>
						</tr>
						<tr>
							<th>Status</th>
							<td id="td-detail-status"></td>
						</tr>
						<tr>
							<th>Progress</th>
							<td id="td-detail-progress"></td>
						</tr>
						<tr>
							<th>Result</th>
							<td id="td-detail-result"></td>
						</tr>
					</tbody>
				</table>

				<div id="train_success_reminder" class="alert alert-success"
					role="alert" hidden="hidden">
					<strong>Training has been started!</strong> Go to <a
						href="/nlpccd/train" target="_blank">Records</a> for tracking
					progress.
				</div>
				<div id="train_failed_reminder" class="alert alert-danger"
					role="alert" hidden="hidden">
					<strong>Error occurred when starting training! Maybe there
						is another running training.</strong>
				</div>

				<div id="retest_success_reminder" class="alert alert-success"
					role="alert" hidden="hidden">
					<strong>A new test has been started!</strong> Go to <a
						href="/nlpccd/dis/testfiles" target="_blank">Test Files</a> for
					tracking progress.
				</div>
				<div id="retest_failed_reminder" class="alert alert-danger"
					role="alert" hidden="hidden">
					<strong>Error occurred when starting testing!</strong>
				</div>

				<div class="input-group">
					<input id="word-input" type="text" class="form-control"
						placeholder="Search word...">

					<div class="input-group-btn">
						<button id="dropdown-current" type="button"
							class="btn btn-warning dropdown-toggle" data-toggle="dropdown"
							aria-haspopup="true" aria-expanded="false">Show All</button>
						<div class="dropdown-menu dropdown-menu-right">
							<a id="dropdown-all" class="dropdown-item" href="#">Show All</a>
							<div role="separator" class="dropdown-divider"></div>
							<a id="dropdown-errors" class="dropdown-item" href="#">Show
								Errors</a> <a id="dropdown-notagged" class="dropdown-item" href="#">Show
								No Tagged</a> <a id="dropdown-tested" class="dropdown-item" href="#">Show
								Tested</a> <a id="dropdown-notretrained" class="dropdown-item"
								href="#">Show Not Retrained</a>
						</div>
					</div>

					<button class="btn btn-success" id="word-search-btn" type="button">Search</button>
				</div>

				<div class="btn-group" role="group">
					<button type="button" class="btn btn-primary" id="btn-retrain">Retrain</button>

					<div class="btn-group" role="group">
						<button id="retrain-current" type="button"
							class="btn btn-secondary dropdown-toggle" data-toggle="dropdown"
							aria-haspopup="true" aria-expanded="false">all words
							that have real sense</button>
						<div class="dropdown-menu">
							<a class="dropdown-item" id="retrain-dropdown-realwords">all
								words that have real sense</a> <a class="dropdown-item"
								id="retrain-dropdown-taggedwords">selected tagged words</a> <a
								class="dropdown-item" id="retrain-dropdown-unisensewords">uni-sense
								words</a> <a class="dropdown-item"
								id="retrain-dropdown-confidentwords">confident words (preview)</a>
						</div>
					</div>
				</div>
				<div class="btn-group" role="group">
					<button type="button" class="btn btn-primary" id="btn-retest">Retest</button>
				</div>

				<div class="table-responsive table-entries">
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
							onclick="nextPage(true)" aria-label="Next"> <span
								aria-hidden="true">&raquo;</span> <span class="sr-only">Next</span>
						</a></li>
					</ul>
					</nav>

					<table class="table table-striped" id="entries_table">
						<thead>
							<tr>
								<th>Word</th>
								<th>Context</th>
								<th>Tested Sense</th>
								<th>Real Sense</th>
								<th>Confidence</th>
								<th>Tag <a tabindex="0" role="button" data-toggle="popover"
									data-trigger="focus" class="tag-info"
									data-content="You can tag the tested sense and then retrain."><span
										class="glyphicon glyphicon-info-sign"></span></a></th>
								<th><span class="input-group-addon"><input
										type="checkbox" id="entry-select-all"></input></span></th>
							</tr>
						</thead>
						<tbody id="entries_table_body">

						</tbody>
					</table>
					<button class="btn btn-secondary btn-nextpage" id="btn-next-page"
						onclick="nextPage(false)">>>> Next Page <<<</button>
				</div>
			</div>

			<div class="modal fade" id="dis-loading-modal" tabindex="-1"
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

			<span hidden="hidden" id="testfile_details">${details}</span>

			<jsp:include page="dis_side.jsp">
				<jsp:param value="testfiles" name="dis_current" />
			</jsp:include>
		</div>
	</div>

	<jsp:include page="footer.jsp" flush="true" />

</body>

<script type="text/javascript">
	var record_data = $('#testfile_details').text();
	var record_model = JSON.parse(record_data);
	var pageIndex = 1;
	var pageSize = 20;
	var dropdown_current = "all";

	loadDetails();
	load(true);

	function loadDetails() {
		$('#td-detail-date').text(record_model.date);
		$('#td-detail-count').text(record_model.entryNum);
		$('#td-detail-status').text(
				record_model.isCompleted ? "Completed" : "Running");
		$('#td-detail-progress').text(record_model.progress + "%");
		$('#td-detail-result').text(record_model.result);
	}

	function load(isClear) {
		var query = $("#word-input").val();

		var model_str = JSON.stringify({
			searchString : query,
			pageIndex : (pageIndex - 1),
			pageSize : pageSize,
			orderBy : dropdown_current
		});

		$('#dis-loading-modal').modal('show');

		$.ajax({
			type : 'post',
			url : "/nlpccd/dis/file/" + record_model.no + "/entries",
			data : {
				query : model_str
			},
			success : function(data) {
				show_result(data, isClear);
				setTimeout(function() {
					$('#dis-loading-modal').modal('hide');
				}, 1000);
			}
		});
	}

	function show_result(data, isClear) {
		var model = JSON.parse(data);

		var totalPages = parseInt(model.totalCount / pageSize) + 1;
		pageIndex = model.pageIndex + 1;

		if (pageIndex * pageSize >= model.totalCount) {
			$('#btn-next-page').attr("hidden", "hidden");
		} else {
			$('#btn-next-page').removeAttr("hidden");
		}

		$('#entry-select-all')[0].checked = false;

		$("#footer_index").text(totalPages);
		$("#current_index").val(pageIndex);

		var base = (pageIndex - 1) * pageSize;

		var body = $("#entries_table_body");

		if (isClear)
			body.empty();

		for (i = 0; i < model.data.length; i++) {
			var entry = model.data[i];
			var tr = $('<tr id="tr-'+entry.id+'"></tr>');

			var index = parseInt(entry.index);
			var pre = entry.context.substring(0, index);
			var suf = entry.context.substring(index + entry.word.length);

			var display = $('<span>' + pre + '<strong style="color: red">'
					+ entry.word + '</strong>' + suf + '</span>');

			tr
					.append($('<td></td>')
							.append(
									$(
											'<a target="_blank" href="/nlpccd/train/word/' + entry.word + '"></a>')
											.text(entry.word)));
			tr.append($('<td></td>').append(display));
			tr.append($('<td></td>').text(entry.testSense));
			tr.append($('<td></td>').text(entry.realSense));
			tr.append($('<td></td>').text(entry.confidence));

			var dropdown = generateEntryDropdown(entry.id);
			tr.append($('<td></td>').append(dropdown));

			var checkbox = $('<input type="checkbox" class="entry-select" id="entry-select-'+entry.id+'"></input>');
			tr.append($('<td></td>').append(
					$('<span class="input-group-addon"></span>').append(
							checkbox)));
			body.append(tr);

			if (entry.tag != "") {
				tr.addClass("table-warning");
				checkbox.attr('disabled', 'disabled');
				dropdown.children('button').attr('disabled', 'disabled');
				var type = "unsure";
				switch (entry.tag) {
					case "1" :
						type = "yes";
						break;
					case "2" :
						type = "no";
						break;
				}
				refresh_entry_dropdown(entry.id, type);
			}
		}

		function generateEntryDropdown(id) {

			var dropdown = $('<div class="input-group-btn entry-dropdown"></div>');
			dropdown
					.append($('<button id="entry-current-'+id+'" type="button" class="btn btn-secondary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"><span class="glyphicon glyphicon-minus"><span id="entry-current-indicator-'+id+'" hidden="hidden">unsure</span></button>'));

			var menu = $('<div class="dropdown-menu dropdown-menu-right"></div>');
			menu
					.append($('<a id="entry-dropdown-unsure-'+id+'" class="dropdown-item entry-dropdown-unsure"><span class="glyphicon glyphicon-minus"></a>'));
			menu
					.append($('<a id="entry-dropdown-yes-'+id+'" class="dropdown-item entry-dropdown-yes"><span class="glyphicon glyphicon-ok"></a>'));
			menu
					.append($('<a id="entry-dropdown-no-'+id+'" class="dropdown-item entry-dropdown-no"><span class="glyphicon glyphicon-remove"></a>'));

			dropdown.append(menu);
			return dropdown;
		}

		$('.entry-dropdown-unsure').click(function() {
			var id = this.id;
			var index = id.substring("entry-dropdown-unsure-".length);
			refresh_entry_dropdown(index, "unsure");
		});

		$('.entry-dropdown-yes').click(function() {
			var id = this.id;
			var index = id.substring("entry-dropdown-yes-".length);
			refresh_entry_dropdown(index, "yes");

			$('#entry-select-' + index)[0].checked = true;
		});

		$('.entry-dropdown-no').click(function() {
			var id = this.id;
			var index = id.substring("entry-dropdown-no-".length);
			refresh_entry_dropdown(index, "no");

			$('#entry-select-' + index)[0].checked = true;
		});

		function refresh_entry_dropdown(index, type) {
			var button = $('#entry-current-' + index);
			button.empty();

			var span = $('<span class="glyphicon glyphicon-minus">');
			switch (type) {
				case "yes" :
					span = $('<span class="glyphicon glyphicon-ok">');
					break;
				case "no" :
					span = $('<span class="glyphicon glyphicon-remove">');
					break;
			}

			var indicator = $('<span id="entry-current-indicator-'+index+'" hidden="hidden"></span>');
			indicator.text(type);

			button.append(span);
			button.append(indicator);
		}
	}

	$('#entry-select-all').click(function() {
		var isChecked = this.checked;
		$('.entry-select').each(function() {
			this.checked = isChecked;
		});
	});

	$("#word-search-btn").click(function() {
		pageIndex = 1;
		load(true);
	});

	$("#word-input").keypress(function(event) {
		var keyCode = event.which;
		if (keyCode == 13) {
			pageIndex = 1;
			load(true);
		}
	});

	$("#current_index").keypress(function(event) {
		var keyCode = event.which;
		if (keyCode == 46 || (keyCode >= 48 && keyCode <= 57))
			return true;
		else if (keyCode == 13) {
			pageIndex = parseInt($("#current_index").val());
			load(true);
		} else
			return false;
	}).focus(function() {
		this.style.imeMode = 'disabled';
	});

	function prevPage() {
		if (pageIndex > 1) {
			pageIndex--;
			load(true);
		}
	}

	function nextPage(isClear) {
		pageIndex++;
		load(isClear);
	}

	$(".specified_page").click(function() {
		pageIndex = parseInt(this.text);
		load(true);
	});

	$('#dropdown-all').click(function() {
		dropdown_current = "all";
		refresh_dropdown();
	});

	$('#dropdown-errors').click(function() {
		dropdown_current = "errors";
		refresh_dropdown();
	});

	$('#dropdown-notagged').click(function() {
		dropdown_current = "notagged";
		refresh_dropdown();
	});

	$('#dropdown-tested').click(function() {
		dropdown_current = "tested";
		refresh_dropdown();
	});

	$('#dropdown-notretrained').click(function() {
		dropdown_current = "notretrained";
		refresh_dropdown();
	});

	function refresh_dropdown() {
		var text = "Show All";
		switch (dropdown_current) {
			case "all" :
				text = "Show All";
				break;
			case "errors" :
				text = "Show Errors";
				break;
			case "notagged" :
				text = "Show No Tagged";
				break;
			case "tested" :
				text = "Show Tested";
				break;
			case "notretrained" :
				text = "Show Not Retrained";
				break;
		}
		$('#dropdown-current').text(text);
		pageIndex = 1;
		load(true);
	}

	$('#btn-retest').click(function() {
		$('#dis-loading-modal').modal('show');

		$.ajax({
			type : 'post',
			url : "/nlpccd/dis/file/" + record_model.no + "/retest",
			data : {},
			success : function(data) {
				if (data && data != "") {
					$('#retest_failed_reminder').attr("hidden", "hidden");
					$('#retest_success_reminder').removeAttr("hidden");
				} else {
					$('#retest_failed_reminder').removeAttr("hidden");
					$('#retest_success_reminder').attr("hidden", "hidden");
				}
				setTimeout(function() {
					$('#dis-loading-modal').modal('hide');
				}, 1000);
			}
		});
	});

	var retrain_dropdown_current = "real";

	$('#retrain-dropdown-realwords').click(function() {
		retrain_dropdown_current = "real";
		refresh_retrain_dropdown();
	});

	$('#retrain-dropdown-taggedwords').click(function() {
		retrain_dropdown_current = "tagged";
		refresh_retrain_dropdown();
	});

	$('#retrain-dropdown-unisensewords').click(function() {
		retrain_dropdown_current = "unisense";
		refresh_retrain_dropdown();
	});

	$('#retrain-dropdown-confidentwords').click(function() {
		retrain_dropdown_current = "confident";
		refresh_retrain_dropdown();
	});

	function refresh_retrain_dropdown() {
		var text = "all words that have real sense";
		switch (retrain_dropdown_current) {
			case "real" :
				text = "all words that have real sense";
				break;
			case "tagged" :
				text = "selected tagged words";
				break;
			case "unisense" :
				text = "uni-sense words";
				break;
			case "confident" :
				text = "confident words (preview)";
				break;
		}
		$('#retrain-current').text(text);
	}

	$('#btn-retrain')
			.click(
					function() {
						$('#dis-loading-modal').modal('show');

						var type = 0;
						switch (retrain_dropdown_current) {
							case "real" :
								type = 0;
								break;

							case "tagged" :
								type = 1;
								break;

							case "unisense" :
								type = 2;
								break;

							case "confident" :
								type = 3;
								break;
						}

						var param = "";
						if (type == 1) {
							var list = [];
							$('.entry-select')
									.each(
											function() {
												if (this.checked) {
													var id = this.id;
													var index = id
															.substring("entry-select-".length);
													var indicator = $(
															'#entry-current-indicator-'
																	+ index)
															.text();
													var tag = 0;
													switch (indicator) {
														case "unsure" :
															tag = 0;
															break;
														case "yes" :
															tag = 1;
															break;
														case "no" :
															tag = 2;
															break;
													}

													if (tag != 0) {
														list.push({
															id : index,
															tag : tag
														});
													}
												}
											});
							param = JSON.stringify(list);
						}

						$.ajax({
							type : 'post',
							url : "/nlpccd/dis/file/" + record_model.no
									+ "/retrain",
							data : {
								param : param,
								type : type
							},
							success : function(data) {
								if (data) {
									$('#train_failed_reminder').attr("hidden",
											"hidden");
									$('#train_success_reminder').removeAttr(
											"hidden");
								} else {
									$('#train_failed_reminder').removeAttr(
											"hidden");
									$('#train_success_reminder').attr("hidden",
											"hidden");
								}
								setTimeout(function() {
									$('#dis-loading-modal').modal('hide');
								}, 1000);
							}
						});
					});

	$('.tag-info').popover({
		trigger : 'focus'
	});
</script>
</html>