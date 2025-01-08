package com.stardew.gui;

import com.googlecode.lanterna.gui2.dialogs.ActionListDialog;

public abstract class DialogAction implements Runnable {
    final ActionListDialog dialog;

    public DialogAction(ActionListDialog dialog) {
        this.dialog = dialog;
    }
}
