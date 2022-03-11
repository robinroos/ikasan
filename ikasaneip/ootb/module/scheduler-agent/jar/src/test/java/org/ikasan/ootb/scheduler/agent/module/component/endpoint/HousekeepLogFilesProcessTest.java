package org.ikasan.ootb.scheduler.agent.module.component.endpoint;

import org.apache.commons.io.FileUtils;
import org.ikasan.ootb.scheduler.agent.module.configuration.HousekeepLogFilesProcessConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;

public class HousekeepLogFilesProcessTest {

    HousekeepLogFilesProcessConfiguration configuration;
    HousekeepLogFilesProcess housekeepLogFilesProcess;

    @Before
    public void setup() throws IOException {

        configuration = new HousekeepLogFilesProcessConfiguration();
        configuration.setLogFolder("src/test/resources/data/housekeep");
        configuration.setFolderToMove("src/test/resources/data/housekeep/archive");
        configuration.setTimeToLive(100);

        housekeepLogFilesProcess = new HousekeepLogFilesProcess();
        housekeepLogFilesProcess.setConfiguration(configuration);

        Files.createDirectory(Paths.get(configuration.getLogFolder()));
        Files.createDirectory(Paths.get(configuration.getFolderToMove()));

        LocalDate oldDate = LocalDate.of(2020, 12, 31);
        Instant instant = oldDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Path path1 = Paths.get(configuration.getLogFolder(), "logFile-1.txt");
        Path path2 = Paths.get(configuration.getLogFolder(), "logFile-2.txt");
        Path path3 = Paths.get(configuration.getLogFolder(), "logFile-3.txt");

        Files.createFile(path1);
        Files.createFile(path2);
        Files.createFile(path3);

        Files.setLastModifiedTime(path1, FileTime.from(instant));
        Files.setLastModifiedTime(path2, FileTime.from(instant));
    }

    @After
    public void clean() throws IOException {
        FileUtils.deleteDirectory(new File("src/test/resources/data/housekeep"));
    }

    @Test
    public void test_housekeep_delete() {

        Assert.assertEquals(3, FileUtils.listFiles(new File("src/test/resources/data/housekeep"), null, false).size());

        housekeepLogFilesProcess.invoke(null);

        Collection<File> files = FileUtils.listFiles(new File("src/test/resources/data/housekeep"), null, false);

        Assert.assertEquals(1, files.size());
        Assert.assertEquals("logFile-3.txt", files.stream().findFirst().get().getName() );

    }


}
