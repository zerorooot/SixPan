package com.github.zerorooot.util;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.ClipboardOwner;

/**
 * @Author: zero
 * @Date: 2020/8/23 19:58
 *
 */
public class ClipBoardUtil implements ClipboardOwner {
    /**
     * 把文本内容输出到系统剪贴板
     * @param text 文本
     */
    public static void setClipboardString(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 封装文本内容
        Transferable trans = new StringSelection(text);
        // 把文本内容设置到系统剪贴板
        clipboard.setContents(trans, null);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable transferable) {}

}
