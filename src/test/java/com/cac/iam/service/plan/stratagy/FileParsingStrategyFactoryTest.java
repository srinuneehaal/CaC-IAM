package com.cac.iam.service.plan.stratagy;

import com.cac.iam.exception.UnsupportedFileCategoryException;
import com.cac.iam.exception.UnsupportedFilePathException;
import com.cac.iam.model.FileCategory;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class FileParsingStrategyFactoryTest {

    @Test
    void resolvesByPathAndCategory() {
        FileParsingStrategy strategy = mock(FileParsingStrategy.class);
        when(strategy.getCategory()).thenReturn(FileCategory.POLICIES);
        when(strategy.supports(any(Path.class))).thenReturn(true);
        FileParsingStrategyFactory factory = new FileParsingStrategyFactory(List.of(strategy));

        factory.resolve(Path.of("file.json"));
        factory.resolve(FileCategory.POLICIES);

        verify(strategy, times(1)).supports(any(Path.class));
    }

    @Test
    void throwsForUnsupportedPathAndCategory() {
        FileParsingStrategy strategy = mock(FileParsingStrategy.class);
        when(strategy.getCategory()).thenReturn(FileCategory.POLICIES);
        when(strategy.supports(any(Path.class))).thenReturn(false);
        FileParsingStrategyFactory factory = new FileParsingStrategyFactory(List.of(strategy));

        assertThatThrownBy(() -> factory.resolve(Path.of("x"))).isInstanceOf(UnsupportedFilePathException.class);
        assertThatThrownBy(() -> factory.resolve(FileCategory.ROLES)).isInstanceOf(UnsupportedFileCategoryException.class);
    }
}
