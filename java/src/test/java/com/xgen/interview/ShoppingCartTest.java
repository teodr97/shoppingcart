package com.xgen.interview;

import com.xgen.interview.Pricer;
import com.xgen.interview.ShoppingCart;
import com.xgen.interview.util.Currency;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;


public class ShoppingCartTest {

    ShoppingCart sc;
    ByteArrayOutputStream myOut;

    @Before
    public void initTest() {
        sc = new ShoppingCart(new Pricer());
        myOut = new ByteArrayOutputStream();
    }

    @Test
    public void canAddAnItem() {
        sc.addItem("apple", 1);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();
        assertTrue(myOut.toString().contains(String.format("apple - 1 - €1.00%n")));
    }

    @Test
    public void canAddMoreThanOneItem() {
        sc.addItem("apple", 2);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();
        assertTrue(myOut.toString().contains(String.format("apple - 2 - €2.00%n")));
    }

    @Test
    public void canAddDifferentItems() {
        sc.addItem("apple", 2);
        sc.addItem("banana", 1);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();

        String result = myOut.toString();

        assertEquals(String.format("apple - 2 - €2.00%nbanana - 1 - €2.00%nTOTAL PRICE: €4.00%n"), result);
    }

    @Test
    public void testReceiptFormatOneSymbol() {
        sc = new ShoppingCart(new Pricer(), "<qt>");

        sc.addItem("apple", 2);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();

        String result = myOut.toString();
        assertEquals(String.format("2%nTOTAL PRICE: €2.00%n"), result);
    }

    @Test
    public void testReceiptFormatOneSymbolRepeat() {
        sc = new ShoppingCart(new Pricer(), "<qt> <qt><qt> abc<qt> | <qt>");

        sc.addItem("apple", 2);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();

        String result = myOut.toString();
        assertEquals(String.format("2 22 abc2 | 2%nTOTAL PRICE: €2.00%n"), result);
    }

    @Test
    public void testReceiptFormatMoreSymbols() {
        sc = new ShoppingCart(new Pricer(), "<qt> <pc><pn> abc<pc> | <qt>");

        sc.addItem("apple", 2);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();

        String result = myOut.toString();
        assertEquals(String.format("2 €2.00apple abc€2.00 | 2%nTOTAL PRICE: €2.00%n"), result);
    }

    @Test
    public void testReceiptNoSymbols() {
        sc = new ShoppingCart(new Pricer(), "line");

        sc.addItem("apple", 2);
        sc.addItem("banana", 2);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();

        String result = myOut.toString();
        assertEquals(String.format("line%nline%nTOTAL PRICE: €6.00%n"), result);
    }

    @Test
    public void testReceiptPriceFirst() {
        sc = new ShoppingCart(sc.getPricer(), "<pc> - <pn> - <qt>");
        sc.addItem("apple", 2);
        sc.addItem("banana", 1);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();

        String result = myOut.toString();

        assertEquals(String.format("€2.00 - apple - 2%n€2.00 - banana - 1%nTOTAL PRICE: €4.00%n"), result);
    }

    @Test
    public void testNoNegativeItems() {
        assertThrows(IllegalArgumentException.class, () -> sc.addItem("apple", -1));
        sc.addItem("apple", 2);
        assertThrows(IllegalArgumentException.class, () -> sc.addItem("apple", -3));
    }

    @Test
    public void testReceiptOtherCurrencyPriceFirst() {
        sc = new ShoppingCart(sc.getPricer(), "<pc> - <pn> - <qt>", Currency.RON);
        sc.addItem("apple", 2);
        sc.addItem("banana", 1);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();

        String result = myOut.toString();

        assertEquals(String.format("LEI 2.00 - apple - 2%nLEI 2.00 - banana - 1%nTOTAL PRICE: LEI 4.00%n"), result);
    }

    @Test
    public void testReceiptOtherCurrency() {
        sc = new ShoppingCart(sc.getPricer(), Currency.JPY);
        sc.addItem("apple", 2);
        sc.addItem("banana", 1);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();

        String result = myOut.toString();

        assertEquals(String.format("apple - 2 - JPY2.00%nbanana - 1 - JPY2.00%nTOTAL PRICE: JPY4.00%n"), result);
    }

    @Test
    public void testNoItems() {
        sc = new ShoppingCart(new Pricer());

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();

        String result = myOut.toString();
        assertEquals(String.format("TOTAL PRICE: €0.00%n"), result);
    }

    @Test
    public void receiptOrderSameAsAddOrder() {
        sc.addItem("apple", 2);
        sc.addItem("banana", 143);
        sc.addItem("pen", 914);
        sc.addItem("answerToLife", 42);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();

        String result = myOut.toString();

        assertEquals(
                String.format("apple - 2 - €2.00%n" +
                        "banana - 143 - €286.00%n" +
                        "pen - 914 - €0.00%n" +
                        "answerToLife - 42 - €0.00%n" +
                        "TOTAL PRICE: €288.00%n"),
                result);
    }

    @Test
    public void canManageFreeItems() {
        sc.addItem("crisps", 2);

        System.setOut(new PrintStream(myOut));

        sc.printReceipt();
        assertTrue(myOut.toString().contains(String.format("crisps - 2 - €0.00")));
        assertTrue(myOut.toString().contains("TOTAL PRICE: €0.00"));
    }
}


