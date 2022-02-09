package com.github.osisoft.dataviewsample;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Properties;
import java.time.*;

import com.google.gson.Gson;

import com.github.osisoft.ocs_sample_library_preview.*;
import com.github.osisoft.ocs_sample_library_preview.sds.*;
import com.github.osisoft.ocs_sample_library_preview.dataviews.*;

public class App {
    // get appsettings
    static Appsettings appsettings = getAppsettings();

    // Get Configuration
    static String tenantId = appsettings.getTenantId();
    static String namespaceId = appsettings.getNamespaceId();

    // Sample Data Information
    static String sampleTypeId1 = "Time_SampleType1";
    static String sampleTypeId2 = "Time_SampleType2";
    static String sampleStreamId1 = "dvTank2";
    static String sampleStreamName1 = "Tank2";
    static String sampleStreamId2 = "dvTank100";
    static String sampleStreamName2 = "Tank100";
    static String sampleFieldToConsolidateTo = "temperature";
    static String sampleFieldToConsolidate = "ambient_temp";
    static String sampleFieldToAddUom1 = "pressure";
    static String sampleFieldToAddUom2 = "temperature";
    static String sampleFieldToSummarize = "pressure";
    static SdsSummaryType summaryType1 = SdsSummaryType.Mean;
    static SdsSummaryType summaryType2 = SdsSummaryType.Total;
    static Instant sampleStartTime = null;
    static Instant sampleEndTime = null;

    // Data View Information
    static String sampleDataViewId = "DataView_Sample";
    static String sampleDataViewName = "DataView_Sample_Name";
    static String sampleDataViewDescription = "A Sample Description that describes that this DataView is just used for our sample.";
    static String sampleQueryId = "stream";
    static String sampleQueryString = "dvTank*";
    static String sampleInterval = "00:20:00";

    static boolean needData = true;

    private static FieldSet findFieldSet(FieldSet[] fieldSets, String queryId) {
        for (FieldSet fieldSet : fieldSets) {
            if (fieldSet.getQueryId().equals(queryId)) {
                return fieldSet;
            }
        }
        return null;
    }

    private static Field findField(Field[] fields, FieldSource fieldSource, String fieldId) {
        for (Field field : fields) {
            if (field.getSource() == fieldSource) {
                for (String key : field.getKeys()) {
                    if (key.equals(fieldId)) {
                        return field;
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws InterruptedException {
        toRun();
        System.out.println("Complete!");
    }

    public static Boolean toRun() {
        Boolean success = true;

        System.out.println("------------------------------------------------------------------------------------");
        System.out.println(" ######                      #    #                       #    #    #     #    #    ");
        System.out.println(" #     #   ##   #####   ##   #    # # ###### #    #       #   # #   #     #   # #   ");
        System.out.println(" #     #  #  #    #    #  #  #    # # #      #    #       #  #   #  #     #  #   #  ");
        System.out.println(" #     # #    #   #   #    # #    # # #####  #    #       # #     # #     # #     # ");
        System.out.println(" #     # ######   #   ###### #    # # #      # ## # #     # #######  #   #  ####### ");
        System.out.println(" #     # #    #   #   #    #  #  #  # #      ##  ## #     # #     #   # #   #     # ");
        System.out.println(" ######  #    #   #   #    #   ##   # ###### #    #  #####  #     #    #    #     # ");
        System.out.println("------------------------------------------------------------------------------------");

        // Step 1
        System.out.println();
        System.out.println("Step 1: Authenticate against ADH");
        ADHClient adhClient = new ADHClient(appsettings.getApiVersion(), 
                                            appsettings.getTenantId(), 
                                            appsettings.getClientId(), 
                                            appsettings.getClientSecret(), 
                                            appsettings.getResource());

        try {
            // Step 2
            System.out.println();
            System.out.println("Step 2: Create types, streams, and data");
            if (needData) {
                createData(adhClient);
            }

            // Step 3
            System.out.println();
            System.out.println("Step 3: Create a data view");
            DataView dataView = new DataView(sampleDataViewId, sampleDataViewName, sampleDataViewDescription);
            adhClient.DataViews.createOrUpdateDataView(namespaceId, dataView);

            // Step 4
            System.out.println();
            System.out.println("Step 4: Retrieve the data view");
            dataView = adhClient.DataViews.getDataView(namespaceId, sampleDataViewId);
            System.out.println(adhClient.mGson.toJson(dataView));

            // Step 5
            System.out.println();
            System.out.println("Step 5: Add a query for data items");
            Query query = new Query(sampleQueryId, sampleQueryString);
            dataView.setQueries(new Query[] { query });
            adhClient.DataViews.createOrUpdateDataView(namespaceId, dataView);

            // Step 6
            System.out.println();
            System.out.println("Step 6: View items found by the query");
            System.out.println("List data items found by the query:");
            ResolvedItems<DataItem> dataItems = adhClient.DataViews.getDataItemsByQuery(namespaceId, sampleDataViewId,
                    sampleQueryId);
            System.out.println(adhClient.mGson.toJson(dataItems));

            System.out.println("List ineligible data items found by the query:");
            dataItems = adhClient.DataViews.getIneligibleDataItemsByQuery(namespaceId, sampleDataViewId, sampleQueryId);
            System.out.println(adhClient.mGson.toJson(dataItems));

            // Step 7
            System.out.println();
            System.out.println("Step 7: View fields available to include in the data view");
            ResolvedItems<FieldSet> availableFields = adhClient.DataViews.getAvailableFieldSets(namespaceId,
                    sampleDataViewId);
            System.out.println(adhClient.mGson.toJson(availableFields));

            // Step 8
            System.out.println();
            System.out.println("Step 8: Include some of the available fields");
            dataView.setDataFieldSets(availableFields.getItems());
            adhClient.DataViews.createOrUpdateDataView(namespaceId, dataView);

            System.out.println("List available field sets:");
            availableFields = adhClient.DataViews.getAvailableFieldSets(namespaceId, sampleDataViewId);
            System.out.println(adhClient.mGson.toJson(availableFields));

            System.out.println("Retrieving interpolated data from the data view:");
            String dataViewInterpData = adhClient.DataViews.getDataViewInterpolatedData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString(), sampleInterval).getResponse();
            System.out.println(dataViewInterpData);
            assert dataViewInterpData.length() > 0 : "Error getting data view interpolated data";

            System.out.println("Retrieving stored data from the data view:");
            String dataViewStoredData = adhClient.DataViews.getDataViewStoredData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString()).getResponse();
            System.out.println(dataViewStoredData);
            assert dataViewStoredData.length() > 0 : "Error getting data view stored data";

            // Step 9
            System.out.println();
            System.out.println("Step 9: Group the data view");
            Field grouping = new Field(FieldSource.Id, null, "{DistinguisherValue} {FirstKey}");
            dataView.setGroupingFields(new Field[] { grouping });
            adhClient.DataViews.createOrUpdateDataView(namespaceId, dataView);

            System.out.println("Retrieving interpolated data from the data view:");
            dataViewInterpData = adhClient.DataViews.getDataViewInterpolatedData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString(), sampleInterval).getResponse();
            System.out.println(dataViewInterpData);
            assert dataViewInterpData.length() > 0 : "Error getting data view interpolated data";

            System.out.println("Retrieving stored data from the data view:");
            dataViewStoredData = adhClient.DataViews.getDataViewStoredData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString()).getResponse();
            System.out.println(dataViewStoredData);
            assert dataViewStoredData.length() > 0 : "Error getting data view stored data";

            // Step 10
            System.out.println();
            System.out.println("Step 10: Identify data items");
            FieldSet dvDataItemFieldSet = findFieldSet(dataView.getDataFieldSets(), sampleQueryId);
            assert dvDataItemFieldSet != null : "Error finding field set";
            dvDataItemFieldSet.setIdentifyingField(dataView.getGroupingFields()[0]);
            dataView.setGroupingFields(new Field[0]);
            adhClient.DataViews.createOrUpdateDataView(namespaceId, dataView);

            System.out.println("Retrieving interpolated data from the data view:");
            dataViewInterpData = adhClient.DataViews.getDataViewInterpolatedData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString(), sampleInterval).getResponse();
            System.out.println(dataViewInterpData);
            assert dataViewInterpData.length() > 0 : "Error getting data view interpolated data";

            System.out.println("Retrieving stored data from the data view:");
            dataViewStoredData = adhClient.DataViews.getDataViewStoredData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString()).getResponse();
            System.out.println(dataViewStoredData);
            assert dataViewStoredData.length() > 0 : "Error getting data view stored data";

            // Step 11
            System.out.println();
            System.out.println("Step 11: Consolidate data fields");
            Field field1 = findField(dvDataItemFieldSet.getDataFields(), FieldSource.PropertyId,
                    sampleFieldToConsolidateTo);
            Field field2 = findField(dvDataItemFieldSet.getDataFields(), FieldSource.PropertyId,
                    sampleFieldToConsolidate);
            assert field1 != null : "Error finding data field";
            assert field2 != null : "Error finding data field";
            System.out.println(adhClient.mGson.toJson(field1));
            System.out.println(adhClient.mGson.toJson(field2));
            ArrayList<String> keys = new ArrayList<String>(Arrays.asList(field1.getKeys()));
            keys.add(sampleFieldToConsolidate);
            field1.setKeys(Arrays.copyOf(keys.toArray(), keys.size(), String[].class));
            ArrayList<Field> fields = new ArrayList<Field>(Arrays.asList(dvDataItemFieldSet.getDataFields()));
            fields.remove(field2);
            dvDataItemFieldSet.setDataFields(Arrays.copyOf(fields.toArray(), fields.size(), Field[].class));
            adhClient.DataViews.createOrUpdateDataView(namespaceId, dataView);

            System.out.println("Retrieving interpolated data from the data view:");
            dataViewInterpData = adhClient.DataViews.getDataViewInterpolatedData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString(), sampleInterval).getResponse();
            System.out.println(dataViewInterpData);
            assert dataViewInterpData.length() > 0 : "Error getting data view interpolated data";

            System.out.println("Retrieving stored data from the data view:");
            dataViewStoredData = adhClient.DataViews.getDataViewStoredData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString()).getResponse();
            System.out.println(dataViewStoredData);
            assert dataViewStoredData.length() > 0 : "Error getting data view stored data";

            // Step 12
            System.out.println();
            System.out.println("Step 12: Add Units of Measure Column");
            field1 = findField(dvDataItemFieldSet.getDataFields(), FieldSource.PropertyId,
                    sampleFieldToAddUom1);
            field2 = findField(dvDataItemFieldSet.getDataFields(), FieldSource.PropertyId,
                    sampleFieldToAddUom2);
            assert field1 != null : "Error finding data field";
            assert field2 != null : "Error finding data field";
            System.out.println(adhClient.mGson.toJson(field1));
            System.out.println(adhClient.mGson.toJson(field2));

            field1.setIncludeUom(true);
            field2.setIncludeUom(true);
            adhClient.DataViews.createOrUpdateDataView(namespaceId, dataView);

            System.out.println("Retrieving interpolated data from the data view:");
            dataViewInterpData = adhClient.DataViews.getDataViewInterpolatedData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString(), sampleInterval).getResponse();
            System.out.println(dataViewInterpData);
            assert dataViewInterpData.length() > 0 : "Error getting data view interpolated data";

            System.out.println("Retrieving stored data from the data view:");
            dataViewStoredData = adhClient.DataViews.getDataViewStoredData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString()).getResponse();
            System.out.println(dataViewStoredData);
            assert dataViewStoredData.length() > 0 : "Error getting data view stored data";

            // Step 13
            System.out.println();
            System.out.println("Step 13: Add Summaries Columns");
            field1 = findField(dvDataItemFieldSet.getDataFields(), FieldSource.PropertyId,
                    sampleFieldToSummarize);
            assert field1 != null : "Error finding data field";
            System.out.println(adhClient.mGson.toJson(field1));

            Field summaryField1 = new Field(field1);
            Field summaryField2 = new Field(field1);

            summaryField1.setSummaryDirection(SummaryDirection.Forward);
            summaryField1.setSummaryType(summaryType1);
            summaryField2.setSummaryDirection(SummaryDirection.Forward);
            summaryField2.setSummaryType(summaryType2);

            fields = new ArrayList<Field>(Arrays.asList(dvDataItemFieldSet.getDataFields()));
            fields.add(summaryField1);
            fields.add(summaryField2);
            dvDataItemFieldSet.setDataFields(Arrays.copyOf(fields.toArray(), fields.size(), Field[].class));
            adhClient.DataViews.createOrUpdateDataView(namespaceId, dataView);
            adhClient.DataViews.createOrUpdateDataView(namespaceId, dataView);

            System.out.println("Retrieving interpolated data from the data view:");
            dataViewInterpData = adhClient.DataViews.getDataViewInterpolatedData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString(), sampleInterval).getResponse();
            System.out.println(dataViewInterpData);
            assert dataViewInterpData.length() > 0 : "Error getting data view interpolated data";

            System.out.println("Retrieving stored data from the data view:");
            dataViewStoredData = adhClient.DataViews.getDataViewStoredData(namespaceId, sampleDataViewId,
                    sampleStartTime.toString(), sampleEndTime.toString()).getResponse();
            System.out.println(dataViewStoredData);
            assert dataViewStoredData.length() > 0 : "Error getting data view stored data";

        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            // Step 14
            System.out.println();
            System.out.println("Step 14: Delete sample objects from ADH");
            try {
                System.out.println("Deleting data view...");
                adhClient.DataViews.deleteDataView(namespaceId, sampleDataViewId);
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
            if (needData) {
                cleanUp(adhClient);
            }
        }
        return success;
    }

    private static void createData(ADHClient adhClient) throws Exception {
        try {
            SdsType doubleType = new SdsType("doubleType", "", "", SdsTypeCode.Double);
            SdsType dateTimeType = new SdsType("dateTimeType", "", "", SdsTypeCode.DateTime);

            SdsTypeProperty pressureDoubleProperty = new SdsTypeProperty("pressure", "", "", doubleType, false, "bar");
            SdsTypeProperty temperatureDoubleProperty = new SdsTypeProperty(sampleFieldToConsolidateTo, "", "",
                    doubleType, false, "degree Celsius");
            SdsTypeProperty ambientTemperatureDoubleProperty = new SdsTypeProperty(sampleFieldToConsolidate, "", "",
                    doubleType, false, "degree Celsius");
            SdsTypeProperty timeDateTimeProperty = new SdsTypeProperty("time", "", "", dateTimeType, true);

            SdsType sdsType1 = new SdsType(sampleTypeId1, "", "", SdsTypeCode.Object,
                    new SdsTypeProperty[] { pressureDoubleProperty, temperatureDoubleProperty, timeDateTimeProperty });
            SdsType sdsType2 = new SdsType(sampleTypeId2, "", "", SdsTypeCode.Object, new SdsTypeProperty[] {
                    pressureDoubleProperty, ambientTemperatureDoubleProperty, timeDateTimeProperty });

            System.out.println("Creating SDS Types...");
            adhClient.Types.createType(tenantId, namespaceId, sdsType1);
            adhClient.Types.createType(tenantId, namespaceId, sdsType2);

            SdsStream stream1 = new SdsStream(sampleStreamId1, sampleTypeId1, "", sampleStreamName1);
            SdsStream stream2 = new SdsStream(sampleStreamId2, sampleTypeId2, "", sampleStreamName2);

            System.out.println("Creating SDS Streams...");
            adhClient.Streams.createStream(tenantId, namespaceId, stream1);
            adhClient.Streams.createStream(tenantId, namespaceId, stream2);

            sampleStartTime = Instant.now().minus(Duration.ofHours(1));
            sampleEndTime = Instant.now();

            ArrayList<String> values1 = new ArrayList<String>();
            ArrayList<String> values2 = new ArrayList<String>();

            System.out.println("Generating values...");
            for (int i = 1; i < 30; i += 1) {
                String val1 = ("{\"time\" : \"" + sampleStartTime.plus(Duration.ofMinutes(i * 2)) + "\", \"pressure\":"
                        + Math.random() * 100 + ", \"" + sampleFieldToConsolidateTo + "\":" + (Math.random() * 20) + 50
                        + "}");
                String val2 = ("{\"time\" : \"" + sampleStartTime.plus(Duration.ofMinutes(i * 2)) + "\", \"pressure\":"
                        + Math.random() * 100 + ", \"" + sampleFieldToConsolidate + "\":" + (Math.random() * 20) + 50
                        + "}");
                values1.add(val1);
                values2.add(val2);
            }

            String pVals = "[" + String.join(",", values1) + "]";
            String tVals = "[" + String.join(",", values2) + "]";

            System.out.println("Sending values...");
            adhClient.Streams.updateValues(tenantId, namespaceId, sampleStreamId1, pVals);
            adhClient.Streams.updateValues(tenantId, namespaceId, sampleStreamId2, tVals);
        } catch (Exception e) {
            printError("Error creating Sds Objects", e);
            throw e;
        }
    }

    /**
     * Prints out a formated error string
     *
     * @param exceptionDescription - the description of what the error is
     * @param exception            - the exception thrown
     */
    private static void printError(String exceptionDescription, Exception exception) {
        System.out.println("\n\n======= " + exceptionDescription + " =======");
        System.out.println(exception.toString());
        System.out.println("======= End of " + exceptionDescription + " =======");
    }

    private static Appsettings getAppsettings() {
        Gson mGson = new Gson();

        Appsettings appsettings = new Appsettings();
        System.out.println(new File(".").getAbsolutePath());

        try (InputStream inputStream = new FileInputStream("appsettings.json")) {
            String fileString = null;
            int bytesToRead = inputStream.available();

            if (bytesToRead > 0) {
                byte[] bytes = new byte[bytesToRead];
                if (inputStream.read(bytes) != bytesToRead)
                    System.out.println("Error reading input stream");
                fileString = new String(bytes);
            }

            appsettings = mGson.fromJson(fileString, Appsettings.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return appsettings;
    }

    public static void cleanUp(ADHClient adhClient) {
        System.out.println("Deleting sample streams...");
        try {
            adhClient.Streams.deleteStream(tenantId, namespaceId, sampleStreamId1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            adhClient.Streams.deleteStream(tenantId, namespaceId, sampleStreamId2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Deleting sample types...");
        try {
            adhClient.Types.deleteType(tenantId, namespaceId, sampleTypeId1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            adhClient.Types.deleteType(tenantId, namespaceId, sampleTypeId2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
