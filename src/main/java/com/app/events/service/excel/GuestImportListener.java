package com.app.events.service.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.app.events.dto.GuestExcelDto;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class GuestImportListener extends AnalysisEventListener<GuestExcelDto> {

    @Getter
    private final List<GuestExcelDto> guests = new ArrayList<>();

    @Override
    public void invoke(GuestExcelDto data, AnalysisContext context) {
        if (data != null && data.getEmail() != null && !data.getEmail().isEmpty()) {
            guests.add(data);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // No-op
    }
}
