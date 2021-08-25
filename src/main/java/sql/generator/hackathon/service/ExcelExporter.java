package sql.generator.hackathon.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import sql.generator.hackathon.model.ColumnInfo;

@Service
public class ExcelExporter {
	private  Map<String, List<List<ColumnInfo>>> dataList ;

    private  HSSFCellStyle createHeaderRow(HSSFWorkbook workbook) {
        dataList = new HashMap<>();
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        //Table A
        ColumnInfo columnInfo = new ColumnInfo();
        columnInfo.setName("Title");
        columnInfo.setTypeValue("123");

        ColumnInfo columnInfo2 = new ColumnInfo();
        columnInfo2.setName("Author");
        columnInfo2.setTypeValue("123");

        ColumnInfo columnInfo3 = new ColumnInfo();
        columnInfo3.setName("Price");
        columnInfo3.setTypeValue("123");

        List<ColumnInfo> columnInfosList = new ArrayList<>();
        columnInfosList.add(columnInfo);
        columnInfosList.add(columnInfo2);
        columnInfosList.add(columnInfo3);
        List<List<ColumnInfo>> columnInfosListCdA = new ArrayList<>();
        columnInfosListCdA.add(columnInfosList);
        dataList.put("TableA",columnInfosListCdA);

        // row1
        ColumnInfo columnInfoB = new ColumnInfo();
        columnInfoB.setName("Effective Java");
        columnInfoB.setTypeValue("123");

        ColumnInfo columnInfoB2 = new ColumnInfo();
        columnInfoB2.setName("Joshua Bloch");
        columnInfoB2.setTypeValue("123");


        List<ColumnInfo> columnInfosList2 = new ArrayList<>();
        columnInfosList2.add(columnInfoB2);
        columnInfosList2.add(columnInfoB);

        // row2
        ColumnInfo columnInfoC = new ColumnInfo();
        columnInfoC.setName("Head First Java");
        columnInfoC.setTypeValue("123");

        ColumnInfo columnInfoBC = new ColumnInfo();
        columnInfoBC.setName("Kathy Serria");
        columnInfoBC.setTypeValue("123");

        List<ColumnInfo> columnInfosListC = new ArrayList<>();
        columnInfosListC.add(columnInfoC);
        columnInfosListC.add(columnInfoBC);

        List<List<ColumnInfo>> columnInfosListCd = new ArrayList<>();
        columnInfosListCd.add(columnInfosListC);
        columnInfosListCd.add(columnInfosList2);

        Map<String, List<List<ColumnInfo>>> lissColumB = new HashMap<>();
        dataList.put("TableB",columnInfosListCd);

        System.out.println(dataList.size());
        return style;

    }

    public HSSFWorkbook createEex(Map<String, List<List<ColumnInfo>>> dataList)  {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFCellStyle style = createHeaderRow(workbook);
        HSSFFont font = workbook.createFont();
        style.setFont(font);

        dataList.entrySet().forEach(entry -> {
                HSSFSheet sheet = workbook.createSheet(entry.getKey());

                List<List<ColumnInfo>> isNameTable= entry.getValue();
                int rownum = 0;
                        for (int i = 0 ; i< isNameTable.size() ;i ++){
                            Row  row = sheet.createRow(rownum);
                            rownum++;
                            Cell cell;
                            if (i==0){
                                createheader(sheet, isNameTable.get(i),row,style);
                            }else {
                                createRow(sheet, isNameTable.get(i),row,style);
                            }
                }
            });

        return workbook;
        }

    private void createRow(Sheet sheet, List<ColumnInfo> isNameTable,Row  row,HSSFCellStyle style ) {
        Cell cell;
        for (int i =0 ; i< isNameTable.size() ;i ++){
                cell = row.createCell(i, CellType.STRING);
                cell.setCellValue(isNameTable.get(i).getTypeValue());
                System.out.println(isNameTable.get(i).getName());
        }
    }

    private void createheader(Sheet sheet, List<ColumnInfo> isNameTable,Row  row,HSSFCellStyle style ) {
        Cell cell;
        for (int i =0 ; i< isNameTable.size() ;i ++){
            cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(isNameTable.get(i).getName());
            cell.setCellStyle(style);
            System.out.println(isNameTable.get(i).getName());
        }
    }
    public FileOutputStream outputFile(String fileName) throws FileNotFoundException {
       // HSSFWorkbook workbook =  this.createEex();

        File file = null;
        FileOutputStream outFile =null;
            try {
                    file= new File(fileName);
                    outFile = new FileOutputStream(file);
                    file.getParentFile().mkdirs();
                    //workbook.write(outFile);

            } catch (IOException e) {
                e.printStackTrace();
            }
        System.out.println("Created file: " + file.getAbsolutePath());
        return  outFile;

    }
    public FileOutputStream outputFileSQL(ServiceParse serviceParse, String fileName) throws FileNotFoundException{
        File file = null;
        FileOutputStream outFile =null;
        try {
            file= new File(fileName);
            outFile = new FileOutputStream(file);
            file.getParentFile().mkdirs();
         //   serviceParse.dataToSqlInsert();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  outFile;
    }
}
