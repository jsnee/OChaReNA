package com.jophus.ocharena.character;

public class ASCIICharacter extends ParsedCharacter {

	public ASCIICharacter(int characterCode) {
		super(characterCode);
	}

	@Override
	public String toString() {
		return Character.toString((char)characterCode);
	}

}
