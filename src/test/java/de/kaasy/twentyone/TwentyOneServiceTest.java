package de.kaasy.twentyone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import de.kaasy.twentyone.dto.CatFactResponse;
import de.kaasy.twentyone.dto.YesNoResponse;
import de.kaasy.twentyone.value.YesNoCatFact;
import de.kaasy.twentyone.web.CatFactsClient;
import de.kaasy.twentyone.web.HttpBinClient;
import de.kaasy.twentyone.web.YesNoClient;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TwentyOneService.class})
class TwentyOneServiceTest {
    @Autowired
    TwentyOneService twentyOneService;

    @MockBean
    CatFactsClient catFactsClient;

    @MockBean
    YesNoClient yesNoClient;

    // Unused Bean with name as 'Unnamed patterns and variables are not supported at language level '21''
    @MockBean
    HttpBinClient httpBinClient;

    @Nested
    class GetData {
        @Test
        void goodCase() {
            // given
            var aCatFact = "foo";
            var aYesNoValue = "yes";
            var aYesNoPicUrl = "https://dummy";

            var aCatFactResponse = new CatFactResponse() {
                {
                    setFact(aCatFact);
                }
            };

            var aYesNoResponse = new YesNoResponse() {
                {
                    setAnswer(aYesNoValue);
                }

                {
                    setImage(aYesNoPicUrl);
                }
            };

            doReturn(aCatFactResponse).when(catFactsClient).fact();
            doReturn(aYesNoResponse).when(yesNoClient).api();

            // when
            var getDataResponse = twentyOneService.getData(1);

            // then
            assertThat(getDataResponse)
                    .isEqualTo(YesNoCatFact.builder()
                            .withCatFact(aCatFact)
                            .withYesNoValue(aYesNoValue)
                            .withYesNoPicUrl(aYesNoPicUrl)
                            .build());
        }

        @Test
        void shouldFailWithRuntimeExceptionIfFailingCatFactClient() {
            // given
            var aYesNoValue = "yes";
            var aYesNoPicUrl = "https://dummy";

            var aYesNoResponse = new YesNoResponse() {
                {
                    setAnswer(aYesNoValue);
                }

                {
                    setImage(aYesNoPicUrl);
                }
            };

            doThrow(new IllegalArgumentException("No CatFact"))
                    .when(catFactsClient)
                    .fact();
            doReturn(aYesNoResponse).when(yesNoClient).api();

            // when + then
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> twentyOneService.getData(1))
                    .withCauseInstanceOf(ExecutionException.class)
                    .havingCause()
                    .withCauseInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldFailWithRuntimeExceptionIfFailingYesNoClient() {
            // given
            var aCatFact = "foo";

            var aCatFactResponse = new CatFactResponse() {
                {
                    setFact(aCatFact);
                }
            };

            doReturn(aCatFactResponse).when(catFactsClient).fact();
            doThrow(new IllegalArgumentException("No YesNo")).when(yesNoClient).api();

            // when + then
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> twentyOneService.getData(1))
                    .withCauseInstanceOf(ExecutionException.class)
                    .havingCause()
                    .withCauseInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class StoreData {}
}
