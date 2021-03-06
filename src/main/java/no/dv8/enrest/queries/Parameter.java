package no.dv8.enrest.queries;

import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@EqualsAndHashCode
@Getter
@Builder
@AllArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE)
public class Parameter {

    String name;
    String javaType;
    String htmlType = "text";
    @Setter
    String value;
}
