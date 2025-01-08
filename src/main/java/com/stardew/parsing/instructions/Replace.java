package com.stardew.parsing.instructions;

import com.stardew.parsing.instructions.types.CharacterType;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Replace {
    private CharacterType characterType;
    private Long uid;
}
