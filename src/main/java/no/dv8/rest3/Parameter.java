package no.dv8.rest3;

import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode
@Getter
@Builder
@FieldDefaults( level = AccessLevel.PRIVATE)
public class Parameter {

    String name;
    String javaType;
    String htmlType = "text";
    @Setter
    String value;
}
