package com.stardew.parsing.instructions;

import com.stardew.parsing.instructions.types.CharacterType;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CopyAs {
    private CharacterType characterType;
    private Replace replace;
}
