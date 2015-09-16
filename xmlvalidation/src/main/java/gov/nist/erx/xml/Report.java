package gov.nist.erx.xml;

import java.util.ArrayList;

public class Report {
    
    private ArrayList<ReportItem> items = new ArrayList<ReportItem>();
    private int errorCount = 0;

    public void addItem(ReportItem ri){
        this.items.add(ri);
        if(ri instanceof ErrorReportItem){
            this.errorCount++;
        }
    }

    public int getErrorCount(){
        return this.errorCount;
    }
    public int getCount(){
        return this.items.size();
    }


    public String getReport(){
        StringBuilder report = new StringBuilder();
        for(ReportItem item : items){
            report.append(item.getFilePath());
            report.append(" : ");
            if(item instanceof ErrorReportItem){
                report.append("ERROR - ");
                report.append(((ErrorReportItem) item).getErrorMessage());
            } else {
                report.append("SUCCESS");
            }
            report.append("\n");
        }
        return report.toString();
    }
}