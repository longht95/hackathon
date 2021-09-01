var editor;
var inforTable = [];
var tableDataSet;
$(document).ready(function() {
	var myTextarea = $("#inputQuerySQL")[0];
	editor = CodeMirror.fromTextArea(myTextarea, {
		lineNumbers: true,
		mode: 'text/x-sql',
		lineWrapping: true,
	});
	$('input[type="text"]#searchColumn').keyup(function() {
		console.log('xxxxxxxxxx');
		var searchText = $(this).val().toUpperCase();

		$('.node-child > li').each(function() {

			var currentLiText = $(this).text().toUpperCase(),
				showCurrentLi = currentLiText.indexOf(searchText) !== -1;
			if (showCurrentLi) {
				$(this).addClass('tv-in-tm').removeClass('tv-out-tm');
			} else {
				$(this).addClass('tv-out-tm').removeClass('tv-in-tm');
			}

		});
	});
});
function updateDataSet(nameTable) {
	let tableTarget = inforTable.find(item => item.tableName == nameTable);
	let listDataTable = $('#tableDataSet table.table').find('tbody tr');
	let listDataInforUpdate = [];
	for (let i = 0; i < listDataTable.length; i++) {
		let listDataCell = $(listDataTable[i]).find('td');
		let listDataUpdate = [];
		for (let j = 0; j < listDataCell.length; j++) {
			if (j == 0) {
				listDataUpdate.push($(listDataCell[0]).find('input').is(':checked'));
			} else {
				listDataUpdate.push(listDataCell[j].innerHTML);
			}
		}
		listDataInforUpdate.push(listDataUpdate);
	}
	tableTarget.listData = listDataInforUpdate;

	//update column

	let listColumnTable = $('#tableDataSet table.table').find('thead tr th');

	let listColumnUpdate = [];

	for (let i = 0; i < listColumnTable.length; i++) {
		if (i != 0) {
			listColumnUpdate.push(listColumnTable[i].innerHTML);
		}
	}
	tableTarget.listColumnName = listColumnUpdate;
	console.log('listColumnUpdate', listColumnUpdate);
}
function showTable(table) {
	let nameTable = table.innerHTML;
	if (tableDataSet && tableDataSet == nameTable) {
		return;
	}
	if (tableDataSet) {
		updateDataSet(tableDataSet);
	}
	tableDataSet = nameTable;
	let tableTarget = inforTable.find(item => item.tableName == nameTable);

	let tableTag = document.createElement('table');
	let theadTag = document.createElement('thead');
	let tbodyTag = document.createElement('tbody');
	if (tableTarget) {
		let trTag = document.createElement('tr');

		let thAction = document.createElement('th');
		thAction.appendChild(document.createTextNode('#'));
		trTag.appendChild(thAction);

		for (const column in tableTarget.listColumnName) {
			let th = document.createElement('th');
			th.appendChild(document.createTextNode(tableTarget.listColumnName[column]));
			trTag.appendChild(th);
		}
		theadTag.appendChild(trTag);

		for (const dataIndex in tableTarget.listData) {
			let trBodyTag = document.createElement('tr');
			for (const cellIndex in tableTarget.listData[dataIndex]) {
				let tdBodyTag = document.createElement('td');
				if (cellIndex == 0) {
					let checkbox = document.createElement('input');
					checkbox.type = 'checkbox';
					checkbox.checked = tableTarget.listData[dataIndex][cellIndex];
					tdBodyTag.appendChild(checkbox);
				} else {
					tdBodyTag.appendChild(document.createTextNode(tableTarget.listData[dataIndex][cellIndex]));
					tdBodyTag.setAttribute('contenteditable', 'true');
				}
				trBodyTag.appendChild(tdBodyTag);
			}
			tbodyTag.appendChild(trBodyTag);
		}
	}

	tableTag.appendChild(theadTag);
	tableTag.appendChild(tbodyTag);
	tableTag.setAttribute('class', 'table');
	$('#tableDataSet table').remove();
	$('#tableDataSet').append(tableTag);
}

function addColumnDataSet() {
	let tableDataSet = $('table.table');
	let headerDataSet = $(tableDataSet).find('thead tr th');
	let rowDataSet = $(tableDataSet).find('tbody');
	let listRowDataSet = $(rowDataSet).find('tr');
	for (let i = 0; i < listRowDataSet.length; i++) {
		let listTD = $(listRowDataSet[i]).find('td');
		let cell = document.createElement('td');
		cell.setAttribute('contenteditable', 'true');
		cell.appendChild(document.createTextNode(''));
		$(listTD).first().after(cell);
	}

	let cellColumn = document.createElement('th');
	cellColumn.setAttribute('contenteditable', 'true');
	cellColumn.appendChild(document.createTextNode('column'));
	$(headerDataSet).first().after(cellColumn);
}

function addRowDataSet() {
	let listColumn = $('table.table thead tr th');
	let tbl = $("table.table > tbody");
	let row = document.createElement('tr');
	for (let i = 0; i < listColumn.length; i++) {
		let cell = document.createElement('td');
		if (i == 0) {
			let input = document.createElement('input');
			input.setAttribute('id', tableDataSet);
			input.type = "checkbox";
			cell.appendChild(input);
		} else {
			cell.setAttribute('contenteditable', 'true');
			cell.appendChild(document.createTextNode(''));
		}
		row.appendChild(cell);
	}
	tbl.prepend(row);
}
function switchNavigation(id) {
	stateNav = id;
	$('.box-content-nav').hide();
	$('#' + stateNav).show();
	$('.icon-nav').removeClass('active');
	$('#' + id + '-nav').addClass('active');

}
function openModalDownload() {
	updateDataSet(tableDataSet);
	var modal = document.getElementById("myModal");
	modal.style.display = "block";
}
function closeModal() {
	var modal = document.getElementById("myModal");
	modal.style.display = "none";

}
