package io.virinchi.glowup.dto;

public class CreateOrderRequest {

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private String alternatePhone;

    private String address;

    private String city;

    private String province;

    private String deliveryNotes;

    private String deliveryMethod;

    private String paymentMethod;


    // ==========================================
    // GETTERS
    // ==========================================

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAlternatePhone() {
        return alternatePhone;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }


    // ==========================================
    // SETTERS
    // ==========================================

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAlternatePhone(String alternatePhone) {
        this.alternatePhone = alternatePhone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}