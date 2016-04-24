
package com.stripe.exec;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCardCollection;
import com.stripe.model.CustomerCollection;
import com.stripe.model.CustomerSubscriptionCollection;
import com.stripe.model.ExternalAccount;
import com.stripe.model.ExternalAccountCollection;
import com.stripe.model.Plan;
import com.stripe.model.Refund;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import com.stripe.net.RequestOptions.RequestOptionsBuilder;

import junit.framework.TestCase;


public class StripeUtilities extends TestCase {
	static Customer existingCustomer;
	static Plan existingPlan;
	static Subscription existingSubscription;
	
	static {
		//this api is a test api given by stripe
		Stripe.apiKey = "sk_test_2iJzVgtKmNLXVBCktxSpXS55";
		
		try {
			existingCustomer = Customer.retrieve("cus_8JL4NR0TSKhoID");
			existingPlan = Plan.retrieve("sundayplan1");
						
			CustomerSubscriptionCollection cSC = existingCustomer.getSubscriptions();
			existingSubscription = cSC.retrieve("sub_8KTAEX9MNdmq8j");
	
		} catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException
				| APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
  
   
    @Test
    public void testCreateCustomer() throws Exception{
    	//Create a Customer
    	Customer customer = ___createCustomer();
	    if(customer==null) {
	    	throw new Exception("Error creating the Customer");
	    } 
	    assertNotNull(customer); //JUnit 
	    System.out.println("New Customer was created with id: " + customer.getId());
    }
    
    private static Customer ___createCustomer() throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException{
    	 Map<String, Object> customerParams = new HashMap<String, Object>();
         customerParams.put("description", "New Customer");
         Customer customer = Customer.create(customerParams);
         return customer;
    }
    
    
    
    @Test
    public void testChargeCustomer() throws Exception{
    	 //you can also create a new customer and charge him with
    	//chargeCustomer(___createCustomer());
    	
    	Charge charge = ___chargeCustomer(existingCustomer);
    	if (charge == null) {
        	throw new Exception("Error charging the customer. Does he has a Card?");
        }
       assertNotNull(charge); //Junit
       System.out.format("Customer successfully charged with id: %s and money: %d%s ",charge.getId(),charge.getAmount(),charge.getCurrency());
    }
    
    
    private static Charge ___chargeCustomer(Customer customer) throws Exception{
    	 // Charge the Customer instead of the card
    	Map<String, Object> chargeParams = new HashMap<String, Object>();
    	chargeParams.put("amount", 100); // amount in cents
    	chargeParams.put("currency", "eur");
    	chargeParams.put("customer", customer.getId());
    	
           
           	Charge charge = Charge.create(chargeParams);
            
           return charge;
    }
    
    @Test
   public void testChargeCard() throws Exception{
         Map<String, Object> chargeMap = new HashMap<String, Object>();
         chargeMap.put("amount", 10000);
         chargeMap.put("currency", "usd");
         Map<String, Object> cardMap = new HashMap<String, Object>();
         cardMap.put("number", "4242424242424242");
         cardMap.put("exp_month", 12);
         cardMap.put("exp_year", 2020);
         chargeMap.put("card", cardMap);
         
         Charge charge = Charge.create(chargeMap);
         if(charge==null) {
            throw new Exception("Failed to charge");
         } 
         
         assertNotNull(charge); //Junit
         System.out.format("Card successfully charged with id: %s and money: %d%s ",charge.getId(),charge.getAmount(),charge.getCurrency());
         
    }
      
    
    @Test
    public void testCreatePlan() throws Exception{
        Plan plan = ___createPlan("900","month","Sunday", "EUR", "sundayplan1");
        if(plan==null){
        	throw new Exception("Error creating plan");
        }
        assertNotNull(plan); //JUnit
        System.out.format("Plan created with id: %s and amount: %d",plan.getId(), plan.getAmount());
        //return plan;
   }
    
    private static Plan ___createPlan(String varAmount, String varInterval, String varName, String varCurrency, String varId) throws Exception{
   	 	Map<String, Object> planParams = new HashMap<String, Object>();
	    planParams.put("amount", varAmount);
	    planParams.put("interval", varInterval);
	    planParams.put("name", varName);
	    planParams.put("currency", varCurrency);
	    planParams.put("id", varId);

        Plan plan = Plan.create(planParams);
        return plan;
   }
    
    @Test
    public void testSubscribeCustomer() throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException, Exception{
    	//Subscribe a Customer to an existing Subscription Plan
    	Plan plan = Plan.retrieve("sundayplan1");
    	
    	//or create a new one with ___createPlan(arguements);
        Subscription subscription = ___subscribeCustomer(existingCustomer, plan);
        
        if(subscription==null){
        	throw new Exception("Error creating subscription");
        }
        assertNotNull(subscription); //JUnit
        System.out.format("SubScription created with id: %s",subscription.getId());
    }
    
    private static Subscription ___subscribeCustomer(Customer customer,Plan plan) throws Exception{
    	Map<String, Object> customerParams = new HashMap<String, Object>();
    	customerParams.put("plan", plan.getId());
    	Subscription subscription = customer.createSubscription(customerParams);
    	
    	return subscription;
    }
    
    @Test
    public void testChangeSubscription() throws Exception{
    	//Change Customer's Subscription to another
   	  ___changeSubscription(existingCustomer, existingSubscription, existingPlan); //(String customerId, String subscriptionId)  
    }
    
    
    private static void ___changeSubscription(Customer specificCustomer, Subscription subscription, Plan plan) throws Exception{
        Map<String, Object> subscriptionParams = new HashMap<String, Object>();
        
        String planid = plan.getId(); //subscripe to new planid
        subscriptionParams.put("plan", planid); 
        
        subscription.update(subscriptionParams);
        
      	System.out.println("Subscription changed to Plan with id: " + planid);  
    }
    
    @Test
    public void testUnSubscribeCustomer() throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException{
    	___unsubscribeCustomer(existingCustomer, existingSubscription);
    }
    
    
    private static void ___unsubscribeCustomer(Customer customer, Subscription existingSubscription) throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException {	
    	for(Subscription subscription : customer.getSubscriptions().getData()){
    	  if(subscription.getId().equals(existingSubscription.getId())){
    	    subscription.cancel(null);
    	    System.out.println("Subscription deleted");	
    	    break;
    	  }
    	}
    }
    
    @Test
    public void testUpdatePlanAndSubscriptionAmount() throws Exception{
    	//You can not update a subscriptions amount payment
    	//To solve this i delete, the Plan and create an exactly same one
   	 	//with different payment amount and i link it to the same Customer
    	
    	Customer customer = Customer.retrieve("cus_8JL4NR0TSKhoID"); //customer ID
    	
 	    CustomerSubscriptionCollection cSC = existingCustomer.getSubscriptions();
    	Subscription subscription = cSC.retrieve("sub_8KTAEX9MNdmq8j"); //Subscription ID
    	
    	Plan plan = Plan.retrieve("gold67"); //Plan ID
    	
    	___updatePlanAndSubscriptionAmount(plan,customer, subscription);
    }
    
    private static void ___updatePlanAndSubscriptionAmount(Plan plan, Customer customer, Subscription subscription) throws Exception{
    	    
    	    int amount = 150; //set new amount
    	    //Save old options of the Plan
    	    String interval = plan.getInterval();
    	    String name = plan.getName();
    	    String currency = plan.getCurrency();
    	    String id = plan.getId();
    	    
    	    plan.delete();   	 
      	    System.out.println("Old Plan deleted"); 
      	    
     	   //create new plan with same details
    	    Plan newPlan = ___createPlan(String.valueOf(amount), interval, name, currency, id);

    	    //change subscription to new plan
    	      ___changeSubscription(customer, subscription, newPlan);
    }
    
    @Test
    public void testRefundCustomerCharge() throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException{
    	Map<String, Object> refundParams = new HashMap<String, Object>();
    	
    	//set charge id
    	refundParams.put("charge", "ch_183tUpK22Kpb2KFRj2HK3RzQ");

    	Refund.create(refundParams);
    }
    @Test 
    public void testListAllCustomers() throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException{
    	Map<String, Object> customerParams = new HashMap<String, Object>();
    	customerParams.put("limit", 3); 
    	
    	List<Customer> allCustomers = Customer.list(customerParams).getData();
    
    	for (Customer customer : allCustomers) {
			System.out.println(customer.getId()); //print only the Id's of the Customer
		}
    }
    @Test
    public void testListChargesOfCustomer() throws AuthenticationException, InvalidRequestException, APIConnectionException, CardException, APIException {
    	
    	Map<String, Object> chargeParams = new HashMap<String, Object>();
    	chargeParams.put("limit", 5);
    	chargeParams.put("customer", existingCustomer.getId());
    	System.out.println(Charge.list(chargeParams));
    }
    
    @Test
    public void testChargeCustomerCard() throws Exception{
    	
    	//retrieving card id
    	ExternalAccountCollection externalAC = existingCustomer.getSources();
    	List<ExternalAccount> cards = externalAC.getData();
    	ExternalAccount card = cards.get(0);
    	
    	Map<String, Object> updateParams = new HashMap<String, Object>();
        updateParams.put("source", card.getId());
        updateParams.put("customer", existingCustomer.getId());
        updateParams.put("amount", 10000);
        updateParams.put("currency", "usd");
        updateParams.put("application_fee", 144);
    	
        Charge charge = Charge.create(updateParams);
        if(charge==null) {
            throw new Exception("Failed to charge");
        } 
         
         assertNotNull(charge); //Junit
         System.out.format("Card successfully charged with id: %s and money: %d%s ",charge.getId(),charge.getAmount(),charge.getCurrency());    }
    
    } 
