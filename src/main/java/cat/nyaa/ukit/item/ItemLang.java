package cat.nyaa.ukit.item;

import land.melon.lab.simplelanguageloader.components.Text;

public class ItemLang {
    public Text help = Text.of(
            "&7Usage:",
            "&7    /ukit item name <name...>: change the item name in the main hand"
    );
    public Text noItemInHand = Text.of("&7Please hold an item in your hand before using this command");
    public Text cantOffer = Text.of("&7You need at least {amount}{currencyUnit} to change the name of items in your hand");
    public Text success =Text.of("&7You have changed the name of the items in your hand to {name}&7, costing {amount}{currencyUnit}.");
    public Text nameTooLong = Text.of("&cYou couldn't rename your item with name exceed {max} characters.");
}
