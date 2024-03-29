package com.buschmais.jqassistant.core.report;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportReader;

import org.jqassistant.schema.report.v2.*;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

class XmlReportTest {

    private static final ReportReader REPORT_READER = new ReportReader();

    private XmlReportTestHelper xmlReportTestHelper = new XmlReportTestHelper();

    @Test
    void writeAndReadReport() throws ReportException, MalformedURLException {
        File xmlReport = xmlReportTestHelper.createXmlReport();
        JqassistantReport report = readReport(xmlReport);
        assertThat(report).isNotNull();
        assertThat(report.getGroupOrConceptOrConstraint()).hasSize(1);
        GroupType groupType = (GroupType) report.getGroupOrConceptOrConstraint()
            .get(0);
        assertThat(groupType.getDate()).isNotNull();
        assertThat(groupType.getId()).isEqualTo("default");
        assertThat(groupType.getGroupOrConceptOrConstraint()).hasSize(1);
        ExecutableRuleType ruleType = (ExecutableRuleType) groupType.getGroupOrConceptOrConstraint()
            .get(0);
        assertThat(ruleType.getStatus()).isEqualTo(StatusEnumType.SUCCESS);
        assertThat(ruleType).isInstanceOf(ConceptType.class);
        assertThat(ruleType.getId()).isEqualTo("my:concept");
        assertThat(ruleType.getDescription()).isEqualTo("My concept description");
        // Result
        assertThat(ruleType.getResult()).isNotNull();
        ResultType result = ruleType.getResult();
        assertThat(result.getColumns()
            .getCount()).isEqualTo(2);
        assertThat(result.getColumns().getPrimary()).isEqualTo("c2");
        List<String> columnHeaders = result.getColumns()
            .getColumn();
        assertThat(columnHeaders).hasSize(2);
        assertThat(columnHeaders).containsExactly("c1", "c2");
        assertThat(result.getRows()
            .getCount()).isEqualTo(1);
        List<RowType> rows = result.getRows()
            .getRow();
        assertThat(rows).hasSize(1);
        RowType rowType = rows.get(0);
        assertThat(rowType.getKey()).hasSize(64);
        assertThat(rowType.getColumn()
            .size()).isEqualTo(2);
        for (ColumnType column : rowType.getColumn()) {
            assertThat(column.getName()).isIn("c1", "c2");
            if ("c1".equals(column.getName())) {
                assertThat(column.getValue()).isEqualTo("simpleValue");
            } else if ("c2".equals(column.getName())) {
                assertThat(column.getElement()
                    .getLanguage()).isEqualTo("TestLanguage");
                assertThat(column.getElement()
                    .getValue()).isEqualTo("TestElement");
                assertThat(column.getValue()).isEqualTo("descriptorValue");
                SourceLocationType source = column.getSource();
                assertThat(source.getFileName()).isEqualTo("Test.java");
                assertThat(source.getStartLine()).isEqualTo(1);
                assertThat(source.getEndLine()).isEqualTo(2);
            }
        }
        // Reports
        ReportsType reports = ruleType.getReports();
        assertThat(reports).isNotNull();
        List<AbstractReportType> imageOrLink = reports.getImageOrLink();
        assertThat(imageOrLink).hasSize(2);
        Map<String, AbstractReportType> reportsByLabel = imageOrLink.stream()
            .collect(toMap(r -> r.getLabel(), r -> r));
        AbstractReportType image = reportsByLabel.get("Image");
        assertThat(image).isInstanceOf(ImageType.class);
        assertThat(image.getValue()).isEqualTo("file:image.png");
        AbstractReportType link = reportsByLabel.get("Link");
        assertThat(link).isInstanceOf(LinkType.class);
        assertThat(link.getValue()).isEqualTo("file:report.csv");
    }

    @Test
    void testReportWithConstraint() throws ReportException {
        File xmlReport = xmlReportTestHelper.createXmlReportWithConstraints();
        JqassistantReport report = readReport(xmlReport);
        assertThat(report.getGroupOrConceptOrConstraint()).hasSize(1);
        GroupType groupType = (GroupType) report.getGroupOrConceptOrConstraint()
            .get(0);
        assertThat(groupType.getId()).isEqualTo("default");
        assertThat(groupType.getGroupOrConceptOrConstraint()).hasSize(1);
        ExecutableRuleType ruleType = (ExecutableRuleType) groupType.getGroupOrConceptOrConstraint()
            .get(0);
        assertThat(ruleType).isInstanceOf(ConstraintType.class);
        assertThat(ruleType.getId()).isEqualTo("my:Constraint");
        assertThat(ruleType.getSeverity()
            .getValue()).isEqualTo("critical");
        assertThat(ruleType.getStatus()).isEqualTo(StatusEnumType.FAILURE);
        ResultType result = ruleType.getResult();
        assertThat(result).isNotNull();
        ColumnsHeaderType columnsHeader = result.getColumns();
        assertThat(columnsHeader.getCount()).isEqualTo(2);
        assertThat(columnsHeader.getPrimary()).isEqualTo("c1");
        List<String> columnHeaders = columnsHeader.getColumn();
        assertThat(columnHeaders).containsExactly("c1", "c2");
    }

    @Test
    void reportEncoding() throws ReportException {
        String description = "ÄÖÜß";
        File xmlReport = xmlReportTestHelper.createXmlWithUmlauts(description);
        JqassistantReport jqassistantReport = readReport(xmlReport);
        List<ReferencableRuleType> groups = jqassistantReport.getGroupOrConceptOrConstraint();
        assertThat(groups).hasSize(1);
        ReferencableRuleType groupType = groups.get(0);
        assertThat(groupType).isInstanceOf(GroupType.class);
        GroupType defaultGrroup = (GroupType) groupType;
        List<ReferencableRuleType> concepts = defaultGrroup.getGroupOrConceptOrConstraint();
        assertThat(concepts).hasSize(1);
        ReferencableRuleType conceptType = concepts.get(0);
        assertThat(conceptType).isInstanceOf(ConceptType.class);
        ConceptType meinKonzept = (ConceptType) conceptType;
        assertThat(meinKonzept.getDescription()).isEqualTo(description);
    }

    private JqassistantReport readReport(File xmlReport) {
        return REPORT_READER.read(xmlReport);
    }

}
