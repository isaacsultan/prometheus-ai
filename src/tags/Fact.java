package tags;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a fact in the Expert System. Facts are calculus predicates that represent something that is seen as
 * true.
 * <p>
 * Facts are composed of a predicate name and a set of arguments: P(ARG1, ARG2, ...)
 */

public class Fact extends Tag implements IPredicate {

    private String predicateName;
    private List<Argument> arguments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Fact fact = (Fact) o;

        if (predicateName != null ? !predicateName.equals(fact.predicateName) : fact.predicateName != null)
            return false;
        return arguments != null ? arguments.equals(fact.arguments) : fact.arguments == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (predicateName != null ? predicateName.hashCode() : 0);
        result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
        return result;
    }

    /**
     * Constructs a Fact object from a string
     * <p>
     * NB: There should be no space characters between the arguments in a string i.e. "P(ARG1,ARG2,ARG3...)".
     * Arguments are delimited by commas within parenthesis.
     *
     * @param value           String input
     * @param confidenceValue double in range [0,1] i.e. 0.n representing n0% confidence
     */

    public Fact(String value, double confidenceValue) {

        String[] tokens = value.split("[(),]");

        this.type = TagType.FACT;
        this.predicateName = tokens[0];
        this.arguments = argStringParser(tokens);
        this.setConfidenceValue(confidenceValue);
    }

    /**
     * {@code confidenceValue} defaults to 1.0
     *
     * @see #Fact(String, double)
     */

    public Fact(String value) {
        this(value, 1.0);
    }

    /**
     * Prints predicate name, arguments and confidence value of Fact
     * <p>
     * e.g. "[P(ARG1, ARG2) 100%]"
     * @return string value of Fact
     */

    @Override
    public String toString() {
        return "[" + predicateName + '(' + arguments + ") " + getConfidenceValue() * 100 + "% ]";
    }

    /**
     * Parses a raw string into a list of string tokens that represent each argument
     * <p>
     *
     * @param tokens string input
     * @return list of string arguments
     */

    private List<Argument> argStringParser(String[] tokens) {
        List<Argument> argSet = new ArrayList<>();
        for (int i = 1; i < tokens.length; i++) {
            Argument argument = makeArgument(tokens[i]);
                argSet.add(argument);
        }
        return argSet;
    }

    /**
     * Calls the appropriate Argument constructor on a string token.
     * <p>
     * If argument is numeric {@literal ->} NumericArgument; If contains {@literal [?*&]} {@literal ->} VariableArgument; Else {@literal ->}StringArgument
     * @param argString String token
     * @return A single argument
     */

    static Argument makeArgument(String argString) {
        String[] argTokens = argString.split("[=><!]");
        int lastElem = argTokens.length - 1;

        if (argTokens[lastElem].matches("-?\\d+(\\.\\d+)?")) {
            return new NumericArgument(argString, argTokens);
        } else if (argTokens[lastElem].matches("[?*]") || argTokens[lastElem].charAt(0) == '&') {
            return new VariableArgument(argString, argTokens);
        } else {
            return new StringArgument(argString, argTokens);
        }
    }

    @Override
    public String getPredicateName() {
        return predicateName;
    }

    @Override
    public List<Argument> getArguments() {
        return arguments;
    }

    /**
     * Compares two facts to see if they are compatible.
     * <p>
     * If matching occurs on a variable argument, return object includes a list of tuple[s].
     *
     * @param inputFact fact contained in a Rule
     * @return true if facts are 'matched' (notice not necessarily equal)
     */

    public VariableReturn matches(Fact inputFact) {

        VariableReturn result = new VariableReturn();
        if (!this.predicateName.equals(inputFact.predicateName)) {
            result.setDoesMatch(false);
            return result;
        }

        if (inputFact.arguments.size() > this.arguments.size()) {
            for (int i = this.arguments.size(); i < inputFact.arguments.size(); i++) {
                if (!inputFact.arguments.get(i).getSymbol().equals(Argument.ArgTypes.MATCHALL)) {
                    result.setDoesMatch(false);
                    return result;
                }
            }
        }

        Iterator iterFact = this.arguments.iterator();
        Iterator iterInputFact = inputFact.arguments.iterator();

        while (iterFact.hasNext()) {
            Argument argFact = (Argument) iterFact.next();
            Argument argInputFact = (Argument) iterInputFact.next();
            if (argFact.getSymbol().equals(Argument.ArgTypes.MATCHALL) || argInputFact.getSymbol().equals(Argument.ArgTypes.MATCHALL)) {
                result.setDoesMatch(true);
                return result;
            }
            if (argInputFact.getSymbol().equals(Argument.ArgTypes.VAR)) {
                result.setDoesMatch(true);
                result.getPairs().put(argInputFact.getName(), argFact);
            }
            result.setDoesMatch(argFact.matches(argInputFact));
            if (!result.isDoesMatch()) {
                return result;
            }
        }
        if (iterInputFact.hasNext()) {
            Argument argFact = (Argument) iterInputFact.next();
            result.setDoesMatch((argFact.getSymbol().equals(Argument.ArgTypes.MATCHALL)));
            return result;
        }
        result.setDoesMatch(true);
        return result;

    }
}
