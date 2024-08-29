package com.example.dig_pass;

public class GatePass {
    private String gatePassNumber;
    private String leavingTime;
    private String date;
    private String reason;
    private String ctaprov;
    private String hodaprov;
    private String princaprov;


    public GatePass() {
        // Default constructor required for calls to DataSnapshot.getValue(GatePass.class)
    }

    public GatePass(String gatePassNumber, String leavingTime, String date, String reason, String ctaprov, String hodaprov, String princaprov) {
        this.gatePassNumber = gatePassNumber;
        this.leavingTime = leavingTime;
        this.date = date;
        this.reason = reason;
        this.ctaprov = ctaprov;
        this.hodaprov =hodaprov;
        this.princaprov = princaprov;
    }

    public String getHodaprov() {
        return hodaprov;
    }

    public void setHodaprov(String hodaprov) {
        this.hodaprov = hodaprov;
    }

    public String getPrincaprov() {
        return princaprov;
    }

    public void setPrincaprov(String princaprov) {
        this.princaprov = princaprov;
    }

    public String getCtaprov() {
        return ctaprov;
    }

    public void setCtaprov(String ctaprov) {
        this.ctaprov = ctaprov;
    }

    public String getGatePassNumber() {
        return gatePassNumber;
    }

    public void setGatePassNumber(String gatePassNumber) {
        this.gatePassNumber = gatePassNumber;
    }

    public String getLeavingTime() {
        return leavingTime;
    }

    public void setLeavingTime(String leavingTime) {
        this.leavingTime = leavingTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

