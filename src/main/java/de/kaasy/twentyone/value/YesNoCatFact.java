package de.kaasy.twentyone.value;

import lombok.Builder;
import lombok.Value;

@Builder(setterPrefix = "with")
@Value
public class YesNoCatFact {
    String yesNoValue;
    String yesNoPicUrl;
    String catFact;
}
