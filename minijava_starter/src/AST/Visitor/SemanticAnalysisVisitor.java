package AST.Visitor;

import AST.*;
import Symtab.*;

public class SemanticAnalysisVisitor implements ObjectVisitor {

	SymbolTable st = null;
	public int errors = 0;

	public void setSymtab(SymbolTable s) {
		st = s;
	}

	public SymbolTable getSymtab() {
		return st;
	}

	public void report_error(int line, String msg) {
		System.out.println(line + ": " + msg);
		++errors;
	}

	public String checkIntType(Object o) {
		if (o != null && o instanceof String && ((String) o).equals("int")) {
			return "int";
		}
		return null;
	}

	public Symbol getSymbol(ASTNode node) {
		String name = null;

		if (node == null) {
			return null;
		} else if (node instanceof IdentifierExp) {
			IdentifierExp i = (IdentifierExp) node;
			name = i.s;
		} else if (node instanceof Identifier) {
			Identifier i = (Identifier) node;
			name = i.s;
		}

		// symbol was found
		Symbol s = st.lookupSymbol(name);
		if (s != null) {
			return s;
		}

		// get the parent class
		ASTNode p = node;
		while (p != null && !(p instanceof ClassDecl)) {
			p = p.getParent();
		}

		// check if the parent class is extended
		if (p != null && p instanceof ClassDeclExtends) {
			ClassDeclExtends c = (ClassDeclExtends) p;
			ClassSymbol cs = (ClassSymbol) st.lookupSymbol(c.j.s);
			if (cs != null) {
				return cs.getVariable(name);
			}
		}

		return null;
	}

	// Display added for toy example language. Not used in regular MiniJava
	public Object visit(Display n) {
		if (n.e != null)
			n.e.accept(this);
		return null;
	}

	// MainClass m;
	// ClassDeclList cl;
	public Object visit(Program n) {
		if (n.m != null)
			n.m.accept(this);
		for (int x = 0; x < n.cl.size(); x++) {
			n.cl.get(x).accept(this);
		}
		return null;
	}

	// Identifier i1,i2;
	// Statement s;
	public Object visit(MainClass n) {
		Object o;
		if(n.i1 != null)
			o = n.i1.accept(this);
		else
			o = null;
		st = st.findScope(n.i1.toString());
		st = st.findScope("main");
		if (n.s != null)
			n.s.accept(this);
		st = st.exitScope();
		st = st.exitScope();
		return n.i1.toString();
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Object visit(ClassDeclSimple n) {
		Object o;
		if(n.i != null)
			o = n.i.accept(this);
		else
			o = null;
		
		st = st.findScope(n.i.toString());
		
		if (n.vl != null) {
			for (int x = 0; x < n.vl.size(); x++) {
				if (n.vl.get(x) != null)
					n.vl.get(x).accept(this);
			}
		}

		if (n.ml != null) {
			for (int x = 0; x < n.ml.size(); x++) {
				if (n.ml.get(x) != null)
					n.ml.get(x).accept(this);
			}
		}
		
		st = st.exitScope();
		return o;
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Object visit(ClassDeclExtends n) {
		Object o;
		if(n.i != null)
			o = n.i.accept(this);
		else
			o = null;
		
		if (n.j != null)
			n.j.accept(this);

		st = st.findScope(n.i.toString());

		if (n.vl != null) {
			for (int x = 0; x < n.vl.size(); x++) {
				if (n.vl.get(x) != null)
					n.vl.get(x).accept(this);
			}
		}

		if (n.ml != null) {
			for (int x = 0; x < n.ml.size(); x++) {
				if (n.ml.get(x) != null)
					n.ml.get(x).accept(this);
			}
		}

		st = st.exitScope();
		return o;
	}

	// Type t;
	// Identifier i;
	public Object visit(VarDecl n) {
		if(n.t != null)
			return n.t.accept(this);
		else
			return null;
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public Object visit(MethodDecl n) {
		Object o;
		
		if(n.t != null)
			o = n.t.accept(this);
		else
			o = null;
		
		st = st.findScope(n.i.toString());

		if (n.fl != null) {
			for (int x = 0; x < n.fl.size(); x++) {
				if (n.fl.get(x) != null)
					n.fl.get(x).accept(this);
			}
		}

		if (n.vl != null) {
			for (int x = 0; x < n.vl.size(); x++) {
				if (n.vl.get(x) != null)
					n.vl.get(x).accept(this);
			}
		}

		if (n.sl != null) {
			for (int x = 0; x < n.sl.size(); x++) {
				if (n.sl.get(x) != null)
					n.sl.get(x).accept(this);
			}
		}

		
		Object ret;
		if(n.e != null)
			ret = n.e.accept(this);
		else
			ret = null;
		
		if (ret != null && !ret.equals(o)) {
			report_error(n.line_number, "Mismatched function call return type for " + n.i.toString() + ".");
		}

		st = st.exitScope();

		return o;
	}

	// Type t;
	// Identifier i;
	public Object visit(Formal n) {
		if(n.t != null)
			return n.t.accept(this);
		else
			return null;
	}

	public Object visit(IntArrayType n) {
		return "int[]";
	}

	public Object visit(BooleanType n) {
		return "Boolean";
	}

	public Object visit(IntegerType n) {
		return "int";
	}

	// String s;
	public Object visit(IdentifierType n) {
		return n.s;
	}

	// StatementList sl;
	public Object visit(Block n) {
		for (int x = 0; x < n.sl.size(); x++) {
			n.sl.get(x).accept(this);
		}
		return null;
	}

	// Exp e;
	// Statement s1,s2;
	public Object visit(If n) {
		Object o;
		if(n.e != null)
			o = n.e.accept(this);
		else
			o = null;

		Object o1;
		if(n.s1 != null)
			o1 = n.s1.accept(this);
		else
			o1 = null;

		Object o2;
		if(n.s2 != null)
			o2 = n.s2.accept(this);
		else
			o2 = null;

		if (o == null || !(o instanceof String) || !((String) o).equals("Boolean")) {
			report_error(n.line_number, "Expecting boolean type for if condition.");
		}

		return null;
	}

	// Exp e;
	// Statement s;
	public Object visit(While n) {
		Object o1;
		if(n.e != null)
			o1 = n.e.accept(this);
		else
			o1 = null;
		
		Object o2;
		if(n.s != null)
			o2 = n.s.accept(this);
		else
			o2 = null;
		
		if (o1 == null || !(o1 instanceof String) || !((String) o1).equals("Boolean")) {
			report_error(n.line_number, "Expecting boolean type for while loop condition.");
		}

		return null;
	}

	// Exp e;
	public Object visit(Print n) {
		Object o;
		if(n.e != null)
			o = n.e.accept(this);
		else
			o = null;
		
		return "void";
	}

	// Identifier i;
	// Exp e;
	public Object visit(Assign n) {
		Object o1 = (n.i != null) ? n.i.accept(this) : null;
		Object o2 = (n.e != null) ? n.e.accept(this) : null;

		if (o1 == null || o2 == null || !o1.equals(o2)) {
			report_error(n.line_number, "Mismatched lhs & rhs in assignment statement.");
		}

		return o1;
	}

	// Identifier i;
	// Exp e1,e2;
	public Object visit(ArrayAssign n) {
		Object o;
		if(n.i != null)
			o = n.i.accept(this);
		else
			o = null;
		//HOW DO I DO THIS
		
		Object o1;
		if(n.e1 != null)
			o1 = n.e1.accept(this);
		else
			o1 = null;
		Object o2;
		if(n.e2 != null)
			o2 = n.e2.accept(this);
		else
			o2 = null;
		
		String t;
		if(o != null && o instanceof String)
			t = (String) o;
		else
			t = null;

		if (t == null || t != "int[]") {
			report_error(n.line_number, "Undefined array type.");
		} else {
			t = t.replaceAll("\\[\\]", "");
		}

		if (o1 == null || !(o1 instanceof String) || !((String) o1).equals("int")) {
			report_error(n.line_number, "Expecting int type for array index.");
		}

		if (t == null || o2 == null || !t.equals(o2)) {
			report_error(n.line_number, "Mismatched lhs & rhs in array assignment statement.");
		}

		return o;
	}

	// Exp e1,e2;
	public Object visit(And n) {
		Object o1;
		if(n.e1 != null)
			o1 = n.e1.accept(this);
		else
			o1 = null;
		

		Object o2;
		if(n.e1 != null)
			o2 = n.e2.accept(this);
		else
			o2 = null;
		
		String s1;
		if(o1 != null && o1 instanceof String)
			s1 = (String) o1;
		else
			s1 = "";

		String s2;
		if(o2 != null && o2 instanceof String)
			s2 = (String) o2;
		else
			s2 = "";

		if (s1 != s2 || s1 != "Boolean") {
			report_error(n.line_number, "Expecting boolean operand types in and operation.");
			return null;
		}

		return s1;
	}

	// Exp e1,e2;
	public Object visit(LessThan n) {
		String s1;
		if(n.e1 != null)
			s1 = checkIntType(n.e1.accept(this));
		else
			s1 = checkIntType(null);
		
		String s2;
		if(n.e2 != null)
			s2 = checkIntType(n.e2.accept(this));
		else
			s2 = checkIntType(null);
		if (s1 != s2 || s1 != "int") {
			report_error(n.line_number, "Expecting int operand types in compare operation.");
			return null;
		}

		return "Boolean";
	}

	// Exp e1,e2;
	public Object visit(Plus n) {
		String s1;
		if(n.e1 != null)
			s1 = checkIntType(n.e1.accept(this));
		else
			s1 = checkIntType(null);
		
		String s2;
		if(n.e2 != null)
			s2 = checkIntType(n.e2.accept(this));
		else
			s2 = checkIntType(null);
		if (s1 != s2 || s1 != "int") {
			report_error(n.line_number, "Expecting int operand types in add operation.");
			return null;
		}

		return s1;
	}

	// Exp e1,e2;
	public Object visit(Minus n) {
		String s1;
		if(n.e1 != null)
			s1 = checkIntType(n.e1.accept(this));
		else
			s1 = checkIntType(null);
		
		String s2;
		if(n.e2 != null)
			s2 = checkIntType(n.e2.accept(this));
		else
			s2 = checkIntType(null);
		if (s1 != s2 || s1 != "int") {
			report_error(n.line_number, "Expecting int operand types in subtract operation.");
			return null;
		}

		return s1;
	}

	// Exp e1,e2;
	public Object visit(Times n) {
		String s1;
		if(n.e1 != null)
			s1 = checkIntType(n.e1.accept(this));
		else
			s1 = checkIntType(null);
		
		String s2;
		if(n.e2 != null)
			s2 = checkIntType(n.e2.accept(this));
		else
			s2 = checkIntType(null);
		
		if (s1 != s2 || s1 != "int") {
			report_error(n.line_number, "Expecting int operand types in multiply operation.");
			return null;
		}

		return s1;
	}
	
	// Exp e1,e2;
	public Object visit(Divide n) {
		String s1;
		if(n.e1 != null)
			s1 = checkIntType(n.e1.accept(this));
		else
			s1 = checkIntType(null);
		
		String s2;
		if(n.e2 != null)
			s2 = checkIntType(n.e2.accept(this));
		else
			s2 = checkIntType(null);
		
		if (s1 != s2 || s1 != "int") {
			report_error(n.line_number, "Expecting int operand types in divide operation.");
			return null;
		}

		return s1;
	}

	// Exp e1[e2];
	public Object visit(ArrayLookup n) {
		Object o1;
		if(n.e1 != null)
			o1 = n.e1.accept(this);
		else
			o1 = null;
		
		String t;
		if(o1 != null && o1 instanceof String)
			t = (String) o1;
		else
			t = null;

		if (t == null) {
			report_error(n.line_number, "Undefined array type.");
		} else {
			t = t.replaceAll("\\[\\]", "");
		}

		
		Object o2;
		if(n.e2 != null)
			o2 = n.e2.accept(this);
		else
			o2 = null;
		
		if (o2 == null || !(o2 instanceof String) || !((String) o2).equals("int")) {
			report_error(n.line_number, "Expecting int type for array index.");
		}

		return t;
	}

	// Exp e;
	public Object visit(ArrayLength n) {
		if (n.e != null)
			n.e.accept(this);
		return "int";
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public Object visit(Call n) {
		String csTemp;
		if (n.e != null)
			csTemp = (String) n.e.accept(this);
		else
			csTemp = "";

		if (csTemp != null && csTemp != "") {
			Symbol s = st.lookupSymbol(csTemp);
			if (s == null || !(s instanceof ClassSymbol)) {
				report_error(n.e.line_number, "Class object \"" + csTemp + "\" is undefined.");
				return null;
			}

			ClassSymbol cs = (ClassSymbol) s;
			MethodSymbol ms = cs.getMethod(n.i.s);

			if (ms != null) {
				if (ms.getParameters().size() != n.el.size()) {
					report_error(n.e.line_number, "Function call \"" + n.i.s + "\" expecting "
							+ ms.getParameters().size() + " argument(s), but " + n.el.size() + " provided.");
				} 
				else {
					for (int x = 0; x < n.el.size(); x++) {
						String arg = ms.getParameters().get(x).getType();
						String param = (String) n.el.get(x).accept(this);
						Symbol paramSym;
						if (param != null)
							paramSym = st.lookupSymbol(param);
						else
							paramSym = null;

						if (paramSym != null && paramSym instanceof ClassSymbol) {
							String paramExt = paramSym.getType();
							if (!param.equals(arg) && !paramExt.equals(arg)) {
								report_error(n.e.line_number, "Function call \"" + n.i.s + "\" argument " + x
										+ " expecting " + arg + " type.");
							}
						} else if (!param.equals(arg)) {
							report_error(n.e.line_number,
									"Function call \"" + n.i.s + "\" argument " + x + " expecting " + arg + " type.");

						}
					}
				}


			return ms.getType();
			}

		report_error(n.line_number, "Function call \"" + n.i.s + "\" is undefined.");
		return null;
		}

		Symbol s = st.lookupSymbol(n.i.s);
		if(s != null && s instanceof MethodSymbol){
			MethodSymbol ms = (MethodSymbol) s;

			if(ms.getParameters().size() != n.el.size()){
				report_error(n.e.line_number, "Function call \""+n.i.s+"\" expecting "+ms.getParameters().size()+" arguments, but only "+n.el.size()+" were provided.");
			}
			else{
				for(int x = 0; x < n.el.size(); x++){
					Object o = n.el.get(x).accept(this);
					String arg = ms.getParameters().get(x).getType();
					if(!o.equals(arg))
						report_error(n.e.line_number, "Function call \""+n.i.s+"\" argument "+x+" expecting "+arg+" type.");

				}
			}

			return ms.getType();
		}

		report_error(n.line_number, "Function call \""+n.i.s+"\" is undefined.");
		return null;

	}

	// int i;
	public Object visit(IntegerLiteral n) {
		return "int";
	}

	public Object visit(True n) {
		return "Boolean";
	}

	public Object visit(False n) {
		return "Boolean";
	}

	// String s;
	public Object visit(IdentifierExp n) {
		Symbol s = getSymbol(n);
		if (s == null) {
			report_error(n.line_number, "Symbol \"" + n.s + "\" has not been declared.");
			return null;
		}

		return s.getType();
	}

	public Object visit(This n) {

		// get the current scope
		ASTNode a = st.getScope();

		// Get the parent class
		while (a != null && !(a instanceof ClassDecl)) {
			a = a.getParent();
		}

		if (a != null && a instanceof ClassDeclSimple) {
			ClassDeclSimple c = (ClassDeclSimple) a;
			return c.i.toString();
		} else if (a != null && a instanceof ClassDeclExtends) {
			ClassDeclExtends c = (ClassDeclExtends) a;
			return c.i.toString();
		}

		report_error(n.line_number, "Unable to determine \"this\" reference.");
		return null;
	}

	// Exp e;
	public Object visit(NewArray n) {
		// new int[e]
		Object o;
		if (n.e != null)
			o = n.e.accept(this);
		else
			o = null;

		if (o == null || !(o instanceof String) || !((String) o).equals("int")) {
			report_error(n.line_number, "New array expecting integer size.");
		}
		return "int[]";
	}

	// Identifier i;
	public Object visit(NewObject n) {
		Symbol s = getSymbol(n.i);
		if (s == null) {
			report_error(n.line_number, "Object \"" + n.i.s + "\" has not been declared.");
		}

		return n.i.s;
	}

	// Exp e;
	public Object visit(Not n) {
		if (n.e != null)
			return n.e.accept(this);
		else
			return null;
	}

	// String s;
	public Object visit(Identifier n) {
		Symbol s = getSymbol(n);
		if (s == null) {
			getSymbol(n);
			report_error(n.line_number, "Symbol \"" + n.s + "\" has not been declared.");
			return null;
		}
		return s.getType();
	}
}