package com.app.events.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class GuestExcelDto {
    @ExcelProperty("Name")
    private String name;

    @ExcelProperty("Email")
    private String email;

    @ExcelProperty("Group")
    private String group;

    @ExcelProperty("Status")
    private String status;

    @ExcelProperty("Dietary")
    private String dietary;

    @ExcelProperty("Notes")
    private String notes;
}
