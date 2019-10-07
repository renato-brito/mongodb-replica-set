package com.silaev.mongodb.replicaset;

import com.silaev.mongodb.replicaset.converter.impl.StringToMongoRsStatusConverter;
import com.silaev.mongodb.replicaset.converter.impl.VersionConverter;
import com.silaev.mongodb.replicaset.exception.IncorrectUserInputException;
import com.silaev.mongodb.replicaset.exception.MongoNodeInitializationException;
import com.silaev.mongodb.replicaset.model.MongoRsStatus;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Konstantin Silaev on 10/4/2019
 */
@ExtendWith(MockitoExtension.class)
class MongoDbReplicaSetTest {
    @Mock
    StringToMongoRsStatusConverter converter;

    private MongoDbReplicaSet replicaSet;

    @BeforeEach
    void setUp() {
        replicaSet = spy(new MongoDbReplicaSet(converter));
    }

    @Test
    void shouldNotGetReplicaSetUrl() {
        //GIVEN
        //replicaSet

        //WHEN
        Executable executable = replicaSet::getReplicaSetUrl;

        //THEN
        assertThrows(IllegalStateException.class, executable);
    }

    @Test
    void shouldNotGetMongoRsStatus() {
        //GIVEN
        //replicaSet

        //WHEN
        Executable executable = replicaSet::getMongoRsStatus;

        //THEN
        assertThrows(IllegalStateException.class, executable);
    }

    @Test
    void shouldNotCheckMongoNodeExitCodeAfterWaiting() {
        //GIVEN
        //replicaSet

        val container = mock(GenericContainer.class);
        val execResult = mock(Container.ExecResult.class);
        val nodeName = "nodeName";
        val awaitNodeInitAttempts = 29;
        when(execResult.getExitCode())
            .thenReturn(MongoDbReplicaSet.ERROR_CONTAINER_EXIT_CODE);
        val execResultStatusCommand = mock(Container.ExecResult.class);
        doReturn(execResultStatusCommand)
            .when(replicaSet)
            .execMongoDbCommandInContainer(container, MongoDbReplicaSet.STATUS_COMMAND);
        when(execResultStatusCommand.getStdout()).thenReturn("stdout");

        //WHEN
        Executable executable = () -> replicaSet.checkMongoNodeExitCodeAfterWaiting(
            container,
            execResult,
            nodeName,
            awaitNodeInitAttempts
        );

        //THEN
        assertThrows(MongoNodeInitializationException.class, executable);
    }

    @Test
    void shouldNotCheckMongoNodeExitCode() {
        //GIVEN
        //replicaSet

        val command = "command";
        val execResult = mock(Container.ExecResult.class);
        when(execResult.getExitCode())
            .thenReturn(MongoDbReplicaSet.ERROR_CONTAINER_EXIT_CODE);
        when(execResult.getStderr()).thenReturn("stderr");

        //WHEN
        Executable executable =
            () -> replicaSet.checkMongoNodeExitCode(execResult, command);

        //THEN
        assertThrows(MongoNodeInitializationException.class, executable);
    }

    @ParameterizedTest(name = "{index}: version: {0}")
    @ValueSource(strings = {"3.6.13", "3.4.22", "2.6.22"})
    void shouldNotTestVersion(final String inputVersion) {
        //GIVEN
        //inputVersion
        MongoRsStatus status = mock(MongoRsStatus.class);
        when(converter.convert(inputVersion)).thenReturn(status);
        when(status.getVersion())
            .thenReturn(new VersionConverter().convert(inputVersion));

        //WHEN
        Executable executable = () -> replicaSet.verifyVersion(inputVersion);

        //THEN
        assertThrows(IncorrectUserInputException.class, executable);
    }
}