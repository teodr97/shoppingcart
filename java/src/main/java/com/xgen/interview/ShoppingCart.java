package com.xgen.interview;

import com.xgen.interview.util.Currency;

import java.text.ParseException;
import java.util.*;

/**
 * Class which deals with scanning items and printing
 * them to a receipt. The class also accepts custom formatting
 * for receipt printing and different currencies if needed.
 */
public class ShoppingCart implements IShoppingCart {
    HashMap<String, Integer> contents = new HashMap<>();
    Pricer pricer;

    // Used to retain the order of the products as scanned
    LinkedList<String> productQueue = new LinkedList<>();
    // Stores the format the receipt should be printed in
    String receiptFormat = null;
    // What currency to print the receipt in
    String CURRENCY = String.format("€");

    /**
     * Default constructor
     * @param pricer - The Pricer object to be used with the ShoppingCart object
     */
    public ShoppingCart(Pricer pricer) {
        this.pricer = pricer;
    }

    //TODO Replace the switch case with a database to retrieve currency symbols
    /**
     * Constructor which also specifies what
     * currency to use.
     * @param pricer - The Pricer object to be used with the ShoppingCart object
     * @param currency - The currency (from the Currency enum) that the ShoppingCart object should use
     */
    public ShoppingCart(Pricer pricer, Currency currency) {
        this(pricer);

        switch (currency) {
            case EUR:
                CURRENCY = String.format("€");
                break;
            case RON:
                CURRENCY = String.format("LEI ");
                break;
            case USD:
                CURRENCY = String.format("$");
                break;
            default:
                CURRENCY = String.format(currency.toString());
                break;
        }
    }

    /**
     * <p>Constructor method with custom format to be used
     * when printing the receipt. The format describes
     * what each line used to list a product will look
     * like.</p>
     * <p></p>
     * <p>
     * Symbols:
     * <ul>
     *     <li>"&lt;qt>" - Format for product quantity</li>
     *     <li>"&lt;pn>" - Format for product name</li>
     *     <li>"&lt;pc>" - Format for product price</li>
     * </ul>
     * <p>
     * Each symbol can be used an arbitrary amount of times (or none
     * at all). Every other character besides the symbols will be
     * printed as-is.
     * </p>
     * <p>
     * Examples formatted lines:
     * <p>"&lt;qt> - &lt;prod_name> - &lt;price>": "3 - Apple - €3"</p>
     * <p>"&lt;prod_name> | &lt;price>": "Apple | €3" </p>
     * <p>"&lt;qt> @ &lt;price>": "3 @ €3" </p>
     * </p>
     * </p>
     * @param pricer - The Pricer object to be used with the ShoppingCart object
     * @param receiptFormat - The printing format to be used.
     */
    public ShoppingCart(Pricer pricer, String receiptFormat) {
        this(pricer);
        this.receiptFormat = receiptFormat;
    }

    /**
     * Constructor which includes both
     * a custom line format for the receipt to use and a
     * specific currency. For the line formatting documentation
     * check the javadoc for ```public ShoppingCart(Pricer, String)```.
     * @param pricer - The Pricer object to be used with the ShoppingCart object
     * @param receiptFormat - The printing format to be used.
     * @param currency - The currency (from the Currency enum) that the ShoppingCart object should use
     */
    public ShoppingCart(Pricer pricer, String receiptFormat, Currency currency) {
        this(pricer, currency);
        this.receiptFormat = receiptFormat;
    }

    /**
     * Called once for every item/collection of items that is scanned by the till.
     * If the item has already been scanned, add the number parameter to the
     * already-existing quantity. The method also permits items to be removed
     * as long as the quantity doesn't fall below zero.
     * @param itemType - The item being scanned by the hardware
     * @param number - The number of items the cashier is moving to the bagging area
     * @throws IllegalArgumentException - Thrown if there is a negative amount of items
     * detected (for one reason or another)
     */
    public void addItem(String itemType, int number) throws IllegalArgumentException {
        if (!contents.containsKey(itemType)) {
            if (number <= 0)
                throw new IllegalArgumentException("Items of negative or nil quantity " +
                        "can't be added to the total.");
            contents.put(itemType, number);
            productQueue.offer(itemType);
        } else {
            int existing = contents.get(itemType);

            if (existing + number < 0)
                throw new IllegalArgumentException("Exception occurred while attempting to remove more " +
                        "of an item than the quantity already scanned.");
            else if (existing + number == 0) contents.remove(itemType);
            else contents.put(itemType, existing + number);
        }
    }

    /**
     * Goes through the scanned contents of the shopping cart
     * (in the order in which they were scanned) and calls
     * printLine() for each item.
     */
    public void printReceipt(){
        // Stores the total price of the products
        float totalPrice = 0.0f;
        Object[] keys = contents.keySet().toArray();

        while (!productQueue.isEmpty())
        {
            // Used to store the keys returned by the queue
            String productName = productQueue.poll();
            Integer quantity = contents.get(productName);
            Integer price = pricer.getPrice(productName) * contents.get(productName);
            Float priceFloat = new Float(new Float(price) / 100);

            //TODO Find a better way to deal with parsing exceptions from printReceipt()
            try {
                printLine(productName, quantity, priceFloat);
            }
            catch (ParseException e) {
                System.out.println("[BLANK]");
            }

            totalPrice += priceFloat;
        }
        // Print the total price to the bottom of the receipt
        System.out.println("TOTAL PRICE: " + String.format(CURRENCY + "%.2f", totalPrice));
    }

    //TODO Modify the method so that it can accept an arbitrary number of symbol types in the future
    /**
     * Prints a line for the product data passed as arguments according
     * to the (custom) receipt format.
     * @param productName - The name (key in the database) of the product
     * @param quantity - The quantity of the product
     * @param priceFloat - The price of the product
     * @throws ParseException - If an invalid symbol is found in the line format
     */
    private void printLine(String productName, Integer quantity, Float priceFloat) throws ParseException {
        // Format the currency and the price to be printed on the receipt
        String priceString = String.format(CURRENCY + "%.2f", priceFloat);

        // If there's no custom format, revert to the default one
        if (receiptFormat == null) System.out.println(productName + " - " + contents.get(productName) + " - " + priceString);
        else {
            // Stores the symbols to check for
            String[] symbols = new String[] {"<qt>", "<pn>", "<pc>"};
            // Used to build the layout of the line
            StringBuilder lineFormat = new StringBuilder();
            // Used to go through the receipt format - if we come across a symbol
            // we print the corresponding item, and we print the rest of the
            // format as it is. Makes a copy of receiptFormat and progressively
            // removes parts of it as it prints them.
            String linePart = receiptFormat;

            // After we print some part of the linePart variable, we
            // remove it from the string.
            while (linePart.length() != 0) {
                int firstSymbolIndex = -1;

                // If there is any symbol in the format string, change
                // firstSymbolIndex variable so that it points to the
                // left-most symbol
                for (String symbol : symbols) {
                    int index = linePart.indexOf(symbol);

                    if (index != -1) {
                        if (firstSymbolIndex == -1) firstSymbolIndex = index;
                        else firstSymbolIndex = Math.min(firstSymbolIndex, index);
                    }
                }

                // If the variable is still -1, then no symbol has been found.
                if (firstSymbolIndex == -1) {
                    lineFormat.append(linePart);
                    break;
                }
                // We add everything until the position of the left-most symbol found.
                lineFormat.append(linePart.substring(0, firstSymbolIndex));

                String symbol = linePart.substring(firstSymbolIndex, firstSymbolIndex + 4);

                switch (symbol) {
                    case "<qt>": lineFormat.append(quantity);
                        break;
                    case "<pn>": lineFormat.append(productName);
                        break;
                    case "<pc>": lineFormat.append(priceString);
                        break;
                    default: throw new ParseException("Invalid symbol found in line format.", 0);
                }
                // Get rid of everything up to and including the symbol - it's been printed.
                linePart = linePart.substring(firstSymbolIndex + 4);
            }
            System.out.println(lineFormat);
        }
    }

    public Pricer getPricer() { return this.pricer; }

    public String getCURRENCY() { return CURRENCY; }
}
