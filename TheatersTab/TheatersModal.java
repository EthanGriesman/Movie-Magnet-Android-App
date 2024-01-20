package TheatersTab;

public class TheatersModal {

    // theater elements
    private String theaterName;
    private String theaterAddress;
    private String theaterPhone;
    private String theaterLink;

    /**
     * Constructor for a TheatersModal object
     * @param theaterName name of this theater
     * @param theaterAddress address of this theater
     * @param theaterPhone phone number of this theater
     */
    public TheatersModal(String theaterName, String theaterAddress, String theaterPhone, String theaterLink) {
        this.theaterName = theaterName;
        this.theaterAddress = theaterAddress;
        this.theaterPhone = theaterPhone;
        this.theaterLink = theaterLink;
    }

    /**
     * Get method for this theater's name.
     * @return theaterName
     */
    // getter and setter methods
    public String getTheaterName() {
        return theaterName;
    }

    /**
     * Get method for this theater's address.
     * @return theaterAddress
     */
    public String getTheaterAddress() {
        return theaterAddress;
    }

    /**
     * Get method for this theater's phone number.
     * @return theaterPhone
     */
    public String getTheaterPhone() {
        return theaterPhone;
    }

    public String getTheaterlink() {return theaterLink;}

}
