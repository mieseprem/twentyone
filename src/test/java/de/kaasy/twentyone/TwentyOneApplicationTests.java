package de.kaasy.twentyone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import de.kaasy.twentyone.value.YesNoCatFact;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TwentyOneApplication.class})
class TwentyOneApplicationTests {
    @Autowired
    TwentyOneApplication twentyOneApplication;

    @MockBean
    TwentyOneService twentyOneService;

    @Captor
    ArgumentCaptor<YesNoCatFact> yesNoCatFactArgumentCaptor;

    @Nested
    class DoYourWork {
        @Test
        void shouldProcessOneOfTwoDespiteOneIsFailingAtGetData() {
            // given
            doThrow(new RuntimeException("something went wrong"))
                    .when(twentyOneService)
                    .getData(eq(1));

            var aYesNoFact = YesNoCatFact.builder()
                    .withYesNoValue("yes")
                    .withYesNoPicUrl("https://dummy")
                    .withCatFact("foo")
                    .build();
            doReturn(aYesNoFact).when(twentyOneService).getData(eq(2));

            // when
            twentyOneApplication.doYourWork(2);

            // then
            verify(twentyOneService, times(2)).getData(anyInt());
            verify(twentyOneService, times(1)).storeData(anyInt(), anyInt(), yesNoCatFactArgumentCaptor.capture());
            verifyNoMoreInteractions(twentyOneService);

            assertThat(yesNoCatFactArgumentCaptor.getValue()).isEqualTo(aYesNoFact);
        }

        @Test
        void shouldProcessOneOfTwoDespiteOneIsFailingAtStoreData() {
            // given
            var aYesNoFact = YesNoCatFact.builder().build();

            doReturn(aYesNoFact).when(twentyOneService).getData(eq(1));
            doReturn(aYesNoFact).when(twentyOneService).getData(eq(2));

            doThrow(new RuntimeException("something went wrong"))
                    .when(twentyOneService)
                    .storeData(eq(1), anyInt(), any(YesNoCatFact.class));

            // when
            twentyOneApplication.doYourWork(2);

            // then
            verify(twentyOneService, times(2)).getData(anyInt());
            verify(twentyOneService, times(2))
                    .storeData(ArgumentMatchers.intThat(i -> 0 < i && i < 3), anyInt(), eq(aYesNoFact));
            verifyNoMoreInteractions(twentyOneService);
        }
    }
}
