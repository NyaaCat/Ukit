package cat.nyaa.ukit.redbag;

import land.melon.lab.simplelanguageloader.components.Text;

public class RedbagLang {
    public Text help = Text.of(
            "&7Usage:",
            "&7    &n/u redbag <options...>",
            "&7    &n/u redbag create fixed <amount> <quantity> [password(optional)]&7: create a fixed number of red-bags, each containing specified money",
            "&7    &n/u redbag create lucky <amount> <quantity> [password(optional)]&7: create a number of random money red-bags, totally containing specified money"
    );
    public Text activeOneExist = Text.of("&7You have an active redbag at present, you can only send another redbag after it expired or being cleared");
    public Text invalidQuantity = Text.of("{input} is not a valid quantity");
    public Text invalidAmount = Text.of("{input} is not a valid amount");
    public Text fixedRedbagName = Text.of("&#b34d46Fixed Amount Redbag");
    public Text luckyRedbagName = Text.of("&#4fb346Lucky Redbag");
    public Text passwordTooLong = Text.of("&7Your password {password} is too long. Maximum length is {maxLength}");
    public Text redbagPreview = Text.of(
            "&7You are creating an redbag:",
            "&7  Type: {type}",
            "&7  Amount Total: {amount}",
            "&7  Quantity: {quantity}",
            "&7  Password: &#e38364{password}",
            "&7Continue&8? {yesButton} {noButton}"
    );
    public Text previewYesButtonText = Text.of("&7[&#79a15fYes&7]");
    public Text previewYesButtonHoverText = Text.of("&#79a15f/ukit redbag confirm");
    public Text previewNoButtonText = Text.of("&7[&#a15f5fNo&7]");
    public Text previewNoButtonHoverText = Text.of("&#a15f5f/ukit redbag cancel");
    public Text amountTooLow = Text.of("The average amount of your redbag must higher than {amount}{currencyUnit}");
    public Text quantityNotInRange = Text.of("Your quantity must in range of {minimum} to {maximum}");
    public Text cantOffer = Text.of("&7Failed to pay for your redbag. Please make sure that you have at least {amount}{currencyUnit}");
    public Text payFailure = Text.of("&7Failed to pay for your redbag due to unexpected error. Please wait for a while and try again");
    public Text redbagCancelFeedback = Text.of("&7Creation cancelled.");
    public Text redbagCreatedFeedback = Text.of("&7Your redbag has been created");
    public Text noWaitingRedbag = Text.of("&7You have no redbag waiting for confirm");
    public Text redbagBroadcast = Text.of(
            "&#e36f49&m              &r",
            "&7{player} is sending a &#e38364{type}&7!",
            "&7Password: &#e38364{password}",
            "&8 > {grabButton}&8 < ",
            "&#e36f49&m              &r"
    );
    public Text grabButtonText = Text.of("&7[&#f0ea75Click to Type Password&7]");
    public Text grabButtonHoverText = Text.of("&7Click to Type: &#e38364{password}");
    public Text duplicatePassword = Text.of("&7This password is same to the other redbag exist, please change to the other one or wait for a while");
    public Text cantUseFormatCode = Text.of("&7You may not able to use format code in passwords");
    public Text cantStartsWithSlash = Text.of("&7You may not able to start your password with \"/\"");

    public Text alreadyGrabbed = Text.of("&aYou are already grabbed this redbag!");
    public Text grabbedFeedbackToOwner = Text.of("&7{player} have grabbed {amount}{currencyUnit} from your {type}&7!");
    public Text grabbedFeedbackToGrabber = Text.of("&7You have grabbed {amount}{currencyUnit} from {owner}'s {type}&7!");
    public Text fixedRedbagDone = Text.of("&7{owner}'s {type}&7 has been cleared");
    public Text luckRedbagDone = Text.of("&7{owner}'s {type}&7 has been cleared, {luckiestOne} is the most lucky one who grabbed {amount}{currencyUnit}");
    public Text tooLate = Text.of("&7You are too late to grab this redbag");
    public Text failedToGrab = Text.of("&7Failed to grab redbag, please try again after a while");
    public Text refundFeedback = Text.of("&7Your active redbag has been expired, {amount}{currencyUnit} were refunded to your balance");
    public Text failedToRefund = Text.of("&eFailed to refund {amount}{currentUnit} to your vault, please contact server administrators. Timestamp: {timestamp}");
}
