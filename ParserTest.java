package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.AST.*;

import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class ParserTest {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Simple test case with an empty program. This test expects an exception
	 * because all legal programs must have at least an identifier
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = ""; // The input is the empty string. Parsing should fail
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the tokens
		Parser parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}


	@Test
	public void testNameOnly() throws LexicalException, SyntaxException {
		String input = "prog";  //Legal program with only a name
		show(input);            //display input
		Scanner scanner = new Scanner(input).scan();   //Create scanner and create token list
		show(scanner);    //display the tokens
		Parser parser = new Parser(scanner);   //create parser
		Program ast = parser.parse();          //parse program and get AST
		show(ast);                             //Display the AST
		assertEquals(ast.name, "prog");        //Check the name field in the Program object
		assertTrue(ast.decsAndStatements.isEmpty());   //Check the decsAndStatements list in the Program object.  It should be empty.
	}

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "prog int k;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_int, dec.type.kind);
		assertEquals("k", dec.name);
		assertNull(dec.e);
	}

	@Test
	public void testprog2() throws LexicalException, SyntaxException {
		String input = "psst image k;";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);  //
		
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "psst"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Image dec = (Declaration_Image) ast.decsAndStatements
				.get(0);  
		//assertEquals(KW_image, dec.type.kind);
		assertNull(dec.xSize);
		assertNull(dec.ySize);
		assertEquals("k", dec.name);
		assertNull(dec.source);
	}
	
	@Test
	public void testprog7() throws LexicalException, SyntaxException {
		String input = "programmingtosee int myvar=7;";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);  
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "programmingtosee"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		Expression_IntLit ex = (Expression_IntLit)dec.e;
		assertEquals(KW_int, dec.type.kind);
		
		assertEquals("myvar", dec.name);
		assertEquals(7,ex.value);
	}
		
	
	@Test
	public void expression88() throws SyntaxException, LexicalException {
		String input = "x|Z";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);  
		Expression ast =parser.expression();  //Call expression directly. 
		show(ast);
		Expression_Binary dex = (Expression_Binary)ast;
		Expression_PredefinedName ex = (Expression_PredefinedName)dex.e0;
		Expression_PredefinedName ex2 = (Expression_PredefinedName)dex.e1;
		assertEquals(KW_x, ex.kind);
		assertEquals(OP_OR, dex.op);
		assertEquals(KW_Z, ex2.kind);
		
	}
	
	@Test
	public void testprog57() throws LexicalException, SyntaxException {
		String input = "a * r ?25:37";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);  //
		Expression ast =parser.expression();
		show(ast);
		Expression_Conditional dex = (Expression_Conditional)ast;
		Expression_Binary dex1 = (Expression_Binary)dex.condition;
		Expression_PredefinedName ex = (Expression_PredefinedName)dex1.e0;
		Expression_PredefinedName ex1 = (Expression_PredefinedName)dex1.e1;
		Expression_IntLit ex2 = (Expression_IntLit)dex.trueExpression;
		Expression_IntLit ex3 = (Expression_IntLit)dex.falseExpression;
		assertEquals(KW_a, ex.kind);
		assertEquals(OP_TIMES, dex1.op);
		assertEquals(KW_r, ex1.kind);
		assertEquals(25, ex2.value);
		assertEquals(37, ex3.value);
	}
	
	
	@Test
	public void testDec3() throws LexicalException, SyntaxException {
		String input = "programmingtosee int vagisha = true | false;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "programmingtosee"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_int, dec.type.kind);
		assertEquals("vagisha", dec.name);
		Expression_Binary e = (Expression_Binary)dec.e;
		Expression_BooleanLit e0 = (Expression_BooleanLit)e.e0;
		assertEquals(true, e0.value);
		assertEquals(OP_OR, e.op);
		Expression_BooleanLit e1 = (Expression_BooleanLit)e.e1;
		assertEquals(false, e1.value);
	}

	
	
	@Test
	public void myexp1() throws SyntaxException, LexicalException {
	String input = "Y-young";
	Expression e = (new Parser(new Scanner(input).scan())).expression();
	show(e);
	assertEquals(Expression_Binary.class, e.getClass());
	Expression_Binary ebin = (Expression_Binary)e;
	assertEquals(Expression_PredefinedName.class, ebin.e0.getClass());
	assertEquals(KW_Y, ((Expression_PredefinedName)(ebin.e0)).kind);
	assertEquals(Expression_Ident.class, ebin.e1.getClass());
	assertEquals("young", ((Expression_Ident)(ebin.e1)).name);
	assertEquals(OP_MINUS, ebin.op);
	}
	
	@Test
	public void myexp4() throws SyntaxException, LexicalException {
	String input = "vagisha int g;";
	show(input);
	Scanner scanner = new Scanner(input).scan(); 
	show(scanner); 
	Parser parser = new Parser(scanner);
	Program ast = parser.parse();
	show(ast);
	assertEquals(ast.name, "vagisha"); 
//	//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
	Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
			.get(0);  
	assertEquals(KW_int, dec.type.kind);
	assertEquals("g", dec.name);
	assertNull(dec.e);
	
	}

	
	@Test
	public void myexp5() throws SyntaxException, LexicalException {
	String input = "vagisha image prudhvi;";
	show(input);
	Scanner scanner = new Scanner(input).scan(); 
	show(scanner); 
	Parser parser = new Parser(scanner);
	Program ast = parser.parse();
	show(ast);
	assertEquals(ast.name, "vagisha"); 
//	//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
	Declaration_Image dec = (Declaration_Image) ast.decsAndStatements
			.get(0);  
	assertNull(dec.xSize);
	assertNull(dec.ySize);
	assertEquals("prudhvi", dec.name);
	assertNull(dec.source);
	}
	

	@Test
	public void myTestcase1() throws SyntaxException, LexicalException {
		String input = "10";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	@Test
	public void myTestcase2() throws SyntaxException, LexicalException {
		String input = "b cdefg";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	@Test
	public void myTestcase3() throws SyntaxException, LexicalException {
		String input = "R A";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	@Test
	public void myTestcase4() throws SyntaxException, LexicalException {
		String input = "programmingtosee int 200";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	
	
	@Test
	public void myTestcase7() throws SyntaxException, LexicalException {
		String input = "programmingtosee image[png,jpeg] imName <- impng; \n boolean ab=true;"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		Program ast=parser.program();
		show(ast);
		assertEquals("programmingtosee",ast.name);
		// First Declaration statement
		Declaration_Image dec = (Declaration_Image) ast.decsAndStatements.get(0);  
		assertEquals(KW_image, dec.firstToken.kind);
		assertEquals("imName", dec.name);
		Expression_Ident ei=(Expression_Ident)dec.xSize;
		assertEquals("png",ei.name);
		ei=(Expression_Ident)dec.ySize;
		assertEquals("jpeg",ei.name);
		Source_Ident s=(Source_Ident) dec.source;
	    assertEquals("impng",s.name);
		// Second Declaration statement
	    Declaration_Variable dec2 = (Declaration_Variable) ast.decsAndStatements.get(1);  
		assertEquals("ab", dec2.name);
		assertEquals(KW_boolean, dec2.firstToken.kind);
		Expression_BooleanLit ebi=(Expression_BooleanLit)dec2.e;
		assertEquals(true,ebi.value);		
	}
	
	
	
	@Test
	public void myTestcase110() throws SyntaxException, LexicalException {
		String input = "programming \"abcdefghed\" boolean a=true;"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast=parser.parse();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	

	@Test
	public void mytestcase12() throws SyntaxException, LexicalException {
		String input = "isBoolean boolean abcdef=true; boolean cdef==true; abhuicd=true ? return true: return false;"; //Should fail for ==
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast=parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
		
	
	
	@Test
	public void myTestcase17() throws SyntaxException, LexicalException {
		String input =  "isthisaFile file fileorpng=\"abcd\" \n @expr=12; url fileorpng=@expr; \n url fileorpng=abcdefg";  //Should fail for ; in line one
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast=parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		}  
	}
	
	@Test
	public void myTestcase18() throws SyntaxException, LexicalException {
		String input =  "checkurl url somekindofname;";  //Should fail for url as url can only be initalised
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast=parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		}  
	}
	
	
	
@Test
    public void myTestCase7() throws SyntaxException, LexicalException {
        String input = "file toBeCheckedFile = \"xyzabcdef\"";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);
        Declaration_SourceSink dec = parser.sourceSinkDeclaration();   //Parse the program
        show(dec);
        assertEquals(dec.type, KW_file);
        assertEquals(dec.name, "toBeCheckedFile");
        assertEquals(dec.source.getClass(), Source_StringLiteral.class);
        Source_StringLiteral src = (Source_StringLiteral)dec.source;
        assertEquals(src.fileOrUrl, "xyzabcdef");
    }
@Test
    public void myTestCase8() throws SyntaxException, LexicalException {
    String input = "image [hjhgjhjh]";
    show(input);
    Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
    show(scanner);   //Display the Scanner
    Parser parser = new Parser(scanner);   
    thrown.expect(SyntaxException.class);
    try {
        parser.imageDeclaration();   //Parse the program
    }
    catch (SyntaxException e) {
        show(e);
        throw e;
    }
    }
@Test
    public void myTestCase11() throws SyntaxException, LexicalException {
        String input = "image l <- \"dontdo\"";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);
        Declaration_Image dec = parser.imageDeclaration();   //Parse the program
        show(dec);
        assertEquals(dec.getClass(), Declaration_Image.class);
        assertEquals(dec.name, "l");
        assertEquals(dec.source.getClass(), Source_StringLiteral.class);
        assertEquals(((Source_StringLiteral)(dec.source)).fileOrUrl, "dontdo");   
    }
@Test
    public void myTestCase12() throws SyntaxException, LexicalException {
        String input = "statemenhgdjgjt = true ? z; f";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);  
        thrown.expect(SyntaxException.class);
        try {
            parser.statement();   //Parse the program
        }
        catch (SyntaxException e) {
            show(e);
            throw e;
        }
    }
@Test
    public void myTestCase13() throws SyntaxException, LexicalException {
        String input = "seethis = false ? z: f";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);
        Statement st = parser.statement();   //Parse the program
        show(st);
        assertEquals(st.getClass(), Statement_Assign.class);
        Statement_Assign st_assign = (Statement_Assign)st;
        assertEquals(((LHS)(st_assign.lhs)).name, "seethis");
        Expression_Conditional st_assign_exp = (Expression_Conditional)st_assign.e;
        assertEquals(st_assign_exp.condition.getClass(), Expression_BooleanLit.class);
        assertEquals(((Expression_BooleanLit)(((Expression_Conditional)(st_assign_exp)).condition)).value,false);
        assertEquals(((Expression_Ident)(st_assign_exp.trueExpression)).name, "z");
        assertEquals(((Expression_Ident)(st_assign_exp.falseExpression)).name, "f");
    }

@Test
    public void myTestCase16() throws SyntaxException, LexicalException {
        String input = "atan(a+b)];";
        show(input);
        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
        show(scanner);   //Display the Scanner
        Parser parser = new Parser(scanner);
        Expression exp = parser.expression();   //Parse the program
        show(exp);
        assertEquals(exp.getClass(), Expression_FunctionAppWithExprArg.class);
        Expression_FunctionAppWithExprArg exp_fn = (Expression_FunctionAppWithExprArg)exp;
        assertEquals(exp_fn.function, KW_atan);
        assertEquals(exp_fn.arg.getClass(), Expression_Binary.class);
        Expression_Binary exp_fn_arg = (Expression_Binary)exp_fn.arg;
        assertEquals(((Expression_PredefinedName)exp_fn_arg.e0).kind, KW_a);
        assertEquals(exp_fn_arg.op, OP_PLUS);
        assertEquals(((Expression_Ident)exp_fn_arg.e1).name, "b" );
    }
@Test
	public void myTestCase17() throws SyntaxException, LexicalException {
		String input = "checkthisprog int fght;";
		show(input);
		Scanner scanner = new Scanner(input).scan();
        Parser parser = new Parser(scanner);  
        Program ast = parser.parse();  //Parse the program 
        show(ast);
        assertEquals(ast.name, "checkthisprog");
        Declaration_Variable dec = (Declaration_Variable)ast.decsAndStatements.get(0);
        assertEquals(dec.type.kind, KW_int);
        assertEquals(dec.name, "fght");
        assertEquals(dec.e, null);
	}
@Test
	public void myTestCase18() throws SyntaxException, LexicalException {
		String input = "- !Y*Z";
		show(input);
		Scanner scanner = new Scanner(input).scan();
        Parser parser = new Parser(scanner);  
        Expression exp = parser.expression();  //Parse the program 
        show(exp);
        assertEquals(exp.getClass(), Expression_Binary.class);
        Expression_Binary expbin = (Expression_Binary)exp;
        assertEquals(expbin.e0.getClass(), Expression_Unary.class);
        assertEquals(((Expression_Unary)expbin.e0).op, OP_MINUS);
        assertEquals(((Expression_Unary)expbin.e0).e.getClass(), Expression_Unary.class);
        assertEquals(((Expression_Unary)((Expression_Unary)expbin.e0).e).op, OP_EXCL);
        assertEquals(expbin.op, OP_TIMES);
        assertEquals(expbin.e1.getClass(), Expression_PredefinedName.class);
        assertEquals(((Expression_PredefinedName)expbin.e1).kind, KW_Z);
}


@Test
public void myexpp5() throws SyntaxException, LexicalException {
String input = "vagisha buddy=A;";
show(input);
Scanner scanner = new Scanner(input).scan(); 
show(scanner); 
Parser parser = new Parser(scanner);
Program ast = parser.parse();
show(ast);

}

}

