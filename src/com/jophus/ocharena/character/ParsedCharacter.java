package com.jophus.ocharena.character;

import java.util.ArrayList;

@Deprecated
/**
 * ParsedCharacter class.
 * @author Joe Snee
 * deprecated. See OcharenaSettings
 */
public abstract class ParsedCharacter {
	
	//ArrayList of all parsed characters
	public static ArrayList<ParsedCharacter> parsedCharacters = new ArrayList<ParsedCharacter>();
	
	//Character index code
	protected int characterCode;
	
	public ParsedCharacter(int characterCode) {
		this.characterCode = characterCode;
	}
	
	public int getCharacterCode() {
		return characterCode;
	}
	
	public abstract String toString();
	
	//Basic Arabic uppercase ASCII characters
	public static final ParsedCharacter ARABIC_A_UPPERCASE = new ASCIICharacter(65);
	public static final ParsedCharacter ARABIC_B_UPPERCASE = new ASCIICharacter(66);
	public static final ParsedCharacter ARABIC_C_UPPERCASE = new ASCIICharacter(67);
	public static final ParsedCharacter ARABIC_D_UPPERCASE = new ASCIICharacter(68);
	public static final ParsedCharacter ARABIC_E_UPPERCASE = new ASCIICharacter(69);
	public static final ParsedCharacter ARABIC_F_UPPERCASE = new ASCIICharacter(70);
	public static final ParsedCharacter ARABIC_G_UPPERCASE = new ASCIICharacter(71);
	public static final ParsedCharacter ARABIC_H_UPPERCASE = new ASCIICharacter(72);
	public static final ParsedCharacter ARABIC_I_UPPERCASE = new ASCIICharacter(73);
	public static final ParsedCharacter ARABIC_J_UPPERCASE = new ASCIICharacter(74);
	public static final ParsedCharacter ARABIC_K_UPPERCASE = new ASCIICharacter(75);
	public static final ParsedCharacter ARABIC_L_UPPERCASE = new ASCIICharacter(76);
	public static final ParsedCharacter ARABIC_M_UPPERCASE = new ASCIICharacter(77);
	public static final ParsedCharacter ARABIC_N_UPPERCASE = new ASCIICharacter(78);
	public static final ParsedCharacter ARABIC_O_UPPERCASE = new ASCIICharacter(79);
	public static final ParsedCharacter ARABIC_P_UPPERCASE = new ASCIICharacter(80);
	public static final ParsedCharacter ARABIC_Q_UPPERCASE = new ASCIICharacter(81);
	public static final ParsedCharacter ARABIC_R_UPPERCASE = new ASCIICharacter(82);
	public static final ParsedCharacter ARABIC_S_UPPERCASE = new ASCIICharacter(83);
	public static final ParsedCharacter ARABIC_T_UPPERCASE = new ASCIICharacter(84);
	public static final ParsedCharacter ARABIC_U_UPPERCASE = new ASCIICharacter(85);
	public static final ParsedCharacter ARABIC_V_UPPERCASE = new ASCIICharacter(86);
	public static final ParsedCharacter ARABIC_W_UPPERCASE = new ASCIICharacter(87);
	public static final ParsedCharacter ARABIC_X_UPPERCASE = new ASCIICharacter(88);
	public static final ParsedCharacter ARABIC_Y_UPPERCASE = new ASCIICharacter(89);
	public static final ParsedCharacter ARABIC_Z_UPPERCASE = new ASCIICharacter(90);
	
	//Basic Arabic lowercase ASCII characters
	public static final ParsedCharacter ARABIC_A_LOWERCASE = new ASCIICharacter(97);
	public static final ParsedCharacter ARABIC_B_LOWERCASE = new ASCIICharacter(98);
	public static final ParsedCharacter ARABIC_C_LOWERCASE = new ASCIICharacter(99);
	public static final ParsedCharacter ARABIC_D_LOWERCASE = new ASCIICharacter(100);
	public static final ParsedCharacter ARABIC_E_LOWERCASE = new ASCIICharacter(101);
	public static final ParsedCharacter ARABIC_F_LOWERCASE = new ASCIICharacter(102);
	public static final ParsedCharacter ARABIC_G_LOWERCASE = new ASCIICharacter(103);
	public static final ParsedCharacter ARABIC_H_LOWERCASE = new ASCIICharacter(104);
	public static final ParsedCharacter ARABIC_I_LOWERCASE = new ASCIICharacter(105);
	public static final ParsedCharacter ARABIC_J_LOWERCASE = new ASCIICharacter(106);
	public static final ParsedCharacter ARABIC_K_LOWERCASE = new ASCIICharacter(107);
	public static final ParsedCharacter ARABIC_L_LOWERCASE = new ASCIICharacter(108);
	public static final ParsedCharacter ARABIC_M_LOWERCASE = new ASCIICharacter(109);
	public static final ParsedCharacter ARABIC_N_LOWERCASE = new ASCIICharacter(110);
	public static final ParsedCharacter ARABIC_O_LOWERCASE = new ASCIICharacter(111);
	public static final ParsedCharacter ARABIC_P_LOWERCASE = new ASCIICharacter(112);
	public static final ParsedCharacter ARABIC_Q_LOWERCASE = new ASCIICharacter(113);
	public static final ParsedCharacter ARABIC_R_LOWERCASE = new ASCIICharacter(114);
	public static final ParsedCharacter ARABIC_S_LOWERCASE = new ASCIICharacter(115);
	public static final ParsedCharacter ARABIC_T_LOWERCASE = new ASCIICharacter(116);
	public static final ParsedCharacter ARABIC_U_LOWERCASE = new ASCIICharacter(117);
	public static final ParsedCharacter ARABIC_V_LOWERCASE = new ASCIICharacter(118);
	public static final ParsedCharacter ARABIC_W_LOWERCASE = new ASCIICharacter(119);
	public static final ParsedCharacter ARABIC_X_LOWERCASE = new ASCIICharacter(120);
	public static final ParsedCharacter ARABIC_Y_LOWERCASE = new ASCIICharacter(121);
	public static final ParsedCharacter ARABIC_Z_LOWERCASE = new ASCIICharacter(122);
	
	//Basic Arabic ASCII digits
	public static final ParsedCharacter ARABIC_0_DIGIT = new ASCIICharacter(48);
	public static final ParsedCharacter ARABIC_1_DIGIT = new ASCIICharacter(49);
	public static final ParsedCharacter ARABIC_2_DIGIT = new ASCIICharacter(50);
	public static final ParsedCharacter ARABIC_3_DIGIT = new ASCIICharacter(51);
	public static final ParsedCharacter ARABIC_4_DIGIT = new ASCIICharacter(52);
	public static final ParsedCharacter ARABIC_5_DIGIT = new ASCIICharacter(53);
	public static final ParsedCharacter ARABIC_6_DIGIT = new ASCIICharacter(54);
	public static final ParsedCharacter ARABIC_7_DIGIT = new ASCIICharacter(55);
	public static final ParsedCharacter ARABIC_8_DIGIT = new ASCIICharacter(56);
	public static final ParsedCharacter ARABIC_9_DIGIT = new ASCIICharacter(57);

}
