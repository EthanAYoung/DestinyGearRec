import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GearRec {
	
	public static HashMap<String, ArrayList<Armor>> armors;
	public static String bestCombo;
	public static String[] slots;
	public static int mostThresholdsHit;
	
	public static class Armor {
		
		String name;
		int mobility;
		int resilience;
		int recovery;
		int discipline;
		int intellect;
		int strength;
		
		public Armor(String n, int mob, int res, int rec, int dis, int intel, int str) {
			name = n;
			mobility = mob;
			resilience = res;
			recovery = rec;
			discipline = dis;
			intellect = intel;
			strength = str;
		}
		
	}

	public static void main(String[] args) throws IOException {
		slots = new String[4];
		slots[0] = "head";
		slots[1] = "arms";
		slots[2] = "chest";
		slots[3] = "legs";
		getArmors();
		getBestCombo();
		
		System.out.println(bestCombo);
	}
	
	public static void getArmors() throws IOException {
		armors = new HashMap<String, ArrayList<Armor>>();
		armors.put("head", new ArrayList<Armor>());
		armors.put("arms", new ArrayList<Armor>());
		armors.put("chest", new ArrayList<Armor>());
		armors.put("legs", new ArrayList<Armor>());
		
		JsonObject profile = queryBungie("https://www.bungie.net/platform/Destiny2/3/Profile/destinyMembershipId");
		JsonObject inventory = profile.getAsJsonObject("Response").getAsJsonObject("data").getAsJsonObject("profileInventory");
		JsonArray items = inventory.getAsJsonObject("data").getAsJsonArray("items");
		ArrayList<Integer> lockables = new ArrayList<Integer>();
		for (int i = 0; i < items.size(); i++) {
			JsonObject curr = items.get(i).getAsJsonObject();
			if (curr.get("lockable").getAsBoolean()) {
				lockables.add(curr.get("itemInstanceId").getAsInt());
			}
		}
		for (int i = 0; i < lockables.size(); i++) {
			JsonObject item = queryBungie("https://www.bungie.net/platform/Destiny2/3/Profile/destinyMembershipId/Item/"+lockables.get(i));
			String name = item.getAsJsonObject("Response").getAsJsonObject("data").get("itemName").getAsString();
			JsonArray stats = item.getAsJsonObject("Response").getAsJsonObject("data").getAsJsonObject("stats").getAsJsonObject("data").getAsJsonArray("stats");
			int mob = 0;
			int res = 0;
			int rec = 0;
			int dis = 0;
			int intel = 0;
			int str = 0;
			for (int j = 0; j < stats.size(); j++) {
				JsonObject displayProperties = stats.get(i).getAsJsonObject().get("displayProperties").getAsJsonObject();
				String statName = displayProperties.get("name").getAsString();
				switch (statName) {
				  case "Mobility":
				    mob = displayProperties.get("value").getAsInt();
				    break;
				  case "Resilience":
				    res = displayProperties.get("value").getAsInt();
				    break;
				  case "Recovery":
				    rec = displayProperties.get("value").getAsInt();
				    break;
				  case "Discipline":
				    dis = displayProperties.get("value").getAsInt();
				    break;
				  case "Intellect":
				    intel = displayProperties.get("value").getAsInt();
				    break;
				  case "Strength":
				    str = displayProperties.get("value").getAsInt();
				    break;
				}
			}
			if (mob != 0) {
				String slot = getArmorSlot(item.getAsJsonObject("Response").getAsJsonObject("data").get("traitIds").getAsJsonArray());
				Armor newArmor = new Armor(name, mob, res, rec, dis, intel, str);
				ArrayList<Armor> temp = armors.get(slot);
				temp.add(newArmor);
				armors.put(slot, temp);
			}
		}
	}
	
	public static JsonObject queryBungie(String query) throws IOException {
		String apiKey = "destinyApiKey";
		
		String url = query;
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		con.setRequestMethod("GET");
		con.setRequestProperty("X-API-KEY", apiKey);
		
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to Bungie.Net : " + url);
		System.out.println("Response Code : " + responseCode);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		String response = "";
		
		while ((inputLine = in.readLine()) != null) {
		    response += inputLine;
		}
		
		in.close();
		
		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(response);
		
		System.out.println();
		System.out.println(json);
		return json;
	}
	
	public static String getArmorSlot(JsonArray traitIds) {
		for (int i = 0; i < traitIds.size(); i++) {
			String curr = traitIds.get(i).getAsString();
			if (curr == "item.armor.head")
				return "head";
			if (curr == "item.armor.arms")
				return "arms";
			if (curr == "item.armor.chest")
				return "chest";
			if (curr == "item.armor.legs")
				return "legs";
		}
		return "";
	}
	
	public static void getBestCombo() {
		mostThresholdsHit = 0;
		bestCombo = "";
		getBestComboRecur(0, 0, 0, 0, 0, 0, 0, " -- ");
	}
	
	public static void getBestComboRecur(int slotInd, int totalMob, int totalRes, int totalRec, int totalDis, int totalInt, int totalStr, String combo) {
		if (slotInd >= 4) {
			return;
		}
		ArrayList<Armor> armorsOfThisSlot = armors.get(slots[slotInd]);
		for (int i = 0; i < armorsOfThisSlot.size(); i++) {
			Armor curr = armorsOfThisSlot.get(i);
			int newMob = totalMob + curr.mobility;
			int newRes = totalRes + curr.resilience;
			int newRec = totalRec + curr.recovery;
			int newDis = totalDis + curr.discipline;
			int newInt = totalInt + curr.intellect;
			int newStr = totalStr + curr.strength;
			int currThresholdsHit = newMob / 10 + newRes / 10 + newRec / 10 + newDis / 10 + newInt / 10 + newStr / 10;
			
			StringBuilder builder = new StringBuilder();
			builder.append(combo);
			builder.append(curr.name);
			builder.append(" -- ");
			String newCombo = builder.toString();
			
			if (currThresholdsHit > mostThresholdsHit) {
				mostThresholdsHit = currThresholdsHit;
				bestCombo = newCombo;
			}
			
			getBestComboRecur(slotInd+1, newMob, newRes, newRec, newDis, newInt, newStr, newCombo);
		}
	}
}
