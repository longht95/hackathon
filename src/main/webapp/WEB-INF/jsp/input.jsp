<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Document</title>
<link rel="stylesheet" href="css/input.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"
	integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm"
	crossorigin="anonymous">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
</head>
<body>
	<div class="container">
		<div class="head">
			<label for="">Table: </label> <select class="selectbox"
				onchange="selectTable(this)">
				<option value=""></option>
				<option value="users"></option>
				<option value="TABLE2">User1</option>
				<option value="TABLE3">TABLE3</option>
				<option value="TABLE4">TABLE4</option>
				<%-- <c:forEach items="${listTable}" var="tblName">
					<option value="${tblName}">${tblName}</option>
				<c:forEach> --%>
			</select>

		</div>
		<label for="">Câu query: </label>
		<textarea name="" id="" cols="100" rows="2">${query}</textarea>
		<input type="submit" value="Update">
		<div class="wrap-table">
			<table class="table">
				<thead>
					<th>Chọn</th>
					<th>Chọn</th>
					<th>Chọn</th>
					<th>Chọn</th>
					<th>Chọn</th>
					<th>Chọn</th>
				</thead>
				<tbody>
				</tbody>
			</table>
		</div>
		<div class="wrap-btn">
			<button>Gen data</button>
		</div>
	</div>
	<script>
		function selectTable(select) {
			let tableName = select.value;
			$
					.ajax({
						type : "GET",
						contentType : "application/json",
						url : "/selectTable",
						data : {
							"tableName" : tableName
						},
						dataType : 'json',
						timeout : 100000,
						success : function(data) {
							console.log("SUCCESS: ", data);
							let tbl = $(".table > tbody");
							let ob = JSON.parse(data);
							let arrData = ob.listData;
							let arrColumn = ob.listColumnName;
							console.log(ob);
							let tien = "";
							tien += "<th>Chọn</th>";
							let tbl1 = $(".table > thead");
							tbl1.empty();
							for (let j = 0; j < arrColumn.length; j++) {
								tien += "<th>" + arrColumn[j] + "</th>";
							}
							tbl.empty();
							tbl1.append(tien);
							for (let i = 1; i < arrData.length; i++) {
								let abc = "<tr>";
								let tmp = 1;
								abc += '<td><input type="checkbox" id="vehicle1" name="vehicle1" value="Bike"></td>';
								for (let k = 0; k < arrData[i].length; k++) {
									abc += "<td id ='row"+ tmp +"-col"+ tmp +"-p' class='appDetails'>";
									abc += arrData[i][k];
									abc += "</td>";
									abc += '<td id="row1-col1-input" class="appDetails" style="display: none;"><input  type="text" placeholder="Nhập..."></td>'
									tmp++;
								}
								abc += "</tr>";
								tbl.append(abc);
							}
							console.log(ob);
						},
						error : function(e) {
							console.log("ERROR: ", e);
						}
					});
		}

		$(document).on("click", ".appDetails", function() {
			var clickedBtnID = $(this).attr('id'); // or var clickedBtnID = this.id
			alert('you clicked on button #' + clickedBtnID);
			$("#" + clickedBtnID).hide();
			$("#row1-col1-input").show();
		});
	</script>
</body>
</html>
