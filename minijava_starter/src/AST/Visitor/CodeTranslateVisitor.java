package AST.Visitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import AST.*;
import Symtab.*;

public class CodeTranslateVisitor implements Visitor {

	SymbolTable st = null;
	private HashMap<VarSymbol, Integer> symTab = new HashMap<VarSymbol, Integer>();
	public int errors = 0;
	int tabs = 0;
	int stackPos = 0;
	int argPos = 0;
	int labelNum = 0;
	final String regis[] = { "%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9" };

	public void setSymtab(SymbolTable s) {
		st = s;
	}

	public SymbolTable getSymtab() {
		return st;
	}

	public Integer getSymLocInteger(VarSymbol vs) {
		return symTab.get(vs);
	}

	public void setSymLocInteger(VarSymbol vs, int loc) {
		symTab.put(vs, new Integer(loc));
	}

	public void incTab() {
		tabs += 1;
	}

	public void decTab() {
		tabs -= 1;
		if (tabs < 0)
			tabs = 0;
	}

	public String printTabs() {
		String spaces = "";
		for (int i = 0; i < tabs; i++) {
			spaces += " ";
		}
		return spaces;
	}

	public String getTypeString(Type t) {
		if (t == null) {
			return "";
		} else if (t instanceof IntArrayType) {
			return "int[]";
		} else if (t instanceof BooleanType) {
			return "Boolean";
		} else if (t instanceof IntegerType) {
			return "int";
		} else if (t instanceof IdentifierType) {
			return ((IdentifierType) t).s;
		} else
			return "";
	}

	public int getObjectSize(String t, boolean is_class) {
		if (t == null || t == "") {
			return 0;
		} else if (t.equals("int"))
			return 8;
		else if (t.equals("int[]"))
			return 8;
		else if (t.equals("Boolean") || t.equals("boolean"))
			return 8;
		else if (is_class)
			return 8;

		int size = 0;
		ClassSymbol cs = (ClassSymbol) st.lookupSymbol(t, "ClassSymbol");
		if (cs != null && cs.getVariables() != null) {
			List<VarSymbol> vsl = cs.getVariables();
			for (int i = 0; i < vsl.size(); i++) {
				size += getObjectSize(vsl.get(i).toString(), true);
			}
		}
		return size;
	}

	public void printAsm(String instr, String[] args) {
		String builder = "";
		builder += printTabs();
		builder += instr;

		int len = 8 - instr.length();
		if (len > 0) {
			for (int t = 0; t < len; t++) {
				builder += " ";
			}
			for (int a = 0; a < args.length; a++) {
				builder += " " + args[a];
			}
		}
		System.out.println(builder);
	}

	public void printAsm(String instr) {
		String builder = "";
		builder += printTabs();
		builder += instr;
		System.out.println(builder);
	}

	public String getLabel(Identifier i) {
		return getLabel("", i.s);
	}

	public String getLabel(String classname, String callname) {
		String label = "";
		if (!classname.isEmpty())
			label = classname + "$" + callname;
		else
			label = callname;

		return label;
	}

	public void report_error(int line, String msg) {
		System.out.println(line + ": " + msg);
		++errors;
	}

	public String getExp(ASTNode n, boolean dst) {
		if (n == null) {
			return "";
		} else if (n instanceof IntegerLiteral) {
			IntegerLiteral i = (IntegerLiteral) n;
			i.accept(this);
			return "%rax";
		} else if (n instanceof True) {
			True i = (True) n;
			i.accept(this);
			return "%rax";
		} else if (n instanceof False) {
			False f = (False) n;
			f.accept(this);
			return "%rax";
		} else if (n instanceof IdentifierExp) {
			IdentifierExp i = (IdentifierExp) n;
			Symbol s = st.getSymbol(i.s);
			if (s != null && s instanceof VarSymbol) {
				VarSymbol vs = (VarSymbol) s;
				String operand = getSymLocInteger(vs) + "(%rbp)";
				if (dst)
					return operand;
				printAsm("movq", new String[] { operand, "%rax" });
				return "%rax";
			}

			ASTNode p = n;
			while (p != null && !(p instanceof ClassDecl)) {
				p = p.getParent();
			}

			if (p != null && p instanceof ClassDeclExtends) {
				ClassDeclExtends c = (ClassDeclExtends) p;
				ClassSymbol cs = (ClassSymbol) st.lookupSymbol(c.j.s);
				if (cs != null) {
					VarSymbol vs = cs.getVariable(i.s);
					if (vs != null) {
						printAsm("movq", new String[] { "-8(%rbp", "%rax" });
						String op = getSymLocInteger(vs) + "(%rax)";
						if (dst)
							return op;
						printAsm("movq", new String[] { op, "%rax" });
						return "%rax";
					}
				}
			}
		} else if (n instanceof Identifier) {
			Identifier i = (Identifier) n;
			Symbol s = st.getSymbol(i.s);
			if (s != null && s instanceof VarSymbol) {
				VarSymbol vs = (VarSymbol) s;
				String operand = getSymLocInteger(vs) + "(%rbp)";
				if (dst)
					return operand;
				printAsm("movq", new String[] { operand, "%rax" });
				return "%rax";
			}
			s = st.lookupSymbol(i.s, "VarSymbol");
			if (s != null && s instanceof VarSymbol) {
				VarSymbol vs = (VarSymbol) s;
				printAsm("movq", new String[] { "-8 (%rbp)", "%rax" });
				String operand = getSymLocInteger(vs) + "(%rax)";
				if (dst)
					return operand;
				printAsm("movq", new String[] { operand, "%rax" });
				return "%rax";
			}

			ASTNode p = n;
			while (p != null && !(p instanceof ClassDecl)) {
				p = p.getParent();
			}

			if (p != null && p instanceof ClassDeclExtends) {
				ClassDeclExtends c = (ClassDeclExtends) p;
				ClassSymbol cs = (ClassSymbol) st.lookupSymbol(c.j.s);
				if (cs != null) {
					VarSymbol vs = cs.getVariable(i.s);
					if (vs != null) {
						printAsm("movq", new String[] { "-8(%rbp", "%rax" });
						String operand = getSymLocInteger(vs) + "(%rax)";
						if (dst)
							return operand;
						printAsm("movq", new String[] { operand, "%rax" });
						return "%rax";
					}
				}
			}
		} else if (n instanceof ArrayLookup) {
			ArrayLookup e = (ArrayLookup) n;
			e.accept(this);
			return "%rax";
		} else if (n instanceof Exp) {
			Exp e = (Exp) n;
			e.accept(this);
			return "%rax";
		}
		// report_error(n.line_number, "SOMETHING WENT HORRIBLY WRONG");
		return "";
	}

	// Display added for toy example language. Not used in regular MiniJava
	public void visit(Display n) {
		n.e.accept(this);
	}

	// MainClass m;
	// ClassDeclList cl;
	public void visit(Program n) {
		System.out.println(".text");
		System.out.println(".glob" + getLabel("", "asm_main"));
		n.m.accept(this);
		if (n.cl != null) {
			for (int i = 0; i < n.cl.size(); i++) {
				n.cl.get(i).accept(this);
			}
		}

	}

	// Identifier i1,i2;
	// Statement s;
	public void visit(MainClass n) {
		n.i1.accept(this);
		st = st.findScope(n.i1.toString());
		n.i2.accept(this);
		st = st.findScope("main");
		stackPos = 0;
		argPos = 0;
		System.out.println("");
		System.out.println(getLabel("", "asm_main") + ":");
		incTab();
		printAsm("pushq", new String[] { "%rbp" });
		printAsm("movq", new String[] { "%rsp", "%rbp" });
		n.s.accept(this);
		printAsm("leave");
		printAsm("ret");
		decTab();
		st = st.exitScope();
		st = st.exitScope();
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		n.i.accept(this);
		st = st.findScope(n.i.toString());
		stackPos = 0;
		argPos = 0;
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.get(i).accept(this);
		}
		st = st.exitScope();
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclExtends n) {
		n.i.accept(this);
		n.j.accept(this);
		st = st.findScope(n.i.toString());
		stackPos = 0;
		argPos = 0;
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.get(i).accept(this);
		}
		st = st.exitScope();
	}

	// Type t;
	// Identifier i;
	public void visit(VarDecl n) {
		if (n.i == null)
			return;
		Symbol sym = st.getVarTable().get(n.i.s);
		if (sym != null && sym instanceof VarSymbol) {
			VarSymbol vs = (VarSymbol) sym;
			stackPos -= 8;
			setSymLocInteger(vs, stackPos);
		}
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public void visit(MethodDecl n) {
		ASTNode p = st.getScope();
		st = st.findScope(n.i.toString());
		stackPos = 0;
		argPos = 0;

		String functionName = n.i.s;
		String className = "";
		if (p != null && p instanceof ClassDeclSimple) {
			ClassDeclSimple c = (ClassDeclSimple) p;
			className = c.i.s;
		}
		System.out.println(getLabel(className, functionName) + ":");
		incTab();
		printAsm("pushq", new String[] { "%rbp" });
		printAsm("movq", new String[] { "%rsp", "%rbp" });

		int stack_size = 8 * (n.fl.size() + n.vl.size() + 1);
		if (stack_size > 0) {
			stack_size += (stack_size % 16);
			printAsm("subq", new String[] { "$" + Integer.toString(stack_size), "%rsp" });
		}
		stackPos -= 8;
		String stack_loc = Integer.toString(stackPos) + "(%rbp)";
		printAsm("movq", new String[] { regis[argPos++], stack_loc });

		for (int i = 0; i < n.fl.size(); i++) {
			n.fl.get(i).accept(this);
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}

		if (n.e != null) {
			String ret = getExp(n.e, false);
			if (ret != "%rax") {
				printAsm("movq", new String[] { ret, "%rax" });
			}
		}
		st = st.exitScope();
		printAsm("leave");
		printAsm("ret");
		decTab();
	}

	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		if (n.i == null)
			return;
		Symbol sym = st.getVarTable().get(n.i.s);
		if (sym != null && sym instanceof VarSymbol) {
			VarSymbol vs = (VarSymbol) sym;
			stackPos -= 8;
			setSymLocInteger(vs, stackPos);
			String stack_loc = Integer.toString(stackPos) + "(%rbp)";
			printAsm("movq", new String[] { regis[argPos++], stack_loc });
		}
	}

	public void visit(IntArrayType n) {
	}

	public void visit(BooleanType n) {
	}

	public void visit(IntegerType n) {
	}

	// String s;
	public void visit(IdentifierType n) {
	}

	// StatementList sl;
	public void visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
	}

	// Exp e;
	// Statement s1,s2;
	public void visit(If n) {
		String label = getLabel("", "L" + labelNum++);
		String cond = getExp(n.e, false);
		printAsm("cmp", new String[] { "$0", cond });
		printAsm("je", new String[] { label });
		n.s1.accept(this);

		if (n.s2 != null) {
			String label2 = getLabel("", "L" + labelNum++);
			printAsm("jmp", new String[] { label2 });
			System.out.println(label + ":");
			n.s2.accept(this);
			label = label2;
		}
		System.out.println(label + ":");
	}

	// Exp e;
	// Statement s;
	public void visit(While n) {
		String label1 = getLabel("", "L" + labelNum++);
		String label2 = getLabel("", "L" + labelNum++);
		System.out.println(label1 + ":");

		String cond = getExp(n.e, false);
		printAsm("cmp", new String[] { "$0", cond });
		printAsm("je", new String[] { label2 });

		n.s.accept(this);
		printAsm("jmp", new String[] { label1 });
		System.out.println(label2 + ":");
	}

	// Exp e;
	public void visit(Print n) {
		String operand = getExp(n.e, false);
		printAsm("movq", new String[] { operand, "%rdi" });
		printAsm("call", new String[] { getLabel("", "put") });
	}

	// Identifier i;
	// Exp e;
	public void visit(Assign n) {
		String e = getExp(n.e, false);
		printAsm("pushq", new String[] { e });

		String i = getExp(n.i, true);
		printAsm("popq", new String[] { "%rdi" });

		printAsm("movq", new String[] { "%rdi", i });
	}

	// Identifier i;
	// Exp e1,e2;
	public void visit(ArrayAssign n) {
		String val = getExp(n.e2, false);
		printAsm("pushq", new String[] { val });

		String arr = getExp(n.i, true);
		printAsm("pushq", new String[] { arr });

		String index = getExp(n.e1, false);
		printAsm("popq", new String[] { "%rdi" });
		printAsm("popq", new String[] { "%rdx" });
		printAsm("movq", new String[] { "%rdx", "16(%rdi, " + index + ",8)" });
	}

	// Exp e1,e2;
	public void visit(And n) {
		String op1 = getExp(n.e1, false);
		printAsm("pushq", new String[] { "%rax" });

		String op2 = getExp(n.e2, false);
		printAsm("popq", new String[] { "%rdi" });

		op1 = "%rdi";
		printAsm("andq", new String[] { op1, op2 });
	}

	// Exp e1,e2;
	public void visit(LessThan n) {
		String op1 = getExp(n.e1, false);
		printAsm("pushq", new String[] { "%rax" });

		String op2 = getExp(n.e2, false);
		printAsm("popq", new String[] { "%rdi" });
		op1 = "%rdi";

		printAsm("cmpq", new String[] { op1, op2 });
		printAsm("setq", new String[] { "%al" });
		printAsm("movezbq", new String[] { "%al", "%rax" });

	}

	// Exp e1,e2;
	public void visit(Plus n) {
		String op1 = getExp(n.e1, false);
		printAsm("pushq", new String[] { "%rax" });

		String op2 = getExp(n.e2, false);
		printAsm("popq", new String[] { "%rdi" });

		op1 = "%rdi";
		printAsm("addq", new String[] { op1, op2 });
	}

	// Exp e1,e2;
	public void visit(Minus n) {
		String op2 = getExp(n.e2, false);
		printAsm("pushq", new String[] { "%rax" });

		String op1 = getExp(n.e1, false);
		printAsm("popq", new String[] { "%rdi" });
		op2 = "%rdi";

		printAsm("subq", new String[] { op2, op1 });
	}

	// Exp e1,e2;
	public void visit(Times n) {
		String op1 = getExp(n.e1, false);
		printAsm("pushq", new String[] { "%rax" });

		String op2 = getExp(n.e2, false);
		printAsm("popq", new String[] { "%rdi" });

		op1 = "%rdi";
		printAsm("imulq", new String[] { op1, op2 });
	}
	
	// Exp e1,e2;
	public void visit(Divide n) {
		System.out.println("YOU HAVE NO IDEA HOW DIVISION WORKS IN ASSEMBLY BWAHAHWHH");
		/*
		String op1 = getExp(n.e1, false);
		printAsm("pushq", new String[] { "%rax" });

		String op2 = getExp(n.e2, false);
		printAsm("popq", new String[] { "%rdi" });

		op1 = "%rdi";
		printAsm("imulq", new String[] { op1, op2 });
		*/
	}

	// Exp e1,e2;
	public void visit(ArrayLookup n) {
		String arr = getExp(n.e1, false);
		printAsm("pushq", new String[] { arr });

		String index = getExp(n.e2, false);
		printAsm("popq", new String[] { "%rdi" });
		printAsm("movq", new String[] { "16(%rdi, " + index + ",8)", "%rax" });
	}

	// Exp e;
	public void visit(ArrayLength n) {
		String arr = getExp(n.e, false);
		printAsm("movq", new String[] { "0(" + arr + ")", "%rax" });
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public void visit(Call n) {
		String obj = getExp(n.e, false);
		printAsm("movq", new String[] { obj, "%rdi" });

		String functionName = n.i.s;
		String className = "";

		for (int i = 0; i < n.el.size(); i++) {
			String e = getExp(n.el.get(i), false);
			if (e.equals(regis[i + 1]))
				continue;
			printAsm("movq", new String[] { e, regis[i + 1] });
		}

		if (n.e != null) {
			if (n.e instanceof IdentifierExp) {
				IdentifierExp i = (IdentifierExp) n.e;
				Symbol s = st.getSymbol(i.s);
				if (s != null && s instanceof ClassSymbol) {
					ClassSymbol csym = (ClassSymbol) s;
					className = csym.getName();
				} else if (s != null && s instanceof VarSymbol) {
					VarSymbol varSym = (VarSymbol) s;
					className = varSym.getName();
				}
			} else if (n.e instanceof Call) {
				Call c = (Call) n.e;
				MethodSymbol msym = (MethodSymbol) st.lookupSymbol(c.i.s, "MethodSymbol");
				if (msym != null) {
					className = msym.getType();
				}
			} else if (n.e instanceof This) {
				This t = (This) n.e;
				ASTNode p = t.getParent();
				while (p != null && !(p instanceof ClassDecl))
					p = p.getParent();
				if (p != null && p instanceof ClassDeclSimple) {
					ClassDeclSimple c = (ClassDeclSimple) p;
					MethodSymbol ms = null;
					ClassSymbol csym = (ClassSymbol) st.lookupSymbol(c.i.s, "ClassSymbol");
					if (csym != null && csym.getMethod(functionName) != null) {
						className = csym.getName();
					}
				} else if (p != null && p instanceof ClassDeclExtends) {
					ClassDeclExtends c = (ClassDeclExtends) p;
					MethodSymbol ms = null;
					ClassSymbol csym = (ClassSymbol) st.lookupSymbol(c.i.s, "ClassSymbol");
					if (csym != null) {
						ms = csym.getMethod(functionName);
						className = csym.getName();
					}

					ClassSymbol csym_ext = (ClassSymbol) st.lookupSymbol(c.j.s, "ClassSymbol");
					if (csym_ext != null) {
						MethodSymbol mse = csym_ext.getMethod(functionName);
						if (className == null || ms == mse) {
							className = csym_ext.getName();
						}
					}
				}
			} else if (n.e instanceof NewObject) {
				NewObject o = (NewObject) n.e;
				className = o.i.s;
			}
		}

		printAsm("call", new String[] { getLabel(className, functionName) });
	}

	// int i;
	public void visit(IntegerLiteral n) {
		String i = "$" + Integer.toString(n.i);
		printAsm("movq", new String[] { i, "%rax" });
	}

	public void visit(True n) {
		printAsm("movq", new String[] { "$1", "%rax" });
	}

	public void visit(False n) {
		printAsm("movq", new String[] { "$0", "%rax" });
	}

	// String s;
	public void visit(IdentifierExp n) {
	}

	public void visit(This n) {
		printAsm("movq", new String[] { "-8(%rbp)", "%rax" });
	}

	// Exp e;
	public void visit(NewArray n) {
		String e = getExp(n.e, false);
		printAsm("pushq", new String[] { e });
		printAsm("imulq", new String[] { "$8", e });
		printAsm("addq", new String[] { "$16", e });
		printAsm("movq", new String[] { e, "%rdi" });
		printAsm("call", new String[] { getLabel("", "mjcalloc") });
		printAsm("popq", new String[] { "0(%rax)" });
		printAsm("movq", new String[] { "$16", "8(%rax)" });
	}

	// Identifier i;
	public void visit(NewObject n) {
		String obj = n.i.s;
		int objSize = getObjectSize(obj, false);
		objSize += (objSize % 16);
		printAsm("movq", new String[] { "$" + objSize, "%rdi" });
		printAsm("call", new String[] { getLabel("", "mjcalloc") });
	}

	// Exp e;
	public void visit(Not n) {
		String op = getExp(n.e, false);
		printAsm("cmpq", new String[] { "$0", op });
		printAsm("sete", new String[] { "%al" });
		printAsm("movzbq", new String[] { "%al", "%rax" });
	}

	// String s;
	public void visit(Identifier n) {
	}
}