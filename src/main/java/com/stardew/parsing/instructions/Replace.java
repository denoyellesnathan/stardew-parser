package com.stardew.parsing.instructions;

import com.stardew.parsing.instructions.types.CharacterType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Replace {
    private CharacterType characterType;
    private Long uid;
}
