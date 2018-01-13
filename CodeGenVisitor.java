package cop5556fa17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		cw.visitField(ACC_STATIC, "x", "I", null, null);
		cw.visitField(ACC_STATIC, "y", "I", null, null);
		cw.visitField(ACC_STATIC, "X", "I", null, null);
		cw.visitField(ACC_STATIC, "Y", "I", null, null);
		cw.visitField(ACC_STATIC, "DEF_X", "I", null, new Integer(256));
		cw.visitField(ACC_STATIC, "DEF_Y", "I", null, new Integer(256));
		cw.visitField(ACC_STATIC, "Z", "I", null, new Integer(16777215));
		cw.visitField(ACC_STATIC, "r", "I", null, null);
		cw.visitField(ACC_STATIC, "a", "I", null, null);
		cw.visitField(ACC_STATIC, "R", "I", null, null);
		cw.visitField(ACC_STATIC, "A", "I", null, null);
		
		
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectively
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		if(statement_Assign.lhs.getNewType()!=Type.IMAGE){
		statement_Assign.getexpr().visit(this, arg); 
	     statement_Assign.getlhs().visit(this, arg);
		} else {
			
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.getName(), "Ljava/awt/image/BufferedImage;");	
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");

			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.getName(), "Ljava/awt/image/BufferedImage;");	
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "X", "I");
				
		Label xst= new Label();
		Label yst= new Label();
		Label yen= new Label();
		Label xen= new Label();
		String Int_type = "I";
		String xpixel = "x";
		String ypixel = "y";
		mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTSTATIC, className, ypixel, Int_type);
        mv.visitInsn(ICONST_0);
	    mv.visitFieldInsn(PUTSTATIC, className, xpixel, Int_type);

        mv.visitLabel(xst);
		
		mv.visitFieldInsn(GETSTATIC, className, xpixel, Int_type);
		
		mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.getName(), "Ljava/awt/image/BufferedImage;");
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
		//check to see if the temp_x is valid and go to label xen
		mv.visitJumpInsn(IF_ICMPEQ, xen);
		
		// start with y, if x is valid
		mv.visitLabel(yst);
		//getting temp_y
		mv.visitFieldInsn(GETSTATIC, className, ypixel, Int_type);
		//getting y
		mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.getName(),"Ljava/awt/image/BufferedImage;");
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
		//check to see if temp_y is valid
		mv.visitJumpInsn(IF_ICMPEQ, yen);
		
		//System.out.println(statement_Assign.isCartesian());
		if(statement_Assign.isCartesian() == false ) { 

          //save value in r static variable
    	  mv.visitFieldInsn(GETSTATIC, className, xpixel, Int_type);
    	  mv.visitFieldInsn(GETSTATIC, className, ypixel, Int_type);
        
         mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
          mv.visitFieldInsn(PUTSTATIC, className, "r", Int_type);

          //save value is a static variable
          mv.visitFieldInsn(GETSTATIC, className, xpixel, Int_type);
          mv.visitFieldInsn(GETSTATIC, className, ypixel, Int_type);
         
         
          mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
          mv.visitFieldInsn(PUTSTATIC, className, "a", Int_type);

      }
		statement_Assign.e.visit(this, arg);
		statement_Assign.lhs.visit(this, arg);

		//increment temp_y count
		mv.visitLdcInsn(1);
		mv.visitFieldInsn(GETSTATIC, className, ypixel, Int_type);
		mv.visitInsn(IADD);
		mv.visitFieldInsn(PUTSTATIC, className, ypixel, Int_type);			
		mv.visitJumpInsn(GOTO, yst);
		
		mv.visitLabel(yen);

		//increment temp_x count
		mv.visitLdcInsn(1);
		mv.visitFieldInsn(GETSTATIC, className, xpixel, Int_type);
		mv.visitInsn(IADD);
		mv.visitFieldInsn(PUTSTATIC, className, xpixel, Int_type);
	    mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTSTATIC, className, ypixel, Int_type);
		mv.visitJumpInsn(GOTO, xst);
		
		mv.visitLabel(xen);
	
		}
	     return null;	
	}
	
	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO 
		if(declaration_Variable.getNewType()==TypeUtils.Type.INTEGER) {
			 FieldVisitor decvar_fv = cw.visitField(ACC_STATIC, declaration_Variable.getname(), "I", null, null);
		}else if (declaration_Variable.getNewType()==TypeUtils.Type.BOOLEAN) {
			 FieldVisitor decvar_fv = cw.visitField(ACC_STATIC, declaration_Variable.getname(),"Z", null, null);
		}      
	    if (declaration_Variable.getExpr() != null) {
	           declaration_Variable.getExpr().visit(this, arg);
	           String decVarStaticType = declaration_Variable.getNewType()==Type.BOOLEAN?"Z":"I";
	           mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.getname(), decVarStaticType);
	    }
	    return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO 
	        Scanner.Kind op = expression_Binary.getOp();
	        Label startLabel = new Label();
	        Label endLabel = new Label();
	        expression_Binary.gete0().visit(this, arg);
	        expression_Binary.gete1().visit(this, arg);
	        if(op==Scanner.Kind.OP_NEQ || op== Scanner.Kind.OP_EQ || op==Scanner.Kind.OP_GE|| op==Scanner.Kind.OP_LE|| op==Scanner.Kind.OP_GT||op==Scanner.Kind.OP_LT){
	           if(op==Scanner.Kind.OP_NEQ) {
	        	   mv.visitJumpInsn(IF_ICMPNE, startLabel);
	           }else if(op== Scanner.Kind.OP_LE) {
	                mv.visitJumpInsn(IF_ICMPLE, startLabel);

	           }else if(op== Scanner.Kind.OP_GT) {
	                mv.visitJumpInsn(IF_ICMPGT, startLabel);

	           }else if(op== Scanner.Kind.OP_EQ) {
	                mv.visitJumpInsn(IF_ICMPEQ, startLabel);

	           }else if(op== Scanner.Kind.OP_GE) {
	                mv.visitJumpInsn(IF_ICMPGE, startLabel);

	           }else if(op== Scanner.Kind.OP_LT) {
	                mv.visitJumpInsn(IF_ICMPLT, startLabel);

	           }       
	                //false block
	                mv.visitInsn(ICONST_0);
	                mv.visitJumpInsn(GOTO, endLabel); //skip false block

	                //true block
	                mv.visitLabel(startLabel);
	                mv.visitInsn(ICONST_1);

	                //end block
	                mv.visitLabel(endLabel);
	        }
	        if(op==Scanner.Kind.OP_AND || op== Scanner.Kind.OP_OR || op==Scanner.Kind.OP_TIMES|| op==Scanner.Kind.OP_PLUS|| op==Scanner.Kind.OP_MINUS||op==Scanner.Kind.OP_DIV||op==Scanner.Kind.OP_MOD){
		           if(op==Scanner.Kind.OP_PLUS) {
		        	   mv.visitInsn(IADD);
		           }else if(op== Scanner.Kind.OP_MINUS) {
		                mv.visitInsn(ISUB);

		           }else if(op== Scanner.Kind.OP_DIV) {
		                mv.visitInsn(IDIV);

		           }else if(op== Scanner.Kind.OP_MOD) {
		                mv.visitInsn(IREM);

		           }else if(op== Scanner.Kind.OP_OR) {
		                mv.visitInsn(IOR);

		           }else if(op== Scanner.Kind.OP_AND) {
		                mv.visitInsn(IAND);

		           } else if(op== Scanner.Kind.OP_TIMES) {
		                mv.visitInsn(IMUL);
		           }          
	        }
	        //CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getNewType()); 
	        return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		 expression_Unary.getExpr().visit(this, arg);
		Scanner.Kind exprOp = expression_Unary.getOp();
		if(exprOp==Scanner.Kind.OP_EXCL) {
			 if(expression_Unary.getNewType() == TypeUtils.Type.INTEGER){
				 mv.visitLdcInsn(INTEGER.MAX_VALUE);
                 mv.visitInsn(IXOR);
             }else {
            	 Label fLabel = new Label();
            	 Label tLabel = new Label();
                 mv.visitJumpInsn(IFNE, fLabel);
                 mv.visitInsn(ICONST_1); 
                 mv.visitJumpInsn(GOTO, tLabel);
                 mv.visitLabel(fLabel);
                 mv.visitInsn(ICONST_0); 
                 mv.visitLabel(tLabel);
             }
		}else if(exprOp==Scanner.Kind.OP_MINUS) {
			 mv.visitInsn(INEG);
		}
		//for OP_PLUS: doing nothing
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getNewType());
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		index.getexp0().visit(this,arg);
		index.getexp1().visit(this, arg);
		//System.out.println(index.isCartesian()+"index");
		if(!index.isCartesian()) {
			//System.out.println("not cartesian");
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x",RuntimeFunctions.cart_xSig ,RuntimeFunctions.class.isInterface());
			mv.visitInsn(DUP_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y",RuntimeFunctions.cart_ySig ,RuntimeFunctions.class.isInterface());
			return null;
		}
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.getName(), ImageSupport.ImageDesc);
		expression_PixelSelector.getIndex().visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", "(Ljava/awt/image/BufferedImage;II)I", false);
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO 
		 Label startLabel = new Label();
		 Label endLabel = new Label();
	        expression_Conditional.getCondition().visit(this, arg);
	        mv.visitInsn(ICONST_0);
	        mv.visitJumpInsn(IF_ICMPEQ,startLabel);

	        expression_Conditional.getTrueCondition().visit(this, arg);
	        mv.visitJumpInsn(GOTO, endLabel);

	        mv.visitLabel(startLabel);
	        expression_Conditional.getFalseCondition().visit(this, arg);

	        mv.visitLabel(endLabel);
	        //System.out.println();
	        //CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.getTrueCondition().getNewType()); 
	        return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		//Add a field to the class with type java.awt.image.BufferedImage
		if(declaration_Image.getNewType()==TypeUtils.Type.IMAGE) {
			cw.visitField(ACC_STATIC, declaration_Image.getname(), ImageSupport.ImageDesc, null, null);
		}
		Expression dec_xsize = declaration_Image.getxsize();
    	Expression dec_ysize = declaration_Image.getysize();
    	if(declaration_Image.getsource()!=null) {
		if(dec_xsize !=null) {
			declaration_Image.source.visit(this, arg);
           	if(dec_ysize !=null) {
           		dec_xsize.visit(this, arg);
           		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer","valueOf", "(I)Ljava/lang/Integer;", false);
           		dec_ysize.visit(this, arg);
           		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer","valueOf", "(I)Ljava/lang/Integer;", false);
           	}else {
           		mv.visitInsn(ACONST_NULL);
    	 		mv.visitInsn(ACONST_NULL);           		
           	}
    	}else {
    			declaration_Image.source.visit(this, arg);
    		    mv.visitInsn(ACONST_NULL);
    	 		mv.visitInsn(ACONST_NULL);
    		
    	}
    	mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"readImage", ImageSupport.readImageSig, false);
    	mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.getname(), "Ljava/awt/image/BufferedImage;");
    	} else {
    		if(dec_xsize!=null && dec_ysize!=null) {
    			dec_xsize.visit(this, arg);
    			dec_ysize.visit(this, arg);
    		}else {
    			mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
    			mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
    		}
        	mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"makeImage", ImageSupport.makeImageSig, false);
    		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.getname(), ImageSupport.ImageDesc);
    	}
    	return null;
		
	}
		
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		mv.visitLdcInsn(source_StringLiteral.getFileOrURL());
		return null;
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO 
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.getParamNum().visit(this, arg);
		mv.visitInsn(AALOAD);
		return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6 check
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.getName(), "Ljava/lang/String;");
		return null;
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		if(declaration_SourceSink.getNewType()==TypeUtils.Type.FILE) {
			 FieldVisitor decvar_fv = cw.visitField(ACC_STATIC, declaration_SourceSink.getname(), "Ljava/lang/String;", null, null);
		}else if (declaration_SourceSink.getNewType()==TypeUtils.Type.URL) {
			 FieldVisitor decvar_fv = cw.visitField(ACC_STATIC, declaration_SourceSink.getname(),"Ljava/lang/String;", null, null);
		}      
	    if (declaration_SourceSink.getSource() != null) {
	    	declaration_SourceSink.getSource().visit(this, arg);
	           String decVarStaticType = declaration_SourceSink.getNewType()==Type.FILE?"Ljava/lang/String;":"Ljava/lang/String;";
	           mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.getname(), decVarStaticType);
	    }
	    return null;
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO 
		mv.visitLdcInsn(expression_IntLit.getVal());
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6 check
		expression_FunctionAppWithExprArg.getExpression().visit(this, arg);
		switch(expression_FunctionAppWithExprArg.getFunction()) {
		case KW_abs:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs","(I)I" ,RuntimeFunctions.class.isInterface());
			break;
		case KW_log:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log","(I)I" ,RuntimeFunctions.class.isInterface());
			break;
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6 check
		expression_FunctionAppWithIndexArg.arg.getexp0().visit(this,arg);	
		expression_FunctionAppWithIndexArg.arg.getexp1().visit(this,arg);
		switch(expression_FunctionAppWithIndexArg.getFunction()) {
		case KW_cart_x:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x","(II)I" ,RuntimeFunctions.class.isInterface());
			break;
		case KW_polar_r:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r","(II)I" ,RuntimeFunctions.class.isInterface());
			break;
		case KW_polar_a:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a","(II)I" ,RuntimeFunctions.class.isInterface());
			break;
		case KW_cart_y:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y","(II)I" ,RuntimeFunctions.class.isInterface());
			break;
		
		}
	
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		Scanner.Kind exp_kind = expression_PredefinedName.kind;
		if(exp_kind==Scanner.Kind.KW_x || exp_kind==Scanner.Kind.KW_X||exp_kind==Scanner.Kind.KW_y||exp_kind==Scanner.Kind.KW_Y||exp_kind==Scanner.Kind.KW_r||exp_kind==Scanner.Kind.KW_R||exp_kind==Scanner.Kind.KW_a||exp_kind==Scanner.Kind.KW_A) {
			String name ="";
			if(exp_kind==Scanner.Kind.KW_x) {
				name = "x";
			}else if(exp_kind==Scanner.Kind.KW_X) {
				name = "X";
			}else if(exp_kind==Scanner.Kind.KW_A) {
				 name = "A";
			}else if(exp_kind==Scanner.Kind.KW_r) {
				 name = "r";
			}else if(exp_kind==Scanner.Kind.KW_R) {
				 name = "R";
			}else if(exp_kind==Scanner.Kind.KW_y) {
				 name = "y";
			}else if(exp_kind==Scanner.Kind.KW_Y) {
				 name = "Y";
			}else if(exp_kind==Scanner.Kind.KW_a) {
				 name = "a";
			}
			mv.visitFieldInsn(GETSTATIC, className, name, "I");
		}else if(exp_kind==Scanner.Kind.KW_Z||exp_kind==Scanner.Kind.KW_DEF_X||exp_kind==Scanner.Kind.KW_DEF_Y) {
			if(exp_kind==Scanner.Kind.KW_DEF_X) {
				mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
				//mv.visitLdcInsn(256);
			}else if(exp_kind==Scanner.Kind.KW_DEF_Y) {
				mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
				//mv.visitLdcInsn(256);
			}else if(exp_kind==Scanner.Kind.KW_Z) {
				mv.visitFieldInsn(GETSTATIC, className, "Z", "I");
				//mv.visitLdcInsn(0xFFFFFF);
			}
		}
		return null;
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		TypeUtils.Type sOut_type = statement_Out.getDec().getNewType();
        String staticVarName = statement_Out.name;
        switch (sOut_type){
            case INTEGER:
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitFieldInsn(GETSTATIC, className,staticVarName, "I");
                CodeGenUtils.genLogTOS(GRADE, mv, sOut_type); 
                String statType = sOut_type==Type.BOOLEAN?"(Z)V":"(I)V";
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", statType, false);
                break;
            case BOOLEAN:
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitFieldInsn(GETSTATIC, className,staticVarName, "Z");
                CodeGenUtils.genLogTOS(GRADE, mv, sOut_type); 
                String stateType = sOut_type==Type.BOOLEAN?"(Z)V":"(I)V";
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", stateType, false);
                break;
            case IMAGE:
                mv.visitFieldInsn(GETSTATIC, className, staticVarName, ImageSupport.ImageDesc);
                CodeGenUtils.genLogTOS(GRADE, mv, sOut_type); 
                statement_Out.getsink().visit(this, arg);
                break;
        }
        return null;
	}
//
//	/**
//	 * Visit source to load rhs, which will be a String, onto the stack
//	 * 
//	 *  In HW5, you only need to handle INTEGER and BOOLEAN
//	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
//	 *  to convert String to actual type. 
//	 *  
//	 *  TODO HW6 remaining types
//	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
		TypeUtils.Type statementType = statement_In.getDec().getNewType();
		 statement_In.source.visit(this, arg); 
	        switch (statementType){
	            case INTEGER:
	                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt","(Ljava/lang/String;)I",false);
	                String intType = "I";
	                mv.visitFieldInsn(PUTSTATIC, className, statement_In.getName(), intType);
	                break;
	            case BOOLEAN:
	                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean","(Ljava/lang/String;)Z",false);
	                String bType = "Z";
	                mv.visitFieldInsn(PUTSTATIC, className, statement_In.getName(), bType);
	                break; 
	            case IMAGE:
	            	Declaration_Image decIm = (Declaration_Image)statement_In.getDec();
	            	Expression dec_xsize = decIm.getxsize();
	            	Expression dec_ysize = decIm.getysize();
	            	if(dec_xsize !=null) {
	                   	if(dec_ysize !=null) {
	                   		dec_ysize.visit(this, arg);
	                   		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer","valueOf", "(I)Ljava/lang/Integer;", false);
	                   		dec_xsize.visit(this, arg);
	                   		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer","valueOf", "(I)Ljava/lang/Integer;", false);
	                   	}else {
	                   		mv.visitInsn(ACONST_NULL);
	            	 		mv.visitInsn(ACONST_NULL);           		
	                   	}
	            	}else { mv.visitInsn(ACONST_NULL);
	            	 		mv.visitInsn(ACONST_NULL);
	            		
	            	}
	            	mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className,"readImage", ImageSupport.readImageSig, false);
	            	mv.visitFieldInsn(PUTSTATIC, className, decIm.getname(), "Ljava/awt/image/BufferedImage;");
	        }      
	        return null;
	}

//	
//	
//
//	/**
//	 * In HW5, only handle INTEGER and BOOLEAN types.
//	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		 String lhsVarType ="";
		 String lhsImgType ="";
		 //if(lhs.index != null)  lhs.index.visit(this, arg);         
	        TypeUtils.Type lhsType = lhs.getNewType();
	        if(lhsType==TypeUtils.Type.INTEGER) {
	        	lhsVarType ="I";
	        	mv.visitFieldInsn(PUTSTATIC, className, lhs.getName(), lhsVarType);
	        }else if(lhsType==TypeUtils.Type.BOOLEAN) {
	        	lhsVarType ="Z";
	        	mv.visitFieldInsn(PUTSTATIC, className, lhs.getName(), lhsVarType);
	        }else if(lhsType==TypeUtils.Type.IMAGE) {
	        	lhsImgType ="I";
	        	mv.visitFieldInsn(GETSTATIC, className, lhs.getName(), ImageSupport.ImageDesc);
	        	mv.visitFieldInsn(GETSTATIC, className, "x", lhsImgType);
	        	mv.visitFieldInsn(GETSTATIC, className, "y", lhsImgType);
	        	mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);
	        }
	        return null;
	}

		
	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		// TODO HW6
		mv.visitMethodInsn(INVOKESTATIC, ImageFrame.className, "makeFrame", "(Ljava/awt/image/BufferedImage;)Ljavax/swing/JFrame;", false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		// TODO HW6
				mv.visitFieldInsn(GETSTATIC, className, sink_Ident.getName(), "Ljava/lang/String;");
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);
				return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		mv.visitLdcInsn(expression_BooleanLit.getVal());
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		if(expression_Ident.getNewType()==TypeUtils.Type.INTEGER) {
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.getName(), "I");
		}
		else if(expression_Ident.getNewType()==TypeUtils.Type.BOOLEAN) {
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.getName(), "Z");	
	    }
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getNewType());
		return null;
	}

}







