package com.stardew.parsing.instructions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Player {
    private Long uid;
    private CopyAs copyAs;
}
