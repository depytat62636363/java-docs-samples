/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.dataflow;

// [START dataflow_bigquery_read_tablerows]
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.TypedRead.Method;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptor;

public class BiqQueryReadTableRows {
  public static void main(String[] args) {
    // Parse the pipeline options passed into the application. Example:
    //   --projectId=$PROJECT_ID
    //   --datasetName=$DATASET_NAME
    //   --tableName=$TABLE_NAME
    // For more information, see https://beam.apache.org/documentation/programming-guide/#configuring-pipeline-options
    PipelineOptionsFactory.register(BigQueryReadOptions.class);
    BigQueryReadOptions options = PipelineOptionsFactory.fromArgs(args)
        .withValidation()
        .as(BigQueryReadOptions.class);

    // Create a pipeline and apply transforms.
    Pipeline pipeline = Pipeline.create(options);
    pipeline
        // Read table data into TableRow objects.
        .apply(BigQueryIO.readTableRows()
            .from(String.format("%s:%s.%s",
                options.getProjectId(),
                options.getDatasetName(),
                options.getTableName()))
            .withMethod(Method.DIRECT_READ)
        )
        // The output from the previous step is a PCollection<TableRow>.
        .apply(MapElements
            .into(TypeDescriptor.of(TableRow.class))
            // Use TableRow to access individual fields in the row.
            .via((TableRow row) -> {
              var name = (String) row.get("user_name");
              var age = (String) row.get("age");
              System.out.printf("Name: %s, Age: %s%n", name, age);
              return row;
            }));
    pipeline.run().waitUntilFinish();
  }
}
// [END dataflow_bigquery_read_tablerows]