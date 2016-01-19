import java.util.Hashtable;
import java.util.Scanner;
import java.util.Stack;

/**A SimpleDataBase class. Supports functionality defined by Thumbtack Design Challenge.
 *I chose to implement stacks to track transaction as well as to track changes within the transaction.
 *I chose to implement hash tables, with Strings as keys, to track variables and corresponding values as well as 
 *values and their corresponding counts.
 *@author Elyh Lapetina*/
public class SimpleDataBase {

	/**Scanner for reading user input*/
	private static Scanner scanner = new Scanner(System.in);

	/**Stack to track all Transaction*/
	private static Stack<Transaction> openTransactions = new Stack<Transaction>();

	/**Hashtable to store variables with their values*/
	private static Hashtable<String, Double> valueTable = new Hashtable<String, Double>(50,(float) .75);

	/**Hashtable that stores occurrences of value*/
	private static Hashtable<String, Integer> countTable = new Hashtable<String, Integer>(50,(float) .75);

	/**Database which data is stored, accepts commands, handles input*/
	private static SimpleDataBase dataBase = new SimpleDataBase();

	/**Executes helper methods*/
	private static boolean retrieveCommand = true;

	/**A class that presents a single database change. Stores value prior to command*/
	private class changeNode{

		/**Variable name, serves as key*/
		public String key = null;

		/**Last assigned value*/
		public Double lastValue = null;

		/**Constructor for change based on value before SET or UNSET command*/
		public changeNode(String key, Double lastValue){
			this.key = key;
			this.lastValue = lastValue;
		}	
	}

	/**class the represents series of changes*/
	private class Transaction extends Stack<changeNode>{	

	}

	/**Method to call helper methods which define basic operations. Terminates if END command is given*/
	public static void retrieveCommand(){
		while(scanner.hasNextLine() && retrieveCommand){

			//Scanner continue until file is read, or command is entered
			String s = scanner.nextLine();

			//Splits scanner line by word
			String[] commandArgs = s.split(" ");

			//Sets creates string variable for input command
			String inputCommand = commandArgs[0];

			//block to run each helper method
			if(inputCommand.equals("SET")){
				//attempts to convert string to double
				try{
					Double value = Double.parseDouble(commandArgs[2]);
					set(commandArgs[1], value);
				} catch(Exception E){
					System.out.println("Not a valid entry!");
				}
			} else if(inputCommand.equals("GET")){
				System.out.println(get(commandArgs[1]));

			} else if(inputCommand.equals("UNSET")){
				unset(commandArgs[1]);

			} else if(inputCommand.equals("NUMEQUALTO")){
				//attempts to convert string to double
				try{
					Double value = Double.parseDouble(commandArgs[1]);
					numEqualTo(value);
				} catch(Exception E){
					System.out.println("Not a valid entry!");
				}

			}else if(inputCommand.equals("ROLLBACK")) {
				rollback();

			}else if(inputCommand.equals("BEGIN")) {
				begin();

			}else if(inputCommand.equals("COMMIT")) {
				commit();

			}else if(inputCommand.equals("END")) {
				end();
				System.exit(0);
			} else {
				System.out.println("Not a valid command!");
			}
		}
		scanner.reset();
	}

	/**Beings a new transaction*/
	public static void begin(){
		openTransactions.push(dataBase.new Transaction());
	}

	/**Restores all edits to most recent transaction*/
	public  static void rollback(){
		//checks if there is an open transactions
		if(!openTransactions.isEmpty()){
			//Removes open transaction
			Transaction rollbackTransaction = openTransactions.pop();

			//Loops through transactions, reverting variable to state represented by node
			while(!rollbackTransaction.isEmpty()){
				changeNode resetChange = rollbackTransaction.pop();
				if(resetChange.lastValue != null){
					set(resetChange.key, resetChange.lastValue);
				} else {
					//Occurs if value was initialized prior to this point
					unset(resetChange.key);
				}
			}

		//occurs if transactions block is empty
		} else {
			System.out.println("NO TRANSACTION");
		}
	}

	/**Commits transactions by emptying open transaction stacks*/
	public static void commit(){

		//Checks if there is a current open transactions
		if(openTransactions.empty()){
			System.out.println("NO TRANSACTION");
		} else {
			//empties all open transactions
			while(!openTransactions.empty()){
				openTransactions.pop();
			}
		}
	}

	/**Ends transactions by ending retrieveCommand loop in main method */
	public static void end(){
		//sets return command
		retrieveCommand = false;
	}

	/**Sets variable name to transaction value
	 *@param varName Desired variable name
	 *@param value New value
	 **/
	public static void set(String varName, Double value){
		
		//stores previous value assigned to value
		Double previousValue = null;
		
		if(valueTable.containsKey(varName)){
			previousValue = valueTable.get(varName);
		}

		if(!openTransactions.isEmpty()){
			//determines if value is newly set
			if(valueTable.containsKey(varName)){
				//creates a new node on transmission stack to track change.
				openTransactions.peek().push(dataBase.new changeNode(varName, valueTable.get(varName)));
			} else {
				//creates a new node on transmission stack to track change.
				openTransactions.peek().push(dataBase.new changeNode(varName, null));
			}
		}

		//puts value with variable name as key into hash table.
		valueTable.put(varName,value);

		//executes if there is an instance of the desired value in the count table.
		if(countTable.containsKey(String.valueOf(value))){
			//checks if user is attempting to set value that is already assigned to variable.
			if(valueTable.get(varName).equals(value)){
				//increments count of this value by one
				Integer count = countTable.get(String.valueOf(value));

				//increase count of new value
				countTable.put(String.valueOf(value), count + 1);
			}
		} else {
			//creates new count entry in valueCount hashtable.
			countTable.put(String.valueOf(value), new Integer(1));
		}


		//checks if variable has been previously set and the previous value is not equal to new value
		if(previousValue != null && !previousValue.equals(value)){
			Integer decrementValue = countTable.get(previousValue.toString());
			countTable.put(previousValue.toString(), decrementValue - 1  );
		}

	}

	/**Returns value associated with variable name
	 *@param varName Desired variable name
	 *@return Value associated with variable.
	 **/
	public static Double get(String varName){
		if(valueTable.containsKey(varName)){
			return valueTable.get(varName);	
		} else {
			return null;
		}

	}

	/**Unsets the desired variable
	 *@param varName Desired variable
	 */
	public static void unset(String varName){

		//stores current value of variable
		Integer currentCount = countTable.get(String.valueOf(valueTable.get(varName)));
		//checks if there is an open transmission to "save" to
		if(!openTransactions.isEmpty()){
			openTransactions.peek().push(dataBase.new changeNode(varName, valueTable.get(varName)));
		}

		//checks if value is equal to one to determine how to handle the count hashtable.
		if(currentCount.intValue() == 1){
			countTable.remove(String.valueOf(valueTable.get(varName)));
		} else {
			countTable.put(String.valueOf(valueTable.get(varName)), currentCount - 1);
		}

		valueTable.remove(varName);

	}


	/**Returns number of occurrences of desired value
	 *@param value desired value to return count of.
	 */
	public static void numEqualTo(Double value){
		//Checks if value exists in countTable
		if(countTable.containsKey(String.valueOf(value))){
			System.out.println(countTable.get(String.valueOf(value)));
		} else {
			System.out.println("0");
		}
	}

	/**Entry point of program*/
	public static void main(String[] args) {
		dataBase.retrieveCommand();
	}	
}
