<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
	<head>
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
	</head>
	<style>
		body {
			margin:0;
		}
		main {
			width:100%;
			display:flex;
		}
		.left-layout {
			width:30%;
			height:100vh;
		    display: flex;
			flex-direction: column;
			border-right: 2px solid #d1d1d1;
		}
		.text-statement {
			background: url(http://i.imgur.com/2cOaJ.png);
			background-attachment: local;
			background-repeat: no-repeat;
			width:100%;
			padding-left: 27px;
			box-sizing: border-box;
			height: 100%;
		}
		textarea {
			padding:15px;
			width:100%;
			height:100%;
			border:none;
			box-sizing: border-box;
			outline: none !important;
			resize: none;
		}
		.right-layout {
			width:70%;
			padding: 15px;
			background-color:#eeeeee;
			box-sizing:border-box;
		}
		.select-database {
			width:140px;
			height:32px;
			border: 1px solid #d9d9d9
		}
		.box-button {
			padding: 15px;
			border-bottom: 1px solid #333;
		}
		.box-control {
			margin-bottom: 15px;
		}
		input[type='submit'] {
			    width: 140px;
			height: 32px;
			border: 1px solid #d9d9d9;
			background: #fff;
		}
		input[type='text'] {
			
			height: 20px;
		}
		
		input[type='text']:not(:last-child) {
			margin-bottom:15px;

		}
		.box-input {
			display: flex;
			flex-direction: column;
		}
		h3 {
			margin: 5px;
		}
		
		.box-connect {
			display:flex;
		}
		table {
			margin:0 auto;
		}
	</style>
	<body>
		<main>
			<div class="left-layout">
					<div class="box-button">
						<div class="box-control">
							<select class="select-database">
							  <option value="volvo">Oracle</option>
							  <option value="saab">Mysql</option>
							  <option value="mercedes">Mercedes</option>
							  <option value="audi">Audi</option>
							</select>
							<input type="submit" value="Import SQL" id="importFile">
							<input type="file" name="inputFile" id="inputFile" style="display:none;">
						</div>
						
						<div class="box-input">
							<input type="text" placeholder="URL">
							<input type="text" placeholder="Schema">
							<input type="text" placeholder="User">
							<input type="text" placeholder="Password">
							<div class="box-connect">
								<input type="submit" value="Test Connection">
								<h3>Success!</h3>
							</div>
						</div>	
					</div>
				<div class="text-statement">
					<textarea rows="10" cols="40">${query}</textarea>
				</div>
			</div>
			<div class="right-layout">
			<label id="idSelectBox" for="">Table: </label> <select id="selectBox" class="selectbox"
				onchange="selectTable(this)">
				<option value="u"></option>
				<option value="users">User</option>
				<option value="TABLE2">User1</option>
				<option value="TABLE3">TABLE3</option>
				<option value="TABLE4">TABLE4</option>
				<%-- <c:forEach items="${listTable}" var="tblName">
					<option value="${tblName}">${tblName}</option>
				<c:forEach> --%>
			</select>
				<table>
					<thead>
						<tr>
							<th>Column4324324324234324324231</th>
							<th>Column2</th>
							<th>Column1</th>
							<th>Column2</th>
							<th>Column1</th>
							
						</tr>
					</thead>
					<tbody>
						
					</tbody>
					
				</table>
			</div>
		</main>
		<script>
		function selectTable(select) {
			let tableName = select.value;
					$.ajax({
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
		
		$('#importFile').on("click", function(){
			console.log('xxxxxxxxxxxx');
			$('#inputFile').trigger('click');
		});
		
		$('#inputFile').on("change", function(e){
			var formData = new FormData();
			formData.append('file', $('#inputFile')[0].files[0]);
			$.ajax({
			       url : '/uploadFile',
			       type : 'POST',
			       data : formData,
			       processData: false,  // tell jQuery not to process the data
			       contentType: false,  // tell jQuery not to set contentType
			       success : function(data) {
			           console.log(data);
			           
			       }
			});
		});
	</script>
	</body>
</html>