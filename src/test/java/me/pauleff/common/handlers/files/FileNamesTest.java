package me.pauleff.common.handlers.files;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileNamesTest
{
    @Nested
    class StripExtension
    {
        @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
        @CsvSource({"player.dat, player",
                "archive.tar.gz, archive.tar",
                "file.name.with.dots.txt, file.name.with.dots",
                "uuid.dat_old, uuid"})
        void removesExtension_when_qualifyingDotPresent(String input, String expected)
        {
            assertEquals(expected, FileNames.stripExtension(input));
        }

        @ParameterizedTest(name = "\"{0}\" unchanged")
        @ValueSource(strings = {"README", "Makefile", ".gitignore", ".env", ""})
        void returnsUnchanged_when_noQualifyingExtension(String input)
        {
            assertEquals(input, FileNames.stripExtension(input));
        }

        @Test
        void trimsBaseName_when_spacesAroundStem()
        {
            assertEquals("player", FileNames.stripExtension(" player .dat"));
        }
    }
}
