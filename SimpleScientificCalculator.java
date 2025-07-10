import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.Math;

public class SimpleScientificCalculator extends JFrame implements ActionListener {
    private final JTextField display = new JTextField();
    private final StringBuilder input = new StringBuilder();

    public SimpleScientificCalculator() {
        setTitle("Scientific Calculator");
        setSize(400, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        display.setFont(new Font("Consolas", Font.BOLD, 24));
        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        add(display, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(6, 4, 5, 5));
        String[] buttons = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "=", "+",
            "(", ")", "C", "DEL",
            "sin", "cos", "sqrt", "^"
        };

        for (String text : buttons) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Arial", Font.BOLD, 18));
            btn.addActionListener(this);
            panel.add(btn);
        }

        add(panel, BorderLayout.CENTER);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        switch (cmd) {
            case "=" -> {
                try {
                    double result = evaluateExpression(input.toString());
                    display.setText(String.valueOf(result));
                    input.setLength(0);
                } catch (Exception ex) {
                    display.setText("Error");
                    input.setLength(0);
                }
            }
            case "C" -> {
                input.setLength(0);
                display.setText("");
            }
            case "DEL" -> {
                if (input.length() > 0) {
                    input.deleteCharAt(input.length() - 1);
                    display.setText(input.toString());
                }
            }
            case "sin", "cos", "sqrt" -> input.append(cmd).append("(");
            default -> input.append(cmd);
        }

        if (!cmd.equals("=")) {
            display.setText(input.toString());
        }
    }

    // Evaluate using stack-based algorithm (Shunting Yard)
    private double evaluateExpression(String expr) {
        Stack<Double> values = new Stack<>();
        Stack<String> ops = new Stack<>();

        StringTokenizer tokens = new StringTokenizer(expr, "+-*/^() ", true);
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken().trim();

            if (token.isEmpty()) continue;

            if (isNumber(token)) {
                values.push(Double.parseDouble(token));
            } else if (token.equals("(")) {
                ops.push(token);
            } else if (token.equals(")")) {
                while (!ops.peek().equals("(")) {
                    values.push(applyOp(ops.pop(), values));
                }
                ops.pop(); // Remove '('

                // Handle function calls like sin, cos, sqrt
                if (!ops.isEmpty() && isFunction(ops.peek())) {
                    values.push(applyOp(ops.pop(), values));
                }
            } else if (isFunction(token)) {
                ops.push(token);
            } else if (isOperator(token)) {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(token)) {
                    values.push(applyOp(ops.pop(), values));
                }
                ops.push(token);
            }
        }

        while (!ops.isEmpty()) {
            values.push(applyOp(ops.pop(), values));
        }

        return values.pop();
    }

    private boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isOperator(String op) {
        return "+-*/^".contains(op);
    }

    private boolean isFunction(String op) {
        return Arrays.asList("sin", "cos", "sqrt").contains(op);
    }

    private int precedence(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "^" -> 3;
            default -> 4; // functions
        };
    }

    private double applyOp(String op, Stack<Double> values) {
        if (isFunction(op)) {
            double a = values.pop();
            return switch (op) {
                case "sin" -> Math.sin(Math.toRadians(a));
                case "cos" -> Math.cos(Math.toRadians(a));
                case "sqrt" -> Math.sqrt(a);
                default -> 0;
            };
        } else {
            double b = values.pop();
            double a = values.pop();
            return switch (op) {
                case "+" -> a + b;
                case "-" -> a - b;
                case "*" -> a * b;
                case "/" -> a / b;
                case "^" -> Math.pow(a, b);
                default -> 0;
            };
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimpleScientificCalculator::new);
    }
}