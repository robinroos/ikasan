package org.ikasan.ootb.scheduler.agent.module.component.filter;

import org.ikasan.component.endpoint.filesystem.messageprovider.CorrelatedFileList;
import org.ikasan.ootb.scheduler.agent.module.component.filter.configuration.FileAgeFilterConfiguration;
import org.ikasan.spec.component.filter.FilterException;
import org.ikasan.spec.scheduled.dryrun.DryRunModeService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileAgeFilterTest {

    @Mock
    File file;

    @Mock
    private DryRunModeService dryRunModeService;

    @Test(expected = FilterException.class)
    public void test_exception_empty_file_list() {
        FileAgeFilterConfiguration configuration = new FileAgeFilterConfiguration();
        configuration.setFileAgeSeconds(30);

        FileAgeFilter fileAgeFilter = new FileAgeFilter(this.dryRunModeService);
        fileAgeFilter.setConfiguration(configuration);

        CorrelatedFileList correlatedFileList = new CorrelatedFileList(List.of()
            , "correlationIdentifier");

        fileAgeFilter.filter(correlatedFileList);
    }

    @Test
    public void test_filter_accept_success() {
        when(this.file.lastModified()).thenReturn(System.currentTimeMillis() - 50000);
        when(this.dryRunModeService.getDryRunMode()).thenReturn(false);
        when(dryRunModeService.isJobDryRun(any(String.class))).thenReturn(false);
        FileAgeFilterConfiguration configuration = new FileAgeFilterConfiguration();
        configuration.setFileAgeSeconds(30);
        configuration.setJobName("jobName");

        FileAgeFilter fileAgeFilter = new FileAgeFilter(this.dryRunModeService);
        fileAgeFilter.setConfiguration(configuration);

        CorrelatedFileList correlatedFileList = new CorrelatedFileList(List.of(file)
            , "correlationIdentifier");

        Assert.assertNotNull(fileAgeFilter.filter(correlatedFileList));
    }

    @Test
    public void test_filter_accept_success_multiple_files() {
        when(file.lastModified()).thenReturn(System.currentTimeMillis() - 50000);
        when(dryRunModeService.isJobDryRun(any(String.class))).thenReturn(false);
        FileAgeFilterConfiguration configuration = new FileAgeFilterConfiguration();
        configuration.setFileAgeSeconds(30);
        configuration.setJobName("jobName");

        FileAgeFilter fileAgeFilter = new FileAgeFilter(this.dryRunModeService);
        fileAgeFilter.setConfiguration(configuration);

        CorrelatedFileList correlatedFileList = new CorrelatedFileList(List.of(file, file)
            , "correlationIdentifier");

        Assert.assertNotNull(fileAgeFilter.filter(correlatedFileList));
    }

    @Test
    public void test_filter_filter_success() {
        when(file.lastModified()).thenReturn(System.currentTimeMillis());
        when(this.dryRunModeService.getDryRunMode()).thenReturn(false);
        when(dryRunModeService.isJobDryRun(any(String.class))).thenReturn(false);
        FileAgeFilterConfiguration configuration = new FileAgeFilterConfiguration();
        configuration.setFileAgeSeconds(30);
        configuration.setJobName("jobName");

        FileAgeFilter fileAgeFilter = new FileAgeFilter(this.dryRunModeService);
        fileAgeFilter.setConfiguration(configuration);

        CorrelatedFileList correlatedFileList = new CorrelatedFileList(List.of(file)
            , "correlationIdentifier");

        Assert.assertNull(fileAgeFilter.filter(correlatedFileList));
    }

    @Test
    public void test_filter_filter_success_multiple_files() {
        when(file.lastModified()).thenReturn(System.currentTimeMillis());
        when(this.dryRunModeService.getDryRunMode()).thenReturn(false);
        when(dryRunModeService.isJobDryRun(any(String.class))).thenReturn(false);
        FileAgeFilterConfiguration configuration = new FileAgeFilterConfiguration();
        configuration.setFileAgeSeconds(30);
        configuration.setJobName("jobName");

        FileAgeFilter fileAgeFilter = new FileAgeFilter(this.dryRunModeService);
        fileAgeFilter.setConfiguration(configuration);

        CorrelatedFileList correlatedFileList = new CorrelatedFileList(List.of(file, file)
            , "correlationIdentifier");

        Assert.assertNull(fileAgeFilter.filter(correlatedFileList));
    }

    @Test
    public void test_filter_dry_run_success() {
        when(this.dryRunModeService.getDryRunMode()).thenReturn(true);
        FileAgeFilterConfiguration configuration = new FileAgeFilterConfiguration();
        configuration.setFileAgeSeconds(30);
        configuration.setJobName("jobName");

        FileAgeFilter fileAgeFilter = new FileAgeFilter(this.dryRunModeService);
        fileAgeFilter.setConfiguration(configuration);

        CorrelatedFileList correlatedFileList = new CorrelatedFileList(List.of(file)
            , "correlationIdentifier");

        Assert.assertNotNull(fileAgeFilter.filter(correlatedFileList));
    }

    @Test
    public void test_filter_job_dry_run_success() {
        when(this.dryRunModeService.getDryRunMode()).thenReturn(false);
        when(dryRunModeService.isJobDryRun(any(String.class))).thenReturn(true);
        FileAgeFilterConfiguration configuration = new FileAgeFilterConfiguration();
        configuration.setFileAgeSeconds(30);
        configuration.setJobName("jobName");

        FileAgeFilter fileAgeFilter = new FileAgeFilter(this.dryRunModeService);
        fileAgeFilter.setConfiguration(configuration);

        CorrelatedFileList correlatedFileList = new CorrelatedFileList(List.of(file)
            , "correlationIdentifier");

        Assert.assertNotNull(fileAgeFilter.filter(correlatedFileList));
    }
}
