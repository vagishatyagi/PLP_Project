package cop5556fa17;


import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

import cop5556fa17.AST.*;
public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		//TODO  implement this
		Program p = null;
		Token ft = t;
		Token tn = t;
		ArrayList<ASTNode> list = new ArrayList<ASTNode>();
		match(IDENTIFIER);
		p = new Program(ft,tn,list);
		//TODO statement
		while(t.kind == KW_int || t.kind == KW_boolean ||t.kind == KW_image || t.kind == KW_url ||t.kind == KW_file || t.kind == IDENTIFIER) {
			if (t.kind == KW_int || t.kind == KW_boolean ||t.kind == KW_image || t.kind == KW_url ||t.kind == KW_file) {
				
				list.add(declaration());
				match(SEMI);
					p = new Program(ft,tn,list);
			}
			else if(t.kind == IDENTIFIER) {
				
				list.add(statement());
				match(SEMI);
				p = new Program(ft,tn,list);
			}
			
		} 
		return p;
		
		//throw new UnsupportedOperationException();
	}
	
	Declaration declaration() throws SyntaxException {
		Token ft=t;
		Declaration d=null;
		if(t.kind == KW_int || t.kind == KW_boolean) {
			d=variableDeclaration();
		}
		else if(t.kind == KW_image) {
			d=imageDeclaration();
		}
		else if (t.kind == KW_url ||t.kind == KW_file) {
			d=sourceSinkDeclaration();
		}
		else throw new SyntaxException(t, "notexpected");
		return d;
	}
	
	Declaration_Variable variableDeclaration() throws SyntaxException {
		Token ft=t,t1=t;
		Expression e=null;
		Declaration_Variable d=null;
		varType();
		Token t2=t;
		if(t.kind==IDENTIFIER) {
			
			consume();
		}
		if(t.kind==OP_ASSIGN) {
			
			consume();
			e=expression();
			d= new Declaration_Variable(ft,t1,t2,e);
		}
		else {
			d= new Declaration_Variable(ft,t1,t2,e);
			return d;
		}
		return d;
		
	}
	
	void varType() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case KW_int: {
			consume();
		}
			break;
		case KW_boolean: {
			consume();
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException(t, "notexpected");
		}
	}
	
	Declaration_SourceSink sourceSinkDeclaration() throws SyntaxException {
		Token ft=t,t1=t,t2=null;
		Source s=null;
		Declaration_SourceSink d=null;
		sourceSinkType();
		t2=t;
		if(t.kind==IDENTIFIER) {
			consume();
		}
		if(t.kind==OP_ASSIGN) {
			consume();
		}
		s=source();
		d= new Declaration_SourceSink(ft,t1,t2,s);
		return d;
	}
	
	Source source() throws SyntaxException {
		Token ft=t,name=null;
		Source s=null;
		Expression e=null;
		if(t.kind == STRING_LITERAL) {
			String a= t.getText();
			s= new Source_StringLiteral(ft,a);
			consume();
			
		}
		else if(t.kind == OP_AT) {
			consume();
			e=expression();
			s= new Source_CommandLineParam(ft,e);
			
		}
		else if(t.kind == IDENTIFIER) {
			name=t;
			s= new Source_Ident(ft,name);
			consume();
			
		}
		else throw new SyntaxException(t, "notexpected");
		return s;
	}
	
	void sourceSinkType() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case KW_url: {
			consume();
		}
			break;
		case KW_file: {
			consume();
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException(t, "notexpected");
		}
	}
	
	Declaration_Image imageDeclaration() throws SyntaxException {
		Declaration_Image d=null;
		Token ft=t,name=null;
		Expression e1=null,e2=null;
		Source s=null;
		match(KW_image);
		if(t.kind == LSQUARE) {
			consume();
			e1=expression();
			match(COMMA);
			e2=expression();
			match(RSQUARE);
		}
		 if(t.kind==IDENTIFIER) {
			name=t;
			consume();
			if(t.kind == OP_LARROW) {
				consume();
				s=source();
				d= new Declaration_Image(ft,e1,e2,name,s);
			} 
			else {
				d= new Declaration_Image(ft,e1,e2,name,s);
				return d;
			}
		}
		else throw new SyntaxException(t, "notexpected");
		return d;
	}
	
	Statement statement() throws SyntaxException {
		Token ft = t;
		Statement s=null;
		Expression ex=null;
		if(t.kind == IDENTIFIER) {
			Token store =t;
			consume();
			if(t.kind == OP_LARROW || t.kind == OP_RARROW) {
				s=identStatementTail(store);
				
			}
			else if(t.kind == LSQUARE ) {
				s=assignmentStatement(store);
				
			}//how to see empty
			else if(t.kind== OP_ASSIGN) {
				LHS lh= lhs(ft);
				consume();
				ex=expression();
				s = new Statement_Assign(ft,lh,ex);
					
			}
		} else {
			throw new SyntaxException(t, "notexpected");
		}
		return s;
	}

	Statement identStatementTail(Token first) throws SyntaxException {
		Token ft = first;
		Statement s=null;
		if(t.kind == OP_LARROW || t.kind == OP_RARROW) {
			if(t.kind == OP_LARROW) {
				s=imageInStatement(ft);
				
			}
			else if(t.kind == OP_RARROW) {
				s=imageOutStatement(ft);
				
			}
		}else {
			throw new SyntaxException(t, "notexpected");
		}
		return s;
		
	}
	
	Sink sink() throws SyntaxException {
		Sink s=null;
		Token ft=t,name=null;
		Kind kind = t.kind;
			
		if(t.kind==IDENTIFIER) {
			name=t;
			s= new Sink_Ident(ft,name);
			consume();
			
		}
			
		else if (t.kind==KW_SCREEN) {
			name=t;
			s= new Sink_SCREEN(ft);
			consume();
			
		} 
		else throw new SyntaxException(t, "notexpected");
		return s;
	
	}
	
	Statement_In imageInStatement(Token first) throws SyntaxException {
		Token ft=first,name=first;
		Source s=null;
		Statement_In s1=null;
		if(t.kind == OP_LARROW) {
			consume();
			s=source();
			s1= new Statement_In(ft,name,s);
			
		}else {
			throw new SyntaxException(t, "notexpected");
		 }
		return s1;
	}
	
	Statement_Out imageOutStatement(Token first) throws SyntaxException {
		Token ft=first,name=first;
		Sink s=null;
		Statement_Out s1=null;
		 if(t.kind == OP_RARROW) {
			consume();
			s=sink();
			s1= new Statement_Out(ft,name,s);
			
		}
		 else {
		throw new SyntaxException(t, "notexpected");
		 }
		return s1;
	}
	
	Statement_Assign assignmentStatement(Token first) throws SyntaxException {
		Token ft=first;
		LHS lh=null;
		Expression e=null;
		Statement_Assign eres=null;
			lh=lhs(ft);
			match(OP_ASSIGN);
			e=expression();
			eres = new Statement_Assign(ft,lh,e);
			return eres;	
	}
	

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	public Expression expression() throws SyntaxException {
		Token ft =t;
		Expression e1=null;
		Expression e2=null;
		Expression e3 = null,e4=null;
		
		//TODO implement this.
		if(t.kind == OP_PLUS || t.kind == OP_MINUS || t.kind == OP_EXCL || t.kind == INTEGER_LITERAL|| t.kind == BOOLEAN_LITERAL || t.kind == LPAREN || t.kind == KW_sin || t.kind == KW_cos 
				|| t.kind == KW_atan || t.kind == KW_abs || t.kind == KW_cart_x || t.kind == KW_cart_y || t.kind == KW_polar_a || t.kind == KW_polar_r || t.kind ==IDENTIFIER ||
				t.kind == KW_x || t.kind ==KW_y || t.kind ==KW_r || t.kind ==KW_a || t.kind ==KW_X || t.kind ==KW_Y || t.kind ==KW_Z || t.kind ==KW_A || t.kind ==KW_R || t.kind ==KW_DEF_X || t.kind ==KW_DEF_Y) {
				e1=orExpression();
				if(t.kind == OP_Q) {
					consume();
					e2=expression();
					match(OP_COLON);
					e3=expression();
					e4= new Expression_Conditional(ft,e1,e2,e3);
					
				} else return e1;
			
			
			
		} else throw new SyntaxException(t, "notexpected");
		//throw new UnsupportedOperationException();
		return e4;
	}

	public Expression orExpression()  throws SyntaxException {
		Token ft =t;
		Expression e1=null;
		Expression e2=null;
		Token op=null;
		e1=andExpression();
		while(t.kind == OP_OR ) {
			op=t;
			consume();
			e2=andExpression();
			e1= new Expression_Binary(ft,e1,op,e2);
		}return e1;
		
	}
	
	public Expression andExpression()  throws SyntaxException {
		Token ft =t;
		Expression e1=null;
		Expression e2=null;
		Token op=null;
		e1=eqExpression();
		while(t.kind ==OP_AND) {
			op=t;
			consume();
			e2=eqExpression();
			e1= new Expression_Binary(ft,e1,op,e2);
		}return e1;
	}
	
	public Expression eqExpression() throws SyntaxException {
		Token ft =t;
		Expression e1=null;
		Expression e2=null;
		Token op=null;
		e1=relExpression();
		while(t.kind ==OP_EQ ||t.kind ==OP_NEQ) {
			op=t;
			consume();
			e2=relExpression();
			e1= new Expression_Binary(ft,e1,op,e2);
		}return e1;
	}
	
	public Expression relExpression() throws SyntaxException {
		Token ft =t;
		Expression e1=null;
		Expression e2=null;
		Token op=null;
		e1=addExpression();
		while(t.kind ==OP_LT ||t.kind ==OP_GT || t.kind ==OP_LE ||t.kind ==OP_GE) {
			op=t;
			consume();
			e2=addExpression();
			e1= new Expression_Binary(ft,e1,op,e2);
		}return e1;
	}
	
	
	public Expression addExpression()  throws SyntaxException {
		Token ft =t;
		Expression e1=null;
		Expression e2=null;
		Token op=null;
		e1=multiExpression();
		while(t.kind ==OP_PLUS ||t.kind ==OP_MINUS ) {
			op=t;
			consume();
			e2=multiExpression();
			e1= new Expression_Binary(ft,e1,op,e2);
		}return e1;
	}
	
	public Expression multiExpression() throws SyntaxException {
		Token ft =t;
		Expression e1=null;
		Expression e2=null;
		Token op=null;
		e1=unaryExpression();
		while(t.kind ==OP_TIMES ||t.kind ==OP_DIV || t.kind ==OP_MOD) {
			op=t;
			consume();
			e2=unaryExpression();
			e1= new Expression_Binary(ft,e1,op,e2);
		} return e1;
	}
	
	public Expression unaryExpression() throws SyntaxException {
		//unaryExpressionNotPlusMinus();
		Token firstToken = t;
		if (t.kind==(OP_PLUS)) {
			match(OP_PLUS);
			Expression exp = unaryExpression();
			return new Expression_Unary(firstToken, firstToken, exp);
		} else if (t.kind==(OP_MINUS)) {
			match(OP_MINUS);
			Expression exp = unaryExpression();
			return new Expression_Unary(firstToken, firstToken, exp);
		} else {
			return unaryExpressionNotPlusMinus();
		}
	}
	
	public Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		Expression ep =null;
		Token ft =t;
		Token op=null;
		if(t.kind==OP_EXCL) {
			op=t;
			consume();
			ep =unaryExpression();
			return new Expression_Unary(ft, op, ep);
		}
		else if ( t.kind == INTEGER_LITERAL ||t.kind == BOOLEAN_LITERAL || t.kind == LPAREN || t.kind == KW_sin || t.kind == KW_cos 
				|| t.kind == KW_atan || t.kind == KW_abs || t.kind == KW_cart_x || t.kind == KW_cart_y || t.kind == KW_polar_a || t.kind == KW_polar_r) {
			ep =primary();
			return ep;
		}
		else if (t.kind == IDENTIFIER) {
			ep =identOrPixelSelectorExpression();
			return ep;
			//TODO
		}
		else if (t.kind == KW_x || t.kind ==KW_y || t.kind ==KW_r || t.kind ==KW_a || t.kind ==KW_X || t.kind ==KW_Y || t.kind ==KW_Z || t.kind ==KW_A || t.kind ==KW_R || t.kind ==KW_DEF_X || t.kind ==KW_DEF_Y) {
			consume();
			ep = new Expression_PredefinedName(ft,ft.kind);
			return ep;
		}
		else {
			throw new SyntaxException(t, "notexpected"); }
		
	}
	
	public Expression primary() throws SyntaxException{
		Token ft =t;
		Expression ex=null;
		if(t.kind == INTEGER_LITERAL) {
			String a =t.getText();
			int val = Integer.parseInt(a);
			consume();
			ex= new Expression_IntLit(ft,val);
		}
		else if(t.kind == BOOLEAN_LITERAL) {
			boolean value = ft.getText().equals("true") ? true : false;
			match(BOOLEAN_LITERAL);
			ex= new Expression_BooleanLit(ft,value);
		}
		else if(t.kind == LPAREN) {
			consume();
			ex = expression();
			match(RPAREN);
		}
		else if(t.kind == KW_sin || t.kind == KW_cos || t.kind == KW_atan || t.kind == KW_abs || t.kind == KW_cart_x || t.kind == KW_cart_y || t.kind == KW_polar_a || t.kind == KW_polar_r) {
			ex = functionApplication();
		}
		else {
			throw new SyntaxException(t, "notexpected");
		}
		return ex;
	}
	
	public Expression identOrPixelSelectorExpression() throws SyntaxException {
		Expression ep = null;
		Token ft = t;
		Token iden =null;
		Index i =null;
		if (t.kind == IDENTIFIER) {
			iden =t;
			consume();
			if(t.kind == LSQUARE) {
				consume();
				i =selector();
				match(RSQUARE);
				ep = new Expression_PixelSelector(ft, iden,i);
			}
			else {
				ep= new Expression_Ident(ft, iden);
				return ep;
			}
			//TODO empty
		} else {
			throw new SyntaxException(t, "notexpected");
		} return ep;
	}
	
	LHS lhs(Token first) throws SyntaxException {
		//match(IDENTIFIER);
		Token ft = first;
		Token nam = first;
		Index i =null;
		LHS lh=null;
		if(t.kind == LSQUARE) {
			consume();
			i=lhsSelector();
			match(RSQUARE);
		}	
		return new LHS(ft,nam,i);
	}
	
	Expression functionApplication() throws SyntaxException {
		Token ft = t;
		Kind kin = ft.kind;
		Index i =null;
		Expression ex=null;
		Expression e1=null;
		functionName();
			if(t.kind == LPAREN) {
				consume();
				e1=expression();
				match(RPAREN);
				ex = new Expression_FunctionAppWithExprArg(ft,kin,e1);
			}
			else if(t.kind == LSQUARE) {
				consume();
				i =selector();
				match(RSQUARE);
				ex = new Expression_FunctionAppWithIndexArg(ft,kin,i);
			}
			else {
				throw new SyntaxException(t, "notexpected");
			} return ex;
	}

	void functionName() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case KW_sin: {
			consume();
		}
			break;
		case KW_cos: {
			consume();
		}
			break;
		case KW_atan: {
			consume();
		}
			break;
		case KW_abs: {
			consume();
		}
			break;
		case KW_cart_x: {
			consume();
		}
			break;
		case KW_cart_y: {
			consume();
		}
			break;
		case KW_polar_a: {
			consume();
		}
			break;
		case KW_polar_r: {
			consume();
		}
			break;	
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException(t, "notexpected");
		}
	}
	//TODO
	Index lhsSelector() throws SyntaxException {
		Token ft = t;
		Index i = null;
		match(LSQUARE);
		//TODO
		if(t.kind == KW_x) {
			i=xySelector();
		}
		else if(t.kind == KW_r) {
			i=raSelector(); 
		}
		else {
			throw new SyntaxException(t, "notexpected");
		}
		match(RSQUARE);
		return i;
	}
	
	Index xySelector() throws SyntaxException {
		Token ft = t;
		Index i =null;
		Expression ep1 =null;
		Expression ep2 =null;
		ep1= new Expression_PredefinedName(t,t.kind);
		match(KW_x); 
		match(COMMA);
		ep2= new Expression_PredefinedName(t,t.kind);
		match(KW_y);
		i= new Index(ft,ep1,ep2);
		return i;
	}
	
	Index raSelector() throws SyntaxException {
		Token ft = t;
		Index i =null;
		Expression ep1 =null;
		Expression ep2 =null;
		ep1= new Expression_PredefinedName(t,t.kind);
		match(KW_r); 
		match(COMMA);
		ep2= new Expression_PredefinedName(t,t.kind);
		match(KW_a);	
		i= new Index(ft,ep1,ep2);
		return i;
	}
	
	Index selector() throws SyntaxException {
		Token ft = t;
		Index i =null;
		Expression ep1 =null;
		Expression ep2 =null;
		ep1 =expression();
		match(COMMA);
		ep2=expression();
			i =new Index(ft,ep1,ep2);
			return i;
	}
	
	// My match function
	private Token match(Kind kind) throws SyntaxException {
		if (t.kind==kind) {
			return consume();
		}
		throw new SyntaxException(t , "notexpected " );
	}
	
	//My consume function
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
