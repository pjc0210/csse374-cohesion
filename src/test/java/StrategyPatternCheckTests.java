import Domain.Category;
import Domain.LintResult;
import Domain.PatternCheck.StrategyPatternCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StrategyPatternCheckTests {

    private StrategyPatternCheck check;

    @BeforeEach
    void setUp() {
        check = new StrategyPatternCheck();
    }

    @Test
    void testNeedsStrategyPatternWithSwitch() throws IOException {
        ClassNode classNode = getClassNode(PaymentProcessorWithSwitch.class);
        List<LintResult> results = check.execute(classNode);

        assertFalse(results.isEmpty(), "Should detect need for Strategy Pattern with switch statement");
        assertTrue(results.stream().anyMatch(r ->
                        r.getMessage().contains("Strategy Pattern")),
                "Should suggest Strategy Pattern");
    }

    @Test
    void testNeedsStrategyPatternWithIfElse() throws IOException {
        ClassNode classNode = getClassNode(DiscountCalculatorWithIfElse.class);
        List<LintResult> results = check.execute(classNode);

        assertFalse(results.isEmpty(), "Should detect need for Strategy Pattern with if-else chain");
    }

    @Test
    void testAlreadyUsesStrategyPattern() throws IOException {
        ClassNode classNode = getClassNode(UsesStrategyPattern.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Should not flag classes already using Strategy Pattern");
    }

    @Test
    void testSimpleMethodNoConditionals() throws IOException {
        ClassNode classNode = getClassNode(SimpleCalculator.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Should not flag simple methods without conditionals");
    }

    @Test
    void testFewConditionals() throws IOException {
        ClassNode classNode = getClassNode(TwoConditionalsClass.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Should not flag methods with only 2 conditionals");
    }

    @Test
    void testGetName() {
        assertEquals("StrategyPattern", check.getName());
    }

    @Test
    void testResultCategory() throws IOException {
        ClassNode classNode = getClassNode(PaymentProcessorWithSwitch.class);
        List<LintResult> results = check.execute(classNode);

        if (!results.isEmpty()) {
            assertEquals(Category.PATTERN, results.get(0).getCategory());
        }
    }

    @Test
    void testMessageIncludesBranchCount() throws IOException {
        ClassNode classNode = getClassNode(PaymentProcessorWithSwitch.class);
        List<LintResult> results = check.execute(classNode);

        if (!results.isEmpty()) {
            String message = results.get(0).getMessage();
            assertTrue(message.matches(".*\\(\\d+\\).*") || message.contains("branches"),
                    "Message should include branch count or mention branches");
        }
    }

    @Test
    void testValidationWithMultipleBranches() throws IOException {
        ClassNode classNode = getClassNode(InputValidator.class);
        List<LintResult> results = check.execute(classNode);

        assertFalse(results.isEmpty(), "Should detect validation methods with many conditionals");
    }

    @Test
    void testStrategyInterfaceImplementation() throws IOException {
        ClassNode classNode = getClassNode(ConcreteStrategy.class);
        List<LintResult> results = check.execute(classNode);

        assertTrue(results.isEmpty(), "Should not flag strategy implementations");
    }

    private ClassNode getClassNode(Class<?> clazz) throws IOException {
        String className = clazz.getName();
        ClassReader classReader = new ClassReader(className);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }

    // ==================== Embedded Test Classes ====================

    // SHOULD TRIGGER - Uses switch to select algorithm
    static class PaymentProcessorWithSwitch {
        public double processPayment(String paymentType, double amount) {
            double fee = 0;
            switch (paymentType) {
                case "CREDIT_CARD":
                    fee = amount * 0.03;
                    break;
                case "DEBIT_CARD":
                    fee = amount * 0.01;
                    break;
                case "PAYPAL":
                    fee = amount * 0.04;
                    break;
                case "BITCOIN":
                    fee = amount * 0.02;
                    break;
                default:
                    fee = 0;
            }
            return amount + fee;
        }
    }

    // SHOULD TRIGGER - Uses if-else chain to select algorithm
    static class DiscountCalculatorWithIfElse {
        public double calculateDiscount(String customerType, double price) {
            double discount = 0;
            if (customerType.equals("STANDARD")) {
                discount = price * 0.1;
            } else if (customerType.equals("PREMIUM")) {
                discount = price * 0.2;
            } else if (customerType.equals("VIP")) {
                discount = price * 0.3;
            } else if (customerType.equals("EMPLOYEE")) {
                discount = price * 0.5;
            }
            return price - discount;
        }
    }

    // SHOULD NOT TRIGGER - Already uses Strategy Pattern
    interface PaymentStrategy {
        double calculateFee(double amount);
    }

    static class UsesStrategyPattern {
        private PaymentStrategy strategy;

        public UsesStrategyPattern(PaymentStrategy strategy) {
            this.strategy = strategy;
        }

        public double processPayment(double amount) {
            return amount + strategy.calculateFee(amount);
        }
    }

    // SHOULD NOT TRIGGER - Simple methods
    static class SimpleCalculator {
        public int add(int a, int b) {
            return a + b;
        }

        public int multiply(int a, int b) {
            return a * b;
        }
    }

    // SHOULD NOT TRIGGER - Only 2 branches (below threshold)
    static class TwoConditionalsClass {
        public String categorize(int value) {
            if (value > 10) {
                return "HIGH";
            } else {
                return "LOW";
            }
        }
    }

    // SHOULD TRIGGER - Many conditionals for validation
    static class InputValidator {
        public boolean validate(String inputType, String value) {
            if (inputType.equals("EMAIL")) {
                return value.contains("@");
            } else if (inputType.equals("PHONE")) {
                return value.length() == 10;
            } else if (inputType.equals("ZIP")) {
                return value.length() == 5;
            } else if (inputType.equals("SSN")) {
                return value.length() == 9;
            }
            return false;
        }
    }

    // SHOULD NOT TRIGGER - Is a strategy implementation itself
    interface ValidationStrategy {
        boolean validate(String value);
    }

    static class ConcreteStrategy implements ValidationStrategy {
        public boolean validate(String value) {
            return value != null && !value.isEmpty();
        }
    }
}