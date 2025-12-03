package com.hibersoft.ms.bankcustomer.datamodeling.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.hibersoft.ms.bankcustomer.datamodeling.model.RawSourceData;

@Component
public class DynamicOutputPathStepListener implements StepExecutionListener {

    @Autowired
    private FlatFileItemWriter<RawSourceData> stagingWriter;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // Get the dynamic output path from the Job Parameters (safe to do at this
        // point)
        String bankId = stepExecution.getJobParameters().getString("bankId");
        String uniquePath = "/opt/data/staging/ingestion_output_" + bankId + "_" + System.currentTimeMillis() + ".csv";

        // Update the writer bean's resource property at runtime
        stagingWriter.setResource(new FileSystemResource(uniquePath));

        // Store the actual path in the Step context so the completion listener can find
        // it later
        stepExecution.getExecutionContext().putString("actualOutputPath", uniquePath);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
