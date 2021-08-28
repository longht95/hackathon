package sql.generator.hackathon.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import sql.generator.hackathon.model.ColumnInfo;

@Service
public class ExcelExporter {
	private Map<String, List<List<ColumnInfo>>> dataList;

	private HSSFCellStyle createHeaderRow(HSSFWorkbook workbook) {
		// dataList = new HashMap<>();
		HSSFFont font = workbook.createFont();
		font.setBold(true);
		HSSFCellStyle style = workbook.createCellStyle();
		style.setFont(font);
		return style;

	}

	public HSSFWorkbook createEex(Map<String, List<List<ColumnInfo>>> dataList, List<String> listMarkColors) {
		HSSFWorkbook workbook = new HSSFWorkbook();

		dataList.entrySet().forEach(entry -> {
			System.out.println("Name Table" + entry.getKey());
			HSSFSheet sheet = workbook.createSheet(entry.getKey());

			List<List<ColumnInfo>> isNameTable = entry.getValue();

			System.out.println("reocrd" + isNameTable.size());
			int rownum = 0;
			for (List<ColumnInfo> imtem : isNameTable) {
				Row row = sheet.createRow(rownum);
				// Auto size all the columns

				System.out.println("ROW Table" + rownum);
				if (rownum == 0) {
					createheader(sheet, imtem, row, workbook);
					rownum++;
					row = sheet.createRow(rownum);
					createRow(sheet, imtem, row, workbook, listMarkColors);
				} else {
					createRow(sheet, imtem, row, workbook, listMarkColors);

				}
				rownum++;
			}
		});

		return workbook;
	}

	private void createRow(Sheet sheet, List<ColumnInfo> isNameTable, Row row, HSSFWorkbook workbook,
			List<String> listMarkColors) {
		Map<String, Short> listMapingMarkColor = mapingColer(listMarkColors);

		for (int i = 0; i < isNameTable.size(); i++) {

			Cell cell = row.createCell(i, CellType.STRING);
			cell.setCellValue(isNameTable.get(i).val);
			// isNameTable.get(1).color = "MarkColor_0";
			// isNameTable.get(4).color = "MarkColor_0";

			for (String entry : listMapingMarkColor.keySet()) {
				if (isNameTable.get(i).color != null) {
					if (isNameTable.get(i).color.equals(entry)) {
						System.out.println("xxxxxxxxxxx" + isNameTable.get(i).color + "xxxx" + "xxxxxx" + entry);
						CellStyle style1 = workbook.createCellStyle();
						style1.setFillForegroundColor(listMapingMarkColor.get(entry));
						style1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						cell.setCellStyle(style1);
					}
				} else {
					System.out.println(isNameTable.get(i));
					CellStyle style1_a2 = workbook.createCellStyle();
					cell.setCellStyle(style1_a2);
					cell.setCellStyle(style1_a2);
				}
			}
		}
	}

	private Map<String, Short> mapingColer(List<String> listMarkColors) {
		Map<String, Short> listMapingMarkColor = new HashMap<>();
		listMarkColors.forEach((temp) -> {
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.BLACK.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.BROWN.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.OLIVE_GREEN.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.DARK_GREEN.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.DARK_TEAL.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.DARK_BLUE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.INDIGO.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.GREY_80_PERCENT.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.ORANGE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.DARK_YELLOW.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.GREEN.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.TEAL.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.BLUE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.BLUE_GREY.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.GREY_50_PERCENT.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.RED.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.LIGHT_ORANGE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.LIME.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.SEA_GREEN.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.AQUA.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.LIGHT_BLUE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.VIOLET.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.PINK.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.GOLD.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.BRIGHT_GREEN.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.TURQUOISE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.DARK_RED.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.SKY_BLUE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.PLUM.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.ROSE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.LIGHT_TURQUOISE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.PALE_BLUE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.LAVENDER.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.WHITE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.CORNFLOWER_BLUE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.LEMON_CHIFFON.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.MAROON.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.ORCHID.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.CORAL.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.ROYAL_BLUE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.LIGHT_CORNFLOWER_BLUE.getIndex());
			listMapingMarkColor.put(temp, HSSFColor.HSSFColorPredefined.TAN.getIndex());

			// System.out.println(temp);
		});
		return listMapingMarkColor;
	}

	private void createheader(Sheet sheet, List<ColumnInfo> isNameTable, Row row, HSSFWorkbook workbook) {
		Cell cell;
		for (int i = 0; i < isNameTable.size(); i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellValue(isNameTable.get(i).getName());
			CellStyle style1 = workbook.createCellStyle();
			style1.setBorderTop(BorderStyle.MEDIUM);
			style1.setBorderBottom(BorderStyle.MEDIUM);
			style1.setBorderLeft(BorderStyle.MEDIUM);
			style1.setBorderRight(BorderStyle.MEDIUM);
			cell.setCellStyle(style1);
			for (int x = 0; x < sheet.getRow(0).getPhysicalNumberOfCells(); x++) {
				sheet.autoSizeColumn(x);
			}
		}
	}

	private void createValiheader(Sheet sheet, List<ColumnInfo> isNameTable, Row row, HSSFCellStyle style) {
		Cell cell;
		for (int i = 0; i < isNameTable.size(); i++) {
			cell = row.createCell(i, CellType.STRING);
			cell.setCellValue(isNameTable.get(i).val);
			cell.setCellStyle(style);
			System.out.println(isNameTable.get(i).val + "header");
		}
	}

	public byte[] outputFieSql(List<String> inputSQL) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			for (String x : inputSQL) {
				bos.write(x.getBytes());
				bos.write(";\n".getBytes());
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			bos.close();
		}
		return bos.toByteArray();
	}

}
