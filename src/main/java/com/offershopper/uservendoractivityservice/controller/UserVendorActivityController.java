package com.offershopper.uservendoractivityservice.controller;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.offershopper.uservendoractivityservice.model.CarryBagBean;
import com.offershopper.uservendoractivityservice.model.OfferBean;
import com.offershopper.uservendoractivityservice.model.SubscribeBean;
import com.offershopper.uservendoractivityservice.model.UaaBean;
import com.offershopper.uservendoractivityservice.model.UserBean;
import com.offershopper.uservendoractivityservice.model.WishlistBean;
import com.offershopper.uservendoractivityservice.rabbit.MessageSender;
import com.offershopper.uservendoractivityservice.repository.UaaProxyRepository;
import com.offershopper.uservendoractivityservice.uservendoractivityproxy.CarryBagProxy;
import com.offershopper.uservendoractivityservice.uservendoractivityproxy.OfferProxy;
import com.offershopper.uservendoractivityservice.uservendoractivityproxy.ProfileProxy;
import com.offershopper.uservendoractivityservice.uservendoractivityproxy.ReferralProxy;
import com.offershopper.uservendoractivityservice.uservendoractivityproxy.RoleProxy;
import com.offershopper.uservendoractivityservice.uservendoractivityproxy.SubscribeProxy;
import com.offershopper.uservendoractivityservice.uservendoractivityproxy.WishlistProxy;


/*
 * File name:UserVendorActivityController
 * Author: Gursharan,Anand,Yashika,Rajneesh
 * Date: 13-April-2018
 * Description: Controller Class of business logic interacting with UI and provides various functionalities to user like Subscription,Wishlist and Referrals
 * Referred Files: Model Classes: CarryBagBean,SubscribeBean,UserBean,WishlistBean; Proxy Classes: carryBagProxy,Profileproxy,UserBeanProxy,SubscribeProxy,WishlistProxy
 */

@RestController
@CrossOrigin
public class UserVendorActivityController {

  @Autowired
  private ProfileProxy profileproxy;

  @Autowired
  public CarryBagProxy carrybagproxy;

  @Autowired
  public WishlistProxy wishlistproxy;

  @Autowired
  public SubscribeProxy subscribeproxy;

  @Autowired
  public ReferralProxy referralproxy;

  @Autowired
  private OfferProxy offerProxy;

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private RoleProxy roleproxy;

  @Autowired
  private UaaProxyRepository userproxyrepository;

  /*
   * Name: addOffer
   * Date: 11-April-2018
   * Description: method to add offer in offer database by vendor
   * Required files: offer-database-service
   */
  @HystrixCommand(fallbackMethod="addOfferFallback")
  @PostMapping("/addOffer")
  public ResponseEntity<Object> addOffer(@RequestBody OfferBean offerBean)
  {
    System.out.println("adding offer before callingproxy");
    ResponseEntity<Object> response = offerProxy.addOffer(offerBean);
    messageSender.produceMessage("Offer is added by vendor");
    return response;
  }
  //Fallback Method
  public ResponseEntity<Object> addOfferFallback(@RequestBody OfferBean offerBean) 
  {    
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
  }

  /*
   * Name: updateOffer
   * Date: 11-April-2018
   * Description: method to update offer in offer database by vendor
   * Required files: offer-database-service
   */
  @HystrixCommand(fallbackMethod="updateOfferFallback")
  @PutMapping("/updateOffer/{id}")
  public ResponseEntity<OfferBean> updateOffer(@PathVariable String id, @RequestBody OfferBean offerBean) 
  {
    ResponseEntity<OfferBean> responseOfferUpdate = offerProxy.updateOffer(id, offerBean);
    
    WishlistBean wishlistBean=new WishlistBean();
    wishlistBean.setOfferId(offerBean.getOfferId());
    wishlistBean.setOfferDiscount(offerBean.getDiscount());
    wishlistBean.setOfferTitle(offerBean.getOfferTitle());
    wishlistBean.setOfferImage(offerBean.getImageURL());
    wishlistBean.setOfferOriginalPrice(offerBean.getOriginalPrice());
    wishlistBean.setOfferValidity(offerBean.getOfferValidity());
    wishlistBean.setUserId(offerBean.getUserId());
    ResponseEntity<Object> responseWishlistUpdate = wishlistproxy.updateWishlistOffer(wishlistBean);

    CarryBagBean carrybagBean=new CarryBagBean();
    carrybagBean.setUserId(id);
    carrybagBean.setOfferDiscount(offerBean.getDiscount());
    carrybagBean.setOfferId(offerBean.getOfferId());
    carrybagBean.setOfferImage(offerBean.getImageURL());
    carrybagBean.setOfferOriginalPrice(offerBean.getOriginalPrice());
    carrybagBean.setOfferTitle(offerBean.getOfferTitle());
    carrybagBean.setVendorId(offerBean.getUserId());
    carrybagBean.setOfferValidity(offerBean.getOfferValidity());    
    System.out.println("==============="+carrybagBean);
    ResponseEntity<Object> responseCarryBagUpdate = carrybagproxy.updateCarryBag(carrybagBean);
    
    messageSender.produceMessage("Offer details is updated by vendor");
    return responseOfferUpdate;
  }
  //Fallback Method
  public ResponseEntity<OfferBean> updateOfferFallback(@PathVariable String id, @RequestBody OfferBean offerBean) 
  {    
    System.out.println("in fallback");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
  }

  /*
   * Name: deleteOffer
   * Date: 11-April-2018
   * Description: method to delete offer in offer database by vendor
   * Required files: offer-database-service
   */
  @HystrixCommand(fallbackMethod="deleteVendorOfferFallback")
  @DeleteMapping("/deleteOffer/{id}")
  public ResponseEntity<Object> deleteVendorOffer(@PathVariable String id) 
  {
    ResponseEntity<Object> response = offerProxy.deleteOffer(id);
    messageSender.produceMessage("offer is deleted vusing Id");
    return response;
  }
  //Fallback Method
  public ResponseEntity<Object> deleteVendorOfferFallback(@PathVariable String id) 
  {    
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
  }

  //GetMapping for User by user Id
  @HystrixCommand(fallbackMethod="fallbackUserData")
  @GetMapping("/userdata/{userId}")
  public ResponseEntity<Optional<UserBean>> UserData(@PathVariable String userId) 
  {
    Optional<UserBean> response = profileproxy.findUserByEmail(userId);
    messageSender.produceMessage("User profile is retreived using userId");
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
  //Fallback Method for getting Userdata
  public ResponseEntity<Optional<UserBean>> fallbackUserData(@PathVariable String userId)
  { 
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }

  //GetMapping for Updating User
  @HystrixCommand(fallbackMethod="fallbackUpdate")
  @PutMapping("/update")
  public ResponseEntity<Object> updateProfile(@RequestBody UserBean user ) 
  {
    ResponseEntity<Object> response = profileproxy.updateProfile(user);
    messageSender.produceMessage("User profile data is updated");
    return response;
  }
  //Fallback Method for getting User updated data
  public ResponseEntity<Object> fallbackUpdate(@RequestBody UserBean user)
  { 
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }

  //GetMapping for retrieving contents of Carrybag
  @HystrixCommand(fallbackMethod="fallbackCarrybag")
  @GetMapping("/carrybag/{userId}")
  public ResponseEntity<List<CarryBagBean>> getCarryBag(@PathVariable String userId) 
  {  
    List<CarryBagBean> response = carrybagproxy.getAllByUserId(userId);
    messageSender.produceMessage("Carrybag is retreived using userId");
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
  //Fallback method for retrieving contents of Carrybag
  public ResponseEntity<List<CarryBagBean>> fallbackCarrybag(@PathVariable String userId)
  { 
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }

  //PostMapping for adding contents to Carrybag
  @HystrixCommand(fallbackMethod="fallbackAddincarrybag")
  @PostMapping("/addincarrybag")
  public HttpStatus addInCarryBag(@RequestBody CarryBagBean carrybean) 
  {
    ResponseEntity<Object> response = carrybagproxy.addToCarryBag(carrybean);
    messageSender.produceMessage("Offer is added in carrybag");
    return response.getStatusCode();
  }
  //Fallback method while adding contents to Carrybag
  public HttpStatus fallbackAddincarrybag(@RequestBody CarryBagBean carrybean)
  { 
    return HttpStatus.BAD_REQUEST;
  }

  //DeleteMapping for deletion of offers from Carrybag
  @HystrixCommand(fallbackMethod="fallbackDeleteincarrybag")
  @DeleteMapping("/deleteincarrybag/{offerId}")
  public HttpStatus deleteInCarryBag(@PathVariable String offerId) 
  {
    ResponseEntity<Object> response = carrybagproxy.deleteById(offerId);
    messageSender.produceMessage("Offer is deleted from carrybag using offerId");
    return response.getStatusCode();
  }
  //Fallback for DeleteMapping for deletion of offers from Carrybag
  public HttpStatus fallbackDeleteincarrybag(@PathVariable String offerId)
  { 
    return HttpStatus.BAD_REQUEST;
  }

  //GetMapping for retrieving contents of WishList
  @HystrixCommand(fallbackMethod="fallbackGetwishlist")
  @GetMapping("/getwishlist/{userId}")
  public ResponseEntity<List<WishlistBean>> getWishlist(@PathVariable String userId){
    ResponseEntity<List<WishlistBean>> response = wishlistproxy.getWishlist(userId);
    messageSender.produceMessage("Wishlist is retreived using userId");
    return response;
  }
  //Fallback GetMapping for retrieving contents of WishList
  public ResponseEntity<List<WishlistBean>> fallbackGetwishlist(@PathVariable String userId)
  {
    messageSender.produceMessage("Wishlist fallback method");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }

  //DeleteMapping for removing offers from WishList
  @HystrixCommand(fallbackMethod="fallbackDeletewish")
  @DeleteMapping("/deletewish/{offerId}/{userId}")
  public ResponseEntity<Object> deleteWish(@PathVariable String offerId,@PathVariable String userId)
  {
    ResponseEntity<Object> response = wishlistproxy.deleteWishlistUserOffer(offerId,userId);
    messageSender.produceMessage("Wishlist is deleted using userId and offerId");
    return response;
  }
  //Fallback for DeleteMapping for removing offers from Wishlist
  public ResponseEntity<Object> fallbackDeletewish(@PathVariable String offerId,@PathVariable String userId)
  {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }
  /*
   * Name: addWish
   * Date: 11-April-2018
   * Description: method to add offers in wishlist
   * Required files: wishlist-database-service
   */
  @HystrixCommand(fallbackMethod="fallbackAddwish")
  @PostMapping("/addwish")
  public ResponseEntity<Object> addWish(@RequestBody WishlistBean wishlist)
  {
    ResponseEntity<Object> response = wishlistproxy.addToWishlist(wishlist);
    messageSender.produceMessage("Wish is added using userId");
    return response;
  }
  //fallback for adding offers to wishlist
  public ResponseEntity<Object> fallbackAddwish(@RequestBody WishlistBean wishlist)
  {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }

  @HystrixCommand(fallbackMethod="fallbackDeletesubscription")
  @DeleteMapping("/deletesubscriptionbyvendorid/{userId}/{vendorId}")
  public ResponseEntity<HttpStatus> deleteSubscription(@PathVariable String userId,@PathVariable String vendorId) 
  {
    ResponseEntity<HttpStatus>response=subscribeproxy.deleteByVendorId(userId,vendorId);
    messageSender.produceMessage("Subscripton is deleted using userId");
    return response;
  }
  public ResponseEntity<HttpStatus> fallbackDeletesubscription(@PathVariable String userId,@PathVariable String vendorId) 
  {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }
  /*
   * Name: getSubscription
   * Date: 11-April-2018
   * Description: method to get list of subscribed products
   * Required files: subscribe-database-service
   */
  @HystrixCommand(fallbackMethod="fallbackGetsubscription")
  @GetMapping("/getsubscription/{userId}")
  public ResponseEntity<List<SubscribeBean>> getSubscription(@PathVariable String userId) 
  {
    ResponseEntity<List<SubscribeBean>>response=subscribeproxy.getByUserId(userId);
    messageSender.produceMessage("Subscription list is been retrieved");
    return response;
  }
  public ResponseEntity<List<SubscribeBean>> fallbackGetsubscription(@PathVariable String userId) 
  {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }

  /*
   * Name: addSubscription
   * Date: 11-April-2018
   * Description: method to subscribe any product
   * Required files: subscribe-database-service
   */
  @HystrixCommand(fallbackMethod="fallbackAddsubscription")
  @PostMapping("/addsubscription")
  public ResponseEntity<HttpStatus> addSubscription(@RequestBody SubscribeBean subscribebean) 
  {
    ResponseEntity<HttpStatus> response=subscribeproxy.addByVendorId(subscribebean);
    messageSender.produceMessage("Subscription list is been added in subscription list");
    return response;
  }
  public ResponseEntity<HttpStatus> fallbackAddsubscription(@RequestBody SubscribeBean subscribebean) 
  {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }

  //PostMapping for updating spinCount on referral  
  @HystrixCommand(fallbackMethod="fallbackReferral")
  @PostMapping("/referral/{userId1}/{userId2}")
  public ResponseEntity<Object> Referral(@PathVariable String userId1,@PathVariable String userId2)
  {
    ResponseEntity<Object> response=referralproxy.addSpin(userId1,userId2);
    messageSender.produceMessage("Referral is made");
    return response;
  }
  public ResponseEntity<Object> fallbackReferral(@PathVariable String userId1,@PathVariable String userId2) 
  {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }


  /*
   * Name: updateSpin count
   * Date: 23-April-2018
   * Description: method to update spin count 
   * Required files: user-database-service
   */
  @HystrixCommand(fallbackMethod="fallbackUpdateSpinCount")
  @PutMapping("/updatespincount/{spinvalue}")
  public ResponseEntity<Object> updateSpincounts(@PathVariable int spinvalue,@PathVariable String userId) 
  {     
    Optional<UserBean> response = profileproxy.findUserByEmail(userId);
    UserBean userBean=new UserBean();

    if(response.isPresent())
    {
      userBean = response.get();
    }

    Date date = new Date(userBean.getTimestamp());

    if(!date.equals(java.time.LocalDate.now()))
    { 
      userBean.setTimestamp(System.currentTimeMillis());
      userBean.setOsCash((userBean.getOsCash()+spinvalue));
      ResponseEntity<Object> request = profileproxy.updateProfile(userBean);
      messageSender.produceMessage("User spin value is updated");
      return request;  
    }
    else if(userBean.getSpinCount()!=0)
    { 
      userBean.setSpinCount((userBean.getSpinCount()-1));
      userBean.setOsCash((userBean.getOsCash()+spinvalue));
      userBean.setTimestamp(System.currentTimeMillis());
      ResponseEntity<Object> request = profileproxy.updateProfile(userBean);
      messageSender.produceMessage("User spin value is updated");
      return request;
    }
    else 
    {
      messageSender.produceMessage("User spin value is updated");
      return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
    }
  }
  //Fallback Method for getting User updated data
  public ResponseEntity<Object> fallbackUpdateSpinCount(@RequestBody UserBean user,@PathVariable int spinvalue)
  { 
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }
  /*
   * Name: convert to vendor
   * Date: 23-April-2018
   * Description: method to convert role of customer to vendor
   * Required files: UAA Server and user-database-service
   */
  @HystrixCommand(fallbackMethod="fallbackConvertToVendor")
  @PostMapping("/converttovendor")
  public ResponseEntity<String> convertToVendor(@RequestBody UserBean user) 
  {
    try {
        user.setRole("Vendor");
        ResponseEntity<Object> response = profileproxy.updateProfile(user);
        messageSender.produceMessage("User is converted to vendor");
        ResponseEntity<String> token=roleproxy.vendorRegistration(user.getEmail(), "Vendor");
        messageSender.produceMessage("Vendor token received");
        return token;
      } catch(Exception e) {
        messageSender.produceMessage("Role had not been updated");  
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed");
     }   
  }

  //fallback for role conversion from user to vendor
  public ResponseEntity<HttpStatus> fallbackConvertToVendor(@PathVariable String userId, @PathVariable String role,@RequestBody UserBean user) 
  {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }
  
  /*
   * Name: forgotPassword
   * Date: 23-April-2018
   * Description: mapping if user forgot his password
   * Required files: user-database-service
   */
  @HystrixCommand(fallbackMethod="fallbackForgotPassword")
  @PostMapping("/forgotpassword")
  public ResponseEntity<String> forgotPassword(@RequestBody UserBean user) 
  {
    ResponseEntity<Object> response = profileproxy.updateProfile(user);  
    messageSender.produceMessage("Password had reset");
    return ResponseEntity.status(HttpStatus.OK).body("Success");
  }
  //fallback for forgot password
  public ResponseEntity<HttpStatus> fallbackForgotPassword(@RequestBody UserBean user) 
  {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }
  /*
   * Name: logout
   * Date: 23-April-2018
   * Description: method to logout user from his account
   * Required files: UAA Server
   */
  @HystrixCommand(fallbackMethod="fallbackLogout")
  @GetMapping("/logout")
  public void logout(@RequestHeader(value="application-token") String token) 
  {
    String result=roleproxy.verifyTokenComingFromService(token);
    String []results=result.split(",");
    if(results[0].equals("true"))
    {  
      String email=results[2];
      Optional<UaaBean> userobject = userproxyrepository.findById(email);
      if((userobject.get().getActiveUser()-1)!=0)
      {
        userobject.get().setActiveUser((userobject.get().getActiveUser()-1));
        userproxyrepository.save(userobject.get());
      }
    }
    messageSender.produceMessage("User had logout from device");
  }
  //fallback for logout method
  public ResponseEntity<HttpStatus> fallbackLogout(@RequestHeader(value="application-token") String token) 
  {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }
}
