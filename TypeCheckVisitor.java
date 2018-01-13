package cop5556fa17;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeCheckVisitor.SemanticException;
import cop5556fa17.TypeUtils.Type;

//import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		
		SymbolTable symtab = new SymbolTable();

	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String nm = declaration_Variable.getname();
		Declaration dec=symtab.lookupDec(nm);
		if(dec==null) {
		boolean check;
		Scanner.Token k= declaration_Variable.getDtype();
		TypeUtils.Type rytType =TypeUtils.getType(k);
		TypeUtils.Type resType =null;
		if(declaration_Variable.getExpr()!=null) {
			TypeUtils.Type sType =  (TypeUtils.Type) declaration_Variable.getExpr().visit(this, arg);
			if(sType==null) sType= declaration_Variable.getExpr().getNewType();
			declaration_Variable.setNewType(rytType);
			check=symtab.insert(nm, declaration_Variable);
			if(!declaration_Variable.getNewType().equals(sType)) {
				throw new SemanticException(declaration_Variable.getFirstToken(), "visitDeclaration_Variable-Type mismatch problem");
			}
		} else {
			declaration_Variable.setNewType(rytType);
			check=symtab.insert(nm, declaration_Variable);
			return declaration_Variable.getNewType();		
		}
			
		return declaration_Variable.getNewType();
		}
		else throw new SemanticException(declaration_Variable.getFirstToken(), "visitDeclaration_Variable: Already exists in symbol table");
		
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeUtils.Type exp0 =  (TypeUtils.Type) expression_Binary.gete0().visit(this, arg);
		TypeUtils.Type exp1 =  (TypeUtils.Type) expression_Binary.gete1().visit(this, arg);
		if(exp0==null) exp0= expression_Binary.gete0().getNewType();
		if(exp1==null) exp1= expression_Binary.gete1().getNewType();
		String op= expression_Binary.getOp().toString();
		TypeUtils.Type resType=null;
		expression_Binary.setNewType(exp1);
		if(exp0.equals(exp1) && expression_Binary.getNewType()!=null) {
			if(op.equals("OP_EQ")||op.equals("OP_NEQ")) {
				expression_Binary.setNewType(TypeUtils.Type.BOOLEAN);
				resType = TypeUtils.Type.BOOLEAN;
				return resType;
			}
			else if(op.equals("OP_GE")||op.equals("OP_GT")||op.equals("OP_LT")||op.equals("OP_LE") && exp0.equals(TypeUtils.Type.INTEGER)) {
				expression_Binary.setNewType(TypeUtils.Type.BOOLEAN);
				resType = TypeUtils.Type.BOOLEAN;
				return resType;
			}
			else if(op.equals("OP_AND")||op.equals("OP_OR")&& (exp0.equals(TypeUtils.Type.INTEGER)|| exp0.equals(TypeUtils.Type.BOOLEAN))) {
				expression_Binary.setNewType(exp0);
				resType = exp0;
				return resType;
			}
			else if(op.equals("OP_DIV")||op.equals("OP_MINUS")||op.equals("OP_MOD")||op.equals("OP_PLUS")||op.equals("OP_POWER")||op.equals("OP_TIMES")&& exp0.equals(TypeUtils.Type.INTEGER)) {
				expression_Binary.setNewType(TypeUtils.Type.INTEGER);
				resType = TypeUtils.Type.INTEGER;
				return resType;
			}
			else return expression_Binary.getNewType();
		}throw new SemanticException(expression_Binary.getFirstToken(), "binary-expression condition mismatch problem");
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeUtils.Type exp0 =  (TypeUtils.Type) expression_Unary.getExpr().visit(this, arg);
		TypeUtils.Type resType=null;
		String op= expression_Unary.getOp().toString();
		if(op.equals("OP_EXCL")&&(exp0.equals(TypeUtils.Type.BOOLEAN)||exp0.equals(TypeUtils.Type.INTEGER))) {
			expression_Unary.setNewType(exp0);
			resType= (exp0);
		}
		else if(op.equals("OP_PLUS")|| op.equals("OP_MINUS")&& exp0.equals(TypeUtils.Type.INTEGER)) {
			expression_Unary.setNewType(TypeUtils.Type.INTEGER);
			resType= TypeUtils.Type.INTEGER;
		}
		else resType= null;
		if(expression_Unary.getNewType()==null) {
			throw new SemanticException(expression_Unary.getFirstToken(), "visitExpression_Unary-null-type-problem");
		} return resType;
	}

	//done1
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeUtils.Type exp0 =  (TypeUtils.Type) index.getexp0().visit(this, arg);
		TypeUtils.Type exp1 =  (TypeUtils.Type) index.getexp1().visit(this, arg);
		if(index.getexp0() instanceof Expression_PredefinedName && index.getexp1() instanceof Expression_PredefinedName) {
			Expression_PredefinedName exxp0 = (Expression_PredefinedName)index.getexp0();
			Expression_PredefinedName exxp1 = (Expression_PredefinedName)index.getexp1();
			
			if(exp0.equals(TypeUtils.Type.INTEGER) && exp1.equals(TypeUtils.Type.INTEGER)) {
				index.setCartesian(!(exxp0.kind==Scanner.Kind.KW_r && exxp1.kind==Scanner.Kind.KW_a));
				return TypeUtils.Type.INTEGER;
			}throw new SemanticException(index.getFirstToken(), "visitIndex-condition-mismatch-problem");
		} else {index.setCartesian(!(index.e0.firstToken.kind == Kind.KW_r && index.e1.firstToken.kind == Kind.KW_a));
		
		return TypeUtils.Type.INTEGER;
		}
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String nm = expression_PixelSelector.getName();
		Index index= expression_PixelSelector.getIndex();
		TypeUtils.Type nameType=symtab.lookupType(nm);
		if(nameType==null) throw new SemanticException(expression_PixelSelector.getFirstToken(), "not declared visitExpression_PixelSelector problem");
		if(index!=null) {
			expression_PixelSelector.index.visit(this,arg);
		}
		TypeUtils.Type resType=null;
	//name type
		if(nameType.equals(TypeUtils.Type.IMAGE)) {
			expression_PixelSelector.setNewType(TypeUtils.Type.INTEGER);
			resType= (TypeUtils.Type.INTEGER);
		}
		else if(index==null) {
			expression_PixelSelector.setNewType(nameType);
			resType =nameType;
		}
		else resType= null;
		if(expression_PixelSelector.getNewType()==null) {
			throw new SemanticException(expression_PixelSelector.getFirstToken(), "visitExpression_PixelSelectorpixel null type problem");
		} return resType;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		TypeUtils.Type cond =  (TypeUtils.Type) expression_Conditional.getCondition().visit(this, arg);
		TypeUtils.Type tcond =  (TypeUtils.Type) expression_Conditional.getTrueCondition().visit(this, arg);
		TypeUtils.Type fcond =  (TypeUtils.Type) expression_Conditional.getFalseCondition().visit(this, arg);
		TypeUtils.Type typeResult=null;
		if(cond.equals(TypeUtils.Type.BOOLEAN) && tcond.equals(fcond)) {
			typeResult= tcond;
			expression_Conditional.setNewType(tcond);
			return expression_Conditional.getNewType();
		} throw new SemanticException(expression_Conditional.getFirstToken(), "visitExpression_Conditional Require problem");
		
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		String nm = declaration_Image.getname();
		TypeUtils.Type dec=symtab.lookupType(nm);
		if(dec==null) {
		boolean check;
		check=symtab.insert(nm, declaration_Image);
		if(!check)
			throw new SemanticException(declaration_Image.getFirstToken(), "visitDeclaration_Image symbol table insertion problem");
		if(declaration_Image.getsource()!=null) {
			declaration_Image.source.visit(this, arg);
		}
		declaration_Image.setNewType(TypeUtils.Type.IMAGE);
		TypeUtils.Type resType=null;
		if(declaration_Image.getxsize()!=null) {
			TypeUtils.Type xType =  (TypeUtils.Type) declaration_Image.getxsize().visit(this, arg);
			TypeUtils.Type yType =  (TypeUtils.Type) declaration_Image.getysize().visit(this, arg);
			if(declaration_Image.getysize()!=null && xType.equals(TypeUtils.Type.INTEGER) && yType.equals(TypeUtils.Type.INTEGER)  ) {
				resType= declaration_Image.getNewType();
			}
			else throw new SemanticException(declaration_Image.getFirstToken(), "type mismatch visitDeclaration_Image problem");
		} 
		resType= declaration_Image.getNewType();
		return resType;
		}
		else throw new SemanticException(declaration_Image.getFirstToken(), "visitDeclaration_Image REQUIRE problem");		
		
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String nm = source_StringLiteral.getFileOrURL();
		TypeUtils.Type resType =null;
		if(nm==null) throw new SemanticException(source_StringLiteral.getFirstToken(), "visitSource_StringLiteral not declared problem");
		if(source_StringLiteral.isValidURL(nm)) {
		source_StringLiteral.setNewType(TypeUtils.Type.URL);
		resType=TypeUtils.Type.URL;
		}else {
			source_StringLiteral.setNewType(TypeUtils.Type.FILE);
			resType=TypeUtils.Type.FILE;
		}
		return resType;
	}

	//done1
	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(source_CommandLineParam.getParamNum()==null)
			new SemanticException(source_CommandLineParam.firstToken,"Parameter not found");
		
		Type t=(Type)source_CommandLineParam.getParamNum().visit(this, arg);
		if(source_CommandLineParam.getParamNum().getNewType().equals(TypeUtils.Type.INTEGER))
			new SemanticException(source_CommandLineParam.firstToken, "CommandLineParam Invalid. Expected Integer found "+t.toString());
		
		source_CommandLineParam.setNewType(null);
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String nm = source_Ident.getName();
		TypeUtils.Type nameType=symtab.lookupType(nm);
		Declaration dec = symtab.lookupDec(nm);
		if(nameType==null || dec==null) {throw  new SemanticException(source_Ident.firstToken, "visitSource_Ident- not declared-problem");}
		source_Ident.setNewType(nameType);
		TypeUtils.Type resultType =null;
		if(source_Ident.getNewType().equals(TypeUtils.Type.FILE) || source_Ident.getNewType().equals(TypeUtils.Type.URL)) {
			source_Ident.setNewType(nameType);
			resultType=nameType;
			return resultType;
		}
		else throw new SemanticException(source_Ident.getFirstToken(), "visitSource_Ident condition mismatch problem");
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String nm = declaration_SourceSink.getname();
		Declaration dec=symtab.lookupDec(nm);
		if(dec==null) {
		boolean check;
		check=symtab.insert(nm, declaration_SourceSink);
		if(!check)
			throw new SemanticException(declaration_SourceSink.getFirstToken(), "visitDeclaration_SourceSink symbol table insertion problem");
		 Scanner.Kind k= declaration_SourceSink.getDtype();
		 TypeUtils.Type rytType =TypeUtils.getType1(k);
			//System.out.println(declaration_SourceSink.source.type);
			//System.out.println(rytType);
		declaration_SourceSink.setNewType(rytType);
		TypeUtils.Type sType =  (TypeUtils.Type) declaration_SourceSink.getSource().visit(this, arg);
		if(sType !=(declaration_SourceSink.getNewType())  && declaration_SourceSink.source.type!=null) {
			throw new SemanticException(declaration_SourceSink.getFirstToken(), "visitDeclaration_SourceSink condition mismatch problem");
		} return rytType;
		}
		else throw new SemanticException(declaration_SourceSink.getFirstToken(), "visitDeclaration_SourceSink REQUIRE problem");
		


	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_IntLit.setNewType(TypeUtils.Type.INTEGER);
		return TypeUtils.Type.INTEGER;
		
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeUtils.Type expType =  (TypeUtils.Type) expression_FunctionAppWithExprArg.getExpression().visit(this, arg);
		TypeUtils.Type resType=null;
		if(expType.equals(TypeUtils.Type.INTEGER)) {
			expression_FunctionAppWithExprArg.setNewType(TypeUtils.Type.INTEGER);
			resType=expression_FunctionAppWithExprArg.getNewType();
			return resType;
		} throw new SemanticException(expression_FunctionAppWithExprArg.getFirstToken(), "visitExpression_FunctionAppWithExprArg condition mismatch problem");
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_FunctionAppWithIndexArg.setNewType(Type.INTEGER);
		return TypeUtils.Type.INTEGER;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_PredefinedName.type=Type.INTEGER;
		return TypeUtils.Type.INTEGER;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String nm = statement_Out.getname();
		Declaration dec=symtab.lookupDec(nm);
		if(dec==null) {
			throw new SemanticException(statement_Out.getFirstToken(), "visitStatement_Out not declared  problem");
		}
		TypeUtils.Type typ= symtab.lookupType(nm);
		if(typ==null) {
			throw new SemanticException(statement_Out.getFirstToken(), "visitStatement_Out not declared problem");
		}
		
		TypeUtils.Type sType =  (TypeUtils.Type) statement_Out.getsink().visit(this, arg);
		//System.out.println("dtype"+typ);
		//System.out.println("sinktype"+statement_Out.sink.type);
		if(((typ.equals(TypeUtils.Type.INTEGER)) || (typ.equals(TypeUtils.Type.BOOLEAN)) && sType.equals(TypeUtils.Type.SCREEN) ) ) {
		statement_Out.setDec(dec);	
		return typ;
		}
		else if((typ.equals(TypeUtils.Type.IMAGE)) && ((sType.equals(TypeUtils.Type.FILE)) || sType.equals(TypeUtils.Type.SCREEN)) ) {
		statement_Out.setDec(dec);	
		return typ;
		}throw new SemanticException(statement_Out.getFirstToken(), "visitStatement_Out problem");
	
	}

	//done1
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String nm = statement_In.getName();
		Declaration dec=symtab.lookupDec(nm);
//		if(dec==null) {
//			throw new SemanticException(statement_In.getFirstToken(), "visitStatement_In not declared problem"); 
//		}
		statement_In.setDec(dec);
		
		TypeUtils.Type nType = symtab.lookupType(nm); 
		if(statement_In.getSource() !=null)
			statement_In.source.visit(this, arg);
		else
			throw new SemanticException(statement_In.firstToken, "Invalid Statement IN - no source");
		 return nType;
	
		
	}

	//done
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub

		statement_Assign.getlhs().visit(this, arg);
		TypeUtils.Type lType = (TypeUtils.Type)statement_Assign.getlhs().visit(this, arg);
		TypeUtils.Type eType =  (TypeUtils.Type) statement_Assign.getexpr().visit(this, arg);
		if(lType==Type.IMAGE && eType==Type.INTEGER) {
			statement_Assign.setCartesian( statement_Assign.getlhs().isCartesian());
			return lType;
		}
		if(!lType.equals(eType)) {
			 throw new SemanticException(statement_Assign.getFirstToken(), "visitStatement_Assign problem");
		}
		else {
			statement_Assign.setCartesian( statement_Assign.getlhs().isCartesian());
			return lType;
		}
	}

	//done
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String nm = lhs.getName();
		Declaration dec = symtab.lookupDec(nm); 
		if(dec==null)
			throw new SemanticException(lhs.getFirstToken(), "visitLHS not declared problem");
		TypeUtils.Type nType = dec.getNewType(); 
		if(nType==null)
			throw new SemanticException(lhs.getFirstToken(), "visitLHS not declared problem");
		lhs.setNewType(nType);
		lhs.setNewDec(dec);
		//TODO
		if(lhs.getIndex()!=null) {
		lhs.index.visit(this, arg);
		Index index=lhs.getIndex();
		boolean res = index.isCartesian();
		lhs.setCartesian(res);
		} else {
			lhs.isCartesian=false;
		}
		return nType;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		sink_SCREEN.type=Type.SCREEN;
		return TypeUtils.Type.SCREEN;
	}

	//done1
	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String nm = sink_Ident.getName();
		TypeUtils.Type nType = symtab.lookupType(nm); 
		TypeUtils.Type resType=null;
		if(nType.equals(TypeUtils.Type.FILE)) {
			sink_Ident.setNewType(nType);
			resType=nType;
			return resType;
		}throw new SemanticException(sink_Ident.getFirstToken(), "visitSink_Ident problem");
	}
//done
	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_BooleanLit.setNewType(TypeUtils.Type.BOOLEAN);
		return TypeUtils.Type.BOOLEAN;
	}

	//done1
	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		String nm = expression_Ident.getName();
		TypeUtils.Type nType = symtab.lookupType(nm);
		//if(symtab.getsizeofarraylist()==0 && nType!=null ) throw new SemanticException(expression_Ident.getFirstToken(), "visitExpression_Ident problem");
		Declaration dec = symtab.lookupDec(nm);
		if(nType==null||dec==null) throw new SemanticException(expression_Ident.getFirstToken(), "visitExpression_Ident problem");
		expression_Ident.setNewType(nType);
		return nType;
	}

}
