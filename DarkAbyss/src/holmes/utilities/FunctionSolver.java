package holmes.utilities;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;

public class FunctionSolver {

	public FunctionSolver() {
		
	}
	
	public void test() {
		Expression e = new ExpressionBuilder("3 * sin(y) - 2 / (x - 2)")
		        .variables("x", "y")
		        .build()
		        .setVariable("x", 2.3)
		        .setVariable("y", 3.14);
		double result = e.evaluate();
		
		ValidationResult res = e.validate();
		
		
	}
}
