<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
</head>
<style>
body {
	margin: 0;
}

main {
	width: 100%;
	display: flex;
}

.left-layout {
	width: 30%;
	height: 100vh;
	display: flex;
	flex-direction: column;
	border-right: 2px solid #d1d1d1;
}

.text-statement {
	background: url(http://i.imgur.com/2cOaJ.png);
	background-attachment: local;
	background-repeat: no-repeat;
	width: 100%;
	padding-left: 27px;
	box-sizing: border-box;
	height: 100%;
}

textarea {
	padding: 15px;
	width: 100%;
	height: 100%;
	border: none;
	box-sizing: border-box;
	outline: none !important;
	resize: none;
}

.right-layout {
	width: 70%;
	padding: 15px;
	background-color: #eeeeee;
	box-sizing: border-box;
}

.select-database {
	width: 140px;
	height: 32px;
	border: 1px solid #d9d9d9
}

.box-button {
	padding: 15px;
	border-bottom: 1px solid #333;
}

.box-control {
	margin-bottom: 15px;
	display: flex;
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
	margin-bottom: 15px;
}

.box-input {
	display: flex;
	flex-direction: column;
}

h3 {
	margin: 5px;
}

.box-connect {
	display: flex;
}
table {
	border-spacing: 0;
	margin-top: 15px;
}
table th {
	border-bottom: 1px solid #333;
	border-right: 1px solid #333;
	padding:5px 10px;
	font-weight: normal;
}
table th:last-child {
	border-right:none;
}
table td:not(:last-child) {
	border-right: 1px solid #333;
}
table td {
	padding: 5px 10px;
}

</style>
<body>
	<main>
		<div class="left-layout">
			<div class="box-button">
				<div class="box-control">
					<select class="select-database" style="margin-right:2.5px;">
						<option value="Oracle">Select Table</option>
						<option value="Oracle">Oracle</option>
						<option value="Mysql">Mysql</option>
					</select>
					<input type="submit" value="Import SQL" id="importFile" style="margin-left:2.5px;">
					<input type="file" name="inputFile" id="inputFile" style="display: none;">
				</div>
				<div class="box-input">
					<input type="text" placeholder="URL"> <input type="text"
						placeholder="Schema"> <input type="text"
						placeholder="User"> <input type="text"
						placeholder="Password">
					<div class="box-connect">
						<input type="submit" value="Test Connection" style="margin-right:2.5px;">
						<input type="submit" value="Update query" id="updateQuery" style="margin-left:2.5px;">
					</div>
				</div>
			</div>
			<div class="text-statement">
				<textarea id="inputQuerySQL" rows="10" cols="40"></textarea>
			</div>
		</div>
		<div class="right-layout">
			<h4 style="margin-top:0px;">DATA SET</h4>
			<select id="selectBox"
				class="select-database" onchange="selectTable(this)">
			</select>
			<table>
				<thead>
					<tr>
						<th>Column1</th>
						<th>Column2</th>
						<th>Column3</th>
						<th>Column4</th>
						<th>Column5</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Data1</td>
						<td>Data2</td>
						<td>Data3</td>
						<td>Data4</td>
						<td>Data5</td>
					</tr>
				</tbody>

			</table>
			<h4>DATA PICK</h4>
			<table>
				<thead>
					<tr>
						<th>Column1</th>
						<th>Column2</th>
						<th>Column3</th>
						<th>Column4</th>
						<th>Column5</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Data1</td>
						<td>Data2</td>
						<td>Data3</td>
						<td>Data4</td>
						<td>Data5</td>
					</tr>
				</tbody>

			</table>
			<h4>EXPORT TYPES</h4>
			<div class="box-generate">
				<select id="selectBox"
					class="select-database">
					<option value="Excel">Excel</option>
					<option value="SQL">SQL</option>
				</select>
				<input type="submit" value="Generate" id="Generate">
			</div>
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
		
		//import file
		$('#importFile').on("click", function(){
			console.log('xxxxxxxxxxxx');
			$('#inputFile').trigger('click');
		});
		
		$('#updateQuery').on("click", function(){
			const queryInput = $('#inputQuerySQL').val();
			$.ajax({
			       url : '/updateQuery',
			       type : 'GET',
			       data : {
						"query" : queryInput
					},
					contentType : "application/json",
					dataType : 'json',
			       success : function(data) {
			           $('textarea#inputQuerySQL').val(data.query);
			           $("#selectBox").empty();
			           for (const tbl in data.listTable) {
			        	   $("#selectBox").append(new Option(data.listTable[tbl], data.listTable[tbl]));
			           }
			       }
			});
		});
		
		//import file
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
			    	   console.log('data', data);
			    	   const dataSQL = JSON.parse(data);
			           console.log('dataSQL', dataSQL);
			           console.log('table', dataSQL.listTable);
			           console.log('query', dataSQL.query);
			           $('textarea#inputQuerySQL').val(dataSQL.query);
			           $("#selectBox").empty();
			           for (const tbl in dataSQL.listTable) {
			        	   console.log('tbl', tbl);
			        	   $("#selectBox").append(new Option(dataSQL.listTable[tbl], dataSQL.listTable[tbl]));
			           }
			       }
			});
		});
	</script>
</body>
</html>