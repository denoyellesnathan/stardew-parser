package com.stardew.parsing.instructions;

import lombok.*;

@Builder
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
