package de.diemex.scoreboardnotifier.message;


import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents one line in a scoreboard
 */
public class MsgLineHolder
{
    private StringBuilder sb = new StringBuilder();
    private ChatColor lineColor = null;
    private String prefix = "";
    private String suffix = "";


    public int length()
    {
        //Reserve two chars per line for the color code
        return ((lineColor != null) ? 2 : 0) +
                prefix.length() + sb.length() + suffix.length();
    }


    public String getLineText()
    {
        return (lineColor != null ? lineColor : "") + prefix + sb.toString() + suffix;
    }


    public String getPlainLine()
    {
        return sb.toString();
    }


    public MsgLineHolder append(String piece)
    {
        sb.append(piece);
        return this;
    }


    public MsgLineHolder setSuffix(String suffix)
    {
        this.suffix = suffix;
        return this;
    }


    public MsgLineHolder setPrefix(String prefix)
    {
        this.prefix = prefix;
        return this;
    }


    public MsgLineHolder setLineColor(ChatColor lineColor)
    {
        this.lineColor = lineColor;
        return this;
    }


    public static List<String> toString(List<MsgLineHolder> msg)
    {
        List<String> output = new ArrayList<>(msg.size());
        output.addAll(msg.stream().map(MsgLineHolder::getLineText).collect(Collectors.toList()));
        return output;
    }


    public static List<MsgLineHolder> fromString(List<String> msg, ChatColor lineColor)
    {
        List<MsgLineHolder> output = msg.stream().map(line -> new MsgLineHolder().append(line).setLineColor(lineColor)).collect(Collectors.toList());
        return output;
    }


    @Override
    public boolean equals(Object other)
    {
        return other instanceof MsgLineHolder
                && this.getPlainLine().equals(((MsgLineHolder) other).getPlainLine());
    }


    /**
     * Return the line as it will be printed
     */
    @Override
    public String toString()
    {
        return (lineColor == null ? "" : lineColor) + prefix + getLineText() + suffix;
    }
}
