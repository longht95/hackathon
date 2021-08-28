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
	margin-top:15px;
	display: flex;
}

table {
	border: 1px solid rgb(192, 192, 192); border-collapse: collapse; padding: 5px;
	margin-top: 15px;
	outline:none;
}
table th {
	background-color: rgb(240, 240, 240); padding: 5px; border-width: 1px; border-style: solid; border-color: rgb(192, 192, 192);
}

table tbody tr td {
	padding: 5px; border-width: 1px; border-style: solid; border-color: rgb(192, 192, 192);
}

.delete-picker {
	width:50px;
}
#rowGen {
	width: 36px;
    border: 1px solid #d9d9d9;
    height: 28px;
}


</style>
<body>
	<main>
		<div class="left-layout">
			<div class="box-button">
				<div class="box-control">
					<select class="select-database" id="select-database" style="margin-right:2.5px;">
						<option value="No database">No database</option>
						<option value="Oracle">Oracle</option>
						<option value="Mysql">Mysql</option>
						<option value="H2">H2</option>
					</select>
					<input type="submit" value="Import SQL" id="importFile" style="margin-left:2.5px;">
					<input type="file" name="inputFile" id="inputFile" style="display: none;">
				</div>
				<div class="box-input" style="display:none;">
					<input type="text" placeholder="URL" id="url">
					<input type="text" placeholder="Schema" id="schema">
					<input type="text" placeholder="User" id="user">
					<input type="text" placeholder="Password" id="pass">
				</div>
				<div class="box-connect">
					<input type="submit" value="Update query" id="updateQuery" style="margin-right:2.5px;">
					<input type="submit" value="Test Connection" id="testConnection" onclick="testConnection()" style="margin-left:2.5px; display:none;">
				</div>
			</div>
			<div class="text-statement">
				<textarea id="inputQuerySQL" rows="10" cols="40"></textarea>
			</div>
		</div>
		<div class="right-layout">
			<h4 style="margin-top:0px;">DATA SET</h4>
			<select id="selectBox" class="select-database"
				onchange="selectTable(this)">
				<option value="None" disabled="disabled" selected="selected">Select table</option>
			</select>
			<input type="submit" id="addColumn" value="Add column" onclick="addColumnDataSet()">
			<input type="submit" value="Add row" onclick="addRowDataSet()">
			<table class="table" contenteditable="true" spellcheck="false">
				<thead>
					
				</thead>
				<tbody>
					
				</tbody>
			</table>
			<h4>DATA PICK</h4>
				<div id="block-data-picker">
			</div>
			<h4>EXPORT TYPES</h4>
			<div class="box-generate">
				<select id="selectType"
					class="select-database">
					<option value="Excel">Excel</option>
					<option value="SQL">SQL</option>
				</select>
				<input type="text" value="1" id="rowGen" >
				<input type="submit" value="Generate" id="Generate" onclick="genate()">
			</div>
		</div>
	</main>
	<script>
	var dataPicker = [];
	
	var stateId = 0;
	
	var stateIdPicker = 0;
	
	$('#select-database').on('change', function() {
		if ($(this).val() == 'No database') {
			$('#testConnection').hide();
			$('.box-input').hide();
			$('#addColumn').show();
		} else {
			$('#testConnection').show();
			$('.box-input').show();
			$('#addColumn').hide();
		}
	});
	
		function delRowDataPicker(row) {
			let id = $(row).attr('id');
			let table = $(row).closest('table').attr('id');
			$(row).closest('tr').remove();
			let tablePicker = $('#block-data-picker').find('table#'+table+' tbody tr');
			if (tablePicker && tablePicker.length == 0) {
				$('#block-data-picker').find('table#'+table).remove();
			}
		}
		
		function addRowDataSet() {
			let tableName = $('#selectBox :selected').val();
			let tableSelected = $('#select-database :selected').val();
			let listColumn = $('table.table thead tr th');
			let tbl = $("table.table > tbody");
			let row = document.createElement('tr');
			row.setAttribute('id', "row"+stateId);
			
			for (let i = 0; i < listColumn.length; i++) {
				let cell = document.createElement('td');
				if (i == 0) {
					let input = document.createElement('input');
					input.setAttribute('id', tableName);
					input.type = "submit";
					input.value = "Add";
					input.style.width = "44px";
					input.setAttribute('onclick', 'addRowDataPicker(this)');
					cell.appendChild(input);
				} else {
					cell.appendChild(document.createTextNode(''));
				}
				row.appendChild(cell);
			}
			stateId++;
			tbl.prepend(row);
		}
		
		function addRowDataPicker(rowId) {
			let listCell = $(rowId).parent().parent().find('td');
			
			let listColumn = $('table.table').find('thead tr th');
			
			let table = $('#selectBox :selected').text();
			
			let listData = [];
			
			let tablePicker = $('#block-data-picker').find('table#'+table);
			
			console.log('tablePicker', tablePicker[0]);
			
			let listDataHeader = [];
			
			
			let tableTag = document.createElement('table');
			
			tableTag.setAttribute('id', table);
			
			tableTag.setAttribute('contenteditable', 'true');
			
			let theadTableName = document.createElement('thead');
			
			let trTableName = document.createElement('tr');
			
			let thTableName = document.createElement('th');
			
			thTableName.style.borderRight = "none";
			
			thTableName.appendChild(document.createTextNode(table));
			
			trTableName.appendChild(thTableName);
			
			theadTableName.appendChild(trTableName);
			
			tableTag.appendChild(theadTableName);
			
			
			let tbody = document.createElement('tbody');
			
			let row = document.createElement('tr');
			
			for (let i = 0 ; i <listCell.length; i++) {
				let cell = document.createElement('td');
				if (i == 0) {
					let inputTag = document.createElement('input');
					inputTag.type = "submit";
					inputTag.value = "Del";
					inputTag.style.width = "44px";
					inputTag.setAttribute('id', stateIdPicker);
					inputTag.setAttribute("onclick", "delRowDataPicker(this)");
					cell.appendChild(inputTag);
					listData.push(stateIdPicker);
					stateIdPicker++;
				} else {
					let text = $(listCell[i]).text();
					cell.appendChild(document.createTextNode(text));
					listData.push(text);
				}
				row.appendChild(cell);
			}
			
			tbody.appendChild(row);
			
			let theadTag = document.createElement('thead');
			
			theadTag.setAttribute('class', 'isColumn');
			
			let rowThead = document.createElement('tr');
			
			
			//chua co get column
			for (let i = 0 ; i < listColumn.length; i++) {
				let cellThead = document.createElement('th');
				
				if ($(listColumn[i]).children().prop("tagName") == "INPUT") {
					cellThead.appendChild(document.createTextNode($(listColumn[i]).children().val()));
					listDataHeader.push($(listColumn[i]).children().val());
				} else {
					listDataHeader.push($(listColumn[i]).html());
					cellThead.appendChild(document.createTextNode($(listColumn[i]).html()));
				}
				rowThead.appendChild(cellThead);
			}
			theadTag.appendChild(rowThead);
			
			if (tablePicker[0]) {
				
				$(tablePicker[0]).find('tbody').prepend(row);
				
			}
			
			if (!tablePicker[0]) {
				
				tableTag.appendChild(theadTag);
				tableTag.appendChild(tbody);
				$('#block-data-picker').append(tableTag);
			}
		}
		
		function addColumnDataSet() {
			let tableDataSet = $('table.table');
			let headerDataSet = $(tableDataSet).find('thead tr th');
			let rowDataSet = $(tableDataSet).find('tbody');
			let listRowDataSet = $(rowDataSet).find('tr');
			let row = document.createElement('tr');
			
			for (let i = 0 ; i < listRowDataSet.length ; i++) {
				let listTD = $(listRowDataSet[i]).find('td');
				let cell = document.createElement('td');
				cell.appendChild(document.createTextNode(''));
				$(listTD).first().after(cell);
			}
			
			let cellColumn = document.createElement('th');
			cellColumn.appendChild(document.createTextNode('column'));
			
			$(headerDataSet).first().after(cellColumn);
		}
		
		function selectTable(select) {
			let url = $('#url').val();
			let schema = $('#schema').val();
			let user = $('#user').val();
			let pass = $('#pass').val();
			let query = $('#inputQuerySQL').val();
			let tableSelected = $('#select-database :selected').val();
			let tableName = select.value;
					$.ajax({
						type : "GET",
						contentType : "application/json",
						url : "/selectTable",
						data : {
							"tableName" : tableName,
							"url" : url,
							"schema" : schema,
							"user" : user,
							"pass" :pass,
							"tableSelected" : tableSelected,
							"query" : query,
						},
						dataType : 'json',
						timeout : 10000,
						success : function(data) {
							if("Table not exit" == data) {
								alert("Table not exit");
								return;
							}
							if("Connect error" == data) {
								alert("Connect error");
								return;
							} 
							console.log("SUCCESS: ", data);
							let tbl = $(".table > tbody");
							let arrData = data.listData;
							let arrColumn = data.listColumnName;
														
							let tbl1 = $(".table > thead");
							tbl1.empty();
							tbl.empty();
							
							let trThead = document.createElement('tr');
							
							let thAction = document.createElement('th');
							
							thAction.appendChild(document.createTextNode('#'));
							
							trThead.appendChild(thAction);
							
							for (let j = 0; j < arrColumn.length; j++) {
								
								let thThead = document.createElement('th');
								
								thThead.appendChild(document.createTextNode(arrColumn[j]));
								
								trThead.appendChild(thThead);
							}
							tbl1.append(trThead);
							if (arrData && arrData.length != 0) {
								for (let i = 0; i < arrData.length; i++) {
									let row = document.createElement('tr');
									
									row.setAttribute('id', 'row'+stateId);
									
									let cellAction = document.createElement('td');
									
									let input = document.createElement('input');
									
									input.type = "submit";
									
									input.value = "Add";
									
									input.style.width = "44px";
									
									input.setAttribute('onclick', 'addRowDataPicker(this)');
									
									cellAction.appendChild(input);
									
									row.appendChild(cellAction);
									
									for (let k = 0; k < arrData[i].length; k++) {
										
										let cell = document.createElement('td');
										
										cell.appendChild(document.createTextNode(arrData[i][k]));
										row.appendChild(cell);
									}
									tbl.append(row);
								}
							}
						},
						error : function(e) {
							alert("Connect Error");
						}
					});
		}
		
		//import file
		$('#importFile').on("click", function(){
			$('#inputFile').trigger('click');
		});
		
		$('#updateQuery').on("click", function(){
			const queryInput = $('#inputQuerySQL').val();
			$('table.table').find('thead').empty();
			$('table.table').find('tbody').empty();
			$.ajax({
			       url : '/updateQuery',
			       type : 'GET',
			       data : {
						"query" : queryInput
					},
					contentType : "application/json",
					dataType : 'json',
			       success : function(data) {
			    	   if (data.mess == null) {
			    		   alert("Update success");
			    		   $('textarea#inputQuerySQL').val(data.query);
				           $("#selectBox").empty();
				           $("#selectBox").append('<option value="None" disabled="disabled" selected="selected">Select table</option>');
				           for (const tbl in data.listTable) {
				        	   $("#selectBox").append(new Option(data.listTable[tbl], data.listTable[tbl]));
				           }
				           
			    	   } else {
			    		   console.log(data.mess);
			    		   $("#selectBox").append(data.mess);
			    		   alert(data.mess);
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
			           $("#selectBox").append('<option value="None" disabled="disabled" selected="selected">Select table</option>');
			           for (const tbl in dataSQL.listTable) {
			        	   console.log('tbl', tbl);
			        	   $("#selectBox").append(new Option(dataSQL.listTable[tbl], dataSQL.listTable[tbl]));
			           }
			       }
			});
		});
		
		function testConnection() {
			console.log('xxx');
			let url = $('#url').val();
			let schema = $('#schema').val();
			let user = $('#user').val();
			let pass = $('#pass').val();
			let tableSelected = $('#select-database :selected').val();
			$.ajax({
			       url : '/testConnection',
			       type : 'GET',
			       data : {
						"url" : url,
						"schema" : schema,
						"user" : user,
						"pass" :pass,
						"tableSelected" : tableSelected
					},
					contentType : "application/json",
					dataType : 'json',
			       success : function(data) {
			           alert(data);
			       }
					
			});
		}
		
		function genate() {
			const infoDatabase = {
					type : $('#select-database :selected').val(),
					url : $('#url').val(),
					schema : $('#schema').val(),
					user : $('#user').val(),
					password : $('#pass').val(),
			}
			
			const dataPickers = [];
			
			let listTable = $('#block-data-picker').find('table');
			
			if (listTable) {
				console.log('haveeeeeeee', listTable.length);
				for (let i = 0 ; i < listTable.length ; i++) {
					
					let listColumnInfo = [];
					let listDataColumn = [];
					let listColumn = $(listTable[i]).find('thead.isColumn tr th');
					console.log('list', listColumn);
					for (let c = 0; c < listColumn.length ; c++) {
						listDataColumn.push($(listColumn[c]).text());
					}
					
					let listDataRow = $(listTable[i]).find('tbody tr');
					
					for (let r = 0 ; r < listDataRow.length; r++) {
						let listDataCell = $(listDataRow[r]).find('td');
						let outputData = [];
						for (let cell = 1 ; cell < listDataCell.length ; cell++) {
							const columnInfo = {
								name : listDataColumn[cell],
								val : $(listDataCell[cell]).text(),
							}
							outputData.push(columnInfo);
						}
						listColumnInfo.push(outputData);
						
					}
					const objectDataPicker = {
							tableName : $(listTable[i]).attr('id'),
							listColumnInfo : listColumnInfo,
					};
					
					dataPickers.push(objectDataPicker);
					
				}
				
			}
			console.log('listColumn', dataPickers);
			var xhr = new XMLHttpRequest();
			xhr.open('POST', '/generate', true);
			xhr.responseType = 'blob';
			xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
			xhr.onload = function(e) {
			    if (this.status == 200) {
			    	var blob = new Blob([xhr.response], {type: xhr.getResponseHeader("Content-Type")});
			        var downloadUrl = URL.createObjectURL(blob);
			        var a = document.createElement("a");
			        a.href = downloadUrl;
			        if (xhr.getResponseHeader("Content-Type") == 'application/vnd.ms-excel') {
			        	a.download = "data.xls";
			        } else {
			        	a.download = "data.txt";
			        }
			        document.body.appendChild(a);
			        a.click();
			    } else {
			        alert('Unable to download excel.')
			    }
			};
			const jsonData = {
					queryInput : $('#inputQuerySQL').val(),
					typeExport : $('#selectType :selected').text(),
					dataPicker : dataPickers,
					infoDatabase : infoDatabase,
					row :$('#rowGen').val(),
			}
			xhr.send(JSON.stringify(jsonData));
			
		}
		
	</script>
</body>
</html>