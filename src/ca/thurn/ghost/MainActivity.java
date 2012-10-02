package ca.thurn.ghost;

import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.limewire.collection.CharSequenceKeyAnalyzer;
import org.limewire.collection.PatriciaTrie;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Map<Character, PatriciaTrie<String, String>> dictMap;
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
	private static final int[] DICTS = { R.raw.dict_a, R.raw.dict_b,
			R.raw.dict_c, R.raw.dict_d, R.raw.dict_e, R.raw.dict_f,
			R.raw.dict_g, R.raw.dict_h, R.raw.dict_i, R.raw.dict_j,
			R.raw.dict_k, R.raw.dict_l, R.raw.dict_m, R.raw.dict_n,
			R.raw.dict_o, R.raw.dict_p, R.raw.dict_q, R.raw.dict_r,
			R.raw.dict_s, R.raw.dict_t, R.raw.dict_u, R.raw.dict_v,
			R.raw.dict_w, R.raw.dict_x, R.raw.dict_y, R.raw.dict_z };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dictMap = (Map<Character, PatriciaTrie<String, String>>) getLastNonConfigurationInstance();
		if (dictMap == null) {
			dictMap = new HashMap<Character, PatriciaTrie<String, String>>();
		}
	}

	private PatriciaTrie<String, String> loadTrieForLetter(char input) {
		final char letter = Character.toLowerCase(input);
		if (dictMap.containsKey(letter)) return dictMap.get(letter);
		PatriciaTrie<String, String> trie = new PatriciaTrie<String, String>(new CharSequenceKeyAnalyzer());
		int ordinal = (int)letter - 'a'; // a = 0, b = 1, etc.
		BufferedInputStream web2 = new BufferedInputStream(getResources()
				.openRawResource(DICTS[ordinal]));
		final Scanner dictScanner = new Scanner(web2);
		while (dictScanner.hasNext()) {
			String nextLine = dictScanner.nextLine().trim();
			trie.put(nextLine, nextLine);
		}
		dictMap.put(letter, trie);
		return trie;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return dictMap;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void submitLetter(View view) {
		String message = ((EditText) findViewById(R.id.submitLetter)).getText()
				.toString();
		if (message.length() != 1) {
			Toast.makeText(this, "Submit 1 Letter!", Toast.LENGTH_LONG).show();
		}
		TextView textView = (TextView) findViewById(R.id.textView1);
		CharSequence oldText = textView.getText();
		textView.setText(oldText + message.toLowerCase());
		String currentLetters = oldText + message.toLowerCase();
		if (currentLetters.length() > 3 &&
				loadTrieForLetter(currentLetters.charAt(0)).containsKey(currentLetters)) {
			Toast.makeText(this, "That is a word.", Toast.LENGTH_LONG).show();
			return;
		}
		
		Move computerChoice = computeMove(currentLetters);
		
		if (computerChoice.moveType == Move.MoveType.CHALLENGE) {
			Toast.makeText(this, "Challenge!", Toast.LENGTH_LONG).show();
		} else if (computerChoice.moveType == Move.MoveType.GIVE_UP){
			Toast.makeText(this, "I give up!", Toast.LENGTH_LONG).show();
		} else {
			textView.setText(oldText + message.toLowerCase() + computerChoice.guess);
		}
	}
	
	private static class Move {
		enum MoveType {
			GUESS,
			CHALLENGE,
			GIVE_UP
		}
		
		private char guess;
		private MoveType moveType;
		
		private Move(){}

		static Move challenge() {
			Move result = new Move();
			result.moveType = MoveType.CHALLENGE;
			return result;
		}
		
		static Move guess(char c) {
			Move result = new Move();
			result.guess = c;
			result.moveType = MoveType.GUESS;
			return result;
		}
		
		static Move giveUp() {
			Move result = new Move();
			result.moveType = MoveType.GIVE_UP;
			return result;
		}
	}

	private Move computeMove(String input) {
		PatriciaTrie<String, String> trie = loadTrieForLetter(input.charAt(0));
		int max = 0;
		char best = '\0';
		boolean inputCanMakeWord = false;
		for (int i = 0; i < ALPHABET.length(); ++i) {
			char alphabetLetter = ALPHABET.charAt(i);
			String key = input.toLowerCase() + alphabetLetter;
			String proposed = input + alphabetLetter;
			int count = trie.getPrefixedBy(key).size();
			if (count > max) {
				if (proposed.length() > 3 && trie.containsKey(proposed)) {
					inputCanMakeWord = true;
					System.out.println(">>> Input can make word: " + proposed);
				} else {
					System.out.println(">>> New best: " + proposed);
					max = count;
					best = alphabetLetter;
				}
			}
		}
		if (best == '\0') {
			if (inputCanMakeWord) {
				return Move.giveUp();
			} else {
				return Move.challenge();
			}
		}
		return Move.guess(best);
	}
}
