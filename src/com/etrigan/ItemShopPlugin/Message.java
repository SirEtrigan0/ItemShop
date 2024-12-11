package com.etrigan.ItemShopPlugin;

import net.md_5.bungee.api.ChatColor;

public enum Message {

	COST("§7Cost: "),
	PURCH("§eClick to purchase!"),
	SUCS_PURCH("§aSuccessfully purchased "),
	MORE_MSG1("§cYou need "),
	MORE_MSG2(" to make this purchase!");
	
	
	private String defaultMessage;
	
	Message(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}
	
	public void setDefaultMessage(String defaultMessage) {
		String toSet = ChatColor.translateAlternateColorCodes('&', defaultMessage);
		if(toSet.equals("")) return;
		this.defaultMessage = toSet;
	}
	@Override
	public String toString() {
		return this.defaultMessage;
	}
}
