package miniredis.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RedisObject {

    private final Object value;
    private final DataType type;
}
