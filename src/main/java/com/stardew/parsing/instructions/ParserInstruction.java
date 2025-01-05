package com.stardew.parsing.instructions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParserInstruction {
    private boolean clearPlayers;
    private boolean clearFarmers;
    private String fromFile;
    private String toFile;
    private Character character;
}
