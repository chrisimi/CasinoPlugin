package com.chrisimi.casinoplugin.menues;

import com.chrisimi.casinoplugin.main.Main;
import com.chrisimi.casinoplugin.main.MessageManager;
import com.chrisimi.casinoplugin.scripts.CasinoManager;
import com.chrisimi.casinoplugin.scripts.PlayerSignsManager;
import com.chrisimi.casinoplugin.serializables.PlayerSignsConfiguration;
import com.chrisimi.casinoplugin.utils.ItemAPI;
import com.chrisimi.inventoryapi.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SlotsCreationMenu extends Inventory implements IInventoryAPI
{
    private enum WaitingFor
    {
        NONE,
        ADDOPTION1,
        ADDOPTION2,
        ADDOPTION3,
        NEWWEIGHT,
        NEWWINMULTIPLICAND,
        BET,
        NEWOPTION
    }

    private static class Element
    {
        public String symbol;
        public double weight;
        public double winMultiplicand;

        public ItemStack changeOptionItemStack;
        public ItemStack changeWeightItemStack;
        public ItemStack changeWinMultiplicandItemStack;
        public ItemStack removeOptionItemStack;

        /**
         * get an array of {@link Element} from an saved, valid slots string
         * @param inputString string which contains the values
         * @return {@link Element} array, with a depending size of the input
         * @throws Exception if inputString is not in a valid format
         */
        public static Element[] getArray(String inputString) throws Exception
        {
            //split elements
            String[] splined = inputString.split(";");

            //check if it contains 2 '-' to ensure it is a valid string
            for(int i = 0; i < splined.length; i++)
            {
                int count = 0;
                for(char character : splined[i].toCharArray())
                    if(character == '-')
                        count++;

                if(count != 2)
                    throw new Exception("input string is invalid. Element: " + i);
            }
            Element[] result = new Element[splined.length];


            for(int i = 0; i < splined.length; i++)
            {
                //extract saved elements from string
                String[] elements = splined[i].split("-");

                //check if values are all in a valid format
                try
                {
                    result[i].symbol = elements[0];
                    result[i].winMultiplicand = Double.parseDouble(elements[1]);
                    result[i].weight = Double.parseDouble(elements[2]);
                } catch(NumberFormatException nfe)
                {
                    throw new Exception("input string is invalid. One element is not valid, element: " + i);
                }
            }

            return result;
        }

        /**
         * convert a {@link Element} array to it's string version
         * @param input {@link Element} array which should be converted
         * @return a String as the converted version of the array, can be empty if array or elements are null
         */
        public static String toString(Element[] input)
        {
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < input.length; i++)
            {
                if(input[i] == null) continue;

                if(i != 0) sb.append(";");

                sb.append(String.format("%s-%.2f-%.2f", input[i].symbol, input[i].winMultiplicand, input[i].weight));
            }

            return sb.toString();
        }
    }


    private final ItemStack addOption = ItemAPI.createItem("§6add option", Material.STONE_BUTTON);
    private final ItemStack removeOption = ItemAPI.createItem("§aremove option", Material.RED_BANNER); //when click you can remove this element
    private final ItemStack changeWeightOfOption = ItemAPI.createItem("§6change option", Material.YELLOW_BANNER); //when click you can change the chance to get it
    private final ItemStack showOption = ItemAPI.createItem("§6{option numr} {option}", Material.BLUE_BANNER); //when click you can change the item like 'A' to 'B'
    private final ItemStack changeWinMultiplicandOfOption = ItemAPI.createItem("§6{win multiplicand}", Material.BROWN_BANNER); //when click you can change the win multiplicand if you get 3 in a row

    private final ItemStack setBet = ItemAPI.createItem("§6set bet", Material.GOLD_INGOT);
    private final ItemStack setServerSign = ItemAPI.createItem("§6to server sign", Material.GOLD_BLOCK);
    private final ItemStack disableSign = ItemAPI.createItem("§4disable sign", Material.RED_WOOL);
    private final ItemStack enableSign = ItemAPI.createItem("§aenable sign", Material.GREEN_WOOL);
    private final ItemStack finishButton = ItemAPI.createItem("§afinish creation or update of slots", Material.STONE_BUTTON);

    private final ItemStack barrier = ItemAPI.createItem("", Material.PINK_STAINED_GLASS_PANE);

    private final Element[] elements = new Element[9];
    private int currAddPos = 0;
    private int currEditPos = 0;
    private WaitingFor waitingFor = WaitingFor.NONE;
    private boolean isDisabled = false;
    private double bet = Double.MIN_VALUE;
    private boolean isServerSign = false;
    private boolean allValuesValid = false;

    private final Location lrc;
    public SlotsCreationMenu(Location lrc, Player player)
    {
        super(player, 9*3, Main.getInstance(), "Slots creation menu");

        this.lrc = lrc;

        this.addEvents(this);
        openInventory();

        initialize();
        updateInventory();
    }


    public SlotsCreationMenu(PlayerSignsConfiguration conf, Player player)
    {
        this(conf.getLocation(), player);

        initializeValues(conf);

        updateInventory();
    }

    private void initializeValues(PlayerSignsConfiguration conf)
    {
        //parse plus information from sign to element array
        try
        {
            Element[] elements = Element.getArray(conf.plusinformations);
            for(int i = 0; i < elements.length && i < 9; i++)
                this.elements[i] = elements[i];

        } catch(Exception e)
        {
            player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-error-message").replace("{error}", "can't parse information of sign"));
            closeInventory();
        }

        this.bet = conf.bet;
        this.isDisabled = conf.disabled;
        this.isServerSign = conf.isServerOwner();
    }

    private void initialize()
    {
        bukkitInventory.setItem(45, setBet);
        bukkitInventory.setItem(47, setServerSign);
        bukkitInventory.setItem(53, finishButton);
    }

    private void updateInventory()
    {
        sortOptions();
        //symbols things
        bukkitInventory.setItem(currAddPos, addOption);
        updateSymbolsInventory();

        //main sign things
        bukkitInventory.setItem(51, (isDisabled) ? enableSign : disableSign);
        ItemAPI.changeName(setServerSign, (isServerSign) ? "§6to player sign" : "§6to server sign");
        bukkitInventory.setItem(47, setServerSign);

        updateFinishButton();
    }

    /**
     * sort the options in elem array so that there are no empty spaces between elements
     */
    private void sortOptions()
    {
        for(int i = 0; i < 9; i++)
        {
            for(int j = i + 1; j < 9; j++)
            {
                //move one back
                if(elements[i] == null && elements[j] != null)
                {
                    elements[i] = elements[j];
                    elements[j] = null;
                }
            }
        }
        //set the currAddPos to the next empty space

        for(int i = 0; i < 9; i++)
            if(elements[i] == null)
                currAddPos = i;

    }

    private int amountValidElements()
    {
        int result = 0;
        for(Element element : elements)
            if(element != null)
                result++;

        return result;
    }

    private void updateSymbolsInventory()
    {
        for(int i = 0; i < elements.length; i++)
        {
            if(elements[i] == null) continue;

            ItemStack changeOption = new ItemStack(showOption.getType());
            ItemStack changeChance = new ItemStack(changeWeightOfOption.getType());
            ItemStack changeWinMultiplicand = new ItemStack(changeWinMultiplicandOfOption.getType());
            ItemStack removeOptionIS = new ItemStack(removeOption.getType());

            ItemAPI.changeName(changeOption, String.format("§6%s. %s §6- change symbol", i, elements[i].symbol));
            ItemAPI.changeName(changeChance, String.format("§6%s of %s (%.2f) - change weight", elements[i].weight, getWeightSum(), elements[i].weight / getWeightSum()));
            ItemAPI.changeName(changeWinMultiplicand, String.format("§6%.1f x - change win multiplicand", elements[i].winMultiplicand));
            ItemAPI.changeName(removeOptionIS, "§4remove option");

            elements[i].changeOptionItemStack = changeOption;
            elements[i].changeWeightItemStack = changeChance;
            elements[i].changeWinMultiplicandItemStack = changeWinMultiplicand;
            elements[i].removeOptionItemStack = removeOptionIS;

            bukkitInventory.setItem(i, changeOption);
            bukkitInventory.setItem(i + 9, changeChance);
            bukkitInventory.setItem(i + 18, changeWinMultiplicand);
            bukkitInventory.setItem(i + 27, removeOptionIS);
        }
    }

    private double getWeightSum()
    {
        double sum = 0.0;
        for (Element element : elements)
            if (element != null)
                sum += element.weight;

        return sum;
    }

    private void updateFinishButton()
    {
        boolean valuesValid = true;
        List<String> lore = new ArrayList<>();

        if(bet == Double.MIN_VALUE)
        {
            lore.add("-§4 bet is not set");
            valuesValid = false;
        } else
            lore.add("-§a bet is set to " + Main.econ.format(bet));

        lore.add((isServerSign) ? "-§e sign is a server sign" : "-§e sign is a player sign");
        lore.add((isDisabled) ? "-§e sign is disabled" : "-§e sign is enabled");

        if(amountValidElements() < 3)
        {
            lore.add("-§4 sign doesn't have enough elements, minimum is 3");
            valuesValid = false;
        } else
            lore.add("-§a sign has " + amountValidElements() + " elements");

        allValuesValid = valuesValid;
        ItemAPI.changeName(finishButton, (valuesValid) ? "§6create sign" : "§4can't create sign because values are invalid or missing");
        bukkitInventory.setItem(53, finishButton);
    }
    @EventMethodAnnotation
    public void onClick(ClickEvent event)
    {
        if(event.getClicked().equals(addOption)) addOption();
        else if(event.getClicked().equals(setBet)) setBet();
        else if(event.getClicked().equals(setServerSign)) isServerSign = !isServerSign;
        else if(event.getClicked().equals(disableSign)) isDisabled = true;
        else if(event.getClicked().equals(enableSign)) isDisabled = false;
        else if(event.getClicked().equals(finishButton) && allValuesValid) finishButton();

        for(int i = 0; i < elements.length; i++)
            if(elements[i] != null)
            {
                if(event.getClicked().equals(elements[i].changeOptionItemStack)) changeOption(i);
                else if(event.getClicked().equals(elements[i].changeWeightItemStack)) changeWeight(i);
                else if(event.getClicked().equals(elements[i].changeWinMultiplicandItemStack)) changeWinMultiplicand(i);
                else if(event.getClicked().equals(elements[i].removeOptionItemStack)) removeOption(i);
            }

        updateInventory();

    }

    private void finishButton()
    {
        PlayerSignsConfiguration conf = new PlayerSignsConfiguration(this.lrc, PlayerSignsConfiguration.GameMode.SLOTS, this.player, this.bet, Element.toString(elements));

        if(isServerSign)
            conf.ownerUUID = "server";

        conf.disabled = isDisabled;

        PlayerSignsManager.addPlayerSign(conf);
        closeInventory();
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-creation-slots_successful"));

    }

    private void setBet()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-slots-set_bet"));
        closeInventory();
        waitingFor = WaitingFor.BET;
        waitforChatInput(player);
    }

    private void changeOption(int index)
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-slots-set_elem_symbol"));
        closeInventory();
        waitingFor = WaitingFor.NEWOPTION;
        waitforChatInput(player);
        currEditPos = index;
    }

    private void changeWeight(int index)
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-slots-set_elem_weight"));
        closeInventory();
        waitingFor = WaitingFor.NEWWEIGHT;
        waitforChatInput(player);
        currEditPos = index;
    }

    private void changeWinMultiplicand(int index)
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-slots-set_elem_win_multiplicand"));
        closeInventory();
        waitingFor = WaitingFor.NEWWINMULTIPLICAND;
        waitforChatInput(player);
        currEditPos = index;
    }

    private void removeOption(int index)
    {
        elements[index] = null;
    }

    private void addOption()
    {
        player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-slots-set_elem_symbol"));
        closeInventory();
        waitforChatInput(player);
        waitingFor = WaitingFor.ADDOPTION1;
    }

    @EventMethodAnnotation
    public void onChat(ChatEvent event)
    {
        switch(waitingFor)
        {
            case ADDOPTION1:
            {
                //create new element at current pos
                elements[currAddPos] = new Element();
                elements[currAddPos].symbol = event.getMessage();

                //prepare for next chat input (weight)
                player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-slots-set_elem_weight").replace("{total_weight}", String.format("%.2f", getWeightSum())));
                waitforChatInput(player);
                waitingFor = WaitingFor.ADDOPTION2;
                return;

            }
            case ADDOPTION2:
            {
                try
                {
                    //set value
                    elements[currAddPos].weight = Double.parseDouble(event.getMessage());

                    //prepare for next chat input (win multiplicand)
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-slots-set_elem_win_multiplicand"));
                    waitforChatInput(player);
                    waitingFor = WaitingFor.ADDOPTION3;
                    return;
                } catch(Exception e)
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
                    elements[currAddPos] = null;
                }
                break;
            }
            case ADDOPTION3:
            {
                try
                {
                    //set value
                    elements[currAddPos].winMultiplicand = Double.parseDouble(event.getMessage());

                    //proceed with value

                } catch(Exception e)
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
                    elements[currAddPos] = null;
                }
                break;
            }
            case NEWOPTION:
            {
                elements[currEditPos].symbol = event.getMessage();
                currEditPos = -1;
                break;
            }
            case NEWWEIGHT:
            {
                try
                {
                    elements[currEditPos].weight = Double.parseDouble(event.getMessage());
                    currEditPos = -1;
                }
                catch(Exception e)
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
                }
                break;
            }
            case NEWWINMULTIPLICAND:
            {
                try
                {
                    elements[currEditPos].winMultiplicand = Double.parseDouble(event.getMessage());
                    currEditPos = -1;
                } catch(Exception e)
                {
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
                }
                break;
            }
            case BET:
            {
                try
                {
                    bet = Double.parseDouble(event.getMessage());
                } catch(Exception e)
                {
                    bet = Double.MIN_VALUE;
                    player.sendMessage(CasinoManager.getPrefix() + MessageManager.get("creationmenu-input-double_invalid"));
                }

                break;
            }
        }

        updateInventory();
        openInventory();
        waitingFor = WaitingFor.NONE;
    }
}
