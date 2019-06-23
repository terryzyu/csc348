import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import AST.Program;
import AST.Visitor.CodeTranslateVisitor;
import AST.Visitor.PrettyPrintVisitor;
import AST.Visitor.SemanticAnalysisVisitor;
import AST.Visitor.SymTableVisitor;
import Parser.parser;
import Parser.sym;
import Scanner.scanner;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;


public class MiniJava {
	public static void main(String[] args) {
		List<String> CLA = new ArrayList<String>(); // Creates AL to store arguments
		CLA = Arrays.asList(args); // Converts arguments to list

		//Must have at least 2 args (type + path)
		if (CLA.size() != 2) {
			System.out.println("Incorrect Parameters");
			System.exit(1);
		}
		
		//Runs Scanner
		if (CLA.contains("-S")) {
			runScanner(CLA.get(CLA.indexOf("-S") + 1));
		}
		
		if(CLA.contains("-P")) {
			runParser(CLA.get(CLA.indexOf("-P") + 1));
		}
		
		if(CLA.contains("-T")) {
			runTable(CLA.get(CLA.indexOf("-T") + 1));
		}
		
		if(CLA.contains("-A")) {
			runSemantics(CLA.get(CLA.indexOf("-A") + 1));
		}
		
		if(CLA.contains("-C")) {
			runAssembly(CLA.get(CLA.indexOf("-C") + 1));
		}
	}
	
	private static void runAssembly(String path) {
		String filePath = checkFile(path);
		try {
			ComplexSymbolFactory sf = new ComplexSymbolFactory();
			FileReader fileReader = new FileReader(filePath);
			scanner s = new scanner(new BufferedReader(fileReader), sf);
			parser p = new parser(s, sf);
			Symbol root;
			root = p.parse();
			Program program = (Program) root.value;
			SymTableVisitor test = new SymTableVisitor();
			program.accept(test);
			CodeTranslateVisitor ctv = new CodeTranslateVisitor();
			ctv.setSymtab(test.getSymtab());
			program.accept(ctv);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void runTable(String path) {
		String filePath = checkFile(path);
		try {
			ComplexSymbolFactory sf = new ComplexSymbolFactory();
			FileReader fileReader = new FileReader(filePath);
			scanner s = new scanner(new BufferedReader(fileReader), sf);
			parser p = new parser(s, sf);
			Symbol root;
			root = p.parse();
			Program program = (Program) root.value;
			SymTableVisitor test = new SymTableVisitor();
			program.accept(test);
			test.print();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	private static void runSemantics(String path) {
		String filePath = checkFile(path);
		int errors = 0;
		try {
			ComplexSymbolFactory sf = new ComplexSymbolFactory();
			FileReader fileReader = new FileReader(filePath);
			scanner s = new scanner(new BufferedReader(fileReader), sf);
			parser p = new parser(s, sf);
			Symbol root;
			root = p.parse();

			Program program = (Program) root.value;
			SymTableVisitor test = new SymTableVisitor();
			program.accept(test);
			errors += test.errors;
			SemanticAnalysisVisitor sa = new SemanticAnalysisVisitor();
			sa.setSymtab(test.getSymtab());
			program.accept(sa);
			errors += sa.errors;
			System.out.println(errors + " errors were found.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void runParser(String path) {
		String filePath = checkFile(path);
		try {
			ComplexSymbolFactory sf = new ComplexSymbolFactory();
			FileReader fileReader = new FileReader(filePath);
			scanner s = new scanner(new BufferedReader(fileReader), sf);
			parser p = new parser(s, sf);
			Symbol root;
			root = p.parse();
			Program program = (Program) root.value;
			program.accept(new PrettyPrintVisitor());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		ComplexSymbolFactory sf = new ComplexSymbolFactory();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			System.out.println("First");
			scanner s = new scanner(br, sf);
			System.out.println("Second");
            parser p = new parser(s, sf);
			System.out.println("Third");
            Symbol root;
			System.out.println("Fourth");
            root = p.parse();
			System.out.println("Fifth");
            Program program = (Program) root.value;
			System.out.println("Sixth");
            program.accept(new PrettyPrintVisitor());
			System.out.println("Seventh");
            System.out.print("\nParsing completed"); 
		} catch (Exception e) {
			System.out.println(e);
		}
		*/
	}

	private static void runScanner(String path) {
		String filePath = checkFile(path);
		
		ComplexSymbolFactory sf = new ComplexSymbolFactory();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath));){
			scanner s = new scanner(br, sf);
			Symbol t = s.next_token();
			while (t.sym != sym.EOF) {
				// print each token that we scan
				if(!(s.symbolToString(t).equals("")))
					System.out.println(s.symbolToString(t));
				t = s.next_token();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static String checkFile(String path) {
		//Path provided is direct path to file
		File f = new File(path);
		if(f.exists() && !f.isDirectory()) { 
		    return path;
		}
		
		//Path provided is relative path in working directory
		String finalPath = System.getProperty("user.dir"); //Gets working directory
		if(path.substring(0, 1).equals("/"))
			finalPath += path;
		else
			finalPath += "/" + path; //Appends data to relative path
		
		f = new File(finalPath);
		if(f.exists() && !f.isDirectory()) { 
		    return finalPath;
		}
		
		System.out.println("Incorrect file path provided");
		System.exit(1);
		return null;
	}
	
}

