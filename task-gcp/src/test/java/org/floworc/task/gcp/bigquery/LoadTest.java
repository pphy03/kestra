package org.floworc.task.gcp.bigquery;

import com.devskiller.friendly_id.FriendlyId;
import com.google.common.collect.ImmutableMap;
import io.micronaut.context.annotation.Value;
import io.micronaut.test.annotation.MicronautTest;
import org.floworc.core.Utils;
import org.floworc.core.runners.RunContext;
import org.floworc.core.runners.RunOutput;
import org.floworc.core.storages.StorageInterface;
import org.floworc.core.storages.StorageObject;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@MicronautTest
class LoadTest {
    @Inject
    private StorageInterface storageInterface;

    @Value("${floworc.tasks.bigquery.project}")
    private String project;

    @Value("${floworc.tasks.bigquery.dataset}")
    private String dataset;

    @Test
    void fromCsv() throws Exception {
        StorageObject source = storageInterface.put(
            new URI("/" + FriendlyId.createFriendlyId()),
            new FileInputStream(new File(Objects.requireNonNull(LoadTest.class.getClassLoader()
                .getResource("bigquery/insurance_sample.csv"))
                .toURI()))
        );

        Load task = Load.builder()
            .id(LoadTest.class.getSimpleName())
            .type(Load.class.getName())
            .from(source.getUri().toString())
            .destinationTable(project + "." + dataset + "." + FriendlyId.createFriendlyId())
            .format(AbstractLoad.Format.CSV)
            .autodetect(true)
            .csvOptions(AbstractLoad.CsvOptions.builder()
                .fieldDelimiter("|")
                .allowJaggedRows(true)
                .build()
            )
            .build();

        RunContext runContext = Utils.mockRunContext(storageInterface, task, ImmutableMap.of());

        RunOutput run = task.run(runContext);

        assertThat(run.getOutputs().get("rows"), is(5L));
    }

    @Test
    void fromAvro() throws Exception {
        StorageObject source = storageInterface.put(
            new URI("/" + FriendlyId.createFriendlyId()),
            new FileInputStream(new File(Objects.requireNonNull(LoadTest.class.getClassLoader()
                .getResource("bigquery/insurance_sample.avro"))
                .toURI()))
        );

        Load task = Load.builder()
            .id(LoadTest.class.getSimpleName())
            .type(Load.class.getName())
            .from(source.getUri().toString())
            .destinationTable(project + "." + dataset + "." + FriendlyId.createFriendlyId())
            .format(AbstractLoad.Format.AVRO)
            .avroOptions(AbstractLoad.AvroOptions.builder()
                .useAvroLogicalTypes(true)
                .build()
            )
            .build();

        RunContext runContext = Utils.mockRunContext(storageInterface, task, ImmutableMap.of());

        RunOutput run = task.run(runContext);
        assertThat(run.getOutputs().get("rows"), is(5L));
    }
}