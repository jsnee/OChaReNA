package com.jophus.ocharena.character;

@Deprecated
/**
 * ASCIICharacter class. Represents ASCII Characters.
 * @author Joe Snee
 * deprecated. See OcharenaSettings
 */
public class ASCIICharacter extends ParsedCharacter {

	public ASCIICharacter(int characterCode) {
		super(characterCode);
	}

	@Override
	public String toString() {
		return Character.toString((char)characterCode);
	}

}
