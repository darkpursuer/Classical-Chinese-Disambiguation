<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, shrink-to-fit=no">
<meta name="author" content="Yi Zhang">
<link rel="icon" href="resources/images/favicon.ico">

<title>Classical Chinese Disambiguation</title>
<script src="resources/js/jquery-3.2.1.min.js" type="text/javascript"></script>
<script src="resources/js/bootstrap.min.js" type="text/javascript"></script>
<link href="resources/css/bootstrap.min.css" rel="stylesheet">
<link href="resources/css/bootstrap-theme.min.css" rel="stylesheet">
<link href="resources/css/bootstrap-reboot.min.css" rel="stylesheet">
<link href="resources/css/bootstrap-grid.min.css" rel="stylesheet">
<link href="resources/css/index.css" rel="stylesheet">
</head>
<body>

	<jsp:include page="header.jsp" flush="true">
		<jsp:param name="current" value="disambiguate" />
	</jsp:include>

	<div class="container">
		<div class="row row-offcanvas row-offcanvas-right">
			<div class="col-xs-12 col-sm-9">
				<div class="jumbotron">
					<div class="col-sm-8 mx-auto">
						<h1>Start Disambiguation</h1>

						<div class="row context-input">
							<div class="col-lg-12">
								<div class="input-group">
									<input id="context-input" type="text" class="form-control"
										placeholder="Input Context..."> <span
										class="input-group-btn">
										<button class="btn btn-success" id="context-btn" type="button">Next</button>
									</span>
								</div>
							</div>
						</div>

						<div class="context-select display-none">
							<h5>Select the key word that you want to disambiguate:</h5>
							<div class="input-group">
								<div class="btn-toolbar">
									<div class="btn-group mr-2 btn-group-wrap"></div>
								</div>
								<span class="input-group-btn word-select">
									<button class="btn btn-success" id="word-btn" type="button">Next</button>
								</span>
							</div>
							<div class="alert alert-danger select-alert display-none"
								role="alert">
								<strong>Oh snap!</strong> Please select a word to continue.
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

						<div class="disamb-result display-none">
							<h5>Disambiguation Result:</h5>
							<table class="table table-hover">
								<tr>
									<th>Word</th>
									<td id='disamb-result-word'></td>
								</tr>
								<tr>
									<th>Context</th>
									<td id='disamb-result-context'></td>
								</tr>
								<tr>
									<th>Sense</th>
									<td id='disamb-result-senses'></td>
								</tr>
							</table>
						</div>
					</div>
				</div>
			</div>

			<jsp:include page="dis_side.jsp">
				<jsp:param value="disambiguate" name="dis_current" />
			</jsp:include>
		</div>
	</div>

	<jsp:include page="footer.jsp" flush="true" />
</body>

<script type="text/javascript">
	var selected_context = undefined;
	var selected_word = undefined;

	$('#context-btn').click(function(data) {
		clear_context();

		var context = $("#context-input").val();
		if (context) {
			selected_context = context;
			generate_btns(context);
			show($('.context-select'));
		}
	});

	function generate_btns(context) {
		var btn_groups = $('.btn-group-wrap');
		for (i = 0; i < context.length; i++) {
			var c = context.charAt(i);

			var btn = $("<button type='button'></button>").text(c);
			btn.addClass("btn");
			btn.addClass("btn-secondary");
			btn.addClass("btn-word-select");
			btn.attr('id', "btn-word-select-" + i);

			btn_groups.append(btn);
		}
		bind_all();
	}

	function clear_context() {
		$('.btn-group-wrap').empty();
		selected_word = undefined;
		selected_context = undefined;
	}

	function bind_all() {
		$('.btn-word-select').click(function() {
			var selected_id = this.id.replace("btn-word-select-", "");
			if (selected_word != selected_id) {
				if (selected_word) {
					var prev_selected = $('#btn-word-select-' + selected_word);
					prev_selected.removeClass("btn-primary");
					prev_selected.addClass("btn-secondary");
				}

				selected_word = selected_id
				var btn = $('#' + this.id);
				btn.removeClass("btn-secondary");
				btn.addClass("btn-primary");
			}
		});
	}

	$('#word-btn').click(function(data) {
		if (selected_word) {
			$('#loading-modal').modal('show');
			hide($('.select-alert'));
			hide($('.disamb-result'));

			var word = $('#btn-word-select-' + selected_word).text();
			var context = selected_context;
			var index = selected_word;

			var model_str = JSON.stringify({
				word : word,
				context : context,
				index : index
			});

			$.ajax({
				type : 'post',
				url : "/nlpccd/dis/disamb",
				data : {
					model : model_str
				},
				success : function(data) {
					show_result(data);
				}
			});
		} else {
			show($('.select-alert'));
		}
	});

	function show_result(data) {
		var model = JSON.parse(data);
		var display_block = $('.disamb-result');

		show(display_block);

		var index = parseInt(model.index);
		var pre = model.context.substring(0, index);
		var suf = model.context.substring(index + model.word.length);

		var display = $('<span>' + pre + '<strong style="color: red">'
				+ model.word + '</strong>' + suf + '</span>');

		$('#disamb-result-word').text(model.word);
		$('#disamb-result-context').empty();
		$('#disamb-result-context').append(display);

		var senses_str = "";
		if (model.senses) {
			senses_str = "1. " + model.senses[0];
			for (i = 1; i < model.senses.length; i++) {
				senses_str = senses_str + "<br/>" + (i + 1) + ". "
						+ model.senses[i];
			}
		}
		$('#disamb-result-senses').html(senses_str);

		setTimeout(function() {
			$('#loading-modal').modal('hide');
		}, 500);

	}

	function hide(e) {
		e.addClass("display-none");
	}

	function show(e) {
		e.removeClass("display-none");
	}
</script>
</html>