/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
 */

package cop5556fa17;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cop5556fa17.TypeUtils.Type;

public class Scanner {

	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {

		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}

		public int getPos() { return pos; }

	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;

		
	}

	public static enum State {
		START, IN_NZERODIGIT,START_STRING, IN_STRING_LITERAL, IDENT_START,COMMENT_S, AFT_ESC, AFT_EQ, AFT_NOT, AFT_MINUS, AFT_STAR, AFT_DIV, AFT_GREATERTHAN, AFT_LESSTHAN;
	}

	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;

	private static final char i = 0;

	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;

	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  

	public HashMap<String,Kind> Keyword_hm = new HashMap<String,Kind>();


	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
		Keyword_hm.put("x",Kind.KW_x);
		Keyword_hm.put("X",Kind.KW_X);
		Keyword_hm.put("y",Kind.KW_y);
		Keyword_hm.put("Y",Kind.KW_Y);
		Keyword_hm.put("r",Kind.KW_r);
		Keyword_hm.put("R",Kind.KW_R);
		Keyword_hm.put("a",Kind.KW_a);
		Keyword_hm.put("A",Kind.KW_A);
		Keyword_hm.put("true",Kind.BOOLEAN_LITERAL);
		Keyword_hm.put("false",Kind.BOOLEAN_LITERAL);
		Keyword_hm.put("DEF_X",Kind.KW_DEF_X);
		Keyword_hm.put("DEF_Y",Kind.KW_DEF_Y);
		Keyword_hm.put("Z",Kind.KW_Z);
		Keyword_hm.put("SCREEN",Kind.KW_SCREEN);
		Keyword_hm.put("cart_x",Kind.KW_cart_x);
		Keyword_hm.put("cart_y",Kind.KW_cart_y);
		Keyword_hm.put("polar_a",Kind.KW_polar_a);
		Keyword_hm.put("polar_r",Kind.KW_polar_r);
		Keyword_hm.put("abs",Kind.KW_abs);
		Keyword_hm.put("sin",Kind.KW_sin);
		Keyword_hm.put("cos",Kind.KW_cos);
		Keyword_hm.put("atan",Kind.KW_atan); 
		Keyword_hm.put("log",Kind.KW_log);
		Keyword_hm.put("image",Kind.KW_image);
		Keyword_hm.put("int",Kind.KW_int);
		Keyword_hm.put("boolean",Kind.KW_boolean);
		Keyword_hm.put("url",Kind.KW_url); 
		Keyword_hm.put("file",Kind.KW_file);

	}


	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		/* TODO  Replace this with a correct and complete implementation!!! */
		int pos = 0;
		int line = 1;
		int posInLine = 1;
		int startPos = 0;
		State state = State.START;
		while( pos < chars.length -1  ) 
		{
			char ch = chars[pos];
			switch (state) {
				case START: {    
					ch = chars[pos];
					startPos = pos;
					switch (ch) {

						case ';': { 
							tokens.add(new Token(Kind.SEMI, startPos, 1, line, posInLine )); 
							pos++; 
							posInLine++; 
							break; }
							
						case '+': {
							tokens.add(new Token(Kind.OP_PLUS, startPos, 1, line, posInLine));
							pos++; 
							posInLine++; 
							break; }
						case '&': {
							tokens.add(new Token(Kind.OP_AND, startPos, 1, line, posInLine));
							pos++; 
							posInLine++;
							break; }
							
						case '%': {
							tokens.add(new Token(Kind.OP_MOD, startPos, 1, line, posInLine));
							pos++; 
							posInLine++; 
							break; }
						case ',': {
							tokens.add(new Token(Kind.COMMA, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							break; }
						case '(': {
							tokens.add(new Token(Kind.LPAREN, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							break; }
						case ')': {
							tokens.add(new Token(Kind.RPAREN, startPos, 1, line, posInLine));
							pos++; 
							posInLine++; 
							break; }
						case '[': {
							tokens.add(new Token(Kind.LSQUARE, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							break; }
						case ']': {
							tokens.add(new Token(Kind.RSQUARE, startPos, 1, line, posInLine));
							pos++; 
							posInLine++;
							break;}
						case ':': {
							tokens.add(new Token(Kind.OP_COLON, startPos, 1, line, posInLine));
							pos++; 
							posInLine++;
							break; }
						case '?': {
							tokens.add(new Token(Kind.OP_Q, startPos, 1, line, posInLine));
							pos++; 
							posInLine++;
							break; }
						case '@': {
							tokens.add(new Token(Kind.OP_AT, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							break; }  
						case '|': {
							tokens.add(new Token(Kind.OP_OR, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							break; }         
						case '=': {
							state = State.AFT_EQ; 
							if(pos == chars.length - 2) {
								tokens.add(new Token(Kind.OP_ASSIGN, startPos, 1, line, posInLine));
								state = State.START;
								posInLine = posInLine + 1;
							}
							pos++;
							}break;
						case '!': {
							state = State.AFT_NOT;
							if(pos == chars.length - 2) {
								tokens.add(new Token(Kind.OP_EXCL, startPos, 1, line, posInLine));
								state = State.START;
								posInLine = posInLine + 1;
							}
							pos++; 
							}break;
						case '<': {
							state = State.AFT_LESSTHAN;
							if(pos == chars.length - 2) {
								tokens.add(new Token(Kind.OP_LT, startPos, 1, line, posInLine));
								state = State.START;
								posInLine = posInLine + 1;
							}
							pos++; 
							}break;
						case '>': {
							state = State.AFT_GREATERTHAN;
							if(pos == chars.length - 2) {
								tokens.add(new Token(Kind.OP_GT, startPos, 1, line, posInLine));
								state = State.START;
								posInLine = posInLine + 1;
							}
							pos++; 
							}break;
						case '-': {
							state = State.AFT_MINUS;
							if(pos == chars.length - 2) {
								tokens.add(new Token(Kind.OP_MINUS, startPos, 1, line, posInLine));
								state = State.START;
								posInLine = posInLine + 1;
							}
							pos++; 
							}break;
						case '*': {
							state = State.AFT_STAR;
							if(pos == chars.length - 2) {
								tokens.add(new Token(Kind.OP_TIMES, startPos, 1, line, posInLine));
								state = State.START;
								posInLine = posInLine + 1;
							}
							pos++;
							} break;
						case '/': {
							state = State.AFT_DIV; 
							if(pos == chars.length - 2) {
								tokens.add(new Token(Kind.OP_DIV, startPos, 1, line, posInLine));
								state = State.START;
								posInLine = posInLine + 1;
							}
							pos++;
							} break;


						default : {
							if (Character.isDigit(ch)) {
								if(ch == '0') {
									state= State.START;
									tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, 1, line, posInLine));	
									pos++;
									posInLine++;

								}
								else {
									state = State.IN_NZERODIGIT;
									pos++;
									if(chars[pos] == EOFchar)  { 
										tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos, line, posInLine));
										posInLine = posInLine + (pos - startPos);
										try {
											String  myString = new String(chars);
											Integer.parseInt(myString.substring(startPos, pos));
										} catch(NumberFormatException e) {
											throw new LexicalException("Number out of range ", pos);
										}
										state = State.START;
									}
									//posInLine++;

								}
							} 
							else if (Character.isLetter(ch) || chars[pos]=='$' || chars[pos]=='_') {
								state = State.IDENT_START; 	pos++;
								if(chars[pos] == EOFchar)  { 
									String myString = new String(chars);
									Kind hm1 = Keyword_hm.get(myString.substring(startPos, pos ));
									if(hm1!=null)
									{
										tokens.add(new Token(hm1, startPos, pos - startPos , line, posInLine));
										//TODO
										posInLine = posInLine + (pos - startPos);
										
									}
									else {
										tokens.add(new Token(Kind.IDENTIFIER, startPos, pos - startPos , line, posInLine));
										posInLine = posInLine + (pos - startPos);
								
									}
									state = State.START;
								
							} }
								
							
							else if(ch =='\r' && chars[pos+1] == '\n') {
								pos = pos +2; 
								line++;
								posInLine = 1;
							}

							else if (ch == '\n' || ch == '\r' )   {
								pos++; 
								line++;
								posInLine = 1;
							}
							else if (Character.isWhitespace(chars[pos]) ||chars[pos] =='\f' || chars[pos] == '\t') {

								pos++;
								posInLine++;					

							}
					else if (ch =='\"') { 
						state = State.START_STRING;
						pos++;
					}
					else {
						throw new LexicalException(
								"illegal char " + ch+" at pos ",pos);
					}
				}
			} // switch ch

			}  break; // case start

			case IN_NZERODIGIT: {
				if(Character.isDigit(ch)) {
					if(pos == chars.length - 2) { 
						tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos +1, line, posInLine));
						posInLine = posInLine + (pos - startPos);
						try {
							String  myString = new String(chars);
							Integer.parseInt(myString.substring(startPos, pos));
						} catch(NumberFormatException e) {
							throw new LexicalException("Number out of range ", pos);
						}
						state = State.START;
					}
					pos++; 
				}
				else {
					tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos, line, posInLine));
					posInLine = posInLine + (pos - startPos);
					try {
						String  myString = new String(chars);
						Integer.parseInt(myString.substring(startPos, pos));
					} catch(NumberFormatException e) {
						throw new LexicalException("Number out of range ", pos);
					}
					state = State.START;
				}
			}  break;
			case IDENT_START: {
				if (Character.isLetterOrDigit(ch) || ch=='$' || ch=='_') {
					
					if(pos == chars.length - 2 ) { 
						String myString = new String(chars);
						Kind hm1 = Keyword_hm.get(myString.substring(startPos, pos + 1));
						if(hm1!=null)
						{
							tokens.add(new Token(hm1, startPos, pos - startPos +1, line, posInLine));
							//TODO
							posInLine = posInLine + (pos - startPos);
							pos++;
							if(chars[pos] != EOFchar) pos--;
							state = State.START;
							
						}
						else {
							tokens.add(new Token(Kind.IDENTIFIER, startPos, pos - startPos +1, line, posInLine));
							posInLine = posInLine + (pos - startPos);
							pos++;
							if(chars[pos] != EOFchar) pos--;
							state = State.START;
					
						}
						
					}
					else {  pos++; }

				} 
				//check for boolean literal
				else  {
					String myString = new String(chars);
					Kind hm1 = Keyword_hm.get(myString.substring(startPos, pos));
					if(hm1!=null)
					{
						tokens.add(new Token(hm1, startPos, pos - startPos, line, posInLine));
						//TODO
						posInLine = posInLine + (pos - startPos);						
						pos++;
						if(chars[startPos] != EOFchar) pos--;
						state = State.START;
					}
					else {
						tokens.add(new Token(Kind.IDENTIFIER, startPos, pos - startPos, line, posInLine));
						posInLine = posInLine + (pos - startPos);
						pos++;
						if(chars[startPos] != EOFchar) pos--;
						state = State.START;
					}
					
				}
			}  break;

			case AFT_EQ: {
				if(ch =='=') 
				{
					state = State.START;
					tokens.add(new Token(Kind.OP_EQ, startPos, 2, line, posInLine));
					if(chars[pos] != EOFchar) {
						pos++;
						posInLine = posInLine + 2;
					}
				}
				//state = State.IN_IDENT;pos++;
				else 
				{
					tokens.add(new Token(Kind.OP_ASSIGN, startPos, 1, line, posInLine));
					state = State.START;
					posInLine = posInLine + 1;
				}
				break;
			}
			case AFT_NOT: {
				if(ch =='=') 
				{
					state = State.START;
					tokens.add(new Token(Kind.OP_NEQ, startPos, 2, line, posInLine));
					if(chars[pos] != EOFchar) {
						pos++;
						posInLine = posInLine + 2;
					}
				}
				//state = State.IN_IDENT;pos++;
				else 
				{
					tokens.add(new Token(Kind.OP_EXCL, startPos, 1, line, posInLine));
					state = State.START;
					posInLine++;
				}
				break;
			}


			case AFT_GREATERTHAN: {
				if(ch =='=') 
				{
					state = State.START;
					tokens.add(new Token(Kind.OP_GE, startPos, 2, line, posInLine));
					if(chars[pos] != EOFchar) {
						pos++;
						posInLine = posInLine + 2;
					}
				}
				//state = State.IN_IDENT;pos++;
				else 
				{
					tokens.add(new Token(Kind.OP_GT, startPos, 1, line, posInLine));
					state = State.START;
					posInLine++;
				}
				break;
			}
			case AFT_STAR: {
				if(ch =='*') 
				{
					state = State.START;
					tokens.add(new Token(Kind.OP_POWER, startPos, 2, line, posInLine));
					//TODO
					if(chars[pos] != EOFchar) {
						pos++ ;
						posInLine = posInLine + 2;
					}

				}
				//state = State.IN_IDENT;pos++;
				else 
				{
					tokens.add(new Token(Kind.OP_TIMES, startPos, 1, line, posInLine));
					state = State.START;
					posInLine++;
				}
				break;
			}
			case AFT_MINUS: {
				if(ch =='>') 
				{
					state = State.START;
					tokens.add(new Token(Kind.OP_RARROW, startPos, 2, line, posInLine));
					if(chars[pos] != EOFchar) {
						pos++;
						posInLine = posInLine + 2;
					}
				}
				//state = State.IN_IDENT;pos++;
				else 
				{
					tokens.add(new Token(Kind.OP_MINUS, startPos, 1, line, posInLine));
					state = State.START;
					posInLine++;
				}
				break;
			}

			case AFT_LESSTHAN: {
				if(ch =='=') 
				{
					state = State.START;
					tokens.add(new Token(Kind.OP_LE, startPos, 2, line, posInLine));
					if(chars[pos] != EOFchar) {
						pos++;
						posInLine = posInLine + 2;
					}
				}
				else if(ch =='-') 
				{
					state = State.START;
					tokens.add(new Token(Kind.OP_LARROW, startPos, 2, line, posInLine));
					if(chars[pos] != EOFchar) {
						pos++;
						posInLine = posInLine + 2;
					}
				}
				//state = State.IN_IDENT;pos++;
				else 
				{
					tokens.add(new Token(Kind.OP_LT, startPos, 1, line, posInLine));
					state = State.START;
					posInLine++;
				}
				break;
			}
			case AFT_DIV : {
				if(ch == '/') 
					//TODO
				{
					state = State.COMMENT_S;
					if(ch== '\n' || ch == '\r' ) {
						throw new LexicalException("bad input character ", pos);
					}   pos++;
				}
				else 
				{
					tokens.add(new Token(Kind.OP_DIV, startPos, 1, line, posInLine));
					state = State.START;
				}
			}  break;
			case COMMENT_S : {
				if(chars[pos] == '\n' || chars[pos]  == '\r' ) {
					state= State.START;
				}
				else {
					pos++;
				}
				break;
			}
			case START_STRING : {
				state = State.IN_STRING_LITERAL;
				break;
			}

			case IN_STRING_LITERAL : {
				if(chars[pos] == '\n' || chars[pos] == '\r' ) {
					throw new LexicalException("String literal not properly enclosed gbkgbnj\n ", pos);
				}
				if(chars[pos] == '\\' ) 
				{
					//if(chars[pos + 1] == '\"') throw new LexicalException("String literal not properly enclosed ", pos +2);
				 if( chars[pos +1] == 'n' || chars[pos+1]== '\\'|| chars[pos+1] == 'r' || chars[pos +1] == 'b' ||chars[pos +1] == 't'||chars[pos +1] == 'f' || chars[pos+1] == '\'' || chars[pos+1] == '\"' ) 
					{			
						pos = pos + 2;
						state = State.IN_STRING_LITERAL;
					} 
					else 
					{
						throw new LexicalException("String literal not properly enclosed ", pos + 1);
					} 
				}

				if(chars[pos]=='\"') {
					tokens.add(new Token(Kind.STRING_LITERAL, startPos, pos - startPos +1, line, posInLine));
					posInLine = posInLine + (pos - startPos+1);
					pos ++;
					state = State.START;
				}
				else if(pos == chars.length-2) 
				{
					throw new LexicalException("String literal not properly enclosed ", pos + 1);

				}

				else {
					pos++;

				}
			}
			} // switch state
		} //while
		tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
		return this;
	}

	public int skipWhiteSpaces(int pos) {
		while(Character.isWhitespace(chars[pos]) ||chars[pos] =='\f' || chars[pos] == '\t') {

			pos++;
		}
		return pos;
	}


	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}

	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}


	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}
