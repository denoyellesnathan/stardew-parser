package com.stardew.parsing.instructions;

import com.stardew.parsing.instructions.types.EntityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Replace {
    private EntityType type;
    private Long uid;
}
